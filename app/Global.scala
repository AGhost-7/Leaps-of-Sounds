import play.api._
import play.api.mvc._


object Global extends GlobalSettings {
	
	/*
	override def onBadRequest(request: RequestHeader, error: String) =
		BadRequest("foo")

	//500 - internal server error
	override def onError(request: RequestHeader, throwable: Throwable) = {
		InternalServerError(views.html.errors.onError(throwable))
	}

  // 404 - page not found error
  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(views.html.errors.onHandlerNotFound(request))
  }
	*/
}
