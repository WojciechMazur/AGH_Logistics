package transport.model

import transport.{ MediatorTransportIssueResolver, StandardTransportIssueResolver }

import scala.language.postfixOps

case class ConnectionGraph(
  connections:      Seq[Connection],
  virtualSupplier:  Supplier with VirtualNode= VirtualSupplier(0),
  virtualRecipient: Recipient with VirtualNode = VirtualRecipient(0)
) {
  lazy val totalSupply:       Double          = connections.filterNot(_.isVirtual).map(_.supplier).distinct.map(_.supply).sum
  lazy val totalDemand:       Double          = connections.filterNot(_.isVirtual).map(_.recipient).distinct.map(_.demand).sum
  lazy val sortedConnections: Seq[Connection] = connections.sorted
  lazy val reverseSortedConnections: Seq[Connection] = {
    val (real, virtual) =
      connections
        .map(c => (c, (c.supplier, c.recipient)))
        .partition {
          case (_, (_:    VirtualNode, _)) => false
          case (_, (_, _: VirtualNode))   => false
          case _ => true
        } match {
        case (realTuples, virtualTuples) =>
          (realTuples.map(_._1).sorted, virtualTuples.map(_._1).sorted)
      }
    real ++ virtual
  }

  def find(supplier: Supplier, recipient: Recipient): Connection =
    connections
      .find(conn => conn.supplier.id == supplier.id && conn.recipient.id == recipient.id)
      .get

  def indexOf(supplier: Supplier, recipient: Recipient): Int =
    connections
      .indexWhere(c => c.supplier == supplier && c.recipient == recipient)

  private def syncNodes(
    supplier:     Supplier,
    newSupplier:  Supplier,
    recipient:    Recipient,
    newRecipient: Recipient,
    transfer:     Double
  ): ConnectionGraph = {
    val updatedConnections = connections.map { connection =>
      (connection.supplier, connection.recipient) match {
        case (`supplier`, `recipient`) =>
          connection
            .supplier(newSupplier)
            .recipient(newRecipient)
            .units(connection.units + transfer)
        case (`supplier`, _)  => connection.supplier = newSupplier
        case (_, `recipient`) => connection.recipient = newRecipient
        case (_, _)           => connection
      }
    }

    val newVirtualSupplier: Supplier with VirtualNode =
      if (newSupplier.isVirtual) { newSupplier.asInstanceOf[Supplier with VirtualNode] } else virtualSupplier
    val newVirtualRecipient: Recipient with VirtualNode =
      if (newRecipient.isVirtual) { newRecipient.asInstanceOf[Recipient with VirtualNode] } else virtualRecipient

    copy(
      connections = updatedConnections,
      newVirtualSupplier,
      newVirtualRecipient
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


    val (newSupplier: Supplier, newRecipient: Recipient) = (
      supplier.withAvailableDelta(-transfer),
      recipient.withAvailableDelta(transfer)
    )
    syncNodes(supplier, newSupplier, recipient, newRecipient, transfer)
  }

  def resolve: ConnectionGraph = connections.head match {
    case SimpleConnection(_, _, _, _)   => StandardTransportIssueResolver(this)
    case MediatorConnection(_, _, _, _) => MediatorTransportIssueResolver(this)
  }

  def target: Double = connections.map(_.targetFn).sum
}
