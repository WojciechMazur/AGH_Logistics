package transport.model

sealed trait ConnectionAttributes {
  def totalCosts:   Double = 0.0
  def totalProfits: Double = 0.0
  def units:    Double
  def priority: Option[Int]
  def limit:    Option[Double]

  def isBlocked: Boolean = limit.contains(0.0)
  require(units >= 0)
  require(limit.getOrElse(0.0) >= 0)
  require(totalCosts >= 0)
  require(totalProfits >= 0)
}

case class SimpleConnectionAttributes(
  unitTransportCost: Double,
  units:         Double = 0.0,
  limit:         Option[Double] = None,
  priority:      Option[Int] = None
) extends ConnectionAttributes {
  override def totalCosts: Double = unitTransportCost
}

case class MediatorConnectionAttributes(
  unitTransportCost: Double,
  unitPurchaseCost:  Double,
  unitSaleProfit:    Double,
  units:             Double = 0.0,
  limit:             Option[Double] = None,
  priority:          Option[Int] = None
) extends ConnectionAttributes {
  override def totalCosts:   Double = unitPurchaseCost + unitTransportCost
  override def totalProfits: Double = unitSaleProfit
}
