package controllers.taxonomy

import models.User
import play.api.mvc.{Request, AnyContent, Action, Result}

import scala.concurrent.Future

/**
 * This class contains utilities for default response pages.
 * I could've overriden the globals, but I want to have a
 * different response(json) for the REST controllers. Action
 * composition also seemed a bit too "global" for my needs.
 */
abstract class AsyncHtmlController extends AsyncController {

	private def notLoggedIn(implicit req:Request[AnyContent]) =
		Future.successful(
			Unauthorized(
				views.html.errorMessage("Unauthorized",
					"Are you sure you're allowed here? You'll want to login to access this page.")))

	def inLogin(f: User => Future[Result]): Action[AnyContent] = Action.async { implicit request =>
		User.async.fromSession.flatMap {
			case None => notLoggedIn
			case Some(user) => f(user)
		}
	}

	def inLogin(f: (User, Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
		User.async.fromSession.flatMap {
			case None => notLoggedIn
				case Some(user) => f(user, request)
		}
	}
}
