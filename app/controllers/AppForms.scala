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

object AppForms extends Controller  {

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

	def register = Action { implicit request =>
		Ok(views.html.registration(registrationForm))
	}

	def addUser = Action { implicit request =>
		registrationForm.bindFromRequest.fold(
			hasErrors => {
				BadRequest(views.html.registration(hasErrors))
			},
			registratee => {
				registratee.persist
				Redirect(request.host)
					.flashing("successMsg" -> "You have been successfully registered!")
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

	def login = Action { implicit request =>
		Ok(views.html.login(loginForm))
	}

	def beginSession = Action { implicit request =>
		loginForm.bindFromRequest.fold(
			hasErrors => {
				BadRequest(views.html.login(hasErrors))
			},
			user => {
				val token = DB.withTransaction { implicit con =>
					User.getToken(user.username)
				}
				Redirect("http://" + request.host)
					.withSession("username" -> user.username, 
							"token" -> token)
					.flashing("successMsg" -> "You're now logged in!")
			})
	}
}