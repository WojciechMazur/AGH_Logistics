package transport.model

import transport.TransportIssueResolver

import scala.annotation.tailrec
import scala.language.postfixOps

case class ConnectionGraph(
  connections:      Seq[Connection],
  virtualSupplier:  VirtualSupplier = VirtualSupplier(0),
  virtualRecipient: VirtualRecipient = VirtualRecipient(0)
) {

  def sortedConnections: List[Connection] =
    connections.sortBy {
      case Connection(_, _:    VirtualSupplier, _, _) => Double.MaxValue
      case Connection(_, _, _: VirtualRecipient, _)   => Double.MaxValue - 1
      case Connection(weight, _, _, _) => weight
    }.toList

  def suppliers: List[Supplier] =
    sortedConnections
      .map(_.supplier)
      .distinct

  def recipients: List[Recipient] =
    sortedConnections
      .map(_.recipient)
      .distinct

  def find(supplier: Supplier, recipient: Recipient): Connection =
    connections
      .find(conn => conn.supplier == supplier && conn.recipient == recipient)
      .get

  def indexOf(supplier: Supplier, recipient: Recipient): Int =
    connections
      .indexWhere(c => c.supplier == supplier && c.recipient == recipient)

  def updated(supplier: Supplier, recipient: Recipient, amount: Double): ConnectionGraph = {
    val transfer = if (amount > 0) {
      Math.min(amount, supplier.available)
    } else {
      Math.max(amount, recipient.available)
    }

    val newSupplier = supplier.copy(available = supplier.available - transfer)
    val newRecipient =
      recipient.copy(available = recipient.available + transfer)

    val updatedConnections = connections.map {
      case connection @ Connection(_, supp, rec, currentAmount) =>
        (supp, rec) match {
          case (`supplier`, `recipient`) =>
            connection.copy(
              amount    = currentAmount + transfer,
              supplier  = newSupplier,
              recipient = newRecipient
            )
          case (`supplier`, _)  => connection.copy(supplier = newSupplier)
          case (_, `recipient`) => connection.copy(recipient = newRecipient)
          case (_, _)           => connection
        }
    }

    copy(
      connections = updatedConnections,
      virtualRecipient = newRecipient match {
        case r: VirtualRecipient => r
        case _ => virtualRecipient
      },
      virtualSupplier = newSupplier match {
        case s: VirtualSupplier => s
        case _ => virtualSupplier
      }
    )
  }

  def resolve: ConnectionGraph = TransportIssueResolver(this)

  def targetFn: Double = connections.foldLeft(0.0) {
    case (sum, Connection(weight, _, _, amount)) => sum + weight * amount
  }
}

object ConnectionGraph {
  def initialDispose(initialGraph: ConnectionGraph): ConnectionGraph = {
    @tailrec
    def accumulate(suppliers: List[Supplier], recipients: List[Recipient], graph: ConnectionGraph): ConnectionGraph = {
      (suppliers, recipients) match {
        case (supp :: sTail, rec :: rTail) =>
          val updatedGraph =
            graph.updated(supp, rec, rec.demand - rec.available)
          val updatedConnection = graph.connections
            .zip(updatedGraph.connections)
            .find {
              case (old, updated) => old.amount != updated.amount
            }
            .map {
              case (_, updated) => updated
            }
            .get

          val nextSupp = updatedConnection.supplier match {
            case s if s.available == 0 => sTail
            case s                     => s :: sTail
          }

          val nextRecipient = updatedConnection.recipient match {
            case r if r.demand == r.available => rTail
            case r                            => r :: rTail
          }

          accumulate(nextSupp, nextRecipient, updatedGraph)
        case (supp :: sTail, Nil) =>
          val updatedGraph =
            graph.updated(supp, graph.virtualRecipient, supp.available)
          accumulate(sTail, Nil, updatedGraph)

        case (Nil, rec :: rTail) =>
          val updatedGraph = graph.updated(graph.virtualSupplier, rec, rec.demand - rec.available)
          accumulate(Nil, rTail, updatedGraph)

        case (Nil, Nil) => graph
      }
    }

    accumulate(initialGraph.suppliers, initialGraph.recipients, initialGraph)
  }

  def init(connections: Connection*): ConnectionGraph = {
    initialDispose {
      val totalSupply = connections.map(_.supplier).distinct.map(_.supply).sum
      val totalDemand = connections.map(_.recipient).distinct.map(_.demand).sum

      (totalSupply, totalDemand) match {
        case (supply, demand) if supply > demand =>
          val virtualRecipient = VirtualRecipient(supply - demand)
          val virtualConnections = connections
            .map(_.supplier)
            .distinct
            .map { supplier =>
              Connection(0, supplier, virtualRecipient)
            }
          ConnectionGraph(connections ++ virtualConnections)

        case (supply, demand) if supply < demand =>
          val virtualSupplier: VirtualSupplier =
            VirtualSupplier(demand - supply)
          val virtualConnections = connections
            .map(_.recipient)
            .distinct
            .map { recipient =>
              Connection(0, virtualSupplier, recipient)
            }
          ConnectionGraph(connections ++ virtualConnections)

        case _ => ConnectionGraph(connections)
      }
    }
  }
}
