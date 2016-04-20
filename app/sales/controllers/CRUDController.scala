package sales.controllers

import repository.{Identity, CRUDRepository}

import scala.concurrent.Future
import scala.util.{ Try, Success, Failure }

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.libs.json._

/**
 * Created by yangjungis@126.com on 2016/4/17.
 */

class CRUDController [E: Format, ID](val repository: CRUDRepository[E, ID])
                                    (implicit identity: Identity[E, ID]) extends Controller {

  // 默认每页大小
  val DEFAULT_LIMIT = Seq("50")

  def create = Action.async(parse.json) { implicit request =>
    validateAndThen[E] {
      entity =>
        repository.create(entity).map {
          case Right(id) =>
            val data = Json.obj("id" -> id.toString)
            Ok(data)
          case Left(err) => BadRequest(err)
        }
    }
  }

  def read(id: ID) = Action.async {
    repository.read(id).map(_.fold(
      NotFound(s"Entity #$id not found")
    )(entity =>
      Ok(Json.toJson(entity))))
  }

  def update(id: ID) = Action.async(parse.json) { implicit request =>
    validateAndThen[E] {
      entity =>
        repository.update(id, entity).map {
          case Right(id) =>
            val data = Json.obj("id" -> id.toString)
            Ok(data)
          case Left(err) => BadRequest(err)
        }
    }
  }

  def delete(id: ID) = Action.async {
    repository.delete(id).map {
      case Right(id) => Ok
      case Left(err) => BadRequest(err)
    }
  }

  def validateAndThen[T: Reads](t: T => Future[Result])(implicit request: Request[JsValue]) = {
    request.body.validate[T].map(t) match {
      case JsSuccess(result, _) =>
        result.recover { case e => BadRequest(e.getMessage()) }
      case JsError(err) =>
        Future.successful(BadRequest(Json.toJson(err.map {
          case (path, errors) => Json.obj("path" -> path.toString, "errors" -> JsArray(errors.flatMap(e => e.messages.map(JsString(_)))))
        })))
    }
  }

}
