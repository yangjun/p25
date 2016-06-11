package utils

import java.security.MessageDigest
import java.util.UUID

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, DateTimeFormatterBuilder}
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


  /**
    * 格式化当前日期
    * @param pattern
    * @return
    */
   def fmt(pattern: String): String = {
     val now = DateTime.now()
     val fmt = DateTimeFormat.forPattern(pattern)
     fmt.print(now)
   }

  /**
    * 转换当前日期到 yyyy.MM
    * <pre>
    *   2106.05
    * </pre>
    * @return
    */
  def orderFmt(): String = {
    fmt("yyyy.MM")
  }

}
