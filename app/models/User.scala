package models

import play.api._

import play.api.mvc._
import play.api.db.DB

import anorm._
import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent._
import ExecutionContext.Implicits.global

import utils._

case class User(username: String, password: String) {
  
  /**Generates the session token for the user's account, with appropriate DB
   * storage.
   */
  def sessionToken = {
    val uid = java.util.UUID.randomUUID.toString
    DB.withConnection{ implicit con =>
      SQL("""
        INSERT INTO tokens
        VALUES({uid},(SELECT id FROM users WHERE username={username}))
      """)
        .on("username" -> username,
        "uid" -> uid)
        .execute
    }
    uid
  }
}
object User {
  
  def persistUser(reg: Registratee) {
    persistUser(reg.username, reg.password, reg.email)
  }
  
  def persistUser(username: String, password: String, email: String) = {
    val salt = BCrypt.gensalt
    val hashed = BCrypt.hashpw(password, salt)
    future {
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
  
  def authenticate(username: String, password: String) = 
      DB.withConnection { implicit con =>
        SQL("""SELECT password FROM users WHERE username = {username}""")
          .on("username" -> username)() match {
          case result if(result.length == 0) => false
          case result if(!BCrypt.checkpw(password, result.head[String]("password"))) =>
            false
          case result =>
            true
        }
      }
  
  
  
}