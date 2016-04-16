package wx.actor

import javax.inject.Inject

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, JsSuccess}
import wx.{WxOAuth2Token, OAuth2Token, AccessToken, WXClient}
import wx.actor.OAuth2TokenActor.NextToken
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext


/**
 * Created by yangjungis@126.com on 2016/4/16.
 */


object OAuth2TokenActor {
  def props = Props[OAuth2TokenActor]

  // 取下一个Token消息
  case class NextToken(refresh: String, appid: String)
}

class OAuth2TokenActor @Inject()(wxClient: WXClient)() extends Actor {
  val logger = LoggerFactory.getLogger(classOf[OAuth2TokenActor])

  override def receive: Receive = {
    case nextToken: NextToken => {
      logger.debug("nextToken -> {}", nextToken)
      implicit val exec: ExecutionContext = context.dispatcher
      val response = wxClient.OAuth2.refresh(nextToken.refresh, nextToken.appid)
      response map {
        f => {
          f match {
            case s: JsSuccess[OAuth2Token] => {
              val token = s.get
              WxOAuth2Token + (token.openid, token)
              // 取下一个
              var next = token.expires - 120
              if (next < 0) {
                next = 0
              }
              logger.debug("{} 秒后取下一个 oauth2 token", next)
              val nextToken2 = (token.refresh, nextToken.appid)
              context.system.scheduler.scheduleOnce(next seconds, self, nextToken2)
            }
            case e: JsError => {
              logger.error("获取 oauth2 accessToken失败。 -> {}", f)
              logger.debug("{} 秒后取下一个token", 15)
              context.system.scheduler.scheduleOnce(15 seconds, self, nextToken)
            }
          }
        }
      }

      // 失败处理
      response onFailure {
        case e: Exception => {
          logger.error("获取 oauth2 accessToken失败。", e)
          logger.debug("{} 秒后取下一个token", 15)
          context.system.scheduler.scheduleOnce(15 seconds, self, nextToken)
        }
      }

    }
  }
}
