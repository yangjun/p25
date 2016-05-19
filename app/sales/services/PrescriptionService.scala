package sales.services

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json.collection.JSONCollection
import sales.models._
import reactivemongo.play.json._
import scala.concurrent.{ExecutionContext, Future}

/**
  * 处方管理
  * Created by yangjungis@126.com on 2016/5/19.
  */
@Singleton
class PrescriptionService @Inject()(
                                     reactiveMongoApi: ReactiveMongoApi
                                   )
                                   (implicit ec: ExecutionContext) {
  val prescription = prescriptionCollection

  def create(createPrescription: CreatePrescription): Future[Option[String]] = {
    // 构造对象
    val newPrescription = Prescription(
      id = utils.Utils.nextId(),
      doctorId = createPrescription.doctorId.getOrElse(""),
      prescription = createPrescription.prescription,
      created = Some(DateTime.now()),
      hot = false,
      praiseNum = 0L,
      disagreeNum = 0L
    )
    logger.debug("new prescription -> {}", newPrescription)
    prescription.flatMap(_.insert(newPrescription)) map {
      case le if le.ok =>
        Some(newPrescription.id)
      case le =>
        None
    }
  }

  def edit(id: String, editPrescription: EditPrescription): Future[Option[String]] = {
    read(id) flatMap (
      p =>
        p match {
          case Some(p) =>
            val update = p.copy(
              prescription = editPrescription.prescription
            )
            val criteria = Json.obj("id" -> id)
            prescription.flatMap(_.update(criteria, update)) map {
              case le if le.ok =>
                Some(id)
              case le =>
                throw new RuntimeException("更新处方失败")
            }
          case None =>
            throw new RuntimeException("未发现处方")
        }
      )

  }

  def delete(id: String): Future[Option[String]] = {
    val criteria = Json.obj("id" -> id)
    prescription.flatMap(_.remove(criteria)) map {
      case le if le.ok =>
        Some(id)
      case le =>
        throw new RuntimeException("删除处方失败")
    }
  }

  def praise(id: String): Future[Option[Long]] = {
    read(id) flatMap (
      p =>
        p match {
          case Some(p) =>
            val update = p.copy(
              praiseNum = p.praiseNum + 1
            )
            val criteria = Json.obj("id" -> id)
            prescription.flatMap(_.update(criteria, update)) map {
              case le if le.ok =>
                Some(update.praiseNum)
              case le =>
                throw new RuntimeException("点赞处方失败")
            }
          case None =>
            throw new RuntimeException("未发现处方")
        }
      )
  }

  def disagree(id: String): Future[Option[Long]] = {
    read(id) flatMap (
      p =>
        p match {
          case Some(p) =>
            val update = p.copy(
              disagreeNum = p.disagreeNum + 1
            )
            val criteria = Json.obj("id" -> id)
            prescription.flatMap(_.update(criteria, update)) map {
              case le if le.ok =>
                Some(update.disagreeNum)
              case le =>
                throw new RuntimeException("吐槽处方失败")
            }
          case None =>
            throw new RuntimeException("未发现处方")
        }
      )
  }

  // 管理功能
  def swapHot(id: String): Future[Option[Boolean]] = {
    read(id) flatMap (
      p =>
        p match {
          case Some(p) =>
            val update = p.copy(
              hot = !p.hot
            )
            val criteria = Json.obj("id" -> id)
            prescription.flatMap(_.update(criteria, update)) map {
              case le if le.ok =>
                Some(update.hot)
              case le =>
                throw new RuntimeException("更新处方失败")
            }
          case None =>
            throw new RuntimeException("未发现处方")
        }
      )
  }

  def read(id: String): Future[Option[Prescription]] = {
    val criteria = Json.obj("id" -> id)
    logger.debug("criteria -> {}", criteria)
    prescription.flatMap(_.find(criteria).one[Prescription])
  }

  def query(tag: Option[String], skip: Int, limit: Int): Future[Traversable[Prescription]] = {
    var criteria = Json.obj()
    tag match {
      case Some(tag) =>
        criteria = criteria.+(
          "tags", Json.obj(
            "$regex" -> tag,
            "$options" -> "mi"
          )
        )
      case None =>
    }
    search(criteria, skip, limit)
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Prescription]] = {
    logger.debug("criteria -> {}", criteria)
    prescription.flatMap(_.find(criteria).
      sort(Json.obj("hot" -> 1, "praiseNum" -> -1)).
      options(QueryOpts(skipN = skip))
      cursor[Prescription] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  private def prescriptionCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("prescription"))
  }

  private lazy val logger = LoggerFactory.getLogger(classOf[PrescriptionService])
}
