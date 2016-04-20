package sales.repositories

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import repository.{CRUDRepository, MongoCRUDRepository}
import sales.models.{Hospital, NameQuery}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by 军 on 2016/4/18.
  */
trait HospitalRepository extends CRUDRepository[Hospital, String] {
  def search(query: NameQuery, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[Hospital]] = {
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
class HospitalMongoRepository @Inject()(reactiveMongoApi: ReactiveMongoApi)
  extends MongoCRUDRepository[Hospital, String] with HospitalRepository {
  override def collection(implicit ec: ExecutionContext): Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection("hospitals"))
}