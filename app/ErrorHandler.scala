import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent._

/**
  * Created by yangjungis@126.com on 2016/4/24.
  */
class ErrorHandler @Inject() (
                               env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router]
                             ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Future.successful(
      InternalServerError("A server error occurred: " + exception.getMessage)
    )
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden("You're not allowed to access this resource.")
    )
  }

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    //  Future.successful(BadRequest(views.html.defaultpages.badRequest(request.method, request.uri, message)))
    val data = Json.obj(
      "method" -> request.method,
      "uri" -> request.uri,
      "message" -> message
    )
    Future.successful(BadRequest(Json.toJson(data)))
  }
}
