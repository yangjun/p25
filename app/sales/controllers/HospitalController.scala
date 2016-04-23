package sales.controllers

import javax.inject.{Inject, Singleton}

import authentication.Secured
import play.api.libs.json.Json
import play.api.mvc.Action
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import sales.models.{Hospital, NameQuery}
import sales.repositories.{HospitalMongoRepository, HospitalRepository}

import scala.concurrent.ExecutionContext

/**
  * Created by 军 on 2016/4/18.
  */
@Singleton
class HospitalController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                   val hospitalRepository: HospitalMongoRepository)
                                  (implicit exec: ExecutionContext)
  extends CRUDController[Hospital, String](hospitalRepository) with MongoController with ReactiveMongoComponents with Secured {


  // 查询
  def query(name: Option[String]) = Action.async {implicit request =>
    val limit = request.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val query = NameQuery(name)
    hospitalRepository.search(query, limit) map {
      p => Ok(Json.toJson(p))
    }
  }


}
