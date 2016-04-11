package services

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorRef}
import org.slf4j.LoggerFactory

/**
 * Created by 军 on 2016/3/17.
 */
object MyWebSocketActor {
  def props(out: ActorRef, token: String) = Props(new MyWebSocketActor(out, token))
}

class MyWebSocketActor(out: ActorRef, token: String) extends Actor {
  val logger = LoggerFactory.getLogger(MyWebSocketActor.getClass)

  override def receive: Receive = {
    case msg: String  => {
      out ! ("接收到【" + token + "】发来的消息： " + msg)
    }
  }

  override def postStop(): Unit = {
      logger.warn("用户【" + token + "】 退出...")
  }
}
