package sales.repositories

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import repository.{MongoCRUDRepository, CRUDRepository}
import sales.models.{NameQuery, Area}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by yangjungis@126.com on 2016/4/17.
 */

trait AreaRepository extends CRUDRepository[Area, String] {
  def search(query: NameQuery, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[Area]] = {
    query.name match {
      case Some(name) => {
        search(Json.obj("name" -> name), limit)
      }
      case None => {
        search(Json.obj(), limit)
      }
    }
  }
}

@Singleton
class AreaMongoRepository @Inject()(reactiveMongoApi: ReactiveMongoApi)
  extends MongoCRUDRepository[Area, String] with AreaRepository {
  override def collection(implicit ec: ExecutionContext): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("areas"))
}
