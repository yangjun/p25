package authentication

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by yangjungis@126.com on 2016/4/18.
  */

sealed trait Identity {
  val id: String
  val created: DateTime
}

// 系统用户
case class User(id: String,
                username: Option[String],
                email: Option[String],
                mobile: Option[String],
                nickname: Option[String],
                sex: Option[Int],
                province: Option[String],
                city: Option[String],
                country: Option[String],
                avatar: Option[String]
               ) extends Identity {
  val created: DateTime = DateTime.now()
}

object User {
  implicit val format = Json.format[User]
}

// 通过WX注册的用户
case class WxUser(id: String,
                  openid: String,
                  userId: String
                 )

object WxUser {
  implicit val format = Json.format[WxUser]
}

