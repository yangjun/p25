package authentication

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import utils.Utils
import wx.RegisterWxUser

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import org.joda.time.DateTime
import reactivemongo.api.{QueryOpts, ReadPreference}

import reactivemongo.play.json._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yangjungis@126.com on 2016/4/23.
  */
trait UserService {

  /**
    * 注册微信用户
    *
    * @param wxUser
    * @return
    */
  def registerWxUser(wxUser: RegisterWxUser)(implicit ec: ExecutionContext): Future[Either[String, String]]

  /**
    * 查询用户信息
    *
    * @param userId
    * @param ec
    * @return
    */
  def userProfile(userId: String)(implicit ec: ExecutionContext): Future[Option[User]]

}

@Singleton
class UserServiceImpl @Inject()(reactiveMongoApi: ReactiveMongoApi,
                                sessionService: SessionService
                               ) extends UserService {
  val logger = LoggerFactory.getLogger(classOf[UserService])

  private def userCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("users"))
  }

  private def wxUserCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("wxUsers"))
  }

  /**
    * 注册微信用户
    *
    * @param wxUser
    * @return
    */
  override def registerWxUser(wxUser: RegisterWxUser)(implicit ec: ExecutionContext): Future[Either[String, String]] = {
    val criteria = Json.obj("openid" -> wxUser.openid)
    import reactivemongo.play.json._
    // 根据openid 查询
    val cursor: Future[Option[WxUser]] = wxUserCollection.flatMap(_.find(criteria).one[WxUser])
    cursor.map(f => {
      // 未注册
      f match {
        case None => {
          logger.debug("{} 还未注册", wxUser.openid)
          import scala.concurrent.Await
          val userId = Utils.nextId()
          val user = User(userId,
            wxUser.nickname,
            None,
            None,
            wxUser.nickname,
            wxUser.nickname,
            wxUser.sex,
            wxUser.province,
            wxUser.city,
            wxUser.country,
            wxUser.headimgurl,
            Some(DateTime.now()),
            roles = Some(Set(Role.guest))
          )
          // 开始注册
          val result = userCollection.flatMap(_.insert(user).map {
            case le if le.ok => {
              val wx = WxUser(Utils.nextId(),
                wxUser.openid,
                userId
              )
              wxUserCollection.flatMap(_.insert(wx))
              Right(userId)
            }
            case le => Left(le.message)
          })

          Await.result(result, 10 seconds)
        }
        case Some(u) => {
          // 已经注册，直接返回userId
          logger.debug("{} 已经注册", wxUser.openid)
          val result =
            try {
              val userId = u.userId
              Right(userId)
            } catch {
              case e: Exception => {
                Left(e.getMessage)
              }
            }
          result
        }
      } // end match
    }) //end map
  }


  override def userProfile(token: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    // 根据id查询
    sessionService.queryByToken(token) flatMap  (f => {
      f match {
        case Some(session) =>
          val criteria = Json.obj("id" -> session.userId)
          import reactivemongo.play.json._
          val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
          cursor
        case None =>
          Future {
            None
          }
      }
    })
  }

  def user(username: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    // 根据用户名查询
    val r = Json.obj("$regex" -> username, "$options" -> "$mi")
    val criteria = Json.obj("username" -> r)
    import reactivemongo.play.json._
    val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
    cursor
  }

  def mobile(mobile: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    // 根据移动号码名查询
    val r = Json.obj("$regex" -> mobile, "$options" -> "$mi")
    val criteria = Json.obj("mobile" -> r)
    import reactivemongo.play.json._
    val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
    cursor
  }

  def findByRole(roles: Option[Set[String]], skip: Int, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[User]] = {
    // 根据角色查询用户
    val r = Json.obj("$elemMatch" -> Json.obj("$in" -> roles))
    val criteria = Json.obj("roles" -> r)
    import reactivemongo.play.json._
    val cursor = search(criteria = criteria, skip = skip, limit = limit)
    cursor
  }

  def editUser(userId: String, editUser: EditUser)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val criteria = Json.obj("id" -> userId)
    import reactivemongo.play.json._
    val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
    cursor.flatMap(c => {
      c match {
        case Some(u) => {
          val u1 = u.copy(
            displayName = editUser.displayName,
            email = editUser.email,
            mobile = editUser.mobile
          )
          logger.debug("user -> {}", u1)
          userCollection.flatMap(_.update(criteria, u1)) map {
            case le if le.ok => None
            case le => Some(le.message)
          }
        }
        case None => {
          val m = s"未发现用户【$userId】"
          logger.warn(m)
          Future {
            None
          }
        }
      }
    })
  }


  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[User]] = {
    userCollection.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[User] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  /**
    * 添加用户
    *
    * @param user
    * @return
    */
  def addUser(user: User): Future[Option[User]] = {
    userCollection.flatMap(_.insert(user) map {
      case le if le.ok => {
        Some(user)
      }
      case le => {
        throw new RuntimeException(le.message)
      }
    })
  }

  /**
    * 给用户分配角色
    *
    * @param userId
    * @param role
    */
  def allocationRole(userId: String, role: String): Future[Option[Set[String]]] = {
    val criteria = Json.obj("id" -> userId)
    import reactivemongo.play.json._
    val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
    cursor flatMap (user => {
      user match {
        case Some(user) =>
          val roles = user.roles match {
            case Some(roles) =>
              Some(roles.+(role))
            case None =>
              Some(Set(role))
          }
          val updateUser = user.copy(roles = roles)
          userCollection().flatMap(_.update(criteria, updateUser)) map {
            case le if le.ok =>
              roles
            case le =>
              None
          }
        case None =>
          Future {
            None
          }
      }
    })
  }

  /**
    * 取消用户某个角色
    * @param userId
    * @param role
    * @return
    */
  def removeRole(userId: String, role: String): Future[Option[Set[String]]] = {
    val criteria = Json.obj("id" -> userId)
    import reactivemongo.play.json._
    val cursor: Future[Option[User]] = userCollection.flatMap(_.find(criteria).one[User])
    cursor flatMap (user => {
      user match {
        case Some(user) =>
          val roles = user.roles match {
            case Some(roles) =>
              Some(roles.-(role))
            case None =>
              None
          }
          val updateUser = user.copy(roles = roles)
          userCollection().flatMap(_.update(criteria, updateUser)) map {
            case le if le.ok =>
              roles
            case le =>
              None
          }
        case None =>
          Future {
            None
          }
      }
    })
  }

}