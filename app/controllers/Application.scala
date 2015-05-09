package controllers

import play.api.libs.json.Json

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.Play.current

import models._

import controllers.taxonomy._



/** This routes the primary pages of the application. */
object Application extends AsyncHtmlController {

	protected def groupByUser[A <: { def user:Option[Int] }](
			ls: List[A],
			some:List[A] = Nil,
			none:List[A] = Nil): (List[A], List[A]) = ls match {
		case x :: xs => x.user match {
			case Some(_) => groupByUser(xs, x :: some, none)
			case None => groupByUser(xs, some, x :: none)
		}
		case _ => (some, none)
	}


	def index = Action { implicit request =>
		val messages = FlashMessage.getAll
		Ok(views.html.index(messages))
		/*
		User.async.fromSession.flatMap { implicit userOpt =>



			for {
				instruments <- Instrument.async.all
				scales <- Scale.async.all
				tunings <- Tuning.async.ofInstrument(instruments(0).id)
			} yield {
				val selectedInstrument = instruments(0)
				val defaultTuning = tunings.find { tuning =>
					tuning.id == selectedInstrument.defaultTuning
				}.get

				userOpt.fold {
					// makes no sense to sort if theres no user

				} { user =>
					Ok(views.html.index(
						groupByUser(scales),
						groupByUser(tunings.toList),
						groupByUser(instruments),
						selectedInstrument,
						defaultTuning,
						messages))
				}
			}
		}*/
	}

	def indexJson = Action.async { implicit request =>
		User.async.fromSession.flatMap { implicit userOpt =>
			for {
				instruments <- Instrument.async.all
				scales <- Scale.async.all
				tunings <- Tuning.async.ofInstrument(instruments(0).id)
			} yield {
				Ok(Json.obj("scales" -> scales, "instruments" -> instruments, "tunings" -> tunings))
			}
		}
	}

	def logout = Action { implicit request =>
		Redirect(routes.Application.index)
			.withNewSession
			.flashing("infoMsg" -> "You have been logged out.")
	}

	def scaleEditor = inLogin { (user, request) =>
		Scale.async.allOfUser(user).map { scales: List[Scale] =>
			val sorted = scales.sortBy { _.name }
			Ok(views.html.scaleEditor(sorted)(request.session))
		}
	}

	def instrumentEditor = inLogin { (user, req) =>
		for {
			instruments <- Instrument.async.all(Some(user))
			tunings <- Tuning.async.all(Some(user))
		} yield {
			Ok(views.html.instrumentEditor(instruments, Json.toJson(tunings))(req.session))
		}
	}

}
