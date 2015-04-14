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

sealed trait MaybeUser

case class User(id: Int, username: String, password: String) extends MaybeUser
case class FailedAuth(error: String) extends MaybeUser



object User {
	
	def fromRS(rs: scalikejdbc.WrappedResultSet): User = 
			User(rs.int("id"), rs.string("username"), rs.string("password"))
			
	object async {
		
		import scalikejdbc._
		import scalikejdbc.async._
		import scala.concurrent.Future
		import utils.sql._
		
		implicit val mapper = (rs: WrappedResultSet) => 
			User(rs.int("id"), rs.string("username"), rs.string("password"))
			
		def ofUsername(name: String)(implicit ses: AsyncDBSession): Future[Option[User]] = 
			sql"SELECT * FROM users WHERE username = $name"
				.as[User]
				.single
				.future
		
		def authenticate(username: String, password: String)(implicit ses: AsyncDBSession): Future[MaybeUser] =
			ofUsername(username).map { 
					case None => FailedAuth("User name not found.")
					case uOpt @ Some(usr @ User(_, _, pw)) if(BCrypt.checkpw(password, pw)) => usr
					case _ => FailedAuth("Password is incorrect.")
				}
				
		def fromSession(implicit ses: AsyncDBSession, req: Request[AnyContent]): Future[Option[User]] = 
			req.session.get("token").map { token =>
				sql"""
					SELECT * FROM users 
					INNER JOIN tokens 
						ON tokens.user_id = users.id 
					WHERE tokens.token = $token
				"""
					.map(fromRS)
					.single
					.future
					
			}.getOrElse(Future.successful(None))
			
		def mkToken(userId: Int)(implicit ses: AsyncDBSession): Future[String] = {
			val uid = java.util.UUID.randomUUID.toString
			sql"SELECT * FROM begin_session($userId::Int, $uid::Char(37))"
					.execute.future.map{ _ => uid }
		}
			
		def rmToken(tkn: String)(implicit ses: AsyncDBSession) : Future[Int] = 
			sql"DELETE FROM tokens WHERE token = $tkn".update.future
		
		def findValidationError(
				name: String, 
				password: String, 
				password2: String,
				email: String)(implicit ses: AsyncDBSession): Future[Option[String]] = {
			ofUsername(name).map {
				case Some(user) => Some("User name is already taken")
				case None => 
					if(name.length < 6) 
						Some("Minimum user name length is 6 characters.")
					else if(password != password2) 
						Some("Passords do not match")
					else if(password.length < 6) 
						Some("Password must have a minimum lenght of 6 characters.")
					else if(!name.matches("[A-z0-9_-]+")) 
						Some("User name contains illegal characters.")
					else if(!email.matches(".+@.+[.].+"))
						Some("Email is invalid.")
					else 
						None
			}
		}
		
		def create(name: String, password: String, email: String)(implicit ses: AsyncDBSession): Future[User] = {
			val update = 
				sql"INSERT INTO users(username, password, email) VALUES ($name, $password, $email)"
					.update.future
			update.map{ i => User(i, name, password) }
		}
		
	}// async end
	
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
				else Some(User(result(0)[Int]("id"), result(0)[String]("username"), result(0)[String]("password")))
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


