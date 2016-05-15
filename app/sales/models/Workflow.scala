package sales.models

import authentication.User
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by yangjungis@126.com on 2016/5/15.
  */

//================工作流相关=========================

object TaskStatus {
  // 【待办】，初始状态，上一个发起人可以取回
  val idle: String = "idle"
  // 【正在处理】接收任务开始处理
  val processing: String = "processing"
  // 【已完成】，可能是接受或者拒绝，参见  action
  val completed: String = "completed"
}

object Action {
  // 接受
  val permit: String = "permit"
  // 拒绝
  val reject: String = "reject"

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
                 // 谁发来的任务
                 sender: String,
                 no: String,
                 // 参见 TaskStatus
                 status: String = TaskStatus.idle,
                 notes: Option[String],
                 // 动作
                 action: Option[String],
                 // 创建时间
                 created: Option[DateTime],
                 // 开始时间
                 started: Option[DateTime],
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
}

object Payload {
  implicit val format = Json.format[Payload]
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
}


object History {
  implicit val format = Json.format[History]
}

//================= 参与者 =================

case class Stage(
                  // 标识
                  id: String,
                  // 流程
                  workflow: String = "order",
                  // 阶段
                  stage: String,
                  // 阶段干系人（基于角色表示）
                  stakeholders: Set[String]
                )

object Stage {
  implicit val format = Json.format[Stage]
  val workflow = "order"
}

case class Stakeholder(id: String,
                       displayName: Option[String],
                       avatar: Option[String]
                      )

object Stakeholder {
  implicit val format = Json.format[Stakeholder]
  def apply(user: User): Stakeholder = {
    Stakeholder(id = user.id, displayName = user.displayName, avatar = user.avatar)
  }
}

