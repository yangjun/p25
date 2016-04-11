package utils

import java.security.MessageDigest

import sun.misc.BASE64Encoder

/**
 * Created by 军 on 2016/4/11.
 */
object Utils {

   def sha1(v: String): String = {
     val md = MessageDigest.getInstance("SHA-1")
     val digest = md.digest(v.getBytes())
     val result = new BASE64Encoder().encode(digest)
     result
   }
}
