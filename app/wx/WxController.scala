package wx

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import authentication.{SessionService, UserServiceImpl}
import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.functional.syntax._
import utils.Utils
import wx.actor.OAuth2TokenActor

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.xml.NodeSeq
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import pdi.jwt.{JwtSession, _}

import scala.concurrent.duration._

/**
  * Created by 军 on 2016/4/11.
  */

@Singleton
class WxController @Inject()(actorSystem: ActorSystem,
                             config: Configuration,
                             wxClient: WXClient,
                             @Named("oauth2TokenActor") oauth2TokenActor: ActorRef,
                             sessionService: SessionService,
                             userService: UserServiceImpl)
                            (implicit exec: ExecutionContext) extends Controller {

  val logger = LoggerFactory.getLogger(classOf[WxController])

  val token = WXClient.token

  val appid = config.getConfig("wx").getString("appId")
  val secret = config.getConfig("wx").getString("appsecret")

  /**
    * WX验证服务器地址有效性
    *
    * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
    * @param timestamp 时间戳
    * @param nonce     随机数
    * @param echostr   随机字符串
    * @return
    */
  def valid(signature: String, timestamp: String, nonce: String, echostr: String) = Action.async {
    Future {
      val b = WXClient.checkSignature(timestamp, nonce, signature)
      if (b) {
        Ok(echostr)
      } else {
        Ok(Json.obj("result" -> b))
      }
    }

  }

  /**
    * 接收WX消息和事件
    *
    * @param signature
    * @param timestamp
    * @param nonce
    * @return
    */
  def receiveEvent(signature: String, timestamp: String, nonce: String, echostr: String) = Action.async { request =>
    Future {
      val b = WXClient.checkSignature(timestamp, nonce, signature)
      if (b) {
        val body: AnyContent = request.body
        val jsonBody: Option[NodeSeq] = body.asXml
        logger.debug("xml -> {}", jsonBody)
        Ok("")
      } else {
        logger.error("非法请求")
        Ok("")
      }
    }
  }

  /**
    * 测试微信接口
    *
    * @return
    */
  def ip = Action.async {
    wxClient.callbackIp map {
      f => Ok(f)
    }
  }

  /**
    * 创建菜单
    *
    * @return
    */
  def createMenu = Action.async {
    val data = Json.parse(
      """
  {
    "button": [
      {
        "type": "view",
        "name": "主页",
        "url": "http://luotaoyeah.iok.la"
      },
      {
        "type": "click",
        "name": "点我",
        "key":"EVENT_TEST"
      }
    ]
  } """.stripMargin)

    logger.debug("data -> {}", data)
    wxClient.Menu.create(data) map { f =>
      Ok(f)
    }
  }


  def deleteMenu = Action.async {
    wxClient.Menu.delete map { f =>
      Ok(f)
    }
  }

  def getMenu = Action.async {
    wxClient.Menu.get map { f =>
      Ok(f)
    }
  }

  def customMessage = Action.async {
    wxClient.Message.custom("oUO_Rs8PtsJaHJX_XASB9o4LGTbs", "来自开发者的测试消息") map {
      f => {
        Ok(f)
      }
    }
  }

  // WX OAuth2 回调地址
  // 引导页面回调
  def callback(code: Option[String], state: String) = Action.async { implicit request =>
    val code1 = code.getOrElse("")
    logger.debug("code -> {}", code1)
    // 网页引导时填写的值
    logger.debug("state -> {}", state)
    // 使用 code 换取 oauth2 access-token
    val response = wxClient.OAuth2.token(code1, appid, secret)
    response flatMap {
      f =>
      f match {
          case s: JsSuccess[OAuth2Token] => {
            val token: OAuth2Token  = s.get
//            if (!WxOAuth2Token.in(token.openid)) {
              WxOAuth2Token +(token.openid, token)
              oauth2TokenActor ! OAuth2TokenActor.NextToken(token.refresh, appid)
//            }
            // 查询用户信息
            val userInfo = wxClient.User.infoBySns(token.openid)
            userInfo flatMap  { u =>
              logger.debug("用户信息 -> {}", u)
              val wxUserResult = u.validate[RegisterWxUser]
              wxUserResult match {
                case JsSuccess(wxUser, _) => {
                  logger.debug("wx用户信息 -> {}", wxUser)
                  val result = Await.result(userService.registerWxUser(wxUser), 10 seconds)
                  result match {
                    case Right(userId) => {
                      var jwtSession = JwtSession()
                      logger.debug("userId -> {}", userId)
                      sessionService.create(userId = userId) map  {session => {
                        session match {
                          case Some(session) =>
                            jwtSession = jwtSession + ("token", session.token)
                            jwtSession = jwtSession + ("refreshToken", session.refreshToken)
                            jwtSession = jwtSession + ("expiration", session.ttl)
                            // 响应头添加 JWT
                            logger.debug("jwt -> {}", jwtSession)
                            val query = Map("token" -> Seq(jwtSession.serialize))
                            val result = Redirect("/", query , 302)
                            result.withJwtSession(jwtSession)
                            result
                          case None =>
                            emptySession
                        }
                      }}
                    }
                    case Left(x) =>
                      Future {
                        emptySession
                      }
                  }
                } //  end JsSuccess
                case JsError(err) => {
                  Future {
                    emptySession
                  }
                } // end JsError
              }
            } // end userInfo map
          } //end JsSuccess
          case e: JsError => {
            Future {
              emptySession
            }
          } // end error
        } //end match
    } //end map
  }

  private def emptySession: Result = {
      var session = JwtSession()
      session = session +("user", "")
      logger.error("回调出错了...")
      val result = Redirect("/")
      // 响应头添加 JWT
      logger.debug("jwt -> {}", session)
      result.withJwtSession(session)
      result

  }

  // jwt 测试
  def jwt = Action.async { implicit request =>
    Future {
      var session = JwtSession()
      session = session + ("token", "yangjun")
      session = session + ("refreshToken", "11")
      session = session + ("expiration", 7200)
      Ok.withJwtSession(session)
    }
  }


}
