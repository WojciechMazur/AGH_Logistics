package transport.model
import org.scalatest.{Matchers, PrivateMethodTester, WordSpec}

class ConnectionSpec extends WordSpec
    with Matchers
    with PrivateMethodTester {
  implicit def intToString(int: Int): String = int.toString

  "Connections" must {
    val supplier1  = Supplier(1,"A", 50)
    val supplier2  = Supplier(2,"B", 70)
    val recipient1 = Recipient(3,"D", 20)
    val recipient2 = Recipient(4,"E", 40)

    "have correct order" when {
      "simple connection" when {
        "has no priority" in {
          val connections = Map(
            3 -> SimpleConnection(supplier1, recipient1, 5),
            2 -> SimpleConnection(supplier1, recipient2, 3),
            1 -> SimpleConnection(supplier2, recipient1, 1),
            4 -> SimpleConnection(supplier2, recipient2, 8),
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
        "has some priority" in {
          val connections = Map(
            3 -> SimpleConnection(supplier1, recipient1, 1),
            5 -> SimpleConnection(supplier1, VirtualRecipient(20), 0, Some(-1)),
            2 -> SimpleConnection(supplier1, recipient2, 3, Some(1)),
            1 -> SimpleConnection(supplier2, recipient1, 1, Some(1)),
            4 -> SimpleConnection(supplier2, recipient2, 3)
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
        "has full priority" in {
          val connections = Map(
            5 -> SimpleConnection(supplier1, VirtualRecipient(20), 0, Some(-1)),
            1 -> SimpleConnection(supplier1, recipient1, 1, Some(3)),
            4 -> SimpleConnection(supplier1, recipient2, 3, Some(1)),
            3 -> SimpleConnection(supplier2, recipient1, 1, Some(1)),
            2 -> SimpleConnection(supplier2, recipient2, 3, Some(3)),
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
      }
      "mediator connection" when {
        "has no priority" in {
          val connections = Map(
            3 -> MediatorConnection(supplier1, recipient1,  10, 5, 6),
            2 ->  MediatorConnection(supplier1, recipient1, 5, 5, 5),
            1 ->  MediatorConnection(supplier1, recipient1, 5, 5, 6),
            4 ->  MediatorConnection(supplier1, recipient1, 10, 5, 5),
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
        "has some priority" in {
          val connections = Map(
            3 ->  MediatorConnection(supplier1, recipient1, 0, 0, 2),
            5 ->  MediatorConnection(supplier1, recipient1, 0, 0, 10, Some(-1)),
            2 ->  MediatorConnection(supplier1, recipient1, 0, 0, 1, Some(1)),
            1 ->  MediatorConnection(supplier1, recipient1, 0, 0, 2, Some(1)),
            4 ->  MediatorConnection(supplier1, recipient1, 0, 0, 1)
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
        "has full priority" in {
          val connections = Map(
            5 ->  MediatorConnection(supplier1, recipient1, 0, 0, 8, Some(-1)),
            1 ->  MediatorConnection(supplier1, recipient1, 0, 0, 2, Some(2)),
            4 ->  MediatorConnection(supplier1, recipient1, 0, 0, 1, Some(1)),
            3 ->  MediatorConnection(supplier1, recipient1, 0, 0, 2, Some(1)),
            2 ->  MediatorConnection(supplier1, recipient1, 0, 0, 1, Some(2))
          )
          assert(connections.values.toList.sorted == connections.toList.sortBy(_._1).map(_._2))
        }
      }
    }
  }
}
