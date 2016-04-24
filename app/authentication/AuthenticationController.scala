package authentication

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.ExecutionContext
import authentication._
import controllers.JsonValidate
import play.api.libs.json.Json

/**
  * Created by yangjungis@126.com on 2016/4/23.
  */
@Singleton
class AuthenticationController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                         val userService: UserServiceImpl)
                                        (implicit exec: ExecutionContext)
  extends Controller with ReactiveMongoComponents with Secured with JsonValidate {

  def userProfile = Authenticated.async { implicit req =>
    userService.userProfile(req.userId).map(f => {
      f match {
        case Some(u) => {
          import authentication.User.format
          Ok(Json.toJson(u))
        }
        case None => {
          Ok("")
        }
      }
    })
  }

  def user(name: String) = Action.async {
    userService.user(name).map(f => {
      f match {
        case Some(u) => {
          import authentication.User.format
          Ok(Json.toJson(u))
        }
        case None => {
          Ok("")
        }
      }
    })
  }

  def editUser(userId: String) = Action.async(parse.json) { implicit req =>
    validateAndThen[EditUser] {
      e =>
        userService.editUser(userId, e) map { f =>
          f match {
            case Some(e) => BadRequest(e)
            case None => Ok("")
          }
        }
    }
  }
}
