package utils
import play.api.mvc._
import play.api._
import play.api.Play.current
import scala.concurrent._
import ExecutionContext.Implicits.global

package object implicits {
  //val write = utils.Writer("tehlogs.txt")
  /**
   * I want to add some convenience methods to prevent myself from repeating
   * what I'm doing too much.
   */
  implicit class ActionAdHocs(obj: ActionBuilder[Request]) {
    
    def secure(callback: Request[AnyContent] => Future[SimpleResult]) = obj.async { implicit request =>
      val https = request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
      
    /*  write { puts =>
      	puts(s"\n--Request: ${request.host + request.uri} --")
      	puts("Is in production? " + Play.isProd)
      	puts("Is https? " + https)
      }*/
      
      if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
        future {
          Results.Redirect("https://" + request.host + request.uri)
        }
      }
      callback(request)
    }
    
    def secureSync(callback: Request[AnyContent] => SimpleResult) = obj.apply { implicit request =>
      if(Play.isProd && !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")){
        Results.Redirect("https://" + request.host + request.uri)
      }
      callback(request)
    }
  }

}