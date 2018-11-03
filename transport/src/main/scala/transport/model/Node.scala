package transport.model

sealed trait Node {
  def id:        String
  def name:      String
  def available: Double

  require(available >= 0, "Available amount cannot be negative")
  def available_=(value: Double): Node
}

sealed trait SupplierNode extends Node {
  def supply: Double
  def supply_=(value: Double): SupplierNode
}

object SupplierNode {
//  implicit lazy val encorder: Encoder[SupplierNode] = deriveEncoder
//  implicit lazy val decoder: Decoder[SupplierNode] = deriveDecoder
}

sealed trait RecipientNode extends Node {
  def demand: Double
  def demand_=(value: Double): RecipientNode
}

object RecipientNode {
//  implicit lazy val encorder: Encoder[RecipientNode] = deriveEncoder
//  implicit lazy val decoder:  Decoder[RecipientNode] = deriveDecoder
}

sealed trait VirtualNode {}

case class Recipient(id: String, name: String, demand: Double, available: Double = 0) extends RecipientNode {
  override def demand_=(value:    Double): Recipient = copy(demand    = value)
  override def available_=(value: Double): Recipient = copy(available = value)
}

case class Supplier(id: String, name: String, supply: Double, available: Double) extends SupplierNode {
  override def supply_=(value:    Double): Supplier = copy(supply    = value)
  override def available_=(value: Double): Supplier = copy(available = value)
}

object Supplier {
  def apply(id: String, name: String, supply: Double): Supplier =
    new Supplier(id, name, supply, supply)
}

case class VirtualSupplier(supply: Double, available: Double) extends SupplierNode with VirtualNode {
  val id   = "vs-01"
  val name = "virtualSupplier"
  override def supply_=(value:    Double): VirtualSupplier = copy(supply    = value)
  override def available_=(value: Double): VirtualSupplier = copy(available = value)
}

object VirtualSupplier {
  def apply(supply: Double): VirtualSupplier = new VirtualSupplier(supply, supply)
}

case class VirtualRecipient(demand: Double, available: Double) extends RecipientNode with VirtualNode {
  val id   = "vr-01"
  val name = "virtualRecipient"
  override def demand_=(value:    Double): VirtualRecipient = copy(demand    = value)
  override def available_=(value: Double): VirtualRecipient = copy(available = value)
}

object VirtualRecipient {
  def apply(demand: Double): VirtualRecipient = new VirtualRecipient(demand, 0)
}
