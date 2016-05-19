package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.{AuthenticatedRequest, Secured, SessionService}
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import pdi.jwt._
import play.api.mvc.{Action, AnyContent, Controller, Result}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models.{CreatePrescription, EditPrescription}
import sales.services.PrescriptionService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yangjungis@126.com on 2016/5/19.
  */
@Singleton
class PrescriptionController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                       val prescriptionService: PrescriptionService,
                                       val sessionService: SessionService
                                      )
                                      (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  lazy val logger = LoggerFactory.getLogger(classOf[PrescriptionController])

  def create = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[CreatePrescription] {
      createPrescription =>
        sessionService.who(req.token) flatMap (_ match {
          case Some(who) =>
            prescriptionService.create(createPrescription.copy(doctorId = Some(who))) map (_ match {
              case Some(id) =>
                Ok(Json.obj("id" -> id))
              case None =>
                val data = Json.obj()
                BadRequest(Json.toJson(data))
            })
          case None =>
            Future {
              val data = Json.obj()
              BadRequest(Json.toJson(data))
            }
        })
    }
  }

  def edit(id: String) = Authenticated.async(parse.json) { implicit req =>
    validateAndThen[EditPrescription] {
      editPrescription =>
        sessionService.who(req.token) flatMap (_ match {
          case Some(who) =>
            prescriptionService.edit(id, editPrescription.copy(doctorId = Some(who))) map (_ match {
              case Some(id) =>
                Ok(Json.obj("id" -> id))
              case None =>
                val data = Json.obj()
                BadRequest(Json.toJson(data))
            })
          case None =>
            Future {
              val data = Json.obj()
              BadRequest(Json.toJson(data))
            }
        })
    }
  }

  def read(id: String) = Authenticated.async { implicit req =>
    sessionService.who(req.token) flatMap (_ match {
      case Some(who) =>
        prescriptionService.read(id) map (_ match {
          case Some(prescription) =>
            Ok(Json.toJson(prescription))
          case None =>
            val data = Json.obj()
            BadRequest(Json.toJson(data))
        })
      case None =>
        Future {
          val data = Json.obj()
          BadRequest(Json.toJson(data))
        }
    })

  }

  def delete(id: String) = Authenticated.async { implicit req =>
    exec(id, prescriptionService.delete, req)
    //    sessionService.who(req.token) flatMap (_ match {
    //      case Some(who) =>
    //        prescriptionService.delete(id) map (_ match {
    //          case Some(id) =>
    //            Ok(Json.obj("id" -> id))
    //          case None =>
    //            val data = Json.obj()
    //            BadRequest(Json.toJson(data))
    //        })
    //      case None =>
    //        Future {
    //          val data = Json.obj()
    //          BadRequest(Json.toJson(data))
    //        }
    //    })
  }

  def praise(id: String) = Action.async { implicit req =>
    prescriptionService.praise(id) map (_ match {
      case Some(num) =>
        Ok(Json.obj("id" -> id, "num" -> num))
      case None =>
        val data = Json.obj()
        BadRequest(Json.toJson(data))
    })
  }

  def disagree(id: String) = Action.async { implicit req =>
    prescriptionService.disagree(id) map (_ match {
      case Some(num) =>
        Ok(Json.obj("id" -> id, "num" -> num))
      case None =>
        val data = Json.obj()
        BadRequest(Json.toJson(data))
    })
  }

  private def exec(id: String, f: (String) => Future[Option[String]], req: AuthenticatedRequest[AnyContent]): Future[Result] = {
    sessionService.who(req.token) flatMap (_ match {
      case Some(who) =>
        f(id) map (_ match {
          case Some(id) =>
            Ok(Json.obj("id" -> id))
          case None =>
            val data = Json.obj()
            BadRequest(Json.toJson(data))
        })
      case None =>
        Future {
          val data = Json.obj()
          BadRequest(Json.toJson(data))
        }
    })
  }

  def query(tag: Option[String]) = Authenticated.async { implicit req =>
    val skip = req.queryString.get("skip").getOrElse(DEFAULT_SKIP).head.toInt
    val limit = req.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    sessionService.who(req.token) flatMap (_ match {
      case Some(who) =>
        prescriptionService.query(tag, skip, limit) map (items => {
          Ok(Json.toJson(items))
        })
      case None =>
        Future {
          val data = Json.obj()
          BadRequest(Json.toJson(data))
        }
    })
  }

  def swapHot(id: String) = Authenticated.async { implicit req =>
    sessionService.who(req.token) flatMap (_ match {
      case Some(who) =>
        prescriptionService.swapHot(id) map (_ match {
          case Some(hot) =>
            Ok(Json.obj("id" -> id, "hot" -> hot))
          case None =>
            val data = Json.obj()
            BadRequest(Json.toJson(data))
        })
      case None =>
        Future {
          val data = Json.obj()
          BadRequest(Json.toJson(data))
        }
    })

  }

}
