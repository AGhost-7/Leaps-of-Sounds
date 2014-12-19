package controllers.taxonomy

import play.api.mvc._
import models._

/**
 * This class contains utilities for default response pages.
 * I could've overriden the globals, but I want to have a 
 * different response(json) for the REST controllers. Action
 * composition also seemed a bit too "global" for my needs.
 */
abstract class HtmlController extends Controller {
	
	object inLogin {
		
		def apply(func: (Request[AnyContent], User) => Result) = Action { implicit request =>
			User.fromSession.map { user => 
				func(request, user)
			}.getOrElse(Unauthorized(views.html.errorMessage("Unauthorized", 
					"Are you sure you're allowed here? You'll want to login to access this page.")))
		}
		
		def apply(func: models.User => Result):Action[AnyContent] = 
			apply({(_, user) => func(user)})
		
	}
}