package transport.model
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe._

sealed trait Connection {
  type AttributesType <: ConnectionAttributes
  def supplier:   Supplier
  def recipient:  Recipient
  def attributes: AttributesType

  def targetFn: Double
  def units:     Double = attributes.units
  def totalCost: Double = attributes.totalCosts
  def withUnits(units: Double): Connection = this match {
    case c @ SimpleConnection(_, _, attributes)   => c.copy(attributes = attributes.copy(units = units))
    case c @ MediatorConnection(_, _, attributes) => c.copy(attributes = attributes.copy(units = units))
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

  def priorityValidationRules(supplier: Supplier, recipient: Recipient): Option[Int] = (supplier, recipient) match {
    case (_:    VirtualSupplier, _) => Some(Int.MinValue)
    case (_, _: VirtualRecipient)   => Some(Int.MinValue)
    case _ => None
  }
}

case class SimpleConnection(
  supplier:   Supplier,
  recipient:  Recipient,
  attributes: SimpleConnectionAttributes
) extends Connection {
  override type AttributesType = SimpleConnectionAttributes
  override def targetFn: Double = attributes.transportCost * attributes.units
}

object SimpleConnection {
  def apply(
    supplier:      Supplier,
    recipient:     Recipient,
    transportCost: Double,
    priority:      Option[Int] = None
  ): SimpleConnection =
    new SimpleConnection(
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
  supplier:   Supplier,
  recipient:  Recipient,
  attributes: MediatorConnectionAttributes
) extends Connection {
  override type AttributesType = MediatorConnectionAttributes
  override def targetFn: Double = attributes.units * (attributes.totalProfits - attributes.totalCosts)
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

object Connection{
  implicit lazy val decoder: Decoder[Connection] = deriveDecoder
  implicit lazy val encoder: Encoder[Connection] = deriveEncoder
}
