package transport
import transport.TransportIssueResolverProvider.MinValue
import transport.model._

import scala.annotation.tailrec

case class StandardTransportIssueResolver(connectionGraph: ConnectionGraph) extends TransportIssueResolver {

  override type ConnectionType = SimpleConnection

  override val transportIssueResolverProvider: TransportIssueResolverProvider = StandardTransportIssueResolver
  override def dualityCriteriaFn(nonEmptyConnections: Seq[Connection]): Seq[Double] =
    nonEmptyConnections.map(-_.totalCost)
  override protected def optimalityFn(connection: SimpleConnection): Double = connection match {
    case SimpleConnection(_, supplier, recipient, attributes) =>
      (nodeDeltaFactors.get(supplier) ++ nodeDeltaFactors.get(recipient) ++ Some(attributes.transportCost)).sum
  }
  override protected def isOptimal: Boolean = optimalityFactors.forall(_.optimalityFactor >= 0 || optCycle.isEmpty)
}

object StandardTransportIssueResolver extends TransportIssueResolverProvider {
  override val initOrder: TransportIssueResolverProvider.InitOrder = MinValue

  @tailrec
  def apply(connectionGraph: ConnectionGraph): ConnectionGraph = {
    println(s"Total cost: ${connectionGraph.target}")
    val resolver = new StandardTransportIssueResolver(connectionGraph)
    if (resolver.isOptimal) {
      resolver.connectionGraph
    } else {
      StandardTransportIssueResolver(resolver.iterate)
    }
  }

  //Test only!
  private def singleIteration(connectionGraph: ConnectionGraph): ConnectionGraph = {
    val resolver = new StandardTransportIssueResolver(connectionGraph)
    resolver.iterate
  }
  override def buildConnectionGraph(connections: Vector[Connection]): ConnectionGraph = {
    val totalSupply = connections.map(_.supplier).distinct.map(_.supply).sum
    val totalDemand = connections.map(_.recipient).distinct.map(_.demand).sum

    (totalSupply, totalDemand) match {
      case (supply, demand) if supply > demand =>
        val virtualRecipient = VirtualRecipient(supply - demand)
        val virtualConnections = connections
          .map(_.supplier)
          .distinct
          .map { supplier =>
            SimpleConnection(supplier, virtualRecipient, 0.0)
          }
        ConnectionGraph(connections ++ virtualConnections, virtualRecipient = virtualRecipient)

      case (supply, demand) if supply < demand =>
        val virtualSupplier = VirtualSupplier(demand - supply)
        val virtualConnections = connections
          .map(_.recipient)
          .distinct
          .map { recipient =>
            SimpleConnection(virtualSupplier, recipient, 0.0)
          }
        ConnectionGraph(connections ++ virtualConnections, virtualSupplier)

      case _ => ConnectionGraph(connections)
    }
  }

}
