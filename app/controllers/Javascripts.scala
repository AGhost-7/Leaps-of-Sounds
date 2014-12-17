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
      routes.javascript.Tunings.ofInstrument,
      routes.javascript.Scales.list,
      routes.javascript.Scales.insert,
      routes.javascript.Scales.remove,
      routes.javascript.Scales.all,
      routes.javascript.Scales.update
      ))
      .as("text/javascript")
  }
	
	// Dont think I'm going to keep this, but for now...
	def remote(library: String) = //Action.async { request =>
		library match {
			case "jquery" => 
				if(Play.isProd) "https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"
				else "//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.js"
			case "handlebars" =>
				if(Play.isProd) "https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars.min.js"
				else "//cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars.js"
			case "bootstrap" => 
				if(Play.isProd) "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"
				else "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.js"
			case _ => library
		}
		
	//	val file = library + (if(Play.isProd) ".min.js" else ".js")
		
//		url + "/" + file
//		val action = Assets.at(url, file)
//		action.apply(request)
//	}
		
	
	
}