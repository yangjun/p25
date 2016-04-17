package controllers

import javax.inject._

import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
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
  def ws(token: String) = WebSocket.accept[String, String] {
   // WebSocket.acceptWithActor[String, String] {
//    request => out => MyWebSocketActor.props(out, token)
//      Source.actorRef[String](1000, OverflowStrategy.fail).
//      toMat(Sink.publisher)(Keep.both).run()
//      val source = Source.actorRef[String](Int.MaxValue, OverflowStrategy.fail)
//
//      Flow[String].to(Sink.).runWith(source)
    //TODO
    val source = Source.actorRef[String](Int.MaxValue, OverflowStrategy.fail)
    request => {
      Flow[String].map(v => {
        "【" + token + "】" + v
      })
    }
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
