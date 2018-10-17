import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import transport.model.{Connection, ConnectionGraph}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {
  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("agh_logistics")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      } ~
        post {
          path("transport-issue") {
            entity(as[List[Connection]]) { deserializedConnections =>
              val suppliers = deserializedConnections.map(_.supplier).groupBy(_.name).mapValues(_.head)
              val recipients = deserializedConnections.map(_.recipient).groupBy(_.name).mapValues(_.head)
               val connections = deserializedConnections.map(c =>
                c.copy(
                  supplier = suppliers(c.supplier.name),
                  recipient = recipients(c.recipient.name)
                )
              )
              connections.foreach(println)
              val connectionGraph = ConnectionGraph.init(connections: _*)
              val result = connectionGraph.resolve
              complete(result)
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}