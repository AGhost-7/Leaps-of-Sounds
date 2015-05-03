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


/** Standard CRUD operations for Scales data. */
object Scales extends AsyncRestfulController {

	def update(id: Long, name: String, values: String) = inLogin { user =>
		ifValidated(Scale.validInput(name, values)) {
			scalarUpdate(Scale.async.update(id, name, values, user))
		}
	}

	def remove(id: Long) = inLogin { user =>
		scalarUpdate(Scale.async.remove(id, user))
	}

	def insert(name: String, values: String) = inLogin { user =>
		ifValidated(Scale.validInput(name, values)) {
			Scale
				.async
				.insert(name, values, user)
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

}





















