package transport.model

import org.scalatest.{Matchers, PrivateMethodTester, WordSpec}
import transport.TransportIssueResolver

class ConnectionGraphSpec
    extends WordSpec
    with Matchers
    with PrivateMethodTester {
  "Connection Graph" when {
    val supplier1 = Supplier("A", 50)
    val supplier2 = Supplier("B", 70)
    val supplier3 = Supplier("C", 30)
    val recipient1 = Recipient("D", 20)
    val recipient2 = Recipient("E", 40)
    val recipient3 = Recipient("F", 90)

    val baseConnections = List(
      Connection(3, supplier1, recipient1),
      Connection(5, supplier1, recipient2),
      Connection(7, supplier1, recipient3),
      Connection(12, supplier2, recipient1),
      Connection(10, supplier2, recipient2),
      Connection(9, supplier2, recipient3),
      Connection(13, supplier3, recipient1),
      Connection(3, supplier3, recipient2),
      Connection(9, supplier3, recipient3)
    )
    val iterate = PrivateMethod[ConnectionGraph]('singleIteration)
    "balanced connections" should {
      val graph = ConnectionGraph.init(baseConnections: _*)
      "have proper initial values" in {
        assert(graph.virtualSupplier.available == 0)
        assert(graph.virtualSupplier.supply == 0)
        assert(graph.virtualRecipient.available == 0)
        assert(graph.virtualRecipient.demand == 0)
        assert(
          graph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.amount) == Seq(
            // @formatter:off
            20.0, 30.0, 0.0,
            0.0,  0.0,  70.0,
            0.0,  10.0, 20.0
            // @formatter:on
          ))
      }
      val iteratedGraph = TransportIssueResolver invokePrivate iterate(graph)
      "target function should be lower after iteration" in {
        assert(iteratedGraph.targetFn < graph.targetFn)
      }
      "successfully iterate" in {
        assert {
          iteratedGraph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.amount) == Seq(
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
            .map(_.amount) == Seq(
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
      "to much supply" should {
        val extraSupplier = Supplier("S", 30)
        val extraConnections = Seq(
          Connection(8, extraSupplier, recipient1),
          Connection(9, extraSupplier, recipient2),
          Connection(10, extraSupplier, recipient3)
        )

        val graph =
          ConnectionGraph.init(baseConnections ++ extraConnections: _*)
        "have proper initial values" in {

          assert(graph.virtualSupplier.available == 0)
          assert(graph.virtualSupplier.supply == 0)
          assert(graph.virtualRecipient.available == 30)
          assert(graph.virtualRecipient.demand == 30)
          assert(
            graph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.amount) == Seq(
              // @formatter:off
              20.0, 30.0, 0.0,  0.0,
              0.0,  0.0,  40.0, 30.0,
              0.0,  10.0, 20.0, 0.0,
              0.0,  0.0,  30.0, 0.0
              // @formatter:on
            ))
        }
        val iteratedGraph = TransportIssueResolver invokePrivate iterate(graph)
        "target function should be lower after iteration" in {
          assert(iteratedGraph.targetFn < graph.targetFn)
        }
        "successfully iterate" in {
          assert {
            iteratedGraph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.amount) == Seq(
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
              .map(_.amount) == Seq(
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
      "to much demand" should {
        val extraRecipient = Recipient("R", 30)
        val extraConnections = Seq(
          Connection(8, supplier1, extraRecipient),
          Connection(9, supplier2, extraRecipient),
          Connection(10, supplier3, extraRecipient)
        )
        val graph =
          ConnectionGraph.init(baseConnections ++ extraConnections: _*)
        "have proper initial values" in {
          assert(graph.virtualSupplier.available == 0)
          assert(graph.virtualSupplier.supply == 30)
          assert(graph.virtualRecipient.available == 0)
          assert(graph.virtualRecipient.demand == 0)
          graph.connections
            .sortBy(c => (c.supplier.name, c.recipient.name))
            .map(_.amount) == Seq(
            // @formatter:off
            20.0, 30.0, 0.0,  0.0,
            0.0,  0.0,  70.0, 0.0,
            0.0,  10.0, 20.0, 0.0,
            0.0,  0.0,  0.0,  30.0
            // @formatter:on
          )
        }
        val iteratedGraph = TransportIssueResolver invokePrivate iterate(graph)
        "target function should be lower after iteration" in {
          assert(iteratedGraph.targetFn < graph.targetFn)
        }
        "successfully iterate" in {
          assert {
            iteratedGraph.connections
              .sortBy(c => (c.supplier.name, c.recipient.name))
              .map(_.amount) == Seq(
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
              .map(_.amount) == Seq(
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
    }
  }
}
