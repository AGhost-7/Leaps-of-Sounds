package models

import play.api._

import play.api.mvc._
import play.api.db.DB

import anorm._
import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt

import utils._

case class User(username: String, password: String)
object User {
  
  def persistUser(reg: Registratee) {
    persistUser(reg.username, reg.password, reg.email)
  }
  
  def persistUser(username: String, password: String, email: String) = {
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