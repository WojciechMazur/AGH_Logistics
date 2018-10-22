package transport

import breeze.linalg.{ DenseMatrix, DenseVector }
import transport.TransportIssueResolverProvider.{ MaxValue, MinValue }
import transport.model._

import scala.util.Try

trait TransportIssueResolver {
  type ConnectionType <: Connection
  def connectionGraph: ConnectionGraph
  def dualityCriteriaFn(nonEmptyConnections: Seq[Connection]): Seq[Double]
  protected def optimalityFn(connection:     ConnectionType):  Double
  protected def isOptimal: Boolean
  val transportIssueResolverProvider: TransportIssueResolverProvider

  private val connections = connectionGraph.connections.asInstanceOf[Seq[ConnectionType]]
  private val nonZeroConnections: Seq[ConnectionType] = connectionGraph.connections
    .filter(_.units > 0)
    .asInstanceOf[Seq[ConnectionType]]

  protected lazy val (baseSuppliers, baseRecipients) = nonZeroConnections
    .foldLeft((List.empty[Supplier], List.empty[Recipient])) {
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

  private def fullfilsCycleCondition(optimalityFactor: Double): Boolean = {
    transportIssueResolverProvider.initOrder match {
      case MinValue => optimalityFactor < 0
      case MaxValue => optimalityFactor > 0
    }
  }

  private def findCycle(n: Int): Option[Cycle] = {
    sortedConnectionsFactors(n) match {
      case ConnectionOptimality(_, optimalityFactor) if !fullfilsCycleCondition(optimalityFactor) =>
        None
      case ConnectionOptimality(initialConnection, _) =>
        val recipients = nonZeroConnections
          .filter(_.supplier == initialConnection.supplier)
          .map(_.recipient)

        val suppliers = nonZeroConnections
          .filter(_.recipient == initialConnection.recipient)
          .map(_.supplier)

        val corner = nonZeroConnections.find(c => suppliers.contains(c.supplier) && recipients.contains(c.recipient))

        val possibleConnections = corner
          .map { corner =>
            nonZeroConnections.filter(c => c != corner && c != initialConnection)
          }
          .getOrElse(Seq.empty)

        val horizontal = corner.flatMap(
          corner =>
            possibleConnections
              .find(c => c.recipient == corner.recipient && c.supplier == initialConnection.supplier)
        )
        val vertical = corner.flatMap(
          corner =>
            possibleConnections
              .find(c => c.recipient == initialConnection.recipient && c.supplier == corner.supplier)
        )

        Try(Cycle(initialConnection, vertical.get, corner.get, horizontal.get)).toOption
          .orElse(findCycle(n + 1))
    }
  }

  protected lazy val foundCycle: Option[Cycle] = findCycle(0)

  protected def transform(cycleOpt: Option[Cycle]) =
    cycleOpt
      .map { cycle =>
        val transfer = Math.min(cycle.vertical.units, cycle.horizontal.units)

        val updatedConnections = Seq(
          cycle.initial.withUnits(cycle.initial.units + transfer),
          cycle.vertical.withUnits(cycle.vertical.units - transfer),
          cycle.corner.withUnits(cycle.corner.units + transfer),
          cycle.horizontal.withUnits(cycle.horizontal.units - transfer)
        )

        val appliedCycleConnections =
          updatedConnections.foldLeft(connectionGraph.connections) {
            case (accConnections, update) =>
              accConnections.updated(
                accConnections.indexWhere(c => c.recipient == update.recipient && c.supplier == update.supplier),
                update
              )
          }
        assert(
          appliedCycleConnections
            .map(_.units)
            .sum == connectionGraph.connections.map(_.units).sum,
          "Total amount has changed"
        )
        connectionGraph.copy(connections = appliedCycleConnections)
      }
      .getOrElse(connectionGraph)
}
