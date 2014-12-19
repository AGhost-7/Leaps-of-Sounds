package models

import play.api._

import play.api.mvc._
import play.api.db.DB

import anorm._
import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.sql.Connection

import utils._

/**
 * Various classes for the user entity
 */


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

case class LoginUser(username: String, password: String)

case class User(username: String, uuid: String, id: Int) 

object User {

	def authenticate(username: String, password: String)(implicit con: Connection) =
		ofUsername(username) match {
			case result if (result.length == 0) => false
			case result if (!BCrypt.checkpw(password, result.head[String]("password"))) =>
				false
			case result =>
				true
		}
	
	def ofUsername(name: String)(implicit con: Connection) =
		SQL("SELECT * FROM users WHERE username = {username}")
			.on("username" -> name)()
	
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
	
	/**
	 * Generates the session token for the user's account, with appropriate DB
	 * storage. This should only be called AFTER proper authentication.
	 */
	def getToken(username: String)(implicit con: Connection, request: Request[AnyContent]) = {
		val uid = java.util.UUID.randomUUID.toString
		val userId = ofUsername(username).head[Long]("id")
		
		SQL("""SELECT * FROM begin_session({user}::INT,{token}::CHAR(37))""")
			.on("user" -> userId,
					"token" -> uid)()
					
		uid
	}
	
}


