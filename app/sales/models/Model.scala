package sales.models

import java.util.UUID

import authentication.{Role, User}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, Writes}
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
case class Doctor(id: String,
                  // 用户标识
                  userId: Option[String],
                  // 名称
                  name: String,
                  // 职位
                  job: Option[String],
                  // 医院标识
                  hospital: String)

object Doctor {
  implicit val format = Json.format[Doctor]
}

// 为医院添加医生
case class AddDoctor(
                      // 医生姓名
                      name: String,
                      // 电子邮件
                      email: Option[String],
                      // 手机号码
                      mobile: Option[String],
                      // 所在医院职位
                      job: Option[String],
                      // 所在医院
                      hospital: Option[String]
                    ) extends Message {


  /**
    * 构造用户信息
    *
    * @return
    */
  def user(): User = {
    User(
      Utils.nextId(),
      Some(name),
      email,
      mobile,
      Some(name),
      Some(name),
      // sex
      None,
      // province
      None,
      // city
      None,
      // country
      None,
      // avatar
      None,
      Some(DateTime.now()),
      roles = Some(Set(Role.doctor))
    )
  }
}

object AddDoctor {
  implicit val format = Json.format[AddDoctor]
}

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
case class Order(
                  // 标识
                  id: String,
                  // 订单编号（自动生成序号）
                  no: String,
                  // 申请人
                  proposer: String,
                  // 创建日期
                  created: Option[DateTime],
                  // 最后一次更新日期
                  updated: Option[DateTime],
                  // 状态
                  status: String = OrderStatus.idle,
                  // 医院
                  hospitalId: String,
                  // 备注
                  notes: Option[String],
                  // 订单项
                  items: Set[OrderItem],
                  // 出库单
                  stockOrderId: Option[String] = None
                ) {
  private lazy val logger = LoggerFactory.getLogger(classOf[Order])

  /**
    * 提交操作状态变化
    *
    * @return
    */
  def permit(): Order = {
    logger.debug("status -> {}", status)
    val nextStatus = status match {
      case s if s.equals(OrderStatus.idle) =>
        OrderStatus.firstReview
      case OrderStatus.firstReview =>
        OrderStatus.review
      case OrderStatus.review =>
        OrderStatus.stock
      case OrderStatus.stock =>
        OrderStatus.goodsReceipt
      case OrderStatus.goodsReceipt =>
        OrderStatus.achieve
      case OrderStatus.achieve =>
        OrderStatus.achieve
      case _ =>
        status
    }
    copy(status = nextStatus, updated = Some(DateTime.now()))
  }

  /**
    * 拒绝操作状态变化
    *
    * @return
    */
  def reject(): Order = {
    val nextStatus = status match {
      case OrderStatus.idle =>
        OrderStatus.idle
      case OrderStatus.firstReview =>
        OrderStatus.idle
      case OrderStatus.review =>
        OrderStatus.firstReview
      case OrderStatus.stock =>
        OrderStatus.review
      case OrderStatus.goodsReceipt =>
        OrderStatus.goodsReceipt
      case OrderStatus.achieve =>
        OrderStatus.achieve
    }
    copy(status = nextStatus, updated = Some(DateTime.now()))
  }

  /**
    * 取消操作状态变化
    *
    * @return
    */
  def cancel(): Order = {
    val nextStatus = status match {
      case OrderStatus.idle =>
        OrderStatus.cancel
      case OrderStatus.firstReview =>
        OrderStatus.cancel
      case OrderStatus.review =>
        OrderStatus.cancel
      case OrderStatus.stock =>
        OrderStatus.cancel
      case OrderStatus.goodsReceipt =>
        OrderStatus.goodsReceipt
      case OrderStatus.achieve =>
        OrderStatus.achieve
    }
    copy(status = nextStatus, updated = Some(DateTime.now()))
  }
}

object Order {
  implicit val format = Json.format[Order]
}

object OrderStatus {
  // 新建订单后的状态，申请阶段
  val idle = "idle"
  // 初审阶段
  val firstReview = "firstReview"
  // 审核阶段
  val review = "review"
  // 出库阶段
  val stock = "stock"
  // 收货阶段
  val goodsReceipt = "goodsReceipt"
  // 完成，正常归档
  val achieve = "achieve"
  // 取消，异常归档
  val cancel = "cancel"

}


// 订单项
case class OrderItem(
                      // 序号
                      no: Int,
                      // 药品名称
                      goodsName: String,
                      // 规格型号
                      specification: String,
                      // 单位
                      unit: String,
                      // 数量
                      quantity: Int,
                      // 备注
                      notes: Option[String]
                    ) {

}

object OrderItem {
  implicit val format = Json.format[OrderItem]
}

/**
  * 一个订单对应一个出库单
  *
  * @param id
  * @param orderId
  * @param notes
  */
case class StockOrder(
                       // 标识
                       id: String,
                       // 订单标识
                       orderId: String,
                       // 库管
                       storekeeper: String,
                       // 创建日期
                       created: Option[DateTime],
                       // 最后一次更新日期
                       updated: Option[DateTime],
                       // 备注
                       notes: Option[String]
                     )

object StockOrder {
  implicit val format = Json.format[StockOrder]
}

/**
  * 出库物品清单
  */
case class GoodsItem(
                      id: String,
                      // 出库单标识
                      stockOrderId: String,
                      // 电子监管码（一物一码）
                      code: String,
                      // 批号
                      batchNo: Option[String],
                      // 销售代码
                      salesCode: Option[String],
                      // 有效期
                      effective: Option[String]
                    )

object GoodsItem {
  implicit val format = Json.format[GoodsItem]
}

/**
  * 创建出库单
  */
case class CreateStockOrder(notes: Option[String],
                            items: Set[GoodsItem]
                           ) {
  /**
    * 构造出库单
    *
    * @param orderId     订单标识
    * @param storekeeper 库管
    * @return
    */
  def stockOrder(orderId: String, storekeeper: String): StockOrder = {
    val now = DateTime.now()
    StockOrder(id = Utils.nextId(),
      orderId = orderId,
      storekeeper = storekeeper,
      created = Some(now),
      updated = Some(now),
      notes = notes
    )
  }
}

object CreateStockOrder {
  implicit val format = Json.format[CreateStockOrder]
}

case class CreateGoodsItem(
                            // 电子监管码（一物一码）
                            code: String,
                            // 批号
                            batchNo: Option[String],
                            // 销售代码
                            salesCode: Option[String],
                            // 有效期
                            effective: Option[String]
                          ) {
  /**
    * 构建一个出库物品清单
    *
    * @param stockOrderId
    * @return
    */
  def goodsItem(stockOrderId: String): GoodsItem = {
    GoodsItem(id = Utils.nextId(),
      stockOrderId = stockOrderId,
      code = code,
      batchNo = batchNo,
      salesCode = salesCode,
      effective = effective
    )
  }
}

object CreateGoodsItem {
  implicit val format = Json.format[CreateGoodsItem]
}

/**
  * 为出库单添加商品清单
  *
  * @param items
  */
case class AddGoodsItem(items: Set[CreateGoodsItem]) {
  def goodsItems(stockOrderId: String): Set[GoodsItem] = {
    items map {
      item => {
        item.goodsItem(stockOrderId)
      }
    }
  }
}

object AddGoodsItem {
  implicit val format = Json.format[AddGoodsItem]
}

/**
  * 删除商品清单
  *
  * @param items
  */
case class RemoveGoodsItem(items: Set[String]) {
}

object RemoveGoodsItem {
  implicit val format = Json.format[RemoveGoodsItem]
}

case class OrderAudit(id: String,
                      orderId: String,
                      who: String,
                      created: Option[DateTime],
                      notes: Option[String]
                     )

object OrderAudit {
  implicit val format = Json.format[OrderAudit]
}

/**
  * 记录订单相关参与人（订单+参与人唯一）
  *
  * @param id
  * @param orderId
  * @param who
  */
case class OrderRef(id: String,
                    orderId: String,
                    who: String)

object OrderRef {
  implicit val format = Json.format[OrderRef]
}

case class NextOrderNo(
                        // 前缀
                        prefix: String,
                        // 序号
                        serial: Int = 1
                      )

object NextOrderNo {
  implicit val format = Json.format[NextOrderNo]
}

case class CreateOrder(
                        // 医院
                        hospitalId: Option[String],
                        // 订单项
                        items: Set[OrderItem],
                        // 申请人
                        proposer: Option[String],
                        // 备注
                        notes: Option[String]
                      ) {

}

object CreateOrder {
  implicit val format = Json.format[CreateOrder]
}

/**
  * 接受订单
  *
  * @param reason
  */
case class PermitOrder(
                        reason: String
                      ) {

}

object PermitOrder {
  implicit val format = Json.format[PermitOrder]
}

/**
  * 拒绝订单
  *
  * @param reason
  */
case class RejectOrder(
                        // 拒绝原因
                        reason: String
                      )

object RejectOrder {
  implicit val format = Json.format[RejectOrder]
}


/**
  * 取消订单
  *
  * @param reason
  */
case class CancelOrder(
                        reason: String
                      )

object CancelOrder {
  implicit val format = Json.format[CancelOrder]
}


//================工作流相关=========================

object TaskStatus {
  // 初始状态，上一个发起人可以取回
  val idle: String = "idle"
  // 接收任务开始处理
  val processing: String = "processing"
  // 已经完成，可能是
  val completed: String = "completed"
}

object Action {
  // 接受
  val permit: String = "permit"
  // 拒绝
  val reject: String = "Reject"

}

/**
  * 代办任务
  */
case class Task(
                 // 标识
                 id: String,
                 // 人员
                 who: String,
                 // 工单（订单ID）
                 no: String,
                 // 参见 TaskStatus
                 status: String = TaskStatus.idle,
                 // 动作
                 action: String,
                 // 创建时间
                 created: Option[DateTime],
                 // 开始时间
                 statred: Option[DateTime],
                 // 完成时间
                 completed: Option[DateTime]
               )


object Task {
  implicit val format = Json.format[Task]
}

case class Payload(
                    clazz: String,
                    value: String
                  ) {
  /**
    *
    * @tparam T
    * @return
    */
//  def deserialization[T]: T = {
//    import play.api.libs.json.Json
//    Json.parse(value).as[T]
//  }
}

object Payload {
  implicit val format = Json.format[Payload]

  /**
    * 序列化对象
    *
    * @param t
    * @tparam T
    * @return
    */
//  def serialize[T](t: T): Payload = {
//    import play.api.libs.json.Json
//    Payload(t.getClass.getName, Json.toJson(t).toString())
//  }
}

/**
  * 工单历史信息
  */
case class History(
                    // 标识
                    id: String,
                    // 工单（订单ID）
                    no: String,
                    // 任务ID
                    taskId: String,
                    // 工单内容（序列化）
                    payload: Payload,
                    created: Option[DateTime] = Some(DateTime.now())
                  ) {
//  def order: Order = payload.deserialization[Order]
}


object History {
  implicit val format = Json.format[History]
}


