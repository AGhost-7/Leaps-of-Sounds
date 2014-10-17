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

object Application extends Controller {

  def index = Action.async { implicit request =>
    implicit val con = DB.getConnection()
    implicit val iden = Identity.apply
    // get the async objects
    val scales = Scale.getAll
    val tunings = Tuning.ofInstrument("Guitar")
    val instruments = Instrument.getAll
    val messages = FlashMessage.getAll
    // processing is complete when ALL futures
    // have gained their results
    val futures = scales.zip(tunings).zip(instruments)
    futures.map {
      case ((scales, tunings), instruments) =>
        con.close
        Ok(views.html.index(scales, tunings, instruments, messages))
    }
  }

  def index2 = Action.async { implicit request =>
    implicit val con = DB.getConnection()
    implicit val iden = Identity.apply
    // get the async objects
    val scales = Scale.getAll
    val tunings = Tuning.ofInstrument("Guitar")
    val instruments = Instrument.getAll
    val messages = FlashMessage.getAll
    // processing is complete when ALL futures
    // have gained their results
    val futures = scales.zip(tunings).zip(instruments)

    futures.map {
      case ((scales, tunings), instruments) =>
        con.close
        Ok(views.html.index2(scales, tunings, instruments, messages))
    }
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(
      routes.javascript.Application.getTuningsOfInstrument))
      .as("text/javascript")
  }
  
  def getTuningsOfInstrument(name: String) = Action.async { implicit request =>
    implicit val con = DB.getConnection()
    
    Tuning.ofInstrument(name) map { instruments =>
      Ok(Json.toJson(instruments))
    }
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index)
      .withNewSession
      .flashing("infoMsg" -> "You have been logged out.")
  }
  
  
  
  
}




