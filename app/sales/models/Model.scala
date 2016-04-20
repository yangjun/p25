package sales.models

import java.util.UUID

import play.api.libs.json.Json
import repository.Identity

/**
 * Created by yangjungis@126.com on 2016/4/17.
 */

// 名称查询
case class NameQuery(name: Option[String])

trait IdEntity {
  def id: Option[String]
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
  val idle = "IDLE"
}

// 医院
case class Hospital(id: Option[String],
                    name: String,
                    area: Option[String],
                    address: Option[String],
                    loc: Option[LatLng],
                    status: String = ActiveStatus.idle)
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

case class UserProfile(name: String, nickname: Option[String], sex: Int, province: String, city: String, country: String)
object UserProfile {
  implicit val format = Json.format[UserProfile]
}
// 用户
case class User(id: String, openId: String, unionId: Option[String], profile: UserProfile)
object User {
  implicit val format = Json.format[User]
}
// 医生
case class Doctor(id: String, userId: String, name: String, hospital: String)

// 事务所
case class County(id: String, name: String, area: String)

// 销售人员
case class Salesman(id: String, userId: String, county: String)

trait Job {
  def name: String
  def job: String
  def mobile: String
}

// 决定人
case class Principal(name: String , job: String, mobile: String) extends Job

// 用药人
case class PrincipalDoctor(name: String , job: String, mobile: String) extends Job


// 开票单位
case class Invoice(name: String, address: String)

// 对接人
case class DockingPersonnel(name: String, // 姓名
                            telephone: String // 电话
                             )

// 首次时间
case class FirstOrderOverview(mark: String,  // 时间
                              num: String  // 数量
                               )
// 医院档案
case class HospitalCase(id: String,
                        hospital: String, // 所属医院
                        principal: Principal, // 医院负责人
                        principalDoctor: PrincipalDoctor, // 主要用药人
                        county: County, // 开发事务所
                        contacts: Salesman // 销售人员
                         )


// 订单
case class Order()




