package transport.model

sealed trait Node {
  def name:      String
  def available: Double

  require(available >= 0, "Available amount cannot be negative")
}

class Recipient(val name: String, val demand: Double, val available: Double) extends Node {
  override def toString: String = s"Recipient($name: $available/$demand)"

  def copy(name: String = name, demand: Double = demand, available: Double = available) =
    new Recipient(name, demand, available)
}

object Recipient {
  def apply(name: String, demand: Double): Recipient =
    new Recipient(name, demand, 0)
}

case class VirtualRecipient(override val demand: Double, override val available: Double = 0)
    extends Recipient("virtual", demand, available) {
  override def copy(name: String, demand: Double, available: Double): VirtualRecipient =
    VirtualRecipient(demand, available)
}

class Supplier(val name: String, val supply: Double, val available: Double) extends Node {
  override def toString: String = s"Supplier($name: $available/$supply)"
  def copy(name: String = name, supply: Double = supply, available: Double = available) =
    new Supplier(name, supply, available)
}

object Supplier {
  def apply(name: String, supply: Double): Supplier =
    new Supplier(name, supply, supply)
}

case class VirtualSupplier(override val supply: Double, override val available: Double)
    extends Supplier("virtual", supply, available) {
  override def copy(name: String, supply: Double, available: Double): VirtualSupplier =
    VirtualSupplier(supply, available)
}
object VirtualSupplier {
  def apply(supply: Double): VirtualSupplier =
    new VirtualSupplier(supply, supply)
}
