package wx

import scala.util.Success

/**
 * Created by 军 on 2016/4/13.
 */
object WxToken {

  private var tokens = Map[String, String]()

  def get(appId: String): String = {
    tokens.get(appId) match {
      case Some(s) => {
        s
      }
      case None => ""
    }
  }

  def +(appId: String, token: String): Unit = {
    tokens = tokens + (appId -> token)
  }

}
