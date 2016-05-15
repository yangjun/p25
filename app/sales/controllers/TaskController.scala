package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.{Secured, SessionService, UserService}
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models.{EditDevelopResume, PermitOrder, RejectOrder, Stakeholder}
import sales.services.{DoctorService, HospitalService, OrderService, TaskService}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by yangjungis@126.com on 2016/5/15.
  */
@Singleton
class TaskController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                               val taskService: TaskService,
                               val sessionService: SessionService
                              )
                              (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  lazy val logger = LoggerFactory.getLogger(classOf[TaskController])

  /**
    * 根据状态查询当前登录用户待办任务
    *
    * @param status
    * @return
    */
  def query(status: Option[String], action: Option[String]) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val token = req.token
    sessionService.who(token) flatMap {
      who =>
        logger.debug("who -> {}", who)
        val tasks = taskService.query(who = who,
          status = status,
          action= action,
          skip = skip,
          limit = limit)
        tasks map (p => {
          Ok(Json.toJson(p))
        })
    }
  }

  def read(id: String) = Authenticated.async { implicit req =>
    taskService.read(id) map (
      task => {
        task match {
          case Some(task) =>
            Ok(Json.toJson(task))
          case None =>
            Ok(Json.obj())
        }
      })
  }

  def accept(id: String) = Authenticated.async(parse.json) { implicit req =>
    taskService.accept(id) map (
      task => {
        task match {
          case Some(task) =>
            Ok(Json.obj("id" -> task))
          case None =>
            Ok(Json.obj())
        }
      })
  }

  def permit(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[PermitOrder] {
      permitOrder => {
        val token = req.token
        sessionService.who(token) flatMap (
          who =>
            who match {
              case Some(who) =>
                taskService.permit(id, permitOrder) map (
                  task => {
                    task match {
                      case Some(task) =>
                        Ok(Json.obj("id" -> task))
                      case None =>
                        Ok(Json.obj())
                    }
                  })
              case None =>
                Future {
                  Ok(Json.obj())
                }
            })
      }
    }
  }

  def reject(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[RejectOrder] {
      rejectOrder => {
        val token = req.token
        sessionService.who(token) flatMap (
          who =>
            who match {
              case Some(who) =>
                taskService.reject(id, rejectOrder) map (
                  task => {
                    task match {
                      case Some(task) =>
                        Ok(Json.obj("id" -> task))
                      case None =>
                        Ok(Json.obj())
                    }
                  })
              case None =>
                Future {
                  BadRequest(Json.obj("error" -> "非法用户"))
                }
            })
      }
    }
  }

  def stakeholders(id: String) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val token = req.token
    sessionService.who(token) flatMap (
      who =>
        who match {
          case Some(who) =>
            taskService.stakeholders(id, skip, limit) map (
              stakeholders => {
                Ok(Json.toJson(stakeholders))
              })
          case None =>
            Future {
              BadRequest(Json.obj("error" -> "非法用户"))
            }
        })
  }

}
