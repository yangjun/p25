package sales.services

import javax.inject.{Inject, Singleton}

import authentication.UserServiceImpl
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
  * 待办任务
  * Created by yangjungis@126.com on 2016/5/15.
  */
@Singleton
class TaskService @Inject()(
                             userService: UserServiceImpl,
                             orderService: OrderService,
                             reactiveMongoApi: ReactiveMongoApi)
                           (implicit ec: ExecutionContext) {
  val task = taskCollection


  /**
    * 接受一个任务， 把任务从idle -> processing
    *
    * @param id 任务ID
    * @return
    */
  def accept(id: String): Future[Option[String]] = {
    read(id) flatMap (item => {
      item match {
        case Some(item) =>
          item.status match {
            case s if s.equals(TaskStatus.idle) =>
              val update = item.copy(
                status = TaskStatus.processing,
                started = Some(DateTime.now())
              )
              val criteria = Json.obj("id" -> id)
              task.flatMap(_.update(criteria, update)) map {
                case le if le.ok =>
                  Some(id)
                case le =>
                  None
              }
            case _ =>
              logger.warn("待处理任务才能执行当前操作")
              Future(None)
          } // end item.status match
        case None =>
          throw new RuntimeException("任务不存在")
      } // end item match
    })
  }

  /**
    * 审核通过
    * <pre>
    * 更新任务 status = completed
    * action = Some(Action.permit)
    * </pre>
    *
    * @param id 任务ID
    * @param permitOrder
    * @return
    */
  def permit(id: String, permitOrder: PermitOrder): Future[Option[String]] = {
    read(id) flatMap (item => {
      item match {
        case Some(item) =>
          item.status match {
            case status if TaskStatus.processing.equals(status) =>
              val update = item.copy(
                status = TaskStatus.completed,
                action = Some(Action.permit),
                completed = Some(DateTime.now())
              )
              val criteria = Json.obj("id" -> id)
              task.flatMap(_.update(criteria, update)) map {
                case le if le.ok =>
                  // 更新订单
                  orderService.permit(item.no, update.who, permitOrder)
                  Some(id)
                case le =>
                  None
              }
            case _ =>
              Future {
                logger.warn("处理中的任务才能执行当前操作")
                None
              }
          } // end item.status match
        case None =>
          throw new RuntimeException("任务不存在")
      } //end item match
    })
  }

  /**
    * 审核被拒绝
    * <pre>
    * 更新任务 status = completed
    * action = Some(Action.reject)
    * </pre>
    *
    * @param id 任务ID
    * @param rejectOrder
    * @return
    */
  def reject(id: String, rejectOrder: RejectOrder): Future[Option[String]] = {
    read(id) flatMap (item => {
      item match {
        case Some(item) =>
          item.status match {
            case status if TaskStatus.processing.equals(status) =>
              val update = item.copy(
                status = TaskStatus.completed,
                action = Some(Action.reject),
                completed = Some(DateTime.now())
              )
              val criteria = Json.obj("id" -> id)
              task.flatMap(_.update(criteria, update)) map {
                case le if le.ok =>
                  // 回退给发送者
                  orderService.reject(item.no, item.sender, item.who, rejectOrder)
                  Some(id)
                case le =>
                  None
              }
            case _ =>
              Future {
                logger.warn("处理中的任务才能执行当前操作")
                None
              }
          }
        case None =>
          throw new RuntimeException("任务不存在")
      }
    })
  }

  /**
    * 根据操作者和状态分页查询代表任务
    *
    * @param who
    * @param status
    * @param skip
    * @param limit
    * @return
    */
  def query(who: Option[String], status: Option[String], action: Option[String], skip: Int, limit: Int): Future[Traversable[Task]] = {
    var criteria = Json.obj()
    who match {
      case Some(who) =>
        criteria = criteria.+("who", JsString(who))
      case None =>
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

    // 执行动作
    action match {
      case Some(action) => {
        criteria = criteria.+(
          "action", JsString(action)
        )
      }
      case None => {}
    }
    search(criteria, skip, limit)
  }

  def read(id: String): Future[Option[Task]] = {
    var criteria = Json.obj("id" -> id)
    logger.debug("criteria -> {}", criteria)
    task.flatMap(_.find(criteria).one[Task])
  }

  private def search(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Task]] = {
    logger.debug("criteria -> {}", criteria)
    task.flatMap(_.find(criteria).
      options(QueryOpts(skipN = skip))
      cursor[Task] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  private def taskCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("task"))
  }

  private lazy val logger = LoggerFactory.getLogger(classOf[TaskService])
}
