package controllers

import play.api.mvc._
import models._
import anorm._

import play.api.libs.json.Json

object Instruments extends Controller with RestfulController {

	/**
	 * This is a wee bit complicated since the instrument and tuning are 
	 * cross-dependant.
	 */
	def insert(name: String, strings: Int, tuningName: String, tuningValues: String) = 
		inLogin withDB { (user, con) =>
			ifValidated(Instrument.validInput(name, strings) 
					&& Tuning.validInput(tuningName, tuningValues)) {
				
				// Calling stored function
				val row = SQL("""
					SELECT * 
					FROM insert_instrument(
						{inst_name},
						{inst_strings},
						{user_id},
						{tun_name},
						{tun_values}
					)
				""")
					.on("inst_name" -> name,
							"inst_strings" -> strings,
							"user_id" -> user.id,
							"tun_name" -> tuningName,
							"tun_values" -> tuningValues)
					.apply()(con).head
					
				val instrumentId = row[Long]("id_one")
				val tuningId = row[Long]("id_two")
				
				val instrument = Instrument(instrumentId, name, strings, tuningId, Some(user.id))
				val tuning = Tuning(tuningId, tuningName, tuningValues,Some(user.id), instrumentId)
				
				Json.obj("tuning" -> tuning.toJson, "instrument" -> instrument.toJson)
			}
		}
	
	def update(id: Long, name: String, strings: Int, defaultTuning: Long) = 
		inLogin withDB { (user, con) => 
			ifValidated(Instrument.validInput(name, strings)) {
				Instrument.update(id, name, strings, defaultTuning, user)(con).toJson
			}
		}
	
	def remove(id: Long) = inLogin withDB { (user, con) =>
		Instrument.remove(id, user)(con)
		Ok(Json.obj("id" -> id, "success" -> true))
	}
	
	
}