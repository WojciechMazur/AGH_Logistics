package transport
import transport.TransportIssueResolverProvider.{ InitOrder, MaxValue, MinValue }
import transport.model.{ Connection, ConnectionGraph }

trait TransportIssueResolverProvider {
  val initOrder: InitOrder
  protected def buildConnectionGraph(connections: Vector[Connection]): ConnectionGraph
  final def init(connections:                     Connection*): ConnectionGraph = {
    val initialGraph = buildConnectionGraph(connections.toVector.map(_.units(0)))
    val x            = disposeInitialLoad(initialGraph)
    x
  }

  private final def disposeInitialLoad(graph: ConnectionGraph): ConnectionGraph = {
    val connections = initOrder match {
      case MinValue => graph.sortedConnections
      case MaxValue => graph.reverseSortedConnections
    }
    val result = connections.foldLeft(graph) {
      case (accGraph, connection) =>
        val actualConnection = accGraph.find(connection.supplier, connection.recipient)
        val recipient        = actualConnection.recipient
        val transfer         = recipient.demand - recipient.available
        accGraph.updated(actualConnection, transfer)
    }
    result
  }
}

object TransportIssueResolverProvider {
  sealed trait InitOrder
  case object MaxValue extends InitOrder
  case object MinValue extends InitOrder
}
