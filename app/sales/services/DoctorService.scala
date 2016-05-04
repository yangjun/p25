package sales.services

import javax.inject.{Inject, Singleton}

import authentication.{UserService, UserServiceImpl}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import sales.models.{AddDoctor, CreateHospital, Error, Hospital}
import utils.Utils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yangjungis@126.com on 2016/4/30.
  */
@Singleton
class DoctorService @Inject()(
                               userService: UserServiceImpl,
                               reactiveMongoApi: ReactiveMongoApi)
                             (implicit ec: ExecutionContext) {
  private lazy val logger = LoggerFactory.getLogger(classOf[DoctorService])

  private def doctorCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("doctors"))
  }

  private def hospitalCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("hospitals"))
  }

  private def userCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("users"))
  }

  // 医院
  val hospital = hospitalCollection
  // 医生
  val doctor = doctorCollection
  // 用户
  val user = userCollection


  /**
    * 维护医院医生信息
    * <pre>
    * 1. 检查医院标识是否存在
    * 2. 如果填写了电话号码，根据电话号码检查是否是已经存在的用户
    *
    * </pre>
    *
    * @param addDoctor
    * @param ec
    * @return
    */
  def create(addDoctor: AddDoctor)
            (implicit ec: ExecutionContext): Future[Option[String]] = {
    val id = Utils.nextId
    val hospitalId = addDoctor.hospital
    hospitalId match {
      case Some(hid) => {
        pk(hid).map(h => {
          h match {
            case Some(oneHospital) => {
              addDoctor.mobile match {
                  // 查询是否已经注册用户
                case Some(mobile) => {
                  userService.mobile(mobile) map (u => u match {
                      // 已注册用户
                    case Some(user) => {
                      user
                    }
                      // 未注册用户
                    case None => {
                      userService.addUser(addDoctor.user())
                    }
                  })
                }
                case None => {
                  userService.
                }
              }

            }
            case None => {
              throw new RuntimeException(s"未发现标识【$hospitalId】医院")
            }
          }
        }) //end flatMap
      }
      case None => {
        throw new RuntimeException("需要指定医院标识")
      }
    }
  }


  /**
    * 根据 id 查询医院
    *
    * @param id
    * @return
    */
  private def pk(id: String): Future[Option[Hospital]] = {
    val criteria = Json.obj("id" -> id)
    import reactivemongo.play.json._
    hospital.flatMap(_.find(criteria).one[Hospital])
  }
}
