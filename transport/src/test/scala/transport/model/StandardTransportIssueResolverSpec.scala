package transport.model

import org.scalatest._
import transport.StandardTransportIssueResolver

class StandardTransportIssueResolverSpec
    extends WordSpec
    with Matchers
    with PrivateMethodTester {
  implicit def intToString(int: Int): String = int.toString

  "Standard transport issue resolver" when {
    val supplier1 = Supplier(1,"A", 50)
    val supplier2 = Supplier(2,"B", 70)
    val supplier3 = Supplier(3,"C", 30)
    val recipient1 = Recipient(1,"D", 20)
    val recipient2 = Recipient(2,"E", 40)
    val recipient3 = Recipient(3,"F", 90)

    val baseConnections = List(
      SimpleConnection(supplier1, recipient1, 3),
      SimpleConnection(supplier1, recipient2, 5),
      SimpleConnection(supplier1, recipient3, 7),
      SimpleConnection(supplier2, recipient1,12),
      SimpleConnection(supplier2, recipient2,10),
      SimpleConnection(supplier2, recipient3, 9),
      SimpleConnection(supplier3, recipient1,13),
      SimpleConnection(supplier3, recipient2,3),
      SimpleConnection(supplier3, recipient3,9)
    )
    val iterate = PrivateMethod[ConnectionGraph]('singleIteration)
    "balanced connections" must {
      val graph = StandardTransportIssueResolver.init(baseConnections: _*)
      "have proper initial values" in {
        import io.circe.syntax._
        import io.circe.generic.auto._
        println(graph.connections.asJson)

        assert(graph.virtualSupplier.available == 0)
        assert(graph.virtualSupplier.supply == 0)
        assert(graph.virtualRecipient.available == 0)
        assert(graph.virtualRecipient.demand == 0)
        assert(graph.connections.map(_.units).sum == 150)
        assert(
          graph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.units) == Seq(
            // @formatter:off
            20.0, 10.0, 20.0,
            0.0,  0.0,  70.0,
            0.0,  30.0, 0.0
            // @formatter:on
          ))
      }
      val iteratedGraph = StandardTransportIssueResolver invokePrivate iterate(graph)
      "target function should be lower or equal after iteration" in {
        assert(iteratedGraph.target <= graph.target)
      }
      "successfully iterate" in {
        assert {
          iteratedGraph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.units) == Seq(
            // @formatter:off
            20.0, 10.0, 20.0,
            0.0,  0.0,  70.0,
            0.0,  30.0, 0.0
            // @formatter:on
          )
        }
      }
      "successfully resolve" in {
        assert {
          graph.resolve.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.units) == Seq(
            // @formatter:off
            20.0, 10.0, 20.0,
            0.0,  0.0,  70.0,
            0.0,  30.0, 0.0
            // @formatter:on
          )
        }
      }
    }

    "unbalanced connections" when {
      "to much supply" must {
        val extraSupplier = Supplier("extra","S", 30)
        val extraConnections = Seq(
          SimpleConnection(extraSupplier, recipient1, 8),
          SimpleConnection(extraSupplier, recipient2, 9),
          SimpleConnection(extraSupplier, recipient3, 10)
        )

        val graph =
          StandardTransportIssueResolver.init(baseConnections ++ extraConnections: _*)
        "have proper initial values" in {

          assert(graph.virtualSupplier.available == 0)
          assert(graph.virtualSupplier.supply == 0)
          assert(graph.virtualRecipient.available == 30)
          assert(graph.virtualRecipient.demand == 30)
          assert(graph.connections.map(_.units).sum == 180)
          assert(
            graph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.units) == Seq(
              // @formatter:off
              20.0, 10.0, 20.0,  0.0,
              0.0,  0.0,  70.0,  0.0,
              0.0,  30.0, 0.0,   0.0,
              0.0,  0.0,  0.0,   30.0
              // @formatter:on
            ))
        }
        val iteratedGraph = StandardTransportIssueResolver invokePrivate iterate(graph)
        "target function should be lower after iteration" in {
          assert(iteratedGraph.target <= graph.target)
        }
        "successfully iterate" in {
          assert {
            iteratedGraph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.units) == Seq(
              // @formatter:off
              20.0, 10.0, 20.0, 0.0,
              0.0,  0.0,  70.0, 0.0,
              0.0,  30.0, 0.0,  0.0,
              0.0,  0.0,  0.0,  30.0
              // @formatter:on
            )
          }
        }
        "successfully resolve" in {
          assert {
            graph.resolve.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.units) == Seq(
              // @formatter:off
              20.0, 10.0, 20.0, 0.0,
              0.0,  0.0,  70.0, 0.0,
              0.0,  30.0, 0.0,  0.0,
              0.0,  0.0,  0.0,  30.0
              // @formatter:on
            )
          }
        }
      }
      "to much demand" must {
        val extraRecipient = Recipient("extra", "R", 30)
        val extraConnections = Seq(
          SimpleConnection(supplier1, extraRecipient,8),
          SimpleConnection(supplier2, extraRecipient,9),
          SimpleConnection(supplier3, extraRecipient,10)
        )
        val graph =
          StandardTransportIssueResolver.init(baseConnections ++ extraConnections: _*)
        "have proper initial values" in {
          assert(graph.virtualSupplier.available == 0)
          assert(graph.virtualSupplier.supply == 30)
          assert(graph.virtualRecipient.available == 0)
          assert(graph.virtualRecipient.demand == 0)
          assert(graph.connections.map(_.units).sum == 180)

          graph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.units) == Seq(
            // @formatter:off
            20.0, 30.0, 0.0,  0.0,
            0.0,  0.0,  70.0, 0.0,
            0.0,  10.0, 20.0, 0.0,
            0.0,  0.0,  0.0,  30.0
            // @formatter:on
          )
        }
        val iteratedGraph = StandardTransportIssueResolver invokePrivate iterate(graph)
        "target function should be lower after iteration" in {
          assert(iteratedGraph.target <= graph.target)
        }
        "successfully iterate" in {
          assert {
            iteratedGraph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.units) == Seq(
              // @formatter:off
              20.0, 10.0, 20.0, 0.0,
              0.0,  0.0,  40.0, 30.0,
              0.0,  30.0, 0.0,  0.0,
              0.0,  0.0,  30.0, 0.0
              // @formatter:on
            )
          }
        }
        "successfully resolve" in {
          assert {
            graph.resolve.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.units) == Seq(
              // @formatter:off
              20.0, 10.0, 20.0, 0.0,
              0.0,  0.0,  40.0, 30.0,
              0.0,  30.0, 0.0,  0.0,
              0.0,  0.0,  30.0,  0.0
              // @formatter:on
            )
          }
        }
      }
    }
  }
}
