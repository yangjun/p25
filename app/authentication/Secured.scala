package authentication

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future
import pdi.jwt._
import play.mvc.Security.AuthenticatedAction


class AuthenticatedRequest[A](val token: String, request: Request[A]) extends WrappedRequest[A](request) {
}

trait Secured {
  def Authenticated = AuthenticatedAction
  def Admin = AdminAction
}


object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  lazy val logger = LoggerFactory.getLogger(classOf[AuthenticatedAction])
  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) =
    request.jwtSession.getAs[String]("token") match {
      case Some(token) =>
        block(new AuthenticatedRequest(token, request)).map(_.refreshJwtSession(request))
      case _ => Future.successful(Unauthorized)
    }
}

object AdminAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) =
    request.jwtSession.getAs[String]("token") match {
      case Some(token)  => block(new AuthenticatedRequest(token, request)).map(_.refreshJwtSession(request))
      case Some(_) => Future.successful(Forbidden.refreshJwtSession(request))
      case _ => Future.successful(Unauthorized)
    }
}