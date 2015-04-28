package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.db.DB
import play.api.Play.current
import anorm._
import models.{Scale, User}
import controllers.taxonomy._
import scala.concurrent.Future
import scalikejdbc.async._
import scalikejdbc._



/**
 * Standard CRUD operations for Scales data.
 */
object Scales extends AsyncRestfulController {

	def update(id: Long, name: String, values: String) = inLogin { user =>
		ifValidated(Scale.validInput(name, values)) {
			sql"""
				UPDATE scales
				SET name = $name, values = $values
				WHERE user_id = ${user.id}
					AND id = $id"""
				.update
				.future
				.map {
					case 1 => Ok(Scale(id, name, values, Some(user.id)).toJson)
					case 0 => BadRequest(Json.obj("error" -> "Scale doesn't exist"))
				}
		}
	}

	def remove(id: Long) = inLogin { user =>
		scalarUpdate(Scale.async.remove(id, user))
	}

	def insert(name: String, values: String) = inLogin { user =>
		ifValidated(Scale.validInput(name, values)) {
			sql"""
				INSERT INTO scales("name", "values", user_id)
				VALUES ($name, $values, ${user.id})
				"""
			.updateAndReturnGeneratedKey()
			.future
			.map { i =>
				Ok(Scale(i, name, values, Some(user.id)).toJson)
			}
		}
	}

	def list(set: Int) = inLogin { user =>
		Scale.async.ofPageForUser(set, user).map { ls =>
			Ok(Json.toJson(ls))
		}
	}

	def all = Action.async { implicit req =>
		for {
			userOpt <- User.async.fromSession
			scales <- Scale.async.all(userOpt)
		} yield Ok(Json.toJson(scales))
	}

/*
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
*/
}





















