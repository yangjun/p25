package sales.services

import javax.inject.{Inject, Singleton}

import authentication.Role
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yangjungis@126.com on 2016/5/16.
  */
@Singleton
class RoleService @Inject() (implicit ec: ExecutionContext) {


  /**
    * 取得系统支持的角色
    * @param ec
    * @return
    */
  def roles()(implicit ec: ExecutionContext): Future[Traversable[Role]] = {
    Future {
      List(Role(Role.salesman, "事务所开发人员"),
        Role(Role.firstReview, "片区经理"),
        Role(Role.review, "总经理"),
        Role(Role.stock, "库管"),
        Role(Role.doctor, "医生")
      )
    }
  }

  private lazy val logger = LoggerFactory.getLogger(classOf[RoleService])
}
