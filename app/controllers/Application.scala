package controllers

import scala.concurrent._
import play.api._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import anorm._
import utils.implicits._
import models._
import utils._

/**
 * This routes the primary pages of the application.
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
    
    val instruments = Instrument.getAll
    
    // For now, lets make the guitar the default displaying instrument.
    // Maybe I could make this custimizable eventually.
    val selectedInstrument = instruments.find { _.id == 1 }.get
    
    val tunings = Tuning.ofInstrument(1)
    val defaultTuning = tunings.find { _.id == selectedInstrument.defaultTuning }.get
    
    val scalesGroup = groupByUser(Scale.getAll.toList)
    val tuningsGroup = groupByUser(tunings.toList)
    val instrumentsGroup = groupByUser(instruments.toList)
    
    val messages = FlashMessage.getAll.toList
    
    con.close
    
    Ok(views.html.index(scalesGroup, 
    		tuningsGroup, 
    		instrumentsGroup, 
    		selectedInstrument, 
    		defaultTuning, 
    		messages))
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
  
  def instrumentEditor = Action { implicit request =>
  	User.fromSession.map { implicit user => 
  		DB.withTransaction { implicit con =>
  			
  			// Some of the tunings depend on the default instruments provided,
  			// so we're going to need all instruments which are readable
  			// by the user.
	  		val instruments = Instrument.getAll(con, Some(user))
	  		
	  		// The app is going to use a large json object to display its tunings
	  		// instead.
	  		val tunings = Tuning.ofUser(user)
	  		val grouped = tunings.groupBy { _.instrumentId }
	  		val jsTunings = for((key, values) <- grouped ) yield {
	  			val jsValues = values.map { _.toJson }
	  			("" + key, JsArray(jsValues.toSeq))
	  		}
	  		
	  		Ok(views.html.instrumentsEditor(instruments, Json.toJson(jsTunings)))
  		}
  	}.getOrElse(Unauthorized)
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
  
}




