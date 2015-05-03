package controllers

import play.api.mvc._
import models._
import anorm._
import play.api.libs.json.Json
import controllers.taxonomy._
import scalikejdbc._
import scalikejdbc.async._

object Instruments extends AsyncRestfulController {

	/**
	 * This is a wee bit complicated since the instrument and tuning are 
	 * cross-dependant.
	 */
	def insert(name: String, strings: Int, tuningName: String, tuningValues: String) =
		inLogin { user =>
			ifValidated(Instrument.validInput(name, strings)
					&& Tuning.validInput(tuningName, tuningValues)) {
				Instrument
					.async
					.insert(name, strings, tuningName, tuningValues, user)
					.map {
						case (instrumentId, tuningId) =>
							val instrument =
								Instrument(instrumentId,
									name, strings, tuningId, Some(user.id))
							val tuning = Tuning(tuningId,
								tuningName, tuningValues, Some(user.id), instrumentId)
							Ok(Json.obj("tuning" -> tuning, "instrument" -> instrument))
					}
				/*	sql"""
						SELECT * FROM insert_instrument(
		 				$name, $strings, ${user.id}, $tuningName, $tuningValues)
			 			"""
					.map { rs =>
						val instrumentId = rs.long("id_one")
						val tuningId = rs.long("id_two")
						val instrument =
							Instrument(instrumentId,
								name, strings, tuningId, Some(user.id))
						val tuning = Tuning(tuningId,
							tuningName, tuningValues, Some(user.id), instrumentId)

						Ok(Json.obj("tuning" -> tuning, "instrument" -> instrument))

					}.single.future.map { _.get }*/
			}
		}

	def update(id: Long, name: String, strings: Int, defaultTuning: Long) =
		inLogin { user =>
			ifValidated(Instrument.validInput(name, strings)){
				scalarUpdate(Instrument.async.update(id, name, strings, defaultTuning, user))
			}
		}

	def remove(id: Long) = inLogin { user =>
		scalarUpdate(Instrument.async.remove(id, user))
	}

	/*
	def insert(name: String, strings: Int, tuningName: String, tuningValues: String) = 
		inLogin withDB { (user, con) =>
			ifValidated(Instrument.validInput(name, strings) 
					&& Tuning.validInput(tuningName, tuningValues)) {
				
				// Calling stored function
				val row = SQL(
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
	
	*/
}