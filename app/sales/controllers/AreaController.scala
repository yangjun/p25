package sales.controllers

import javax.inject.{Singleton, Inject}

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents, MongoController}
import sales.models.{Area, NameQuery}
import sales.repositories.{AreaMongoRepository, AreaRepository}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by yangjungis@126.com on 2016/4/17.
 */
@Singleton
class AreaController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                               val areaRepository: AreaMongoRepository)
                              (implicit exec: ExecutionContext)
  extends CRUDController[Area, String](areaRepository) with MongoController with ReactiveMongoComponents with Secured {

  // 查询
  def query(name: Option[String]) = Action.async {implicit request =>
    val limit = request.queryString.get("limit").getOrElse(DEFAULT_LIMIT).head.toInt
    val query = NameQuery(name)
    areaRepository.search(query, limit) map {
      p => Ok(Json.toJson(p))
    }
  }

  def privateApi = Authenticated.async {
    Future {
      Ok("Only the best can see that.")
    }
  }

}
