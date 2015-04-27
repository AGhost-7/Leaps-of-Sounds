package controllers
// Scala libs
import scala.concurrent.Future

// Play libs
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.db.DB
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


import models._
import utils._


object AppForms extends Controller  {

	val registrationForm = Form(
		mapping(
			"User name" -> text(minLength = 6),
			"Password" -> text(minLength = 6),
			"Password Confirmation" -> text(minLength = 6),
			"Email" -> email)
			(Registratee.apply)(Registratee.unapply)
				.verifying ("Passwords must match.",
					fields => fields.password2 == fields.password)
	)

	def register = Action { implicit request =>
		Ok(views.html.registration(registrationForm))
	}

	def addUser = Action.async { implicit request =>
		registrationForm.bindFromRequest.fold[Future[Result]](
			hasErrors => {
				val res = BadRequest(views.html.registration(hasErrors))
				Future.successful(res)
			},
			registratee => {
				def errorForm(field: String, msg: String) = {
					val formWithErr = registrationForm
						.fill(registratee)
						.withError(FormError(field, msg))
					val res = BadRequest(views.html.registration(formWithErr))
					Future.successful((res))
				}
				User.async.notUnique(registratee.username, registratee.email).flatMap {
					case Some("username") =>
						errorForm("User name", "User name is already taken")
					case Some("email") =>
						errorForm("email", "Email is already in use")
					case None =>
						for {
							u <- User.async.create(
									registratee.username,
									registratee.password,
									registratee.email)
							tkn <- User.async.mkToken(u.id)
						} yield {
							Redirect("http://" + request.host)
								.withNewSession
								.flashing("successMsg" -> "You have been successfully registered!")
								.withSession("username" -> registratee.username,
									"token" -> tkn)
						}
				}
			})
	}

	val loginForm = Form(
		mapping(
			"User Name" -> text(minLength = 6),
			"Password" -> text(minLength = 6))
			(LoginUser.apply)(LoginUser.unapply)
	)

	def login = Action { implicit request =>
		Ok(views.html.login(loginForm))
	}

	def beginSession = Action.async { implicit request =>
		loginForm.bindFromRequest.fold(
			hasErrors => {
				val res = BadRequest(views.html.login(hasErrors))
				Future.successful(res)
			},
			user => {
				User.async.authenticate(user.username, user.password).flatMap {
					case User(id, _, _) =>
						User.async.mkToken(id).map { token =>
							Redirect("http://" + request.host)
								.withSession("username" -> user.username,
									"token" -> token)
								.flashing("successMsg" -> "You're now logged in!")
						}
					case FailedAuth(err) =>
						val frmFilled = loginForm
							.fill(user)
							.withGlobalError("Authentication failed.")
						val res = BadRequest(views.html.login(frmFilled))
						Future.successful((res))
				}
			})
	}
}