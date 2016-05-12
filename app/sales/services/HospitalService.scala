package sales.services

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import sales.models._
import utils.Utils

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

  private def hospitalArchiveCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("hospitalArchives"))
  }

  // 医院
  val hospital = hospitalCollection
  // 开发履历
  val resumeHistory = hospitalResumeHistoryCollection
  // 归档
  val archive = hospitalArchiveCollection

  /**
    * 创建医院
    *
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
      Some(DateTime.now()),
      archive = None
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
    *
    * @param id
    * @return
    */
  def pk(id: String): Future[Option[Hospital]] = {
    val criteria = Json.obj("id" -> id)
    import reactivemongo.play.json._
    hospital.flatMap(_.find(criteria).one[Hospital])
  }

  /**
    * 编辑医院基本信息
    *
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
    *
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
    * 编辑开发履历
    *
    * @param editDevelopResume
    * @return
    */
  def resume(editDevelopResume: EditDevelopResume): Future[Either[Error, String]] = {
    val id = editDevelopResume.hospitalId
    val cursor: Future[Option[Hospital]] = pk(id)
    cursor.flatMap(f => {
      f match {
        case Some(o) => {
          val r = o.resume(editDevelopResume)
          r match {
            case Some(resume) => {
              val entity = o.copy(lastDevelopResume = Some(resume))
              val criteria = Json.obj("id" -> id)
              // 更新开发履历信息
              hospital.flatMap(_.update(criteria, entity)) map {
                case le if le.ok => {
                  Right(id)
                }
                case le =>
                  logger.error(le.message)
                  Left(Error(le.code.getOrElse(0), le.message))
              }
            } // end Some
            case None => {
              Future {
                Right(id)
              }
            }
          }
        } // end Some
        case None => {
          Future {
            Right(id)
          }
        } // end None
      }
    }) // end flatMap
  }

  /**
    * 开发成功，医院标记为合作伙伴
    *
    * @param becomePartner
    * @return
    */
  def becomePartner(becomePartner: BecomePartner): Future[Option[String]] = {
    val id = becomePartner.id
    // 查询医院
    val cursor: Future[Option[Hospital]] = pk(id)
    cursor.flatMap(f => {
      f match {
        case Some(h) => {
          h.status match {
            case Some(status) => {
              status match {
                // 开发中...
                case ActiveStatus.Developing => {
                  val hospitalArchive = HospitalArchive(
                    Utils.nextId(),
                    id,
                    becomePartner.principal,
                    becomePartner.principalDoctor,
                    "",
                    "",
                    Some(DateTime.now())
                  )
                  archive.flatMap(_.insert(hospitalArchive)) flatMap {
                    case le if le.ok => {
                      val criteria = Json.obj("id" -> id)
                      val h1 = h.copy(status = Some(ActiveStatus.Partner), archive = Some(hospitalArchive.id))
                      hospital.flatMap(_.update(criteria, h1)) map {
                        case le if le.ok => {
                          Some(hospitalArchive.id)
                        }
                        case le => {
                          throw new RuntimeException("更新医院状态失败")
                        }
                      }
                    }
                    case le => {
                      logger.error(le.message)
                      throw new RuntimeException("添加归档失败")
                    } // end le
                  }
                }
                case _ => {
                  throw new RuntimeException("开发中的医院才能归档")
                } // end 其他状态
              } // end status match
            } // end case Some(status)
            case None => {
              throw new RuntimeException("开发中的医院才能归档")
            }
          } // end match status
        } // end case Some(h)
        case None => {
          throw new RuntimeException("未发现医院")
        } // end None
      } // match f
    })
  }


  /**
    * 编辑归档信息
    *
    * @param editHospitalArchive
    * @return
    */
  def editHospitalArchive(editHospitalArchive: EditHospitalArchive): Future[Option[String]] = {
    val id = editHospitalArchive.id
    // 查询医院
    val cursor: Future[Option[Hospital]] = pk(id)
    cursor.flatMap(f => {
      f match {
        case Some(h) => {
          h.archive match {
            case Some(archiveId) => {
              val criteria = Json.obj("id" -> archiveId)
              archive.flatMap(_.find(criteria).one[HospitalArchive]).flatMap(a => {
                a match {
                  case Some(hospitalArchive) => {
                    val entity = hospitalArchive.copy(principal = editHospitalArchive.principal,
                      principalDoctor = editHospitalArchive.principalDoctor)
                    archive.flatMap(_.update(criteria, entity)) map {
                      case le if le.ok => {
                        Some(id)
                      }
                      case le => {
                        throw new RuntimeException("更新归档失败")
                      }
                    }
                  }
                  case None => {
                    throw new RuntimeException("未发现医院归档信息")
                  }
                }
              })
            }
            case None => {
              throw new RuntimeException("未发现归档")
            }
          }
        }
        case None => {
          throw new RuntimeException("未发现医院")
        }
      }
    })
  }

  /**
    * 分页查询
    *
    * @param query
    * @param skip
    * @param limit
    * @return
    */
  def search(query: NameQuery, skip: Int, limit: Int): Future[Traversable[Hospital]] = {
    query.name match {
      case Some(name) => {
        val r = Json.obj("$regex" -> name, "$options" -> "$mi")
        val criteria = Json.obj("base.name" -> r)
        logger.debug("criteria -> {}", criteria)
        search(criteria, skip, limit)
      }
      case None => {
        search(Json.obj(), skip, limit)
      }
    }
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Hospital]] = {
    hospital.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[Hospital] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

}
