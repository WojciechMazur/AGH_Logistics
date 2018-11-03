package transport.model

sealed trait Connection {
  type AttributesType <: ConnectionAttributes
  def id:         String
  def supplier:   SupplierNode
  def recipient:  RecipientNode
  def attributes: AttributesType

  def targetFn: Double
  def units:     Double = attributes.units
  def totalCost: Double = attributes.totalCosts

  def supplier_=(supplierNode:   SupplierNode):  Connection
  def recipient_=(recipientNode: RecipientNode): Connection
  def units_=(units:             Double):        Connection

  override def equals(obj: Any): Boolean = obj match {
    case connection: Connection ⇒ this.id == connection.id
    case _ ⇒ false
  }
}

object Connection {
  implicit lazy val ordering: Ordering[Connection] = new Ordering[Connection] {
    def comparePriority(left: Connection, right: Connection): Int =
      (left.attributes.priority, right.attributes.priority) match {
        case (Some(lValue), None)         => lValue.compare(0)
        case (None, Some(rValue))         => 0.compare(rValue)
        case (Some(lValue), Some(rValue)) => lValue.compare(rValue)
        case _                            => 0
      }

    override def compare(x: Connection, y: Connection): Int = {
      comparePriority(x, y) match {
        case 0 =>
          val xProfit = x.attributes.totalProfits - x.attributes.totalCosts
          val yProfit = y.attributes.totalProfits - y.attributes.totalCosts
          xProfit.compare(yProfit) match {
            case 0 =>
              val xName = x.supplier.name + x.recipient.name
              val yName = y.supplier.name + y.recipient.name
              xName.compare(yName)
            case nonZero => nonZero
          }
        case order => order
      }
    }
  }.reverse

  def priorityValidationRules(supplier: SupplierNode, recipient: RecipientNode): Option[Int] =
    (supplier, recipient) match {
      case (_:    VirtualSupplier, _) => Some(Int.MinValue)
      case (_, _: VirtualRecipient)   => Some(Int.MinValue)
      case _ => None
    }
}

case class SimpleConnection(
  id:         String,
  supplier:   SupplierNode,
  recipient:  RecipientNode,
  attributes: SimpleConnectionAttributes
) extends Connection {
  override type AttributesType = SimpleConnectionAttributes
  override def targetFn: Double = attributes.transportCost * attributes.units
  override def supplier_=(supplierNode:   SupplierNode): Connection = copy(supplier = supplierNode)
  override def recipient_=(recipientNode: RecipientNode): Connection = copy(recipient = recipientNode)
  override def units_=(units:             Double): Connection =
    copy(
      attributes = attributes.copy(
        units = units
      )
    )
}

object SimpleConnection {
  def apply(
    supplier:      SupplierNode,
    recipient:     RecipientNode,
    transportCost: Double,
    priority:      Option[Int] = None
  ): SimpleConnection =
    new SimpleConnection(
      s"${supplier.id}-${recipient.id}",
      supplier,
      recipient,
      SimpleConnectionAttributes(
        transportCost,
        priority = Connection.priorityValidationRules(supplier, recipient).orElse(priority)
      )
    )

  implicit lazy val ordering: Ordering[SimpleConnection] = (x: SimpleConnection, y: SimpleConnection) => {
    Connection.ordering.compare(x, y)
  }
}

case class MediatorConnection(
  id:         String,
  supplier:   SupplierNode,
  recipient:  RecipientNode,
  attributes: MediatorConnectionAttributes
) extends Connection {
  override type AttributesType = MediatorConnectionAttributes
  override def targetFn: Double = attributes.units * (attributes.totalProfits - attributes.totalCosts)
  override def supplier_=(supplierNode:   SupplierNode): Connection = copy(supplier = supplierNode)
  override def recipient_=(recipientNode: RecipientNode): Connection = copy(recipient = recipientNode)
  override def units_=(units:             Double): Connection = copy(
    attributes = attributes.copy(
      units = units
    )
  )
}

object MediatorConnection {
  def apply(
    supplier:       SupplierNode,
    recipient:      RecipientNode,
    transportCost:  Double,
    purchaseCost:   Double,
    unitSaleProfit: Double,
    priority:       Option[Int] = None
  ) = new MediatorConnection(
    s"${supplier.id}-${recipient.id}",
    supplier,
    recipient,
    MediatorConnectionAttributes(
      transportCost,
      purchaseCost,
      unitSaleProfit,
      priority = Connection.priorityValidationRules(supplier, recipient).orElse(priority)
    )
  )
  implicit lazy val ordering: Ordering[MediatorConnection] = (x: MediatorConnection, y: MediatorConnection) => {
    Connection.ordering.compare(x, y)
  }
}
