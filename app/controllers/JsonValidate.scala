package controllers

import play.api.libs.json.{JsArray, JsString, _}
import play.api.mvc.{Controller, Request, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/**
  * JSON 格式验证
  * Created by yangjungis@126.com on 2016/4/24.
  */
trait JsonValidate extends Controller {
  // 默认每页大小
  val DEFAULT_LIMIT = Seq("50")
  // 开始位置
  val DEFAULT_SKIP = Seq("0")

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
