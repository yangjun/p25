package wx

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

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
  val token = "weixi"
  val logger = LoggerFactory.getLogger(classOf[WXClient])
}

@Singleton
class WXClient @Inject()(ws: WSClient)(implicit exec: ExecutionContext) {


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


}
