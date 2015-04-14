package controllers
import play.api.mvc._
import taxonomy._
import models._
import play.api.libs.json._
import scala.concurrent.Future


object Users extends AsyncRestfulController {
	
	def login = Action.async { req =>
		val b = Json.parse(req.body.asText.get)
		
		(b \"name", b \ "password") match {
			
				case (JsString(username), JsString(password)) =>
					User.async.authenticate(username, password).flatMap {
						case user: User => 
							User.async.mkToken(user.id).map { tkn => 
								Ok(Json.obj("token" -> tkn))
									.withSession("username" -> user.username, "token" -> tkn)
							}
						case FailedAuth(err) =>
							val rs = Unauthorized(Json.obj("error" -> err))
							Future.successful(rs)
					}
					
				case _=>
					val rs = BadRequest(Json.obj("error" -> "Invalid message format."))
					Future.successful(rs)
		}
	}
	
	def register = Action.async { req =>
		val b = Json.parse(req.body.asText.get)
		(b \ "username", b \ "password", b \ "password2", b \ "email") match {
			
			case (JsString(username), JsString(password), JsString(password2), JsString(email)) =>
				User.async.findValidationError(username, password, password2, email).flatMap {
					case Some(error) => 
						val rs = BadRequest(Json.obj("error" -> error))
						Future.successful(rs)
					case None =>
						User.async.create(username, password, email).flatMap { user =>
							User.async.mkToken(user.id).map { tkn =>
								Ok(Json.obj("token" -> tkn, "username" -> username))
									.withSession("username" -> user.username, "token" -> tkn)
							}
						}
				}
				
			case _ =>
					val rs = BadRequest(Json.obj("error" -> "Invalid message format."))
					Future.successful(rs)
		}
		
	}
	
	def logout = Action.async { req =>
		req.session.get("token").fold {
			val rs = BadRequest(Json.obj("error" -> "No token found."))
			Future.successful(rs)
		} { tkn => 
			User.async.rmToken(tkn).map { i =>
				println(i)
				Ok("{}").withNewSession
			}
		}
	}
	
	
	
}