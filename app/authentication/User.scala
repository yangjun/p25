package authentication

import org.joda.time.DateTime

/**
  * Created by yangjungis@126.com on 2016/4/18.
  */

sealed trait Identity {
  val id: Option[String]
  val created: DateTime
}

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


case class WxUser(id: String,
                  openid: String,
                  userId: String
                 )

