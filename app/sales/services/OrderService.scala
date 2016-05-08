package sales.services

import javax.inject.{Inject, Singleton}

import authentication.UserServiceImpl
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import sales.models.{OrderStatus, _}
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json.collection.JSONCollection
import sales.models._
import utils.Utils
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * 订单服务
  * Created by yangjungis@126.com on 2016/5/7.
  */
@Singleton
class OrderService @Inject()(
                              userService: UserServiceImpl,
                              reactiveMongoApi: ReactiveMongoApi)
                            (implicit ec: ExecutionContext) {
  private lazy val logger = LoggerFactory.getLogger(classOf[OrderService])

  private def orderCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("order"))
  }

  private def orderNoCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("nextOrderNo"))
  }

  private def orderAuditCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("orderAudit"))
  }

  private def orderRefCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("orderRef"))
  }

  val order = orderCollection

  /**
    * 下一个订单号
    */
  val nextOrderNo = orderNoCollection

  val orderAudit = orderAuditCollection

  val orderRef = orderRefCollection

  /**
    * 创建订单
    * 生成一条订单记录
    * 1. 订单状态为idle
    * 2. 生成一条订单审计，谁在什么时间创建了订单
    *
    * @param createOrder
    * @param ec
    * @return 订单ID
    */
  def create(createOrder: CreateOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    // 计算下一个订单号
    if (createOrder.items.isEmpty) {
      throw new RuntimeException("订单清单为空")
    }
    val id = Utils.nextId()
    nextNo() flatMap { no =>
      val newOrder = Order(id = id,
        no = no,
        proposer = "",
        created = Some(DateTime.now()),
        status = OrderStatus.Idle,
        hospitalId = createOrder.hospitalId.getOrElse(""),
        notes = createOrder.notes,
        items = createOrder.items)
      order.flatMap(_.insert(newOrder)) map {
        case le if le.ok => Some(id)
        case le =>
          logger.error(le.message)
          throw new RuntimeException("新建失败")
      }
    } flatMap { id =>
      id match {
        case Some(id) =>
          // 创建一条审计日志
          createOrderAudit(id, "", "新建订单")
      }
    }
  }

  /**
    * 取消订单
    * <pre>
    *
    * <pre>
    *
    * @param id 订单ID
    * @param ec
    * @return
    */
  def cancel(id: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { order =>
      order match {
        case Some(order) =>
          val status = order.status
          if (OrderStatus.Idle.equals(status) || OrderStatus.Handling.equals(status)) {
            val criteria = Json.obj("id" -> id)
            val update = order.copy(status = OrderStatus.Cancel)
            orderCollection.flatMap(_.update(criteria, update)) flatMap {
              case le if le.ok =>
                createOrderAudit(id, "", "取消订单")
              case le =>
                throw new RuntimeException("取消订单失败")
            }
          } else {
            throw new RuntimeException("订单不能被取消")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 审核通过
    *
    * @param id
    * @param ec
    * @return
    */
  def permit(id: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val status = item.status
          if (OrderStatus.Idle.equals(status) || OrderStatus.Handling.equals(status)) {
            val criteria = Json.obj("id" -> id)
            val update = item.copy(status = OrderStatus.Handling)
            orderCollection.flatMap(_.update(criteria, update)) flatMap {
              case le if le.ok =>
                createOrderAudit(id, "", "审核通过")
              case le =>
                throw new RuntimeException("审核通过订单失败")
            }
          } else {
            throw new RuntimeException("订单不能被审核通过")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 拒绝订单
    *
    * @param id
    * @param reason
    * @param ec
    * @return
    */
  def reject(id: String, reason: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val status = item.status
          if (OrderStatus.Idle.equals(status) || OrderStatus.Handling.equals(status)) {
            val criteria = Json.obj("id" -> id)
            val update = item.copy(status = OrderStatus.Idle)
            orderCollection.flatMap(_.update(criteria, update)) flatMap {
              case le if le.ok =>
                createOrderAudit(id, "", s"订单被拒绝,原因【$reason】")
              case le =>
                throw new RuntimeException("审核拒绝订单失败")
            }
          } else {
            throw new RuntimeException("订单不能被审核拒绝")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 出库
    *
    * @param id
    * @param ec
    * @return
    */
  def goods(id: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val status = item.status
          if (OrderStatus.Handling.equals(status)) {
            val criteria = Json.obj("id" -> id)
            val update = item.copy(status = OrderStatus.Shipping)
            orderCollection.flatMap(_.update(criteria, update)) flatMap {
              case le if le.ok =>
                createOrderAudit(id, "", "准备出库")
              case le =>
                throw new RuntimeException("订单出库失败")
            }
          } else {
            throw new RuntimeException("订单不能出库")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 收货确认
    *
    * @param id
    * @param ec
    * @return
    */
  def confirm(id: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val status = item.status
          if (OrderStatus.Shipping.equals(status)) {
            val criteria = Json.obj("id" -> id)
            // 更新状态为归档模式
            val update = item.copy(status = OrderStatus.achieve)
            orderCollection.flatMap(_.update(criteria, update)) flatMap {
              case le if le.ok =>
                createOrderAudit(id, "", "已收货，订单完成。")
              case le =>
                throw new RuntimeException("收货确认失败")
            }
          } else {
            throw new RuntimeException("订单不能收货确认")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 根据参与者分页查询订单
    *
    * @param who
    * @param no
    * @param skip
    * @param limit
    * @return
    */
  def query(who: Option[String], no: Option[String], status: Option[String], skip: Int, limit: Int): Future[Traversable[Order]] = {
    // 查询用户相关的订单
    var criteria = Json.obj()
    who match {
      case Some(who) => {
        queryOrderRef(who) map { ref =>
          val ids = ref map { id =>
            JsString(id.orderId)
          }
          criteria = criteria.+("id", Json.obj("$in" -> Json.arr(ids)))
        }
      }
      case None => {}
    }

    no match {
      case Some(no) => {
        criteria = criteria.+(
          "no", Json.obj(
            "$regex" -> no,
            "$options" -> "mi"
          )
        )
      }
      case None => {}
    }
    // 状态
    status match {
      case Some(status) => {
        criteria = criteria.+(
          "status", JsString(status)
        )
      }
      case None => {}
    }

    logger.debug("criteria -> {}", criteria)
    search(criteria, skip, limit)
  }

  /**
    * 根据医院查询订单
    *
    * @param hospitalId
    * @param skip
    * @param limit
    * @return
    */
  def queryByHospital(hospitalId: String, skip: Int, limit: Int): Future[Traversable[Order]] = {
    val criteria = Json.obj()
    criteria.+("hospitalId", JsString(hospitalId))
    search(criteria, skip, limit)
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Order]] = {
    order.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[Order] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  /**
    * 根据用户查询相关的订单
    *
    * @param who 用户id
    * @return
    */
  private def queryOrderRef(who: String): Future[Traversable[OrderRef]] = {
    val criteria = Json.obj("who" -> who)
    orderRef.flatMap(_.find(criteria).cursor[OrderRef](readPreference = ReadPreference.nearest)
      .collect[List]())
  }

  private def createOrderAudit(id: String, who: String, notes: String): Future[Option[String]] = {
    val audit = OrderAudit(
      id = Utils.nextId(),
      orderId = id,
      who = who,
      created = Some(DateTime.now()),
      notes = Some(notes)
    )
    orderAudit.flatMap(_.insert(audit)) flatMap {
      case le if le.ok => {
        val criteria = Json.obj("orderId" -> id, "who" -> who)
        val tmpOrderRef = OrderRef(id = Utils.nextId(),
          orderId = id,
          who = who
        )
        orderRef.flatMap(_.find(criteria).one[OrderRef]) flatMap {
          ref =>
            ref match {
              case Some(ref) =>
                // 已经存在，不处理
                Future {
                  Some(id)
                }
              case None =>
                orderRef.flatMap(_.insert(tmpOrderRef)) map {
                  case le if le.ok =>
                    Some(id)
                  case le =>
                    throw new RuntimeException("创建订单审计日志失败")
                }
            }
        }
      }
      case le => {
        throw new RuntimeException("创建订单审计日志失败")
      }
    }
  }

  def pk(id: String): Future[Option[Order]] = {
    val criteria = Json.obj("id" -> id)
    import reactivemongo.play.json._
    order.flatMap(_.find(criteria).one[Order])
  }

  /**
    * 下一个订单号（年+月+流水号（8））
    * <pre>
    * 2016.05.00000001
    * </pre>
    *
    * @return
    */
  def nextNo(): Future[String] = {
    val prefix = Utils.orderFmt()
    val criteria = Json.obj("prefix" -> prefix)

    import reactivemongo.play.json._

    val serial = nextOrderNo.flatMap(_.find(criteria).one[NextOrderNo]) map {
      f =>
        f match {
          // 存在，更新
          case Some(no) =>
            val serial = no.serial + 1
            val update = Json.obj("$inc" -> Json.obj("serial" -> 1))
            nextOrderNo.flatMap(_.update(criteria, update))
            serial
          // 不存在，插入
          case None =>
            val serial = 1
            val entity = NextOrderNo(prefix, serial)
            nextOrderNo.flatMap(_.insert(entity))
            serial
        }
    }
    serial map {
      serial =>
        val fmt = "%s.%08d"
        fmt.format(prefix, serial)
    }
  }
}
