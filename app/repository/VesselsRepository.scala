package repository

import java.util.UUID

import models.Vessel
import play.api.libs.json.Json


import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by 军 on 2016/4/9.
 */


case class SearchQuery(name: String) {
}

trait VesselsRepository extends CRUDRepository[Vessel, UUID]{
  def search(query: SearchQuery, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[Vessel]] = {
    search(Json.obj("name" -> query.name), limit)
  }
}

import reactivemongo.play.json.collection._
import reactivemongo.api.DB

class VesselsMongoRepository(db: Future[DB]) extends MongoCRUDRepository[Vessel, UUID] with VesselsRepository {
  override def collection(implicit ec: ExecutionContext): Future[JSONCollection] = db.map(_.collection("vessels"))
}