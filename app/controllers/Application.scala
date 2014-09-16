package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json.Json
import models._
import utils._

object Application extends Controller {
	
	val write = Writer("logger.txt")
	
	def index = Action {
		Ok(views.html.index(
				Scale.getAll, 
				Tuning.ofInstrument("Guitar"),
				Instrument.getAll
				))
	}
	
	def javascriptRoutes = Action { implicit request =>
		import routes.javascript._
		Ok(Routes.javascriptRouter("jsRoutes")(
				routes.javascript.Application.getTuningsOfInstrument 
		))
		.as("text/javascript")
	}
	
	def getTuningsOfInstrument(name: String) = Action {
		write("Requested tunings for: " + name)
		Ok(Json.toJson(Tuning.ofInstrument(name)))
	}
	
}