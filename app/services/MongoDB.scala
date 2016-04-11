package services

import javax.inject.Singleton

import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}


import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by 军 on 2016/3/29.
 */

@Singleton
class MongoDB {
  val mongoUri = "mongodb://127.0.0.1:27010/ngiam-db"
  val driver = new MongoDriver
//  val connection = driver.connection(MongoConnection.parseURI(mongoUri).get)
  val connection = driver.connection(List("127.0.0.1"))
  val db = connection("ngiam-db")

  val user = db.collection("userEntity")
//  val db = DefaultDB("ngiam-db", connection)
}
