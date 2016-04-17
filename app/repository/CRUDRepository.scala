package repository

import play.api.libs.json._

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by 军 on 2016/4/8.
 */

trait CRUDRepository[E, ID] {

  def create(entity: E)(implicit ec: ExecutionContext): Future[Either[String, ID]]

  def read(id: ID)(implicit ec: ExecutionContext): Future[Option[E]]

  def update(id: ID, entity: E)(implicit ec: ExecutionContext): Future[Either[String, ID]]

  def delete(id: ID)(implicit ec: ExecutionContext): Future[Either[String, ID]]

  def search(criteria: JsObject, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[E]]

  def search(criteria: Criteria, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[E]] = {
    this.search(criteria.toJson, limit)
  }

}

trait Identity[E, ID] {
  def name: String

  def of(entity: E): Option[ID]

  def set(entity: E, id: ID): E

  def clear(entity: E): E

  def next: ID
}

import reactivemongo.api._

abstract class MongoCRUDRepository[E: Format, ID: Format](implicit identity: Identity[E, ID]) extends CRUDRepository[E, ID] {

  import reactivemongo.play.json._
  import reactivemongo.play.json.collection.JSONCollection

  def collection(implicit ec: ExecutionContext): Future[JSONCollection]

  def create(entity: E)(implicit ec: ExecutionContext): Future[Either[String, ID]] = {
    search(Json.toJson(
      identity.clear(entity)).as[JsObject], 1).flatMap {
      case t if t.size > 0 =>
        Future.successful(Right(identity.of(t.head).get)) // let's be idempotent
      case _ => {
        val id = identity.next
        val doc = Json.toJson(identity.set(entity, id)).as[JsObject]

        collection.flatMap(_.insert(doc).map {
          case le if le.ok == true => Right(id)
          case le => Left(le.message)
        })
      }
    }
  }

  def read(id: ID)(implicit ec: ExecutionContext): Future[Option[E]] = collection.flatMap(_.find(Json.obj(identity.name -> id)).one[E])

  def update(id: ID, entity: E)(implicit ec: ExecutionContext): Future[Either[String, ID]] = {
    val doc = Json.toJson(identity.set(entity, id)).as[JsObject]
    collection.flatMap(_.update(Json.obj(identity.name -> id), doc) map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    })
  }

  def delete(id: ID)(implicit ec: ExecutionContext): Future[Either[String, ID]] = collection.flatMap(_.remove(Json.obj(identity.name -> id)) map {
    case le if le.ok == true => Right(id)
    case le => Left(le.message)
  })

  def search(criteria: JsObject, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[E]] =
    collection.flatMap(_.find(criteria).
      cursor[E](readPreference = ReadPreference.nearest).
      collect[List](limit))


}
