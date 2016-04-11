package controllers

import javax.inject._

import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import play.api.Play.current
import play.api.mvc._

import services.MyWebSocketActor
;

/**
 * Created by 军 on 2016/3/17.
 */

@Singleton
class WebSocketController @Inject()(implicit val mat: Materializer) extends Controller {

  /**
   * 使用Actor
   * @param token
   * @return
   */
  def ws(token: String) = WebSocket.acceptWithActor[String, String] {
    request => out => MyWebSocketActor.props(out, token)
  }

  /**
   * 使用流
   * @param token
   * @return
   */
  def ws1(token: String) = WebSocket.accept[String, String] {
    request => {
      Flow[String].map(v => {
        "【" + token + "】" + v
      })
    }

  }
}
