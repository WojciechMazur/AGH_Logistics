package transport.model

case class Connection(weight: Double,
                      supplier: Supplier,
                      recipient: Recipient,
                      amount: Double = 0) {
  require(amount >= 0)
}
