package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.{Role, Secured, SessionService}
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models._
import sales.services.{FAQService}
import pdi.jwt._
import scala.concurrent.{ExecutionContext, Future}

/**
  * FAQ
  * Created by yangjungis@126.com on 2016/5/21.
  */
@Singleton
class FAQController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                              val faqService: FAQService,
                              val sessionService: SessionService
                             )
                             (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  lazy val logger = LoggerFactory.getLogger(classOf[PrescriptionController])

  def crateQuestion = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[CreateQuestion] {
      createQuestion =>
        val roles = Set(Role.salesman, Role.doctor)
        sessionService.isRoles(req.token, roles) flatMap (
          f => f match {
            case true =>
              faqService.crateQuestion(createQuestion) map (
                f => f match {
                  case Some(id) =>
                    val data = Json.obj("id" -> "id")
                    Ok(Json.toJson(data))
                  case None =>
                    BadRequest(Json.toJson(Json.obj()))
                })
            case false =>
              Future {
                val data = Json.obj("error" -> "权限不足")
                BadRequest(Json.toJson(data))
              }
          })
    }
  }

  def readQuestion(id: String) = Authenticated.async { implicit req =>
    faqService.readQuestion(id) map {
      f =>
        Ok(Json.toJson(f))
    }
  }

  def editQuestion(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[EditQuestion] {
      editQuestion =>
        val newEditQuestion = editQuestion.copy(id = id)
        val roles = Set(Role.salesman, Role.doctor)
        sessionService.isRoles(req.token, roles) flatMap (
          f => f match {
            case true =>
              faqService.editQuestion(editQuestion) map (
                f => f match {
                  case Some(id) =>
                    val data = Json.obj("id" -> "id")
                    Ok(Json.toJson(data))
                  case None =>
                    BadRequest(Json.toJson(Json.obj()))
                })
            case false =>
              Future {
                val data = Json.obj("error" -> "权限不足")
                BadRequest(Json.toJson(data))
              }
          })
    }
  }

  def deleteQuestion(id: String) = Authenticated.async { implicit req =>
    val roles = Set(Role.salesman, Role.doctor)
    sessionService.isRoles(req.token, roles) flatMap (
      f => f match {
        case true =>
          faqService.deleteQuestion(id) map (
            f => f match {
              case Some(id) =>
                val data = Json.obj("id" -> "id")
                Ok(Json.toJson(data))
              case None =>
                BadRequest(Json.toJson(Json.obj()))
            })
        case false =>
          Future {
            val data = Json.obj("error" -> "权限不足")
            BadRequest(Json.toJson(data))
          }
      })
  }

  def answerQuestion(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[AnswerQuestion] {
      answerQuestion =>
        sessionService.who(req.token) flatMap (
          userId => userId match {
            case Some(userId) =>
              val answer = answerQuestion.copy(answerer = userId)
              val roles = Set(Role.salesman, Role.firstReview, Role.review, Role.superAdmin)
              sessionService.isRoles(req.token, roles) flatMap (
                f => f match {
                  case true =>
                    faqService.answerQuestion(answer) map (
                      f => f match {
                        case Some(id) =>
                          val data = Json.obj("id" -> "id")
                          Ok(Json.toJson(data))
                        case None =>
                          BadRequest(Json.toJson(Json.obj()))
                      })
                  case false =>
                    Future {
                      val data = Json.obj("error" -> "权限不足")
                      BadRequest(Json.toJson(data))
                    }
                })
            case None =>
              Future {
                val data = Json.obj("error" -> "用户不存在")
                BadRequest(Json.toJson(data))
              }
          }
          )
    }
  }

  def editAnswer(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[AnswerQuestion] {
      answerQuestion =>
        sessionService.who(req.token) flatMap (
          userId => userId match {
            case Some(userId) =>
              val answer = answerQuestion.copy(answerer = userId)
              faqService.editAnswer(answer) map (
                f => f match {
                  case Some(id) =>
                    val data = Json.obj("id" -> "id")
                    Ok(Json.toJson(data))
                  case None =>
                    BadRequest(Json.toJson(Json.obj()))
                })
            case None =>
              Future {
                val data = Json.obj("error" -> "用户不存在")
                BadRequest(Json.toJson(data))
              }
          })
    }
  }

  def query(key: Option[String]) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    faqService.query(key, skip, limit) map {
      items =>
      Ok(Json.toJson(items))
    }
  }

  def queryAnswer(id: String) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    faqService.queryAnswer(id, skip, limit) map {
      items =>
        Ok(Json.toJson(items))
    }
  }

}
