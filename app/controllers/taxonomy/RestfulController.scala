package controllers.taxonomy

import play.api._
import play.api.mvc._
import play.api.db.DB
import models._
import java.sql.Connection
import play.api.Play.current
import play.api.libs.json._

/**
 * This class defines default the behavior of my REST controllers.
 */
abstract class RestfulController extends Controller {
	
	/**
	 * For when the user must be logged in.
	 */
	object inLogin {
		
		lazy val notLoggedInResponse = 
			Unauthorized(Json.obj("errorMessage" -> "You must be logged in."))
		
		
		def apply(func: User => Result) = Action { implicit request =>
			User.fromSession.map { user =>
				func(user)
			}.getOrElse(notLoggedInResponse)
		}
		
		def withDB(func: (User, Connection) => Result) = apply { user =>
			DB.withConnection { con =>
				func(user,con)
			}
		}
	}
	
	/**
	 * If validated, returns a Json string of the inserted data using the function. 
	 * Otherwise default behavior is to return a Bad Request with a Json string 
	 * which contains an errorMessage field.
	 */
	
	val invalidInputResponse = 
		BadRequest(Json.obj("errorMessage" -> "Your input failed the validation rules."))
	
	def ifValidated(bool: Boolean, errorMessage: Option[String] = None)(func: => JsValue): SimpleResult = {
		if(bool){
			Ok(func)
		} else {
			errorMessage.fold { 
				invalidInputResponse 
			} { msg => 
				BadRequest(Json.obj("errorMessage" -> msg))
			}
		}
	}
	
}