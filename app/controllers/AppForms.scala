package controllers
// Scala libs
import scala.concurrent._

// Play libs
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.db.DB
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import anorm._

// project imports
import models._
import utils._
import utils.implicits._

object AppForms extends Controller {

	val registrationForm = Form(
		mapping(
			"User name" -> text(minLength = 6),
			"Password" -> text(minLength = 6, maxLength = 30),
			"Password Confirmation" -> text(minLength = 6),
			"Email" -> email)
			(Registratee.apply)(Registratee.unapply)
				.verifying ("Passwords must match.",
					fields => fields.password2 == fields.password)
				// Custom validation: check for duplicates in the database, single query.
				.verifying ("Username or email is already in use.",
					user => user.isOriginal)
	)

	def register = Action.secure { implicit request =>
		future {
			Ok(views.html.registration(registrationForm))
		}
	}

	def addUser = Action.secure { implicit request =>
		registrationForm.bindFromRequest.fold(
			hasErrors => {
				future {
					BadRequest(views.html.registration(hasErrors))
				}
			},
			registratee => {
				future {
					registratee.persist
					Redirect("http://" + request.host)
						.flashing("successMsg" -> "You have been successfully registered!")
				}
			})
	}

	val loginForm = Form(
		mapping(
			"User Name" -> text,
			"Password" -> text)
			(LoginUser.apply)(LoginUser.unapply)
				.verifying ("Your password or username is incorrect.", { fields =>
					DB.withConnection { implicit con => 
						User.authenticate(fields.username, fields.password)
					}
				})
	)

	def login = Action.secureSync { implicit request =>
		Ok(views.html.login(loginForm))
	}

	def beginSession = Action.secure { implicit request =>
		loginForm.bindFromRequest.fold(
			hasErrors => {
				Future.successful(BadRequest(views.html.login(hasErrors)))
			},
			user => {
				future {
					val token = DB.withTransaction { implicit con =>
						User.getToken(user.username)
					}
					Redirect("http://" + request.host)
						.withSession("username" -> user.username, 
								"token" -> token)
						.flashing("successMsg" -> "You're now logged in!")
				}
			})
	}
}