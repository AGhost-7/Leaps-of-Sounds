package controllers

import play.api._
import play.api.mvc._
import play.api.db.DB
import models._
import java.sql.Connection
import play.api.Play.current
import play.api.libs.json.Json

/**
 * Javascript routing related.
 */
object Javascripts extends Controller {
	
	/**
	 * Assets are automatically minified on production.
	 */
	def at(file: String) = 
		if(Play.isProd) controllers.Assets.at("/public/javascripts", file + ".min.js")
		else controllers.Assets.at("/public/javascripts", file + ".js")
		
	
		
	def router = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(
		//	routes.javascript.Instruments.update,
			routes.javascript.Instruments.insert,
			routes.javascript.Instruments.update,
			routes.javascript.Instruments.remove,
      routes.javascript.Tunings.ofInstrument,
			routes.javascript.Tunings.insert,
			routes.javascript.Tunings.remove,
			routes.javascript.Tunings.update,
      routes.javascript.Scales.list,
      routes.javascript.Scales.insert,
      routes.javascript.Scales.remove,
      routes.javascript.Scales.all,
      routes.javascript.Scales.update
      ))
      .as("text/javascript")
  }
	
}