package models

import play.api._

import play.api.mvc._
import play.api.db.DB

import anorm._
import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import utils._

/**
 * Various classes for the user entity
 */

/**
 * Utility class which discerns the identity of the user based on the session
 * data.
 */

// JFF
/*sealed trait Knowledge[+A]
case class Known[+A](a: A) extends Knowledge[A]
case object Unknown extends Knowledge[Nothing]

class Identity(val id: Future[Knowledge[Int]]) {

}

object Identity {

	def apply(implicit request: Request[AnyContent]) = new Identity(idFromUuid)

	private def idFromUuid(implicit request: Request[AnyContent]): Option[Int]] =
		request.session.get("token") match {
			case Some(token) =>
				future {
					DB.withConnection { implicit con =>
						val result = SQL("""
								SELECT users.id AS user_id, users.username AS user_name 
								FROM users 
								INNER JOIN tokens
									ON tokens.user_id = users.id
								WHERE token ={token}
								""")
							.on("token" -> token)()

						if (result.length == 0) Unknown
						else Known(result.head[Int]("user_id"))
					}
				}
			case None => Future.successful(Unknown)
		}
}*/

case class Registratee(
	username: String,
	password: String,
	password2: String,
	email: String) {

	/**
	 * Returns true if the user is using an email and username which isn't
	 * already taken.
	 */
	def isOriginal =
		DB.withConnection { implicit con =>
			SQL("""
        SELECT COUNT(*) AS Count FROM users 
        WHERE email = {email} OR username = {username}
        """)
				.on("email" -> email, "username" -> username)
				.apply()
				.head[Long]("Count") == 0L
		}

	def persist {
		persist(username, password, email)
	}

	private def persist(username: String, password: String, email: String) {
		val salt = BCrypt.gensalt
		val hashed = BCrypt.hashpw(password, salt)
		DB.withConnection { implicit con =>
			SQL("""
          INSERT INTO users(username, password, email) 
          VALUES ({username}, {password}, {email})
        """)
				.on("username" -> username,
					"password" -> hashed,
					"email" -> email)
				.execute
		}
	}

}

case class LoginUser(username: String, password: String) {
	/**
	 * Generates the session token for the user's account, with appropriate DB
	 * storage.
	 */
	def sessionToken = {
		val uid = java.util.UUID.randomUUID.toString
		DB.withConnection { implicit con =>
			SQL("""
        INSERT INTO tokens
        VALUES ({uid}, (SELECT id FROM users WHERE username={username}))
      """)
				.on("username" -> username,
					"uid" -> uid)
				.execute
		}
		uid
	}
}
case class User(username: String, uuid: String, id: Int)
object User {

	def authenticate(username: String, password: String) =
		DB.withConnection { implicit con =>
			SQL("SELECT password FROM users WHERE username = {username}")
				.on("username" -> username)() match {
					case result if (result.length == 0) => false
					case result if (!BCrypt.checkpw(password, result.head[String]("password"))) =>
						false
					case result =>
						true
				}
		}
	
	def fromSession(implicit request:Request[AnyContent]):Option[User] = {
		request.session.get("token").flatMap { token =>
			DB.withConnection { implicit con =>
				val result = SQL("""
						SELECT * FROM "users"
						INNER JOIN tokens
							ON tokens.user_id = users.id
						WHERE tokens.token = {token}
				""").on("token" -> token)()
				if(result.length == 0) None
				else Some(User(result(0)[String]("username"), token, result(0)[Int]("id")))
			}
		}
	}
}