package transport

import breeze.linalg.{ DenseMatrix, DenseVector }
import transport.TransportIssueResolverProvider.{ MaxValue, MinValue }
import transport.model._

trait TransportIssueResolver {
  type ConnectionType <: Connection
  def connectionGraph: ConnectionGraph
  def dualityCriteriaFn(nonEmptyConnections: Seq[Connection]): Seq[Double]
  protected def optimalityFn(connection:     ConnectionType):  Double
  protected def isOptimal: Boolean
  val transportIssueResolverProvider: TransportIssueResolverProvider

  final def iterate: ConnectionGraph = {
    optCycle match {
      case Some(cycle) => cycle.transform(connectionGraph)
      case None        => connectionGraph
    }
  }

  private val connections = connectionGraph.connections.asInstanceOf[Seq[ConnectionType]]
  private val nonZeroConnections: Seq[ConnectionType] = connectionGraph.connections
    .filter(_.units > 0)
    .asInstanceOf[Seq[ConnectionType]]

  protected lazy val (baseSuppliers, baseRecipients) = nonZeroConnections
    .foldLeft((List.empty[SupplierNode], List.empty[RecipientNode])) {
      case ((suppliers, recipients), connection) =>
        (
          suppliers :+ connection.supplier,
          recipients :+ connection.recipient
        )
    } match {
    case (suppliers, recipients) => (suppliers.distinct, recipients.distinct)
  }

  protected lazy val allBaseNodes: List[Node] = baseSuppliers ++ baseRecipients

  protected lazy val nodeDeltaFactors: Map[Node, Double] = {
    val baseEquations = nonZeroConnections.map { c: ConnectionType =>
      val supplier  = c.supplier
      val recipient = c.recipient
      val x = baseSuppliers.map {
        case `supplier` => 1.0
        case _          => 0.0
      }
      val y = baseRecipients.map {
        case `recipient` => 1.0
        case _           => 0.0
      }
      x ++ y
    }

    val zeroCondition = List
      .fill(allBaseNodes.length)(0.0)
      .updated(baseEquations.head.indexOf(1.0), 1.0)

    val equationsMatrix: DenseMatrix[Double] =
      DenseMatrix[List[Double], Double](baseEquations :+ zeroCondition: _*)

    //Weights with extra zero for zero condition
    val weights:     DenseVector[Double] = DenseVector(dualityCriteriaFn(nonZeroConnections) :+ 0.0: _*)
    val solveResult: DenseVector[Double] = equationsMatrix \ weights

    allBaseNodes
      .zip(solveResult.toArray)
      .toMap
  }

  protected lazy val optimalityFactors: Seq[ConnectionOptimality] = connections.map { c =>
    ConnectionOptimality(c, optimalityFn(c))
  }

  lazy private val sortedConnectionsFactors = {
    val sortedFactors = optimalityFactors.sortBy(_.optimalityFactor)
    transportIssueResolverProvider.initOrder match {
      case MinValue => sortedFactors
      case MaxValue => sortedFactors.reverse
    }
  }

  private def fulfillsCycleCondition(optimalityFactor: Double): Boolean = {
    transportIssueResolverProvider.initOrder match {
      case MinValue => optimalityFactor < 0
      case MaxValue => optimalityFactor > 0
    }
  }

  protected lazy val optCycle: Option[Cycle] = Cycle.find(
    sortedConnectionsFactors
      .filter(co => fulfillsCycleCondition(co.optimalityFactor))
      .map(_.connection)
      .toList,
    nonZeroConnections.toList
  )()

}
