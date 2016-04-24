package sales.services

import javax.inject.{Inject, Singleton}

import authentication.User
import com.sun.org.apache.xpath.internal.functions.FuncTrue
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json.collection.JSONCollection
import sales.models._
import utils.Utils
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yangjungis@126.com on 2016/4/24.
  */
@Singleton
class HospitalService @Inject()(reactiveMongoApi: ReactiveMongoApi)
                               (implicit ec: ExecutionContext) {
  val logger = LoggerFactory.getLogger(classOf[HospitalService])

  private def hospitalCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("hospitals"))
  }
  private def hospitalResumeHistoryCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("hospitalResumeHistories"))
  }

  // 医院
  val hospital = hospitalCollection
  // 开发履历
  val resumeHistory = hospitalResumeHistoryCollection

  /**
    * 创建医院
    * @param createHospital
    * @param ec
    * @return
    */
  def create(createHospital: CreateHospital)
            (implicit ec: ExecutionContext): Future[Either[Error, String]] = {
    val id = Utils.nextId
    val entity = Hospital(
      Some(id),
      createHospital.hospital,
      // 默认为空闲状态
      Some(ActiveStatus.Idle),
      None,
      Some(DateTime.now())
    )
    val insert = hospital.flatMap(_.insert(entity))
    insert.recover {
      case e: DatabaseException =>
        val message = e.message
        val s = s"insert.exception 【$message】"
        logger.error(s)
        Error(e.code.getOrElse(0), e.message)
    }
    insert.map {
      case le if le.ok => Right(id)
      case le =>
        logger.error(le.message)
        Left(Error(le.code.getOrElse(0), le.message))
    }
  }

  /**
    * 根据 id 查询医院
    * @param id
    * @return
    */
  private def pk(id: String): Future[Option[Hospital]] = {
    val criteria = Json.obj("id" -> id)
    import reactivemongo.play.json._
    hospital.flatMap(_.find(criteria).one[Hospital])
  }

  /**
    * 编辑医院基本信息
    * @param id
    * @param editHospital
    * @param ec
    * @return
    */
  def edit(id: String, editHospital: EditHospital)
          (implicit ec: ExecutionContext): Future[Either[Error, String]] = {
    val cursor: Future[Option[Hospital]] = pk(id)
    cursor.flatMap(f =>
      f match {
        case Some(o) => {
          val criteria = Json.obj("id" -> id)
          val entity = o.copy(base = editHospital.hospital)
          hospital.flatMap(_.update(criteria, entity)) map {
            case le if le.ok => Right(id)
            case le =>
              logger.error(le.message)
              Left(Error(le.code.getOrElse(0), le.message))
          }
        } //end Some
        case None => {
          Future {
            Left(Error(0, "未发现更新的对象"))
          }
        } // end None
      }
    )
  }

  /**
    * 申请开发医院
    * @param developHospital
    * @return
    */
  def develop(developHospital: DevelopHospital): Future[Either[Error, String]] = {
    val id = developHospital.hospitalId
    val cursor: Future[Option[Hospital]] = pk(id)
    cursor.flatMap(f => {
      f match {
        case Some(o) => {
          val r = o.dev(developHospital)
          // 更新医院信息，状态变为开发中
          val entity = o.copy(lastDevelopResume = Some(r._1),
            status = Some(ActiveStatus.Developing))
          val criteria = Json.obj("id" -> id)
          hospital.flatMap(_.update(criteria, entity)) map {
            case le if le.ok =>
              r._2 match {
                case Some(history) => {
                  // 插入一条开发履历
                  resumeHistory.flatMap(_.insert(history))
                }
                case None => {
                }
              }
              Right(id)
            case le =>
              logger.error(le.message)
              Left(Error(le.code.getOrElse(0), le.message))
          }
        } // end Some
        case None => {
          Future {
            Left(Error(0, "未发现更新的对象"))
          }
        } // end None
      }
    })
  }

  /**
    * 分页查询
    * @param query
    * @param skip
    * @param limit
    * @return
    */
  def search(query: NameQuery, skip: Int, limit: Int): Future[Traversable[Hospital]] = {
    query.name match {
      case Some(name) => {
        search(Json.obj("name" -> name), skip, limit)
      }
      case None => {
        search(Json.obj(), skip, limit)
      }
    }
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Hospital]] = {
    hospital.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[Hospital](readPreference = ReadPreference.nearest)
      collect[List](limit))
  }
}
