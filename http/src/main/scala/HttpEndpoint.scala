import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import transport.model.{MediatorConnection, SimpleConnection}
import transport.{MediatorTransportIssueResolver, StandardTransportIssueResolver}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HttpEndpoint {
  def main(args: Array[String]) {

    implicit val system:       ActorSystem       = ActorSystem("agh_logistics")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
        path("transport-standard") {
          post {
            entity(as[List[SimpleConnection]]) { connections =>
              val connectionGraph = StandardTransportIssueResolver.init(connections: _*)
              val result          = connectionGraph.resolve
              complete(result)
            }
          }
        } ~ path("transport-mediator") {
          entity(as[List[MediatorConnection]]){ connections ⇒
            complete{
              MediatorTransportIssueResolver.init(connections: _*).resolve
            }
          }
          complete("Not implemented")
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}