package transport.model

sealed trait Connection {

  type AttributesType <: ConnectionAttributes
  def id:         String
  def supplier:   Supplier
  def recipient:  Recipient
  def attributes: AttributesType

  def targetFn: Double
  def units:     Double = attributes.units
  def totalCost: Double = attributes.totalCosts

  def supplier(supplier:Supplier): Connection = supplier_=(supplier)
  def supplier_=(supplierNode:   Supplier):  Connection
  def recipient(recipient: Recipient): Connection = recipient_=(recipient)
  def recipient_=(recipientNode: Recipient): Connection
  def units_=(units:             Double):        Connection
  def units(units:Double): Connection = units_=(units)

  def isVirtual: Boolean = this.recipient.isVirtual || this.supplier.isVirtual
  override def equals(obj: Any): Boolean = obj match {
    case connection: Connection ⇒ this.id == connection.id
    case _ ⇒ false
  }

  def clean: Connection
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

  def priorityValidationRules(supplier: Supplier, recipient: Recipient): Option[Int] =
    (supplier, recipient) match {
      case (_:    VirtualNode, _) => Some(Int.MinValue)
      case (_, _: VirtualNode)   => Some(Int.MinValue)
      case _ => None
    }
}

case class SimpleConnection(
  id:         String,
  supplier:   Supplier,
  recipient:  Recipient,
  attributes: SimpleConnectionAttributes
) extends Connection {
  override type AttributesType = SimpleConnectionAttributes
  override def targetFn: Double = attributes.unitTransportCost * attributes.units
  override def supplier_=(supplierNode:   Supplier): Connection = copy(supplier = supplierNode)
  override def recipient_=(recipientNode: Recipient): Connection = copy(recipient = recipientNode)
  override def units_=(units:             Double): Connection =
    copy(
      attributes = attributes.copy(
        units = units
      )
    )
  override def clean: Connection = copy(
    supplier = supplier.copy(
      available = supplier.supply
    ),
    recipient = recipient.copy(
      available = 0.0
    ),
    attributes = attributes.copy(
      units = 0.0
    )
  )
}

object SimpleConnection {
  def apply(
    supplier:      Supplier,
    recipient:     Recipient,
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
  supplier:   Supplier,
  recipient:  Recipient,
  attributes: MediatorConnectionAttributes
) extends Connection {
  override type AttributesType = MediatorConnectionAttributes
  override def targetFn: Double = attributes.units * (attributes.totalProfits - attributes.totalCosts)
  override def supplier_=(supplierNode:   Supplier): Connection = copy(supplier = supplierNode)
  override def recipient_=(recipientNode: Recipient): Connection = copy(recipient = recipientNode)
  override def units_=(units:             Double): Connection = copy(
    attributes = attributes.copy(
      units = units
    )
  )
  override def clean: Connection = copy(
    supplier = supplier.copy(
      available = supplier.supply
    ),
    recipient = recipient.copy(
      available = 0.0
    ),
    attributes = attributes.copy(
      units = 0.0
    )
  )
}

object MediatorConnection {
  def apply(
    supplier:       Supplier,
    recipient:      Recipient,
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
