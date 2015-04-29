package controllers.taxonomy
import models._
import play.api.mvc._
import scalikejdbc._
import scalikejdbc.async._
import play.api.libs.json._
//import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

abstract class AsyncRestfulController extends Controller {
	

	protected implicit val executionContext = 
		play.api.libs.concurrent.Execution.Implicits.defaultContext

	protected implicit def db = AsyncDB.sharedSession
		
	protected object inLogin {
		
		lazy val notLoggedInResponse: Result = 
			Unauthorized(Json.obj("errorMessage" -> "You must be logged in."))
			
		/*def apply(whenLoggedIn: User => Result): Action[AnyContent] = Action.async { implicit req =>
			User.async.fromSession.map {
				case Some(user) => whenLoggedIn(user)
				case None => notLoggedInResponse
			}
		}*/
		
		def apply(whenLoggedIn: User => Future[Result]): Action[AnyContent] = Action.async { implicit req =>
			User.async.fromSession.flatMap {
				case Some(user) => whenLoggedIn(user)
				case None => Future.successful(notLoggedInResponse)
			}
		}
		
	}
	
	
	protected val invalidInputResponse = {
		val js = Json.obj(
			"success" -> false,
			"errorMessage" -> "Your input failed the validation rules.")
		Future.successful(BadRequest(js))
	}

	protected def ifValidated(bool: Boolean, errorMessage: Option[String] = None)(func: => Future[Result]): Future[Result] = {
		if(bool){
			func
		} else {
			errorMessage.fold { 
				invalidInputResponse 
			} { msg => 
				Future.successful(
					BadRequest(Json.obj("success" -> false, "errorMessage" -> msg)))
			}
		}
	}
	
	protected def scalarUpdate(ft: Future[Int]): Future[Result] = {
		ft.map {
			case 0 => BadRequest(Json.obj("error" -> "Entry does not exist"))
			case 1 => Ok("{}").withHeaders("Content-Type" -> "application/json")
		}
	}
	
}