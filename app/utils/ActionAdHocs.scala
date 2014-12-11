package utils
import play.api.mvc._
import play.api._
import play.api.Play.current
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * I want to add some convenience methods to prevent myself from repeating
 * what I'm doing too much.
 */
package object implicits {

  
  implicit class ActionAdHocs(obj: ActionBuilder[Request]) {
    
    /**
     * Forces https on heroku. Since the app is async in general the default
     * is async.
     */
    def secure(callback: Request[AnyContent] => Future[SimpleResult]) = obj.async { implicit request =>
      //val https = request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
      println("Are we in production? " + Play.isProd)
    	if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
      	Future.successful(Results.Redirect("https://" + request.host + request.uri))
    	} else {
    		callback(request)
    	}
    }
    
    /**
     * Synchronous version of the "secure" adhoc method
     */
    def secureSync(callback: Request[AnyContent] => SimpleResult) = obj.apply { implicit request =>
    	println("Are we in production? " + Play.isProd)
      if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
        Results.Redirect("https://" + request.host + request.uri)
      } else {
        callback(request)
      }
    }
  }
}