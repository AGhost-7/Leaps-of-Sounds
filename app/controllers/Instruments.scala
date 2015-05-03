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

}