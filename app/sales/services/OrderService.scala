package sales.services

import javax.inject.{Inject, Singleton}

import authentication.{User, UserServiceImpl}
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

  private def stockOrderCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("stockOrder"))
  }

  private def goodsItemCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("goodsItem"))
  }

  val order = orderCollection

  /**
    * 下一个订单号
    */
  val nextOrderNo = orderNoCollection

  /**
    * 订单审批历史
    */
  val orderAudit = orderAuditCollection

  /**
    * 订单干系人
    */
  val orderRef = orderRefCollection
  /**
    * 出库单
    */
  val stockOrder = stockOrderCollection

  /**
    * 出库清单
    */
  val goodsItem = goodsItemCollection

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
      val now = DateTime.now()
      val newOrder = Order(id = id,
        no = no,
        proposer = createOrder.proposer.getOrElse(""),
        created = Some(now),
        updated = Some(now),
        status = OrderStatus.idle,
        hospitalId = createOrder.hospitalId.getOrElse(""),
        notes = createOrder.notes,
        stockOrderId = None,
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
  def cancel(id: String, cancelOrder: CancelOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val reason = cancelOrder.reason
          val update = item.cancel()
          val criteria = Json.obj("id" -> id)
          orderCollection.flatMap(_.update(criteria, update)) flatMap {
            case le if le.ok =>
              createOrderAudit(id, User.mockUser, s"取消订单,原因【$reason】")
            case le =>
              throw new RuntimeException("取消订单失败")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 审核通过
    *
    * @param id 订单ID
    * @param ec
    * @return
    */
  def permit(id: String, permitOrder: PermitOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    logger.debug("permitOrder -> {}", permitOrder)
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          logger.debug("order -> {}", item)
          val reason = permitOrder.reason
          logger.debug("reason -> {}", reason)
          val update = item.permit()
          logger.debug("update -> {}", item)
          val criteria = Json.obj("id" -> id)
          order.flatMap(_.update(criteria, update)) flatMap {
            case le if le.ok =>
              logger.debug("order -> {}", item)
              val notes = s"审核通过，原因【$reason】"
              logger.debug("notes -> {}", notes)
              createOrderAudit(id, User.mockUser, reason)
            case le =>
              throw new RuntimeException("审核通过订单失败")
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
    * @param rejectOrder
    * @param ec
    * @return
    */
  def reject(id: String, rejectOrder: RejectOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          val reason = rejectOrder.reason
          val update = item.reject()
          val criteria = Json.obj("id" -> id)
          orderCollection.flatMap(_.update(criteria, update)) flatMap {
            case le if le.ok =>
              createOrderAudit(id, "", s"订单被拒绝,原因【$reason】")
            case le =>
              throw new RuntimeException("审核拒绝订单失败")
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    * 创建出库单
    *
    * @param id
    * @param ec
    * @return 出库单标识
    */
  def createStockOrder(id: String, createStockOrder: CreateStockOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    pk(id) flatMap { item =>
      item match {
        case Some(item) =>
          // 是否已经生成出库单
          val optStockOrderId = item.stockOrderId
          optStockOrderId match {
            // 已经生成
            case Some(stockOrderId) =>
              Future {
                Some(stockOrderId)
              }
            case None =>
              // 创建出库单
              val newStockOrder = createStockOrder.stockOrder(id, User.mockUser)
              stockOrder.flatMap(_.insert(newStockOrder)) flatMap {
                case le if le.ok =>
                  // 更新订单出库单标识
                  val newOrder = item.copy(stockOrderId = Some(newStockOrder.id))
                  val criteria = Json.obj("id" -> item.id)
                  order.flatMap(_.update(criteria, newOrder)) map {
                    case le if le.ok =>
                      Some(newStockOrder.id)
                    case le =>
                      throw new RuntimeException("更新订单出库信息错误")
                  }
                case le =>
                  throw new RuntimeException("创建出库单错误")
              }
          }
        case None =>
          throw new RuntimeException("订单不存在")
      }
    }
  }

  /**
    *
    * @param id 出库单标识
    * @param addGoodsItem
    * @param ec
    * @return
    */
  def addStockItem(id: String, addGoodsItem: AddGoodsItem)(implicit ec: ExecutionContext): Future[Option[String]] = {
    stockOrderById(id) map { item =>
      item match {
        case Some(item) =>
          // 出库单存在
          // 构造出库清单
          val items = addGoodsItem.goodsItems(item.id)
          items map {
            item =>
              goodsItem.flatMap(_.insert(item)) map {
                case le if le.ok =>
                  Some(item.id)
                case le =>
                  throw new RuntimeException("创建出库清单出错")
              }
          }
          Some(id)
        case None =>
          throw new RuntimeException("出库单不存在")
      }
    }
  }

  /**
    * 删除出库清单
    *
    * @param id 出库单标识
    * @param ec
    * @return
    */
  def removeStockItem(id: String, removeGoodsItem: RemoveGoodsItem)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val f = stockOrderById(id) flatMap { item =>
      val stockOrderId = id
      item match {
        case Some(item) =>
          // 出库单存在
          // 查询订单，检查订单状态
          pk(item.orderId) map {
            order =>
              order match {
                case Some(order) =>
                  order.status match {
                    case OrderStatus.stock =>
                      //  订单在出库阶段可以删除出库清单
                      removeGoodsItem.items map {
                        id => {
                          // 查询出库清单
                          val criteria = Json.obj("id" -> id)
                          goodsItem.flatMap(_.find(criteria).one[GoodsItem]) map {
                            item => {
                              item match {
                                case Some(item) =>
                                  // 检查给定标识的出库清单是否与出库单一致
                                  item.stockOrderId match {
                                    case u if u.equals(stockOrderId) =>
                                      goodsItem.flatMap(_.remove(criteria)) map {
                                        case le if le.ok =>
                                          Some(id)
                                        case le =>
                                          throw new RuntimeException("删除出库清单出错")
                                      }
                                    case _ =>
                                      throw new RuntimeException("不是当前库存订单")
                                  } // end match item.stockOrderId
                                case None =>
                                  throw new RuntimeException("订单清单不存在")
                              } // end match item
                            }
                          }
                        }
                      }
                    case _ =>
                      throw new RuntimeException("仅在出库阶段可以删除出库清单")

                  } // end match order.status
                case None =>
                  throw new RuntimeException("未发现出库单对应的订单")
              } // end match order

          }
        case None =>
          throw new RuntimeException("出库单不存在")
      } // end match item
    }

    // 返回结果
    f map (p => Some(id))

  }

  /**
    * 收货确认
    *
    * @param id
    * @param ec
    * @return
    */
  def confirm(id: String, permitOrder: PermitOrder)(implicit ec: ExecutionContext): Future[Option[String]] = {
    permit(id, permitOrder)
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
    var criteria = Json.obj()
    criteria = criteria.+("hospitalId", JsString(hospitalId))
    search(criteria, skip, limit)
  }

  /**
    * 根据出库单查询出库清单
    *
    * @param id 出库单标识
    * @param skip
    * @param limit
    * @return
    */
  def queryStockItemByStock(id: String, skip: Int, limit: Int): Future[Traversable[GoodsItem]] = {
    val criteria = Json.obj("stockOrderId" -> id)
    searchGoodsItem(criteria = criteria, skip = skip, limit = limit)
  }


  /**
    * 根据订单查询出库清单
    *
    * @param id 订单标识
    * @param skip
    * @param limit
    * @return
    */
  def queryStockItemByOrder(id: String, skip: Int, limit: Int): Future[Traversable[GoodsItem]] = {
    pk(id) flatMap {
      order => order match {
        case Some(order) =>
          logger.debug("stockOrderId -> {}", order.stockOrderId)
          order.stockOrderId match {
            case Some(stockOrderId) =>
              queryStockItemByStock(stockOrderId, skip, limit)
            case None =>
              throw new RuntimeException("还未生成出库单")
          }
        case None =>
          throw new RuntimeException(" 订单不存在")
      }
    }
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Order]] = {
    order.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[Order] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  private def searchGoodsItem(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[GoodsItem]] = {
    goodsItem.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[GoodsItem] (readPreference = ReadPreference.nearest)
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

  def stockOrderById(id: String): Future[Option[StockOrder]] = {
    val criteria = Json.obj("id" -> id)
    import reactivemongo.play.json._
    stockOrder.flatMap(_.find(criteria).one[StockOrder])
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
