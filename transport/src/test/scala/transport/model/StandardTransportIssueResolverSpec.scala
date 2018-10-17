package transport.model

import org.scalatest.{Matchers, PrivateMethodTester, WordSpec}
import transport.StandardTransportIssueResolver

class StandardTransportIssueResolverSpec
    extends WordSpec
    with Matchers
    with PrivateMethodTester {
  "Standard transport issue resolver" when {
    val supplier1 = Supplier("A", 50)
    val supplier2 = Supplier("B", 70)
    val supplier3 = Supplier("C", 30)
    val recipient1 = Recipient("D", 20)
    val recipient2 = Recipient("E", 40)
    val recipient3 = Recipient("F", 90)

    val baseConnections = List(
      SimpleConnection(supplier1, recipient1, 3),
      SimpleConnection(supplier1, recipient2, 5),
      SimpleConnection(supplier1, recipient3, 7),
      SimpleConnection( supplier2, recipient1,12),
      SimpleConnection( supplier2, recipient2,10),
      SimpleConnection(supplier2, recipient3, 9),
      SimpleConnection( supplier3, recipient1,13),
      SimpleConnection(supplier3, recipient2,3),
      SimpleConnection(supplier3, recipient3,9)
    )
    val iterate = PrivateMethod[ConnectionGraph]('singleIteration)
    "balanced connections" must {
      val graph = StandardTransportIssueResolver.init(baseConnections: _*)
      "have proper initial values" in {
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
          graph.resolve.get.connections
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
        val extraSupplier = Supplier("S", 30)
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
            graph.resolve.get.connections
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
        val extraRecipient = Recipient("R", 30)
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
            graph.resolve.get.connections
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
