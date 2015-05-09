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
		/*if(Play.isProd)*/ controllers.Assets.at("/public/javascripts", file + ".min.js")
		//else controllers.Assets.at("/public/javascripts", file + ".js")
	
	private def withExt(uri: String): String = 
		if(Play.isProd) uri + ".min.js"
		else uri + ".js"
	
	val lib = new {

		val jquery =  withExt("https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery")
		val jqueryui = withExt("https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui")
		val angular = withExt("https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular")
		val bootstrap = withExt("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap")
		val handlebars = withExt("https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars")
			
	}
	
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
      routes.javascript.Scales.update,
      routes.javascript.Users.login,
      routes.javascript.Users.register,
      routes.javascript.Users.logout,
			routes.javascript.Application.indexJson
      ))
      .as("text/javascript")
  }
	
}