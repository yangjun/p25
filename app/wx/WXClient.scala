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
    logger.debug("{}" ,list)
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
      logger.debug("验证失败...")
    }
    b
  }


}

@Singleton
class WXClient @Inject()(ws: WSClient, config: Configuration)(implicit exec: ExecutionContext) {

  val logger = LoggerFactory.getLogger(classOf[WXClient])

  def appId = config.getString("wx.appId")

  def baseUrl(): String = {
    """https://api.weixin.qq.com/cgi-bin/"""
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
      .withQueryString("access_token" -> WxToken.get(appId))

    val response: Future[WSResponse] = result.get()
    response map {
      f => {
        logger.debug("{}", f.json)
        f.json
      }
    }
  }

}
