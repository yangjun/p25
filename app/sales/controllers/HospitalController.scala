package sales.controllers

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

/**
  * Created by 军 on 2016/4/18.
  */
@Singleton
class HospitalController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                   val hospitalService: HospitalService,
                                   val orderService: OrderService,
                                   val doctorService: DoctorService)
                                  (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  def create = Action.async(parse.json) { implicit req =>
    validateAndThen[CreateHospital] {
      entity =>
        hospitalService.create(entity).map {
          case Right(id) =>
            val data = Json.obj("id" -> id)
            Ok(data)
          case Left(err) => BadRequest(Json.toJson(err))
        }
    }
  }

  def edit(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[EditHospital] {
      entity =>
        hospitalService.edit(id, entity).map {
          case Right(id) =>
            val data = Json.obj("id" -> id)
            Ok(data)
          case Left(err) => BadRequest(Json.toJson(err))
        }
    }
  }

  def query(name: Option[String]) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val query = NameQuery(name)
    hospitalService.search(query, skip, limit) map {
      p => Ok(Json.toJson(p))
    }
  }

  /**
    * 查询单个医院信息
    * @param id
    * @return
    */
  def read(id: String) = Action.async { implicit req =>
    hospitalService.pk(id) map {
      p =>
        p match {
          case Some(p) =>
            Ok (Json.toJson (p) )
          case None => {
            val err = Json.obj("error" -> "为发现")
            BadRequest(Json.toJson(err))
          }
        }
    }
  }

  def develop(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[DevelopHospital] {
      entity =>
        // 取当前登录用户（JWT)
        val v = entity.copy(hospitalId = id)
        hospitalService.develop(v).map {
          case Right(id) =>
            val data = Json.obj("id" -> id)
            Ok(data)
          case Left(err) => BadRequest(Json.toJson(err))
        }
    }
  }

  def resume(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[EditDevelopResume] {
      entity =>
        // TODO
        // 取当前登录用户（JWT)
        val v = entity.copy(hospitalId = id)
        hospitalService.resume(v).map {
          case Right(id) =>
            val data = Json.obj("id" -> id)
            Ok(data)
          case Left(err) => BadRequest(Json.toJson(err))
        }
    }
  }

  // 测试重定向
  def redirect = Action { implicit req =>
    var session = JwtSession()
    session = session +("user", "yangjun")
    session = session.withSignature("secret")
    val result = Redirect("/count", 302)
    result.withJwtSession(session)
  }


  /**
    * 成为合作伙伴
    *
    * @param id
    * @return
    */
  def becomePartner(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[BecomePartner] {
      param =>
        val p = param.copy(id = id)
        hospitalService.becomePartner(p) map (f => {
          f match {
            case Some(id) =>
              val data = Json.obj("id" -> id)
              Ok(data)
            case None =>
              BadRequest(Json.obj())
          }
        })
    }
  }

  /**
    * 编辑归档信息
    *
    * @param id
    * @return
    */
  def editArchive(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[EditHospitalArchive] {
      param =>
        val p = param.copy(id = id)
        hospitalService.editHospitalArchive(p) map (f => {
          f match {
            case Some(id) =>
              val data = Json.obj("id" -> id)
              Ok(data)
            case None =>
              BadRequest(Json.obj())
          }
        })
    }
  }

  /**
    * 添加医生
    *
    * @param id 医院id
    * @return
    */
  def addDoctor(id: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[AddDoctor] {
      param =>
        val p = param.copy(hospital = Some(id))
        doctorService.create(p) map (f => {
          f match {
            case Some(id) =>
              val data = Json.obj("id" -> id)
              Ok(data)
            case None =>
              BadRequest(Json.obj())
          }
        })
    }
  }

  def deleteDoctor(id: String) = Action.async { implicit req =>
    doctorService.delete(id) map (f => {
      f match {
        case Some(id) => {
          val data = Json.obj("id" -> id)
          Ok(data)
        }
        case None =>
          BadRequest(Json.obj())
      }
    })
  }

  def queryDoctor(id: String, name: Option[String]) = Action.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    doctorService.search(id, name, skip, limit) map {
      p => Ok(Json.toJson(p))
    }
  }
}
