package transport.model

import org.scalatest.{Matchers, PrivateMethodTester, WordSpec}
import transport.MediatorTransportIssueResolver

class MediatorTransportIssueResolverSpec
    extends WordSpec
    with Matchers
    with PrivateMethodTester {
  implicit def intToString(int: Int): String = int.toString

  "Mediator transport issue resolver" must {
    val supplier1  = Supplier(1, "A", 20)
    val supplier2  = Supplier(2,"B", 30)
    val recipient1 = Recipient(1,"D", 10)
    val recipient2 = Recipient(2,"E", 28)
    val recipient3 = Recipient(3,"F", 27)

    val purchaseCost = Map(
      supplier1 -> 10,
      supplier2 -> 12
    )
    val sellPrice = Map(
      recipient1 -> 30,
      recipient2 -> 25,
      recipient3 -> 30
    )

    val baseConnections = List(
      MediatorConnection(supplier1, recipient1, 8, purchaseCost(supplier1), sellPrice(recipient1)),
      MediatorConnection(supplier1, recipient2, 14, purchaseCost(supplier1), sellPrice(recipient2)),
      MediatorConnection(supplier1, recipient3, 17, purchaseCost(supplier1), sellPrice(recipient3)),
      MediatorConnection(supplier2, recipient1, 12, purchaseCost(supplier2), sellPrice(recipient1)),
      MediatorConnection(supplier2, recipient2, 9, purchaseCost(supplier2), sellPrice(recipient2)),
      MediatorConnection(supplier2, recipient3, 19, purchaseCost(supplier2), sellPrice(recipient3)),
    )

    val graph = MediatorTransportIssueResolver.init(baseConnections: _*)
    "have proper initial loads" in {
      assert {
        graph.connections
          .sortBy(c => (c.supplier.name, c.recipient.name))
          .map(_.units) == Seq(
          //@formatter:off
          10.0, 0.0, 10.0, 0.0,
          0.0, 28.0, 2.0, 0.0,
          0.0, 0.0, 15.0, 50.0
          //@formatter:on
        )
      }
    }
    "correctly resolve issue" in {
      assert {
          graph.resolve.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.units) == Seq(
          //@formatter:off
            10.0, 0.0,  10.0, 0.0,
            0.0,  28.0, 0.0,  2.0,
            0.0,  0.0,  17.0, 48.0
          //@formatter:on
          )
        }
    }
    "resolve issue with priority" in {
      val connectionsWithPriority = baseConnections.map{c => c.recipient match {
        case `recipient3` => c.copy(attributes = c.attributes.copy(priority = Some(1)))
        case _ => c
      }}
      val graph = MediatorTransportIssueResolver.init(connectionsWithPriority: _*)
      assert {
        graph.resolve.connections
          .sortBy(c => (c.supplier.name, c.recipient.name))
          .map(_.units) == Seq(
          //@formatter:off
          10.0, 0.0, 10.0, 0.0,
          0.0, 28.0, 0.0, 2.0,
          0.0, 0.0, 17.0, 48.0
          //@formatter:on
        )
      }
    }
  }
}
