package controllers
// Scala libs
import scala.concurrent._
import ExecutionContext.Implicits.global

// Play libs
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.db.DB
import play.api.data._
import play.api.data.Forms._
import anorm._

// project imports
import models.{User,Registratee}

object AppForms extends Controller {
  
  val registrationForm = Form(
    mapping(
      "User name" -> text(minLength = 6),
      "Password" -> text(minLength = 6, maxLength = 30),
      "Password Confirmation" -> text(minLength = 6),
      "Email" -> email
    )(Registratee.apply)(Registratee.unapply)
      verifying("Passwords must match", 
        fields => fields.password2 == fields.password)
      // Custom validation: check for duplicates in the database, single query.
      verifying("Username or email is already in use.", 
	fields => fields match {
          case user => 
            DB.withConnection { implicit con =>
              SQL("""
                SELECT COUNT(*) AS Count FROM users 
                WHERE email = {email} OR username = {username}
              """)
                .on("email" -> user.email, "username" -> user.username)
                .apply()
                .head[Long]("Count") == 0L
            }
      })
  )
  
  val log  = { s: String => Logger.logger.info(s, "") }
  
  def register = Action.async { 
    future {
      Ok(views.html.registration(registrationForm))
    }
  }
  
  def addUser = Action.async { implicit request =>
    registrationForm.bindFromRequest.fold(
      hasErrors => {
        future {
          BadRequest(views.html.registration(hasErrors))
        }
      },
      registratee => {
        future { 
          User.persistUser(registratee)
          Redirect(routes.Application.index)
        }
      }
    )
  }
  
  
  val loginForm = Form(
    mapping(
      "User Name" -> text,
      "Password" -> text
    )(User.apply)(User.unapply)  
  )
  
  def login = Action.async {
    future {
      Ok(views.html.login(loginForm))
    }
  }
  
  def beingSession = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      hasErrors =>
        future {
          BadRequest(views.html.login(hasErrors))
        },
      user =>
        future {
          Redirect(routes.Application.index)
        }
    )
  }
}