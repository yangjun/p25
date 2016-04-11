package wx

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsResult, Json}
import play.api.libs.ws.{WSResponse, WSClient, WSRequest}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._

/**
 *
 * Created by 军 on 2016/4/11.
 */

case class WxError(errcode: String, errmsg: String)

case class AccessToken(token: String, expires: Int)

case class GetAccessToken(appId: String, secret: String)

object WXClient {
  val token = "weixi"
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
      .withQueryString("appid" -> appid, "secret" -> secret)

    val response: Future[WSResponse] = result.get()

    response

//    response.map {
//      f => {
//        println("status -> " + f.status)
//        println(f.json)
//        implicit val reads = Json.reads[AccessToken]
//        val j: JsResult[AccessToken] = f.json.validate[AccessToken]
//        if (j.isError) {
//          implicit val reads = Json.reads[WxError]
//          f.json.validate(reads)
//        } else {
//          j
//        }
//      }
//    }

//    response.recover {
//      case e: Exception => {
//        val data = Json.obj("error" -> e.getMessage)
//        //        val data = Map("error" -> Seq(e.getMessage))
//        data
//      }
//    }

  }


}
