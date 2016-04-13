package wx.actor

import javax.inject.Inject

import akka.actor.{Actor, Props}
import org.slf4j.LoggerFactory
import play.api.libs.functional.syntax._
import play.api.libs.json._
import wx.actor.AccessTokenActor.NextToken
import wx.{AccessToken, WXClient, WxToken}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 *
 * Created by 军 on 2016/4/13.
 */

object AccessTokenActor {
  def props = Props[AccessTokenActor]

  /**
   * 取下一个token
   * @param appId
   * @param secret
   */
  case class NextToken(appId: String, secret: String)

}

class AccessTokenActor @Inject()(wxClient: WXClient)() extends Actor {
  val logger = LoggerFactory.getLogger(classOf[AccessTokenActor])

  override def receive: Receive = {
    case nextToken: NextToken => {
      logger.debug("nextToken -> {}", nextToken)
      val response = wxClient.accessToken(nextToken.appId, nextToken.secret)
      implicit val exec: ExecutionContext = context.dispatcher
      response.map {
          f =>
          implicit val reads = (
            (__ \ "access_token").read[String] and
              (__ \ "expires_in").read[Int]
            )(AccessToken)

          val j: JsResult[AccessToken] = f.json.validate[AccessToken]
          j match {
            case s: JsSuccess[AccessToken] => {
              val token = j.get
              logger.debug("accessToken -> {}", token)
              WxToken +(nextToken.appId, token.token)
              // 取下一个
              var next = token.expires - 120
              if (next < 0) {
                next = 0
              }
              logger.debug("{} 秒后取下一个token", next)
              context.system.scheduler.scheduleOnce(next seconds, self, nextToken)
            }
            case e: JsError => {
              logger.error("获取accessToken失败。 -> {}", f.json)
              logger.debug("{} 秒后取下一个token", 15)
              context.system.scheduler.scheduleOnce(15 seconds, self, nextToken)
            }
          }
      }

      // 失败处理
      response onFailure {
        case e: Exception => {
          logger.error("获取accessToken失败。", e)
          logger.debug("{} 秒后取下一个token", 15)
          context.system.scheduler.scheduleOnce(15 seconds, self, nextToken)
        }
      }
    }

  }
}
