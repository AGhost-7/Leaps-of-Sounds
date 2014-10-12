package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json.Json
import models._
import utils._
import scala.concurrent._
import ExecutionContext.Implicits.global
import anorm._
import play.api.db.DB


object Application extends Controller {

  def index = Action.async {
    implicit val con = DB.getConnection()
    // get the async objects
    val scales = Scale.getAll
    val tunings = Tuning.ofInstrument("Guitar")
    val instruments = Instrument.getAll

    // processing is complete when ALL futures
    // have gained their results
    val futures = scales.zip(tunings).zip(instruments)
    futures.map {
      case ((scales, tunings), instruments) =>
        con.close
        Ok(views.html.index(scales, tunings, instruments))
    }
  }

  def index2 = Action.async {
    implicit val con = DB.getConnection()
    // get the async objects
    val scales = Scale.getAll
    val tunings = Tuning.ofInstrument("Guitar")
    val instruments = Instrument.getAll

    // processing is complete when ALL futures
    // have gained their results
    val futures = scales.zip(tunings).zip(instruments)

    futures.map {
      case ((scales, tunings), instruments) =>
        con.close
        Ok(views.html.index(scales, tunings, instruments))
    }
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(
      routes.javascript.Application.getTuningsOfInstrument))
      .as("text/javascript")
  }

  def getTuningsOfInstrument(name: String) = Action.async {
    //	write("Requested tunings for: " + name)
    implicit val con = DB.getConnection()
    Tuning.ofInstrument(name)(con) map { instruments =>
      Ok(Json.toJson(instruments))
    }
  }
}




