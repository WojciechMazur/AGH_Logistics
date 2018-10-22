package transport.model

import transport.{ MediatorTransportIssueResolver, StandardTransportIssueResolver }

import scala.language.postfixOps

case class ConnectionGraph(
  connections:      Seq[Connection],
  virtualSupplier:  VirtualSupplier = VirtualSupplier(0),
  virtualRecipient: VirtualRecipient = VirtualRecipient(0)
) {
  lazy val sortedConnections: Seq[Connection] = connections.sorted
  lazy val reverseSortedConntections: Seq[Connection] = {
    val (real, virtual) =
      connections
        .map(c => (c, (c.supplier, c.recipient)))
        .partition {
          case (_, (_:    VirtualSupplier, _)) => false
          case (_, (_, _: VirtualRecipient))   => false
          case _ => true
        } match {
        case (realTuples, virtualTuples) =>
          (realTuples.map(_._1).sorted, virtualTuples.map(_._1).sorted)
      }
    real ++ virtual
  }

  def find(supplier: Supplier, recipient: Recipient): Connection =
    connections
      .find(conn => conn.supplier.name == supplier.name && conn.recipient.name == recipient.name)
      .get

  def indexOf(supplier: Supplier, recipient: Recipient): Int =
    connections
      .indexWhere(c => c.supplier == supplier && c.recipient == recipient)

  def updated(supplier: Supplier, recipient: Recipient, amount: Double): ConnectionGraph = {
    val transfer = if (amount > 0) {
      Math.min(amount, supplier.available)
    } else {
      Math.min(amount, recipient.available)
    }

    val newSupplier  = supplier.copy(available  = supplier.available - transfer)
    val newRecipient = recipient.copy(available = recipient.available + transfer)

    val updatedConnections = connections.map { connection =>
      (connection.supplier, connection.recipient) match {
        case (`supplier`, `recipient`) =>
          (connection match {
            case c: SimpleConnection   => c.copy(newSupplier, newRecipient)
            case c: MediatorConnection => c.copy(newSupplier, newRecipient)
          }).withUnits(connection.units + transfer)
        case (`supplier`, _) =>
          connection match {
            case c: SimpleConnection   => c.copy(newSupplier)
            case c: MediatorConnection => c.copy(newSupplier)
          }
        case (_, `recipient`) =>
          connection match {
            case c: SimpleConnection   => c.copy(recipient = newRecipient)
            case c: MediatorConnection => c.copy(recipient = newRecipient)
          }
        case (_, _) => connection
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

  def resolve: ConnectionGraph = connections.head match {
    case SimpleConnection(_, _, _)   => StandardTransportIssueResolver(this)
    case MediatorConnection(_, _, _) => MediatorTransportIssueResolver(this)
  }

  def target: Double = connections.map(_.targetFn).sum
}
