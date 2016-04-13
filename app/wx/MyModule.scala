package wx

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import wx.actor.AccessTokenActor


/**
 * Created by 军 on 2016/4/13.
 */
class MyModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    //
    bindActor[AccessTokenActor]("accessTokenActor")
  }
}
