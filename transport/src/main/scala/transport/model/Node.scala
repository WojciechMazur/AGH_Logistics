package transport.model

sealed trait Node {
  def id:        String
  def name:      String
  def available: Double
  def isVirtual: Boolean = this.name.toLowerCase.contains("virtual")
  require(available >= 0, "Available amount cannot be negative")
  def available_=(value: Double): Node
  def withAvailableDelta(value:Double):Node = this.available_=(this.available + value)
}


sealed trait VirtualNode {
  self: Node ⇒
  override def isVirtual: Boolean = true
}

case class Recipient(id: String, name: String, demand: Double, available: Double = 0) extends Node {
  def withDemand(value:    Double): Recipient = copy(demand    = value)
  override def available_=(value: Double): Recipient = copy(available = value)
}

case class Supplier(id: String, name: String, supply: Double, available: Double) extends Node {
  def withSupply(value:    Double): Supplier = copy(supply = value)
  override def available_=(value: Double): Supplier = copy(available = value)
}

object Supplier {
  def apply(id: String, name: String, supply: Double): Supplier =
    new Supplier(id, name, supply, supply)
}

object VirtualSupplier {
  def apply(supply: Double, available: Double): Supplier with VirtualNode =
    new Supplier("vs-01", "virtualSupplier", supply, available) with VirtualNode
  def apply(supply: Double): Supplier with VirtualNode = VirtualSupplier(supply, supply)
}

object VirtualRecipient {
  def apply(demand: Double, available: Double): Recipient with VirtualNode =
    new Recipient("vr-01", "virtualRecipient", demand, available) with VirtualNode
  def apply(demand: Double): Recipient with VirtualNode = VirtualRecipient(demand, 0)
}
