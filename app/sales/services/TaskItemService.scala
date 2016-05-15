package sales.services

import javax.inject.{Inject, Singleton}

import authentication.UserServiceImpl
import org.slf4j.LoggerFactory
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import sales.models.Task

import reactivemongo.play.json._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yangjungis@126.com on 2016/5/15.
  */
@Singleton
class TaskItemService @Inject()(
                                 reactiveMongoApi: ReactiveMongoApi
                               )
                               (implicit ec: ExecutionContext) {
  val task = taskCollection

  /**
    * 新建任务
    *
    * @param newTask
    * @return
    */
  def create(newTask: Task): Future[Option[String]] = {
    logger.debug("new Task -> {}", newTask)
    task.flatMap(_.insert(newTask)) map {
      case le if le.ok =>
        Some(newTask.id)
      case le =>
        None
    }
  }

  private def taskCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("task"))
  }

  private lazy val logger = LoggerFactory.getLogger(classOf[TaskService])
}
