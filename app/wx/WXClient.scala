package wx

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import utils.Utils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

/**
 *
 * Created by 军 on 2016/4/11.
 */


case class WxError(errcode: String, errmsg: String)

case class AccessToken(token: String, expires: Int) {
  val accessTokenReads = (
    (__ \ "token").read[String] and
      (__ \ "expires").read[Int]
    )(AccessToken)

  val accessTokenWrites = (
    (JsPath \ "token").write[String] and
      (JsPath \ "expires").write[Int]
    )(unlift(AccessToken.unapply))

  implicit val accessTokenFormat = Format(accessTokenReads, accessTokenWrites)
}

case class GetAccessToken(appId: String, secret: String)

case class OAuth2Token(token: String, expires: Int, refresh: String, openid: String, scope: String)


object WXClient {
  val token = "wx"
  val logger = LoggerFactory.getLogger(classOf[WXClient])

  /**
   * 微信签名验证
   * @param timestamp
   * @param nonce
   * @return
   */
  def checkSignature(timestamp: String, nonce: String, signature: String): Boolean = {
    val list = List(token, timestamp, nonce)
    logger.debug("{}", list)
    // 字典排序
    val sort = list.sortWith((s, t) => s.compareTo(t) < 0)
    // 拼接为一个字符串
    var v = ""
    sort.foreach(f => {
      v = v.concat(f)
    })
    logger.debug("v -> {}", v)
    // sha1 加密
    val v1 = Utils.sha1(v)
    logger.debug("signature -> {}", v1)
    val b = v1.equals(signature)
    if (b) {
      logger.debug("验证成功...")
    } else {
      logger.error("验证失败...")
    }
    b
  }


}

@Singleton
class WXClient @Inject()(ws: WSClient, config: Configuration)(implicit exec: ExecutionContext) {

  val logger = LoggerFactory.getLogger(classOf[WXClient])

  def appId = config.getString("wx.appId")

  // 下一个token
  def token = {
    val s = WxToken.get(appId)
    logger.debug("access-token -> {}", s)
    s
  }

  def oauth2Token(openid: String) = {
    val s = WxOAuth2Token.get(openid)
    logger.debug("oauth2-access-token -> {}", s)
    s
  }

  def baseUrl(): String = {
    """https://api.weixin.qq.com/cgi-bin/"""
  }

  // SNS
  def snsUrl() = {
    """https://api.weixin.qq.com/sns/"""
  }

  def accessToken(appid: String, secret: String) = {
    val url = baseUrl().concat("token")
    val request: WSRequest = ws.url(url)
    val result = request.withHeaders("Accept" -> "application/json")
      .withRequestTimeout(5.seconds)
      .withQueryString("grant_type" -> "client_credential", "appid" -> appid, "secret" -> secret)

    val response: Future[WSResponse] = result.get()
    response

  }

  def callbackIp() = {
    val url = baseUrl().concat("getcallbackip")
    val request: WSRequest = ws.url(url)
    val result = request.withHeaders("Accept" -> "application/json")
      .withRequestTimeout(5.seconds)
      .withQueryString("access_token" -> token)

    val response: Future[WSResponse] = result.get()
    response map {
      f => {
        logger.debug("{}", f.json)
        f.json
      }
    }
  }

  object Menu {
    // 菜单 URL
    private val menuUrl = baseUrl().concat("menu/")

    // 创建菜单
    def create(data: JsValue) = {
      val url = menuUrl.concat("create")
      val request = ws.url(url).withHeaders("Content-type" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString("access_token" -> token)
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.post(data)
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }

    // 创建菜单
    def delete = {
      val url = menuUrl.concat("delete")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString("access_token" -> token)
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.get()
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }

    // 创建菜单
    def get = {
      val url = menuUrl.concat("get")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString("access_token" -> token)
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.get()
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }

  }

  // 消息
  object Message {
    // 菜单 URL
    private val messageUrl = baseUrl().concat("message/")

    // 客服消息
    def custom(toUser: String, content: String) = {
      val url = messageUrl.concat("custom/send")
      val request = ws.url(url).withHeaders("Content-type" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString("access_token" -> token)
      logger.debug("url -> {}", url)
      val data = Json.obj(
        "touser" -> toUser,
        "msgtype" -> "text",
        "text" -> Json.obj(
          "content" -> content
        )
      )

      val response: Future[WSResponse] = request.post(data)
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }
  }

  // 用户
  object User {
    // 用户 URL
    private val userUrl = baseUrl().concat("user/")

    // 获取用户信息
    def info(openId: String) = {
      val url = userUrl.concat("info")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString(
          "access_token" -> token,
          "openid" -> openId,
          "lang" -> "zh_CN"
        )
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.get()
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }

    def infoBySns(openId: String)  = {
      val url = snsUrl().concat("userinfo")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString(
          "access_token" -> oauth2Token(openId).token,
          "openid" -> openId,
          "lang" -> "zh_CN"
        )
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.get()
      response map {
        f => {
          logger.debug("{}", f.json)
          f.json
        }
      }
    }
  }

  // OAuth2
  object OAuth2 {
    // 用户 URL
    private val oauth2Url = snsUrl().concat("oauth2/")

    // 回调时根据code获取token
    def token(code: String, appid: String, secret: String) = {
      val url = oauth2Url.concat("access_token")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString(
          "appid" -> appid,
          "secret" -> secret,
          "code" -> code,
          "grant_type" -> "authorization_code"
        )
      logger.debug("url -> {}", url)
      val response: Future[WSResponse] = request.get()
      response onFailure {
        case ex: Exception => {
          logger.error("获取oauth2 access-token 出错", ex)
        }
      }
      response map {
        f => {
          implicit val reads = (
            (__ \ "access_token").read[String] and
              (__ \ "expires_in").read[Int] and
              (__ \ "refresh_token").read[String] and
              (__ \ "openid").read[String] and
              (__ \ "scope").read[String]
            )(OAuth2Token)

          f.json.validate(reads)
        }
      }
    }

    def refresh(refresh: String, appid: String) = {
      val url = oauth2Url.concat("refresh_token")
      val request = ws.url(url).withHeaders("Accept" -> "application/json")
        .withRequestTimeout(5.seconds)
        .withQueryString(
          "appid" -> appid,
          "grant_type" -> "refresh_token",
          "refresh_token" -> refresh
        )
      logger.debug("url -> {}", url)

      val response: Future[WSResponse] = request.get()
      response map {
        f => {
          implicit val reads = (
            (__ \ "access_token").read[String] and
              (__ \ "expires_in").read[Int] and
              (__ \ "refresh_token").read[String] and
              (__ \ "openid").read[String] and
              (__ \ "scope").read[String]
            )(OAuth2Token)
          f.json.validate(reads)
        }
      }
    }
  }

}
