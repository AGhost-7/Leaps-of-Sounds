package controllers

import play.api._
import play.api.mvc._
import play.api.db.DB
import models._
import java.sql.Connection
import play.api.Play.current
import play.api.libs.json.Json

/**
 * This trait defines default the behavior of my REST controllers.
 */
trait RestfulController extends Controller {
	
	/**
	 * For when the user must be logged in.
	 */
	object inLogin {
		
		lazy val notLoggedInResponse = {
			val obj = Map("errorMessage" -> "You must be logged in.")
			Unauthorized(Json.toJson(obj))
		}
		
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
	
	val invalidInputResponse = {
		val obj = Map("errorMessage" -> "Your input failed the validation rules.")
		BadRequest(Json.toJson(obj))
	}
	
	def ifValidated(bool: Boolean, errorMessage: Option[String] = None)(func: => play.api.libs.json.JsValue) = {
		if(bool){
			Ok(func)
		} else {
			errorMessage.map { msg =>
				val obj = Map("errorMessage" -> msg)
				BadRequest(Json.toJson(obj))
			}.getOrElse(invalidInputResponse)
		}
	}
	
}