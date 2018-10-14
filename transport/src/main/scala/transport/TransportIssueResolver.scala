package transport

import breeze.linalg.{ DenseMatrix, DenseVector }
import transport.TransportIssueResolver.{ ConnectionOptimality, Cycle }
import transport.model._

import scala.annotation.tailrec
import scala.util.Try

private case class TransportIssueResolver(connectionGraph: ConnectionGraph) {
  private val nonZeroConnections: Seq[Connection] =
    connectionGraph.connections
      .filter(_.amount > 0)

  private val (baseSuppliers, baseRecipients) = nonZeroConnections
    .foldLeft((List.empty[Supplier], List.empty[Recipient])) {
      case ((suppliers, recipents), connection) =>
        (
          suppliers :+ connection.supplier,
          recipents :+ connection.recipient
        )
    } match {
    case (suppliers, recipients) => (suppliers.distinct, recipients.distinct)
  }
  private val allBaseNodes = baseSuppliers ++ baseRecipients

  private lazy val nodeDeltaFactors: Map[Node, Double] = {
    val baseEquations = nonZeroConnections.map {
      case Connection(_, supplier, recipient, _) =>
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
    val weights: DenseVector[Double] = DenseVector(
      nonZeroConnections
        .map(-_.weight) :+ 0.0: _*
    )
    val solveResult: DenseVector[Double] = equationsMatrix \ weights

    allBaseNodes
      .zip(solveResult.toArray)
      .toMap
  }

  private lazy val optimalityFactors: Seq[ConnectionOptimality] =
    connectionGraph.connections
      .map {
        case c @ Connection(weight, supplier, recipient, _) =>
          val optimalityFactor = nodeDeltaFactors.getOrElse(supplier, 0.0) + nodeDeltaFactors
            .getOrElse(recipient, 0.0) + weight
          ConnectionOptimality(c, optimalityFactor)
      }

  private def isOptimal: Boolean =
    optimalityFactors.forall(_.optimalityFactor >= 0)

  private def findCycle(n: Int): Option[Cycle] = {
    val sortedConnectionFactors = optimalityFactors.sortBy(_.optimalityFactor)
    sortedConnectionFactors(n) match {
      case ConnectionOptimality(_, optimalityFactor) if optimalityFactor >= 0 =>
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

  private lazy val foundCycle: Option[Cycle] = findCycle(0)

  private def transform(cycle: Cycle) = {
    val transfer = Math.min(cycle.vertical.amount, cycle.horizontal.amount)

    val updatedConnections = Seq(
      cycle.initial.copy(amount    = cycle.initial.amount + transfer),
      cycle.vertical.copy(amount   = cycle.vertical.amount - transfer),
      cycle.corner.copy(amount     = cycle.corner.amount + transfer),
      cycle.horizontal.copy(amount = cycle.horizontal.amount - transfer)
    )

    val appliedCycleConnections =
      updatedConnections.foldLeft(connectionGraph.connections) {
        case (connections, update) =>
          connections.updated(
            connections.indexWhere(c => c.recipient == update.recipient && c.supplier == update.supplier),
            update
          )
      }
    assert(
      appliedCycleConnections
        .map(_.amount)
        .sum == connectionGraph.connections.map(_.amount).sum,
      "Total amount has changed"
    )
    connectionGraph.copy(connections = appliedCycleConnections)
  }
}

object TransportIssueResolver {
  @tailrec
  def apply(connectionGraph: ConnectionGraph): ConnectionGraph = {
    println(s"Total cost: ${connectionGraph.targetFn}")
    val resolver      = new TransportIssueResolver(connectionGraph)
    lazy val cycleOpt = resolver.foundCycle
    if (resolver.isOptimal || cycleOpt.isEmpty) {
      resolver.connectionGraph
    } else TransportIssueResolver(resolver.transform(cycleOpt.get))
  }

  //Test only!
  private def singleIteration(connectionGraph: ConnectionGraph): ConnectionGraph = {
    val resolver = new TransportIssueResolver(connectionGraph)
    resolver.foundCycle match {
      case Some(cycle) => resolver.transform(cycle)
      case None        => connectionGraph
    }
  }

  private case class ConnectionOptimality(connection: Connection, optimalityFactor: Double)

  private case class Cycle(initial: Connection, vertical: Connection, corner: Connection, horizontal: Connection)

}
