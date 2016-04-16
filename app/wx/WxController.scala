package wx

import javax.inject.{Named, Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.functional.syntax._

import utils.Utils
import wx.actor.OAuth2TokenActor

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import pdi.jwt._

/**
 * Created by 军 on 2016/4/11.
 */

@Singleton
class WxController @Inject()(actorSystem: ActorSystem,
                             config: Configuration,
                             wxClient: WXClient,
                             @Named("oauth2TokenActor") oauth2TokenActor: ActorRef)
                            (implicit exec: ExecutionContext) extends Controller {

  val logger = LoggerFactory.getLogger(classOf[WxController])

  val token = WXClient.token

  val appid = config.getConfig("wx").getString("appId")
  val secret = config.getConfig("wx").getString("appsecret")

  /**
   * WX验证服务器地址有效性
   * @param signature  微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
   * @param timestamp  时间戳
   * @param nonce   随机数
   * @param echostr 随机字符串
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
   * @return
   */
  def ip = Action.async {
    wxClient.callbackIp map {
      f => Ok(f)
    }
  }

  /**
   * 创建菜单
   * @return
   */
  def createMenu = Action.async {
    val data = Json.parse( """
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
    var session = JwtSession()
    response map {
      f => {
        f match {
          case s: JsSuccess[OAuth2Token] => {
            val token = s.get
            logger.debug("")
            WxOAuth2Token +(token.openid, token)
            oauth2TokenActor ! OAuth2TokenActor.NextToken(token.refresh, appid)
            // 查询用户信息
            val userInfo = wxClient.User.infoBySns(token.openid)
            userInfo map { u =>
              logger.debug("用户信息 -> {}", u)
              // 用户信息写入数据库
              // TODO
              val openId = u \ "openid"
              session = session +("user", openId.as[String])
              // 引导到业务系统首页
            }
          }
          case e: JsError => {
            session = session +("user", "")
            logger.error("回调出错了...")
          }
        }

        val result = Redirect("/")
        result
        // 响应头添加 JWT
        result.withJwtSession(session)
      }
    }
  }


  // jwt 测试
  def jwt = Action.async { implicit request =>
    Future {
      val session = JwtSession()
      session +("user", 1)
      Ok.withJwtSession(session)
    }
  }


}
