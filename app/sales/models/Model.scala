package sales.models

import java.util.UUID

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import repository.Identity
import utils.Utils

/**
  * Created by yangjungis@126.com on 2016/4/17.
  */

// 名称查询
case class NameQuery(name: Option[String])

trait IdEntity {
  def id: Option[String]
}

// 消息接口
trait Message {
}

// 错误信息
case class Error(val code: Int, val message: String) {
}

object Error {
  implicit val format = Json.format[Error]
}

// 区域
case class Area(id: Option[String], code: String, name: String) extends IdEntity

object Area {
  implicit val format = Json.format[Area]

  implicit object AreaIdentity extends Identity[Area, String] {
    val name = "uuid"

    def of(entity: Area): Option[String] = entity.id

    def set(entity: Area, id: String): Area = entity.copy(id = Option(id))

    def clear(entity: Area): Area = entity.copy(id = None)

    def next: String = UUID.randomUUID() toString
  }

}

// 经纬度
case class LatLng(latitude: Double, longitude: Double)

object LatLng {
  implicit val format = Json.format[LatLng]
}

object ActiveStatus {
  // 等待开发
  val Idle = "idle"
  // 开发中
  val Developing = "developing"
  // 合作伙伴
  val Partner = "partner"
}

case class HospitalBaseInfo(
                             // 医院名称
                             name: String,
                             // 所在地区
                             area: Option[String],
                             // 地址
                             address: Option[String],
                             // 坐标
                             loc: Option[LatLng]
                           )

object HospitalBaseInfo {
  implicit val format = Json.format[HospitalBaseInfo]
}

// 开发过程中履历信息
case class DevelopResume(
                          // 销售人员
                          salesman: Option[String],
                          // 时间
                          created: Option[DateTime],
                          // 开发天数
                          day: Option[Int],
                          // 申请开发次数
                          serial: Option[Int],
                          // 医院对接情况描述
                          hospitalDescription: Option[String],
                          // 开发进度描述
                          scheduleDescription: Option[String]
                        )

object DevelopResume {
  implicit val format = Json.format[DevelopResume]
}

// 医院
case class Hospital(
                     // 标识
                     id: Option[String],
                     // 基本信息
                     base: HospitalBaseInfo,
                     // 状态
                     status: Option[String] = Some(ActiveStatus.Idle),
                     // 最后一次开发过程
                     lastDevelopResume: Option[DevelopResume],
                     // 录入时间
                     created: Option[DateTime],
                     // 归档信息
                     archive: Option[String]) {
  private lazy val logger = LoggerFactory.getLogger(classOf[Hospital])


  def dev(developHospital: DevelopHospital): (DevelopResume, Option[DevelopResumeHistory]) = {
    logger.debug("status -> {}", status)
    status match {
      case Some(s) => {
        // 空闲状态允许开发
        val developResume = developHospital.resume
        if (s.equals(ActiveStatus.Idle)) {
          lastDevelopResume match {
            // 已经开发过
            case Some(resume) => {
              // 最近
              val last = resume.copy(serial = Some(resume.serial.getOrElse(0) + 1))
              // 历史
              val his = DevelopResumeHistory(
                Some(Utils.nextId()),
                id,
                Some(resume)
              )
              (last, Some(his))
            } //end Some
            // 首次开发
            case None => {
              (developResume, None)
            } //end None
          } // end match
        } // endif
        else {
          throw new RuntimeException("不满足开发条件。")
        } // end else
      } // end Some
      case None => {
        throw new RuntimeException("不满足开发条件。")
      } // end None
    }
  }

  // 构造开发履历
  def resume(editDevelopResume: EditDevelopResume): Option[DevelopResume] = {
    lastDevelopResume match {
      case Some(resume) => {
        Some(resume.copy(hospitalDescription = editDevelopResume.hospitalDescription,
          scheduleDescription = editDevelopResume.scheduleDescription))
      }
      case None => {
        None
      }
    }
  }
}

object Hospital {
  implicit val format = Json.format[Hospital]

  implicit object hospitalIdentity extends Identity[Hospital, String] {
    val name = "uuid"

    def of(entity: Hospital): Option[String] = entity.id

    def set(entity: Hospital, id: String): Hospital = entity.copy(id = Option(id))

    def clear(entity: Hospital): Hospital = entity.copy(id = None)

    def next: String = UUID.randomUUID() toString
  }

}


// 医院开发过程
case class DevelopResumeHistory(id: Option[String],
                                hospital: Option[String],
                                resume: Option[DevelopResume])

object DevelopResumeHistory {
  implicit val format = Json.format[DevelopResumeHistory]
}

// 新建医院
case class CreateHospital(hospital: HospitalBaseInfo) extends Message

object CreateHospital {
  implicit val format = Json.format[CreateHospital]
}

// 编辑医院
case class EditHospital(hospital: HospitalBaseInfo) extends Message

object EditHospital {
  implicit val format = Json.format[EditHospital]
}

// 申请开发医院
case class DevelopHospital(
                            // 医院标识
                            hospitalId: String,
                            // 开发天数
                            day: Int,
                            // 销售人员
                            salesman: Option[String]) extends Message {
  /**
    * 构建开发履历
    *
    * @return
    */
  def resume: DevelopResume = {
    DevelopResume(salesman, Some(DateTime.now()), Some(day), Some(1), None, None)
  }
}

object DevelopHospital {
  implicit val format = Json.format[DevelopHospital]
}

// 新建医院并进行开发
case class CreateAndDevelopHospital(
                                     // 医院信息
                                     hospital: Hospital,
                                     // 开发天数
                                     day: Int,
                                     // 销售人员
                                     salesman: Option[String]) extends Message

object CreateAndDevelopHospital {
  implicit val format = Json.format[CreateAndDevelopHospital]
}

// 记录开发过程
case class EditDevelopResume(
                              // 医院标识
                              hospitalId: String,
                              // 医院对接情况描述
                              hospitalDescription: Option[String],
                              // 开发进度描述
                              scheduleDescription: Option[String]) extends Message

object EditDevelopResume {
  implicit val format = Json.format[EditDevelopResume]
}


// 医生
case class Doctor(id: String, userId: String, name: String, hospital: String)

// 事务所
case class County(id: String, name: String, area: String)
object County {
  implicit val format = Json.format[County]
}

// 销售人员
case class Salesman(id: String, userId: String, county: String)
object Salesman {
  implicit val format = Json.format[Salesman]
}

trait Job {
  def name: String
  def job: String
  def mobile: String
}

// 决定人
case class Principal(name: String, job: String, mobile: String) extends Job
object Principal {
  implicit val format = Json.format[Principal]
}

// 用药人
case class PrincipalDoctor(name: String, job: String, mobile: String) extends Job
object PrincipalDoctor {
  implicit val format = Json.format[PrincipalDoctor]
}


// 开票单位
case class Invoice(name: String, address: String)

// 对接人
case class DockingPersonnel(name: String, // 姓名
                            telephone: String // 电话
                           )

// 首次时间
case class FirstOrderOverview(mark: String, // 时间
                              num: String // 数量
                             )

// 医院档案
case class HospitalArchive(
                            // 标识
                            id: String,
                            // 医院标识
                            hospital: String,
                            principal: Principal, // 医院负责人
                            principalDoctor: PrincipalDoctor, // 主要用药人
                            county: String, // 开发事务所
                            contacts: String, // 销售人员
                            created: Option[DateTime] // 归档时间
                          ) {

}

object HospitalArchive {
  implicit val format = Json.format[HospitalArchive]
}
// 开发成功，成为合作伙伴
case class BecomePartner(
                          // 医院标识
                          id: String,
                          // 医院负责人
                          principal: Principal,
                          // 主要用药人
                          principalDoctor: PrincipalDoctor
                        ) {

}

object BecomePartner {
  implicit val format = Json.format[BecomePartner]
}
// 编辑归档信息
case class EditHospitalArchive(
                                // 医院标识
                                id: String,
                                // 医院负责人
                                principal: Principal,
                                // 主要用药人
                                principalDoctor: PrincipalDoctor
                              ) {

}

object EditHospitalArchive {
  implicit val format = Json.format[EditHospitalArchive]
}

// 订单
case class Order()




