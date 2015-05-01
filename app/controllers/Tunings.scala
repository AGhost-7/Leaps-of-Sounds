package controllers


import play.api.mvc._
import play.api._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json.Json
import models._
import models.implicits._
import scalikejdbc._
import scalikejdbc.async._
import utils.sql._
import taxonomy._

/**
 * Standard CRUD operations for the Tunings data.
 */
object Tunings extends AsyncRestfulController {

	def ofInstrument(instrumentId: Long) = Action.async { implicit request =>
		for {
			userOpt <- User.async.fromSession
			tunings <- userOpt.fold {
						sql"""
							SELECT * FROM tunings
							WHERE instrument = $instrumentId
			 					AND user_id IS NULL
							"""
					} { user =>
						sql"""
							SELECT * FROM tunings
							WHERE instrument = $instrumentId
								AND (user_id = ${user.id} OR user_id IS NULL)
							"""
					}.as[Tuning].list.future
		} yield Ok(Json.toJson(tunings))
	}

	def remove(id: Long) = inLogin { user =>
		sql"""DELETE FROM tunings WHERE id = $id AND user_id = ${user.id}"""
			.update
			.future
			.map {
				case 0 => BadRequest(Json.obj("error" -> "Tuning does not exist"))
				case 1 => Ok(Json.obj("id" -> id, "success" -> true))
			}
	}

	def insert(name: String, values: String, instrumentId: Long) = inLogin { user =>
		ifValidated(Tuning.validInput(name, values)) {
			sql"""
				INSERT INTO tunings(name, values, instrument)
				VALUES ($name, $values, $instrumentId)
				RETURNING id
			"""
				.updateAndReturnGeneratedKey()
				.future
				.map { id =>
					Ok(Tuning(id, name, values, Some(user.id), instrumentId).toJson)
				}
		}
	}

	def update(id: Long, name: String, values: String, instrumentId: Long) =
		inLogin { user =>
			ifValidated(Tuning.validInput(name, values)) {
				sql"""
					UPDATE tunings
					SET name = $name, values = $values
					WHERE user_id = ${user.id} AND id = $id
				"""
					.update()
					.future
					.map {
						case 1 =>
							Ok(Tuning(id, name, values, Some(user.id), instrumentId).toJson)
						case 0 =>
							BadRequest(Json.obj("error" -> "Tuning does not exist"))
					}
			}
		}

	/*
	def ofInstrument(instrumentId: Long) = Action { implicit request =>
    DB.withTransaction { implicit con => 
	    implicit val user = User.fromSession
	    
	    val tunings = Tuning.ofInstrument(instrumentId)
	    
	    Ok(Json.toJson(tunings))
    }
  }
	
	def remove(id: Long) = inLogin withDB { (user, con) =>
		Tuning.remove(id, user)(con)
		Ok(Json.obj("id" -> id, "success" -> true))
	}
	
	def insert(name: String, values: String, instrumentId: Long) = 
		inLogin withDB { (user, con) =>
			ifValidated(Tuning.validInput(name, values)) {
				Tuning.insert(name, values, instrumentId, user)(con).toJson
			}
		}
	
	def update(id: Long, name: String, values: String, instrumentId: Long) = 
		inLogin withDB { (user, con) =>
			ifValidated(Tuning.validInput(name, values)){
				Tuning.update(id, name, values, instrumentId, user)(con).toJson
			}
		}*/
}



