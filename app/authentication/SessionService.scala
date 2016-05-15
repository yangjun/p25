package authentication

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * 用户登录后回话管理
  * Created by yangjungis@126.com on 2016/5/12.
  */
@Singleton
class SessionService @Inject()(reactiveMongoApi: ReactiveMongoApi) {

  lazy val  session = sessionCollection
  private val logger = LoggerFactory.getLogger(classOf[SessionService])

  /**
    * 创建
    * @param userId
    * @param ec
    * @return
    */
  def create(userId: String)(implicit ec: ExecutionContext): Future[Option[Session]] = {
    val newSession = Session.create(userId)
    session.flatMap(_.insert(newSession)) map {
      case le if le.ok => {
        Some(newSession)
      }
      case le => {
        logger.error("创建回话失败.")
        None
      }
    }
  }

  /**
    * 根据token查询
    * @param token
    * @param ec
    * @return
    */
   def queryByToken(token: String)(implicit ec: ExecutionContext): Future[Option[Session]] = {
    import reactivemongo.play.json._
    val criteria = Json.obj("token" -> token)
    val cursor = session.flatMap(_.find(criteria).one[Session])
    cursor
  }


  def who(token: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    queryByToken(token) map (
      session =>
        session match {
          case Some(session) =>
            logger.debug("userId -> {}", session.userId)
            Some(session.userId)
          case None =>
            logger.debug("当前会话，无用户信息")
            None
        }
      )
  }
  /**
    * 判断Token是否过期
    * @param token
    * @param ec
    * @return
    */
  def isExpiration(token: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    queryByToken(token) map {
      s => {
        s match {
          case Some(session) => session.isExpiration()
          case None => true
        }
      }
    }
  }


  private  def sessionCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("session"))
  }

}
