import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import transport.{MediatorTransportIssueResolver, StandardTransportIssueResolver}
import transport.model.{MediatorConnection, SimpleConnection}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HttpEndpoint {
  def main(args: Array[String]) {

    implicit val system:       ActorSystem       = ActorSystem("agh_logistics")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val route: Route = {
      import Directives._
      import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

      val rejectionHandler = corsRejectionHandler
        .withFallback(RejectionHandler.default)
        .mapRejectionResponse(
          response ⇒ {
            println(response)
            response
          }
      )

      val exceptionHandler = ExceptionHandler {
        case e: NoSuchElementException ⇒
          println(e)
          complete(StatusCodes.NotFound → e.getMessage)
      }

      val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

      handleErrors {
        cors() {
          handleErrors {
            path("transport" / "standard") {
              post {
                entity(as[List[SimpleConnection]]) { connections =>
                  val connectionGraph = StandardTransportIssueResolver.init(connections.map(_.clean): _*)
                  val result = connectionGraph.resolve.connections
                    .filterNot(_.isVirtual)
                  complete(result)
                }
              }
            } ~ path("transport" / "mediator") {
            post {
              entity(as[List[MediatorConnection]]) { connections ⇒
                val connectionGraph = MediatorTransportIssueResolver.init(connections.map(_.clean): _*)
                val result = connectionGraph.resolve.connections
                  .filterNot(_.isVirtual)
                complete(result)
              }
            }
          }
        }
      }
    }
    }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}