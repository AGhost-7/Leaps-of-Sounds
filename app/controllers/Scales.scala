package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.db.DB
import play.api.Play.current
import anorm._

import models.{Scale, User}

/**
 * Standard CRUD operations for Scales data.
 */
object Scales extends Controller with RestfulController {

	def update(id: Long, name: String, values: String) = inLogin withDB { (user, con) =>
		ifValidated(Scale.validInput(name, values)){
			Scale.update(id, name, values, user)(con).toJson
		}
	}
	
	def remove(id: Long) = inLogin.withDB { (user, con) =>
		Scale.remove(id, user)(con)
		Ok("{}")
	}
	
	def insert(name: String, values: String) = inLogin withDB { (user, con) =>
		ifValidated(Scale.validInput(name, values)) {
			Scale.insert(user, name, values)(con).toJson
		}
	}
	
	def list(set: Int) = inLogin withDB { (user, con) =>
		Ok(Scale.pageAsJson(set, user)(con))
	}
	
	def all = Action { implicit request =>
		implicit val user = User.fromSession
		DB.withConnection { implicit con =>
			Ok(Json.toJson(Scale.getAll))
		}
	}

}





















