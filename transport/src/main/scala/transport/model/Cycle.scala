package transport.model

case class Cycle(initial: Connection, tail: Seq[Connection]) {
  private lazy val connections = initial +: tail
  private lazy val transferAmount = connections
    .sliding(2, 2)
    .map(_.toList)
    .collect {
      case _ :: removed :: Nil => Some(removed.units)
      case _                   => None
    }
    .flatten
    .min

  def transform(connectionGraph: ConnectionGraph): ConnectionGraph = {
    val updatedGraph: ConnectionGraph = connections
      .sliding(2, 2)
      .foldLeft(connectionGraph) {
        case (accGraph, positive :: negative :: Nil) =>
          accGraph
            .updatedByCycle(positive, transferAmount)
            .updatedByCycle(negative, -transferAmount)
        case (_, other) =>
          sys.error(s"Expected tuples when transforming using cycle, but $other given.")
      }
    val totalUnits = updatedGraph.connections.map(_.units).sum
    assert(totalUnits == updatedGraph.totalDemand, "Total units does not equal total demand")
    assert(totalUnits == updatedGraph.totalSupply, "Total units does not equal total supply")
    updatedGraph
  }

}

object Cycle {
  def find(
    possibleInitialConnections: List[Connection],
    possibleConnections:        List[Connection]
  )(cycleConnections:           Stream[List[Connection]] = Stream.empty): Option[Cycle] = {
    val foundPossibleCycles: Option[Stream[List[Connection]]] = possibleInitialConnections.headOption
      .collect {
        case initial: Connection if cycleConnections.isEmpty =>
          possibleConnections
            .filter(isAccessible(_, initial))
            .map(head => List(head))
            .toStream
        case initial: Connection =>
          cycleConnections.flatMap { possibleCycleConnections =>
            val notUsedConnections = possibleConnections.diff(possibleCycleConnections)
            //Find possible last element connections
            val possibleNewConnections = possibleCycleConnections.lastOption match {
              case None       => notUsedConnections.filter(isAccessible(_, initial))
              case Some(last) => notUsedConnections.filter(isAccessible(_, last))
            }
            //Add found connections to possible cycle connections
            possibleNewConnections.map(connection => possibleCycleConnections :+ connection)
          }
      }
    foundPossibleCycles match {
      case Some(Stream()) => find(possibleInitialConnections.tail, possibleConnections)()
      case Some(stream) =>
        val initial = possibleInitialConnections.head
        stream.find(connectionSeq => isAccessible(initial, connectionSeq.last) && connectionSeq.size >= 3) match {
          case Some(foundCycleConnections) if isValidCycle(initial, foundCycleConnections) =>
            Some(Cycle(initial, foundCycleConnections))
          case _ => find(possibleInitialConnections, possibleConnections)(stream)
        }
      case None => None
    }
  }

  private def isValidCycle(initial: Connection, connections: List[Connection]) = {
    val closedConnections = connections
      .+:(initial)
      .:+(initial)

    val isClosedCycle = closedConnections
      .sliding(2, 1)
      .forall {
        case left :: right :: Nil => isAccessible(left, right)
        case _                    => false
      }
    lazy val hasDuplicates   = connections.distinct.size != connections.size
    lazy val containsInitial = connections.contains(initial)
    lazy val isInline = closedConnections
      .sliding(3, 1)
      .exists { seq =>
        val (suppliers, recipients) = (
          seq.map(_.supplier).distinct,
          seq.map(_.recipient).distinct
        )

        suppliers.size < 2 || recipients.size < 2
      }
    isClosedCycle && !hasDuplicates && !containsInitial && !isInline

  }

  private def isAccessible(left: Connection, right: Connection): Boolean = {
    left != right &&
    left.supplier == right.supplier ||
    left.recipient == right.recipient
  }
}
