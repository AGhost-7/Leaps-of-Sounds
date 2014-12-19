package controllers


import play.api.mvc._
import play.api._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json.Json
import models._
import controllers.traits.RestfulController

/**
 * Standard CRUD operations for the Tunings data.
 */
object Tunings extends Controller with RestfulController {
	
	def ofInstrument(instrumentId: Long) = Action { implicit request =>
    DB.withTransaction { implicit con => 
	    implicit val user = User.fromSession
	    
	    val tunings = Tuning.ofInstrument(instrumentId)
	    
	    Ok(Json.toJson(tunings))
    }
  }
	
	def remove(id: Long) = inLogin withDB { (user, con) =>
		Tuning.remove(id, user)(con)
		Ok("{}")
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
		}
	
}



