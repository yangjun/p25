package utils

import java.security.MessageDigest
import java.util.UUID

import sun.misc.BASE64Encoder

/**
 * Created by 军 on 2016/4/11.
 */
object Utils {

   def sha1(v: String): String = {
     val md = MessageDigest.getInstance("SHA-1")
     val digest = md.digest(v.getBytes())
     val result = Hex.valueOf(digest).toLowerCase
     result
   }

  private object Hex {
    def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
  }

  /**
    * 下一个ID
    * @return
    */
   def nextId(): String = {
     UUID.randomUUID() toString
   }
}
