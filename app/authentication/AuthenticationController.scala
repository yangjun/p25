package authentication

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.ExecutionContext
import authentication._
import controllers.JsonValidate
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import sales.services.RoleService

/**
  * Created by yangjungis@126.com on 2016/4/23.
  */
@Singleton
class AuthenticationController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                         val roleService: RoleService,
                                         val userService: UserServiceImpl)
                                        (implicit exec: ExecutionContext)
  extends Controller with ReactiveMongoComponents with Secured with JsonValidate {

  lazy val logger = LoggerFactory.getLogger(classOf[AuthenticationController])

  /**
    * 查询用户Profile
    *
    * @return
    */
  def userProfile = Authenticated.async { implicit req =>
    userService.userProfile(req.token).map(f => {
      f match {
        case Some(u) => {
          import authentication.User.format
          Ok(Json.toJson(u))
        }
        case None => {
          BadRequest(Json.obj("error" -> "未发现用户信息"))
        }
      }
    })
  }

  /**
    * 根据名称查询用户
    *
    * @param name
    * @return
    */
  def user(name: String) = Action.async {
    userService.user(name).map(f => {
      f match {
        case Some(u) => {
          import authentication.User.format
          Ok(Json.toJson(u))
        }
        case None => {
          BadRequest(Json.obj())
        }
      }
    })
  }

  /**
    * 编辑用户信息
    *
    * @param userId
    * @return
    */
  def editUser(userId: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[EditUser] {
      e =>
        userService.editUser(userId, e) map { f =>
          f match {
            case Some(e) => BadRequest(e)
            case None => Ok(Json.obj("id" -> userId))
          }
        }
    }
  }

  /**
    * 查询系统支持的角色
    *
    * @return
    */
  def roles() = Action.async {
    roleService.roles() map (
      roles => {
        import authentication.Role.format
        Ok(Json.toJson(roles))
      }
      )
  }

  def allocationRole(userId: String, role: String) = Action.async {
    userService.allocationRole(userId = userId, role = role) map (roles => {
      roles match {
        case Some(roles) =>
          Ok(Json.toJson(roles))
        case None =>
          BadRequest(Json.obj())
      }
    })
  }

  def removeRole(userId: String, role: String) = Action.async {
    userService.removeRole(userId = userId, role = role) map (roles => {
      roles match {
        case Some(roles) =>
          Ok(Json.toJson(roles))
        case None =>
          BadRequest(Json.obj())
      }
    })
  }

}
