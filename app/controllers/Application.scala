package controllers

import scala.concurrent._
import play.api._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import anorm._
import utils.implicits._
import models._
import utils._

/**
 * This routes the primary pages of the application
 */

object Application extends Controller {
	
  def index = Action { implicit request =>
    implicit val con = DB.getConnection()
    implicit val user = User.fromSession
    
    def groupByUser[A <: { def user:Option[Int] }](ls: List[A], some:List[A] = Nil, none:List[A] = Nil): (List[A], List[A]) = ls match {
    	case x :: xs => x.user match {
    		case Some(_) => groupByUser(xs, x :: some, none)
    		case None => groupByUser(xs, some, x :: none)
    	}
    	case _ => (some, none)
    }
    
    val scales = groupByUser(Scale.getAll.toList)
    val tunings = groupByUser(Tuning.ofInstrument("Guitar").toList)
    val instruments = groupByUser(Instrument.getAll.toList)
    val messages = FlashMessage.getAll.toList
    
    con.close
    
    Ok(views.html.index(scales, tunings, instruments, messages))
  }


  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(
      routes.javascript.Application.getTuningsOfInstrument,
      routes.javascript.Scales.list,
      routes.javascript.Scales.add,
      routes.javascript.Scales.remove,
      routes.javascript.Scales.all,
      routes.javascript.Scales.update
      ))
      .as("text/javascript")
  }
  
  def getTuningsOfInstrument(name: String) = Action { implicit request =>
    DB.withTransaction { implicit con => 
	    implicit val user = User.fromSession
	    
	    val tunings = Tuning.ofInstrument(name)
	    
	    Ok(Json.toJson(tunings))
    }
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index)
      .withNewSession
      .flashing("infoMsg" -> "You have been logged out.")
  }
  
  def scaleEditor = Action { implicit request =>
  	DB.withConnection { implicit con =>
	  	User.fromSession.map { user => 
				val scales = Scale.ofUser(user).sortWith { _.name < _.name } 
				Ok(views.html.scaleEditor(scales))
	  	}.getOrElse(Unauthorized)
	  }
  }
  
  def tuningEditor = Action { implicit request =>
  	DB.withTransaction { implicit con =>
  		User.fromSession.map { implicit user =>
  			val instruments = Instrument.getAll(con, Some(user))
  			val tunings = Tuning.ofPageForUser(1, user)
  			
  			Ok(Json.toJson(tunings).toString)
  		}.getOrElse(Unauthorized)
  	}
  }
  
  def scaleOfUser(page: Int) = Action { implicit request =>
  	User.fromSession.map { user =>
  		DB.withConnection { implicit con => 
  			Ok(Scale.pageAsJson(page, user))
  		}
  	}.getOrElse(BadRequest)
  }
  
  def addScale(name: String, values: String) = Action { implicit request =>
  	if(Scale.validInput(name, values))
	  	User.fromSession.map { user => 
	  		DB.withTransaction { implicit con =>
	  			Ok(Json.toJson(Scale.insert(user, name, values)))
	  		}
	  	}.getOrElse(Unauthorized)
  	else
  		BadRequest("Invalid input.")
  }
  
}




