package controllers.taxonomy

import scalikejdbc.async.AsyncDB


abstract class AsyncController extends play.api.mvc.Controller {

	protected implicit val executionContext =
		play.api.libs.concurrent.Execution.Implicits.defaultContext

	protected implicit def db = AsyncDB.sharedSession


}
