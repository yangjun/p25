package authentication

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future
import pdi.jwt._


class AuthenticatedRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

trait Secured {
  def Authenticated = AuthenticatedAction
  def Admin = AdminAction
}

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) =
    request.jwtSession.getAs[String]("user") match {
      case Some(userId) => block(new AuthenticatedRequest(userId, request)).map(_.refreshJwtSession(request))
      case _ => Future.successful(Unauthorized)
    }
}

object AdminAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) =
    request.jwtSession.getAs[String]("user") match {
      case Some(userId)  => block(new AuthenticatedRequest(userId, request)).map(_.refreshJwtSession(request))
      case Some(_) => Future.successful(Forbidden.refreshJwtSession(request))
      case _ => Future.successful(Unauthorized)
    }
}