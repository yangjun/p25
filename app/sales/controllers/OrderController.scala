package sales.controllers

import javax.inject.Inject

import javax.inject.{Inject, Singleton}

import authentication.Secured
import controllers.JsonValidate
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

  /**
    * 为医院生成订单
    *
    * @param id 医院id
    * @return
    */
  def createOrder(id: String) = Action.async(parse.json) {implicit req =>
    validateAndThen[CreateOrder] {
      param =>
        //TODO 取当前用户
        val proposer = ""
        val p = param.copy(hospitalId = Some(id), proposer = Some(proposer))
        val order = orderService.create(p)
        order.onFailure {
          case ex =>  BadRequest(Json.obj("error" -> ex.getMessage))
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

  def queryByHospital(id: String) = Action.async {implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    orderService.queryByHospital(id, skip, limit) map {
      p => {
        Ok(Json.toJson(p))
      }
    }
  }

  def query(no: Option[String], status: Option[String]) = Action.async {implicit req =>
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
            Ok (Json.toJson (p) )
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
  def cancel(id: String) = Action.async { implicit req =>
    orderService.cancel(id) map {
      p => {
        val data = Json.obj("id" -> p)
        Ok(Json.toJson(data))
      }
    }
  }

  /**
    * 审核通过
    *
    * @param id
    * @return
    */
  def permit(id: String) = Action.async { implicit req =>
    orderService.permit(id) map {
      p => {
        val data = Json.obj("id" -> p)
        Ok(Json.toJson(data))
      }
    }
  }

  /**
    *
    * @param id
    * @return
    */
  def reject(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[RejectOrder] {
      param =>
        orderService.reject(id, param.reason) map {
          p => {
            val data = Json.obj("id" -> p)
            Ok(Json.toJson(data))
          }
        }
    }
  }

  /**
    * 出库
    * @param id
    * @return
    */
  def goods(id: String) = Action.async { implicit req =>
    orderService.goods(id) map {
      p => {
        val data = Json.obj("id" -> p)
        Ok(Json.toJson(data))
      }
    }
  }

  /**
    * 收货后订单确认
    * @param id
    * @return
    */
  def confirm(id: String) = Action.async { implicit req =>
    orderService.confirm(id) map {
      p => {
        val data = Json.obj("id" -> p)
        Ok(Json.toJson(data))
      }
    }
  }

}
