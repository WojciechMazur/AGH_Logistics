package transport.model

import transport.{ MediatorTransportIssueResolver, StandardTransportIssueResolver }

import scala.language.postfixOps

case class ConnectionGraph(
  connections:      Seq[Connection],
  virtualSupplier:  VirtualSupplier = VirtualSupplier(0),
  virtualRecipient: VirtualRecipient = VirtualRecipient(0)
) {
  lazy val totalSupply:       Double          = connections.map(_.supplier).distinct.map(_.supply).sum
  lazy val totalDemand:       Double          = connections.map(_.recipient).distinct.map(_.demand).sum
  lazy val sortedConnections: Seq[Connection] = connections.sorted
  lazy val reverseSortedConnections: Seq[Connection] = {
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

  def find(supplier: SupplierNode, recipient: RecipientNode): Connection =
    connections
      .find(conn => conn.supplier.id == supplier.id && conn.recipient.id == recipient.id)
      .get

  def indexOf(supplier: SupplierNode, recipient: RecipientNode): Int =
    connections
      .indexWhere(c => c.supplier == supplier && c.recipient == recipient)

  private def syncNodes(
    supplier:     SupplierNode,
    newSupplier:  SupplierNode,
    recipient:    RecipientNode,
    newRecipient: RecipientNode,
    transfer:     Double
  ): ConnectionGraph = {
    val updatedConnections = connections.map { connection =>
      (connection.supplier, connection.recipient) match {
        case (`supplier`, `recipient`) =>
          (connection match {
            case c: SimpleConnection   => c.copy(supplier = newSupplier, recipient = newRecipient)
            case c: MediatorConnection => c.copy(supplier = newSupplier, recipient = newRecipient)
          }).units = connection.units + transfer
        case (`supplier`, _) =>
          connection match {
            case c: SimpleConnection   => c.copy(supplier = newSupplier)
            case c: MediatorConnection => c.copy(supplier = newSupplier)
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

  def updatedByCycle(connection: Connection, amount: Double): ConnectionGraph = {
    val (supplier, recipient) = (connection.supplier, connection.recipient)
    syncNodes(supplier, supplier, recipient, recipient, amount)
  }

  def updated(connection: Connection, amount: Double): ConnectionGraph = {
    val (supplier, recipient) = (connection.supplier, connection.recipient)
    val transfer = if (amount > 0) {
      Math.min(amount, supplier.available)
    } else {
      Math.min(amount, recipient.available)
    }
    val (newSupplier: SupplierNode, newRecipient: RecipientNode) = (
      supplier.available  = supplier.available - transfer,
      recipient.available = recipient.available + transfer
    )
    syncNodes(supplier, newSupplier, recipient, newRecipient, transfer)
  }

  def resolve: ConnectionGraph = connections.head match {
    case SimpleConnection(_, _, _, _)   => StandardTransportIssueResolver(this)
    case MediatorConnection(_, _, _, _) => MediatorTransportIssueResolver(this)
  }

  def target: Double = connections.map(_.targetFn).sum
}
