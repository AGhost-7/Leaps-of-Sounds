package utils
import play.api.mvc._
import play.api._
import play.api.Play.current
import scala.concurrent._
import ExecutionContext.Implicits.global

package object implicits {

  /**
   * I want to add some convenience methods to prevent myself from repeating
   * what I'm doing too much.
   */
  implicit class ActionAdHocs(obj: ActionBuilder[Request]) {
    
    /**
     * Forces https on heroku. Since the app is async in general the default
     * is async.
     */
    def secure(callback: Request[AnyContent] => Future[SimpleResult]) = obj.async { implicit request =>
      val https = request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
      
      if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
        future {
          Results.Redirect("https://" + request.host + request.uri)
        }
      } else {
	callback(request)
      }
    }
    
    /**
     * Synchronous version of the "secure" adhoc method
     */
    def secureSync(callback: Request[AnyContent] => SimpleResult) = obj.apply { implicit request =>
      if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
        Results.Redirect("https://" + request.host + request.uri)
      } else {
        callback(request)
      }
    }
  }

}