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

// OAuth2 Token
object WxOAuth2Token {
  private var tokens = Map[String, OAuth2Token]()
  def get(openid: String): OAuth2Token = {
    tokens.get(openid) match {
      case Some(s) => {
        s
      }
      case None => OAuth2Token("", 0, "", "", "")
    }
  }

  def +(openid: String, token: OAuth2Token): Unit = {
    tokens = tokens + (openid -> token)
  }

  def in(openid: String): Boolean = {
    tokens.get(openid) match {
      case Some(s) => {
        true
      }
      case None => {
        false
      }
    }
  }
}