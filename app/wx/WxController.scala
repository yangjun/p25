package wx

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{Action, Controller}
import utils.Utils

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by 军 on 2016/4/11.
 */

@Singleton
class WxController @Inject()(actorSystem: ActorSystem, wxClient: WXClient)(implicit exec: ExecutionContext) extends Controller {

  val logger = LoggerFactory.getLogger(this.getClass)

  val token = WXClient.token

  /**
   *
   * @param signature  微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
   * @param timestamp  时间戳
   * @param nonce   随机数
   * @param echostr 随机字符串
   * @return
   */
  def valid(signature: String, timestamp: String, nonce: String, echostr: String) = Action.async {
    Future {
      val list = List(token, timestamp, nonce)
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
      // 验证
      val b = v1.equals(signature)

      if (b) {
        logger.debug("验证成功...")
        Ok(echostr)
      } else {
        logger.debug("验证失败...")
        Ok(Json.obj("result" -> b))
      }
    }

  }

  def accessToken(appId: String, secret: String) = Action.async {
    wxClient.accessToken(appId, secret).map {
      f =>
        implicit val reads = Json.reads[AccessToken]
        val j: JsResult[AccessToken] = f.json.validate[AccessToken]
        if (j.isError) {
           logger.debug("error -> {}", f.json)
          Ok(f.json)
        } else {
          implicit val writes = Json.writes[AccessToken]
          Ok(Json.toJson(j.get))
        }
    }
  }


}
