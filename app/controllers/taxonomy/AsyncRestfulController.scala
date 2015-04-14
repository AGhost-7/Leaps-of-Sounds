package controllers.taxonomy
import models._
import play.api.mvc._
import scalikejdbc._
import scalikejdbc.async._
import play.api.libs.json._
//import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

abstract class AsyncRestfulController extends Controller {
	
	protected implicit val con = AsyncDB.sharedSession
	protected implicit val executionContext = 
		play.api.libs.concurrent.Execution.Implicits.defaultContext
		
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
	
	
	protected val invalidInputResponse = 
		BadRequest(Json.obj(
			"success" -> false,
			"errorMessage" -> "Your input failed the validation rules."))
	
	protected def ifValidated(bool: Boolean, errorMessage: Option[String] = None)(func: => JsValue): Result = {
		if(bool){
			Ok(func)
		} else {
			errorMessage.fold { 
				invalidInputResponse 
			} { msg => 
				BadRequest(Json.obj("success" -> false, "errorMessage" -> msg))
			}
		}
	}
	
	
	
}