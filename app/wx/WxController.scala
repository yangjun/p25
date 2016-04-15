package wx

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{AnyContent, Action, Controller}
import utils.Utils

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

/**
 * Created by 军 on 2016/4/11.
 */

@Singleton
class WxController @Inject()(actorSystem: ActorSystem, wxClient: WXClient)(implicit exec: ExecutionContext) extends Controller {

  val logger = LoggerFactory.getLogger(classOf[WxController])

  val token = WXClient.token

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
        "url": "http://192.168.1.100"
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
}
