package authentication

import org.joda.time.{DateTime, Seconds}
import play.api.libs.json.Json
import utils.Utils

/**
  * Created by yangjungis@126.com on 2016/4/18.
  */

sealed trait Identity {
  val id: String
  val created: Option[DateTime]
}

// 系统用户
case class User(id: String,
                username: Option[String],
                email: Option[String],
                mobile: Option[String],
                displayName: Option[String],
                nickname: Option[String],
                sex: Option[Int],
                province: Option[String],
                city: Option[String],
                country: Option[String],
                avatar: Option[String],
                val created: Option[DateTime],
               // 用户拥有的角色
                roles: Option[Set[String]]
               ) extends Identity {
}

object User {
  implicit val format = Json.format[User]
  val mockUser = "root"
}

// 通过WX注册的用户
case class WxUser(id: String,
                  openid: String,
                  userId: String
                 )

object WxUser {
  implicit val format = Json.format[WxUser]
}


/**
  * 编辑用户信息
  * @param email
  * @param mobile
  * @param displayName
  */
case class EditUser(email: Option[String],
                       mobile: Option[String],
                       displayName: Option[String]) {
  require(displayName.nonEmpty, "显示名不能为空")
}

object EditUser {
  implicit val format = Json.format[EditUser]
}

//============== 用户角色（一个用户可以有多个角色） ===============
object Role {
  // 系统超级管理员，具有全部操作权限
  val superAdmin = "superAdmin"
  // 来宾，具有最低权限(注册登录用户默认具有的权限)
  val guest = "guest"
  // 销售人员（事务所开发人员）
  val salesman = "salesman"
  // 初审人员，事务所负责人，片区经理
  val firstReview = "firstReview"
  // 审核人员，经理，总经理
  val review = "review"
  // 医生
  val doctor = "doctor"
  // 还未登录用户，匿名用户
  val anonymous ="anonymous"
  // 库管，负责出库，对审核通过的订单进行出库
  val stock = "stock"
}


//============== 回话管理 =================================

case class Session(
                  // 标识
                  id: String,
                  // 用户ID
                  userId: String,
                  // 登录后生成的Token
                  token: String,
                  // 用于交换下一个Token
                  refreshToken: String,
                  // 创建日期
                  val created: DateTime = DateTime.now(),
                  // 有效期（单位秒）
                  ttl: Long
                  ) {

  /**
    * 是否过期
    * @return
    */
  def isExpiration(): Boolean = {
    val now = DateTime.now()
    Seconds.secondsBetween(created, now).getSeconds > ttl
  }

}
object Session {
  implicit val format = Json.format[Session]
  def create(userId: String): Session = {
    Session(id = Utils.nextId(),
      userId = userId,
      token = Utils.nextId(),
      refreshToken = Utils.nextId(),
      created =  DateTime.now(),
      // 2H
      7200
    )
  }
}

//============== 组织结构 =========================================================

