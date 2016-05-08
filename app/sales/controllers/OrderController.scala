package sales.controllers

import javax.inject.Inject
import javax.inject.{Inject, Singleton}

import authentication.{Secured, User}
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import pdi.jwt._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models._
import sales.services.{DoctorService, HospitalService, OrderService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Left, Right}
import scala.concurrent.ExecutionContext

/**
  * Created by yangjungis@126.com on 2016/5/7.
  */
class OrderController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                val hospitalService: HospitalService,
                                val orderService: OrderService,
                                val doctorService: DoctorService)
                               (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  lazy val logger = LoggerFactory.getLogger(classOf[OrderController])

  /**
    * 为医院生成订单
    *
    * @param id 医院id
    * @return
    */
  def createOrder(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[CreateOrder] {
      param =>
        //TODO 取当前用户
        val proposer = User.mockUser
        val p = param.copy(hospitalId = Some(id), proposer = Some(proposer))
        val order = orderService.create(p)
        order.onFailure {
          case ex => BadRequest(Json.obj("error" -> ex.getMessage))
        }
        order map { f =>
          f match {
            case Some(id) => {
              val data = Json.obj("id" -> id)
              Ok(data)
            }
            case None => {
              BadRequest(Json.obj())
            }
          }
        }
    }
  }

  def queryByHospital(id: String) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    orderService.queryByHospital(id, skip, limit) map {
      p => {
        Ok(Json.toJson(p))
      }
    }
  }

  def query(no: Option[String], status: Option[String]) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val who = None
    orderService.query(who, no, status, skip, limit) map {
      p => {
        Ok(Json.toJson(p))
      }
    }
  }

  def read(id: String) = Action.async { implicit req =>
    orderService.pk(id) map {
      p =>
        p match {
          case Some(p) =>
            Ok(Json.toJson(p))
          case None => {
            val err = Json.obj("error" -> "未发现")
            BadRequest(Json.toJson(err))
          }
        }
    }
  }

  /**
    * 取消订单
    *
    * @param id
    * @return
    */
  def cancel(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[CancelOrder] {
      cancelOrder =>
        orderService.cancel(id, cancelOrder) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

  /**
    *
    * 正常流程
    *
    * @param id
    * @return
    */
  def permit(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[PermitOrder] {
      param =>
        logger.debug("permitOrder -> {}", param)
        orderService.permit(id, param) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

  /**
    * 回退流程
    *
    * @param id
    * @return
    */
  def reject(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[RejectOrder] {
      param =>
        orderService.reject(id, param) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

  /**
    * 创建出库单
    *
    * @param id
    * @return 出库单标识
    */
  def createStockOrder(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[CreateStockOrder] {
      param =>
        orderService.createStockOrder(id, param) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

  /**
    * 给一个出库单批量添加出库清单
    * @param id 出库单标识
    * @return
    */
  def addStockItem(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[AddGoodsItem] {
      param => orderService.addStockItem(id, param) map {
        p => {
          val data = Json.obj("id" -> p)
          Ok(Json.toJson(data))
        }
      }
    }
  }

  /**
    * 批量删除一个出库单的清单项
    * @param id 出库单标识
    * @return
    */
  def removeStockItem(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[RemoveGoodsItem] {
      param => orderService.removeStockItem(id, param) map {
        p => {
          val data = Json.obj("id" -> p)
          Ok(Json.toJson(data))
        }
      }
    }
  }

  /**
    * 根据出库单查询出库清单
    * @param id
    * @return
    */
  def queryStockItemByStock(id: String) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    orderService.queryStockItemByStock(id, skip, limit) map {
        p => {
          Ok(Json.toJson(p))
        }
      }
    }

  /**
    * 根据订单查询出库清单
    * @param id
    * @return
    */
  def queryStockItemByOrder(id: String) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    orderService.queryStockItemByOrder(id, skip, limit) map {
      p => {
        Ok(Json.toJson(p))
      }
    }
  }

  /**
    * 收货后订单确认
    *
    * @param id
    * @return
    */
  def confirm(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[PermitOrder] {
      param =>
        orderService.confirm(id, param) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

}
