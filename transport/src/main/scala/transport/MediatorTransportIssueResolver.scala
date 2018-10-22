package transport
import transport.TransportIssueResolverProvider.MaxValue
import transport.model._

import scala.annotation.tailrec

case class MediatorTransportIssueResolver(connectionGraph: ConnectionGraph) extends TransportIssueResolver {
  override type ConnectionType = MediatorConnection
  override val transportIssueResolverProvider: TransportIssueResolverProvider = MediatorTransportIssueResolver

  override protected def optimalityFn(connection: MediatorConnection): Double = {
    connection.attributes.totalProfits -
      connection.attributes.totalCosts +
      nodeDeltaFactors.getOrElse(connection.supplier, 0.0) +
      nodeDeltaFactors.getOrElse(connection.recipient, 0.0)
  }
  override protected def isOptimal: Boolean = optimalityFactors.forall(_.optimalityFactor <= 0 || foundCycle.isEmpty)
  override def dualityCriteriaFn(nonEmptyConnections: Seq[Connection]): Seq[Double] =
    nonEmptyConnections
      .map(c => c.totalCost - c.attributes.totalProfits)
}

object MediatorTransportIssueResolver extends TransportIssueResolverProvider {
  override val initOrder: TransportIssueResolverProvider.InitOrder = MaxValue

  @tailrec
  def apply(connectionGraph: ConnectionGraph): ConnectionGraph = {
    println(s"Total profit: ${connectionGraph.target}")
    val resolver = new MediatorTransportIssueResolver(connectionGraph)
    if (resolver.isOptimal) {
      resolver.connectionGraph
    } else MediatorTransportIssueResolver(resolver.transform(resolver.foundCycle))
  }

  //Test only!
  private def singleIteration(connectionGraph: ConnectionGraph): ConnectionGraph = {
    val resolver = new MediatorTransportIssueResolver(connectionGraph)
    resolver.transform(resolver.foundCycle)
  }
  override protected def buildConnectionGraph(connections: Vector[Connection]): ConnectionGraph = {
    val totalSupply = connections.map(_.supplier).distinct.map(_.supply).sum
    val totalDemand = connections.map(_.recipient).distinct.map(_.demand).sum

    val suppliers  = connections.map(_.supplier).distinct
    val recipients = connections.map(_.recipient).distinct

    val virtualSupplier  = VirtualSupplier(totalDemand, totalDemand)
    val virtualRecipient = VirtualRecipient(totalSupply)

    val virtualConnections = suppliers.map { MediatorConnection(_, virtualRecipient, 0, 0, 0) } ++
      recipients.map(MediatorConnection(virtualSupplier, _, 0, 0, 0)) :+
      MediatorConnection(virtualSupplier, virtualRecipient, 0, 0, 0)

    ConnectionGraph(
      connections ++ virtualConnections,
      virtualSupplier,
      virtualRecipient
    )
  }
}
