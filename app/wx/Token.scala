package wx

/**
 * Created by 军 on 2016/4/13.
 */
object WxToken {

  private var tokens = Map[String, String]()

  def get(appId: String): Option[String] = {
    tokens.get(appId)
  }

  def +(appId: String, token: String): Unit = {
    tokens = tokens + (appId -> token)
  }

}
