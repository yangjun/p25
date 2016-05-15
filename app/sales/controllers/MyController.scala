package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.{Secured, SessionService}
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents}
import sales.services.{OrderService, TaskService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * 个人中心
  * Created by yangjungis@126.com on 2016/5/15.
  */
@Singleton
class MyController @Inject()(sessionService: SessionService,
                             taskService: TaskService,
                             orderService: OrderService)
                            (implicit exec: ExecutionContext)
  extends Controller with JsonValidate with Secured {

  /**
    * 查询当前用户订单
    * @param no
    * @param status
    * @return
    */
    def order(no: Option[String], status: Option[String]) = Authenticated.async { implicit req =>
      val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
      val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
      val token = req.token
      sessionService.who(token) flatMap {
        who =>
          orderService.query1(
            who = who,
            no = no,
            status = status,
            skip = skip,
            limit = limit
          ) map (f => {
            Ok(Json.toJson(f))
          })
      }
    }

  /**
    * 查询当前用户任务
    * @param status
    * @return
    */
   def task(status: Option[String], action: Option[String]) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val token = req.token
    sessionService.who(token) flatMap {
      who =>
        taskService.query(
          who = who,
          status = status,
          action = action,
          skip = skip,
          limit = limit
        ) map (f => {
          Ok(Json.toJson(f))
        })
    }
  }

  lazy val logger = LoggerFactory.getLogger(classOf[MyController])

}
