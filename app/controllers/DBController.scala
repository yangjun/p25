package controllers


import javax.inject.Inject

import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

import reactivemongo.play.json._
import play.modules.reactivemongo.json.collection._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * Created by 军 on 2016/4/6.
 */
class DBController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {


  def collection(name: String): JSONCollection = reactiveMongoApi.db.collection[JSONCollection](name)

  def findByName(name: String) = Action.async {
    // 取用户集合
    val user = collection("userEntity")
    // 构造查询
    val cursor: Cursor[JsObject] = user
      .find(Json.obj("username" -> name))
//      .sort(Json.obj("username" -> 1))
      .cursor[JsObject]

    val users: Future[JsArray] = cursor.collect[List]().map { u =>
      Json.arr(u)
    }
    users.map { u =>
      Ok(u)
    }
  }
}
