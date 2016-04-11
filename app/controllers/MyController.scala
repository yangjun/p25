package controllers

import javax.inject.{Inject, Singleton}

import controllers.User.UserReader
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JsCursor._
import reactivemongo.play.json.collection.{JSONCollection, JsCursor}
import services.MongoDB

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by 军 on 2016/3/29.
 */


case class User(id: BSONObjectID, name: String, displayName: Option[String])

object User {

  implicit object UserReader extends BSONDocumentReader[User] {
    override def read(doc: BSONDocument): User = {
      val id = doc.getAs[BSONObjectID]("_id").get
      val name = doc.getAs[String]("username").get
      val displayName = doc.getAs[String]("displayName")
      User(id, name, displayName)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = {
      BSONDocument(
        "_id" -> user.id,
        "username" -> user.name
      )
    }
  }

  implicit object UserJsonWriter extends Writes[User] {
    override def writes(user: User) = Json.obj(
      "id" -> user.id.stringify,
      "username" -> user.name,
      "displayName" -> user.displayName
    )
  }

}

@Singleton
class MyController @Inject()(mongodb: MongoDB)
  extends Controller {
  val db = mongodb.db
  val user: BSONCollection = db[BSONCollection]("userEntity")
  val jsonUser: JSONCollection = db[JSONCollection]("userEntity")

  def findByName(user: BSONCollection)(implicit ec: ExecutionContext, reader: BSONDocumentReader[User]): Future[List[User]] = {
    // 查询条件
    val query = BSONDocument("username" -> "admin")
    // 过滤器，返回的字段
    val filter = BSONDocument("id" -> 1,
      "username" -> 1
//      "displayName" -> 1
    )
    // 定义排序
    val sort = BSONDocument("username" -> 1)
    // Cursor.foldWhile[A]
    val cursor = user.find(query, filter)
      .sort(sort)
      .cursor[User]()

    cursor.collect[List](1)
  }

  def findByName1(user: JSONCollection)(implicit ec: ExecutionContext): Future[List[JsObject]] = {
    val query = Json.obj("username" -> "admin")
    //    type ResultType = JsObject
    val cursor = user.find(query).cursor[JsObject]()
    cursor.collect[List](1)
  }

  def find(name: String) = Action.async {

    val f = findByName(user)
    f.map(list => {
      val user = list.head
      //      val username: Option[String] = doc.getAs[String]("lifecycle")
      // 模式匹配
      //      username match {
      //        case Some(v) => {
      //          println(v)
      //        }
      //        case None => {
      //
      //        }
      //      }
      Ok(Json.toJson(user))
    })

  }


}


