package sales.services

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json.collection.JSONCollection
import sales.models._
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * FAQ
  * Created by yangjungis@126.com on 2016/5/21.
  */
@Singleton
class FAQService @Inject()(
                            reactiveMongoApi: ReactiveMongoApi
                          )
                          (implicit ec: ExecutionContext) {
  // 问题
  val question = questionCollection
  // 答案
  val answer = questionCollection

  def crateQuestion(createQuestion: CreateQuestion): Future[Option[String]] = {
    val newQuestion = createQuestion.build
    question.flatMap(_.insert(newQuestion)) map {
      case le if le.ok =>
        Some(newQuestion.id)
      case le =>
        throw new RuntimeException("创建问题失败")
    }
  }

  def editQuestion(editQuestion: EditQuestion): Future[Option[String]] = {
    readQuestion(editQuestion.id) flatMap (
      item =>
        item match {
          case Some(item) =>
            val update = item.copy(
              question = editQuestion.question,
              updated = Some(DateTime.now())
            )
            val criteria = Json.obj("id" -> item.id)
            question.flatMap(_.update(criteria, update)) map {
              case le if le.ok =>
                Some(item.id)
              case le =>
                throw new RuntimeException("更新问题失败")
            }
          case None =>
            throw new RuntimeException("未发现问题")
        }

      )

  }

  // 删除问题
  def deleteQuestion(id: String): Future[Option[String]] = {
    // 先删除答案
    val criteria = Json.obj("questionId" -> id)
    answer.flatMap(_.remove(criteria)) flatMap {
      case le if le.ok =>
        // 再删除问题
        val criteria = Json.obj("id" -> id)
        question.flatMap(_.remove(criteria)) map {
          case le if le.ok =>
            Some(id)
          case le =>
            throw new RuntimeException("删除问题失败")
        }
      case le =>
        throw new RuntimeException("删除问题答案失败")
    }
  }

  // 回答问题
  def answerQuestion(answerQuestion: AnswerQuestion): Future[Option[String]] = {
    readQuestion(answerQuestion.id) flatMap (
      q =>
        q match {
          case Some(q) =>
            val newAnswer = answerQuestion.build()
            answer.flatMap(_.insert(newAnswer)) map {
              case le if le.ok =>
                Some(newAnswer.id)
              case le =>
                throw new RuntimeException("创建问题答案失败")

            }
          case None =>
            throw new RuntimeException("未发现问题")
        }
      )
  }

  // 编辑答案
  def editAnswer(answerQuestion: AnswerQuestion): Future[Option[String]] = {
    readAnswer(answerQuestion.id) flatMap (
      a => a match {
        case Some(a) =>
          a.answerer match {
            // 回答问题本人
            case answerer if answerer.equals(answerQuestion.answerer) =>
              val update = a.copy(
                answer = answerQuestion.answer,
                updated = Some(DateTime.now())
              )
              val criteria = Json.obj("id" -> a.id)
              answer.flatMap(_.update(criteria, update)) map {
                case le if le.ok =>
                  Some(a.id)
                case le =>
                  throw new RuntimeException("更新答案失败")
              }
            case _ =>
              throw new RuntimeException("仅本人可以编辑答案")
          }
        case None =>
          throw new RuntimeException("未发现问题答案")
      })
  }

  // 根据问题查询答案
  def queryAnswer(id: String, skip: Int, limit: Int): Future[Traversable[Answer]] = {
    val criteria = Json.obj("questionId" -> id)
    searchAnswer(criteria = criteria, skip = skip, limit = limit)
  }

  def query(key: Option[String], skip: Int, limit: Int): Future[Traversable[Question]] = {
    var criteria = Json.obj()
    key match {
      case Some(key) =>
        val tags = Json.obj(
          "question.tags" -> Json.obj(
            "$regex" -> key,
            "$options" -> "mi"
          ))
        val content = Json.obj(
          "question.content" -> Json.obj(
            "$regex" -> key,
            "$options" -> "mi"
          ))
        val or = Json.arr(tags, content)
        criteria = criteria.+(
          "$or", or
        )
      case None =>
    }

    searchQuestion(criteria, skip, limit)
  }

  def readQuestion(id: String): Future[Option[Question]] = {
    val criteria = Json.obj("id" -> id)
    logger.debug("criteria -> {}", criteria)
    question.flatMap(_.find(criteria).one[Question])
  }

  def readAnswer(id: String): Future[Option[Answer]] = {
    val criteria = Json.obj("id" -> id)
    logger.debug("criteria -> {}", criteria)
    answer.flatMap(_.find(criteria).one[Answer])
  }

  private def searchQuestion(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Question]] = {
    logger.debug("criteria -> {}", criteria)
    question.flatMap(_.find(criteria).
      sort(Json.obj("updated" -> 1)).
      options(QueryOpts(skipN = skip))
      cursor[Question] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  private def searchAnswer(criteria: JsObject, skip: Int, limit: Int): Future[Traversable[Answer]] = {
    logger.debug("criteria -> {}", criteria)
    answer.flatMap(_.find(criteria).
      sort(Json.obj("created" -> 1)).
      options(QueryOpts(skipN = skip))
      cursor[Answer] (readPreference = ReadPreference.nearest)
      collect[List] (limit))
  }

  private def questionCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("question"))
  }

  private def answerCollection()(implicit ec: ExecutionContext): Future[JSONCollection] = {
    reactiveMongoApi.database.map(_.collection("answer"))
  }

  private lazy val logger = LoggerFactory.getLogger(classOf[FAQService])

}
