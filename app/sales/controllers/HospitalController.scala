package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.Secured
import controllers.JsonValidate
import play.api.libs.json.Json
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models.{CreateHospital, EditHospital, Hospital, NameQuery}
import sales.repositories.{HospitalMongoRepository, HospitalRepository}
import sales.services.HospitalService

import scala.concurrent.ExecutionContext
import scala.util.{Left, Right}

/**
  * Created by 军 on 2016/4/18.
  */
@Singleton
class HospitalController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                   val hospitalService: HospitalService)
                                  (implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents with JsonValidate with Secured {

  def create = Action.async(parse.json) {implicit req =>
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

  def edit(id: String) = Action.async(parse.json) {implicit req =>
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
}
