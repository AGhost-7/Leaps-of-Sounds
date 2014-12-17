package models

import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current
import java.sql.Connection

case class Instrument(id: Long, name: String, strings: Int, defaultTuning: Long, user: Option[Int]) extends JsonAble {
  def toJson = Instrument.toJson(this)
}

object Instrument extends CompWithUserRef[Instrument] {
  implicit val parser = Json.writes[Instrument]
  
  val tableName = "instruments"
  val nameConstraint = """^[A-z1-9\s()_-]{3,}$""".r
  
  def fromRow(row: SqlRow) = 
  	Instrument(row[Int]("id"), 
  			row[String]("name"), 
  			row[Int]("strings"), 
  			row[Long]("default_tuning"), 
  			row[Option[Int]]("user_id"))  
  
  def validInput(name: String, strings: Int) =
  	strings < 16 && nameConstraint.findFirstIn(name).isDefined
  
  def update(id: Long, name: String, strings: Int, defaultTuning: Long, user: User)(implicit con: Connection) = {
  	SQL("""
  		UPDATE "instruments"
  		SET name = {name}, strings = {strings}, default_tuning = {tuning}
  		WHERE id = {id} 
  			AND user_id = {user}
  	""")
  		.on("id" -> id,
  				"name" -> name,
  				"strings" -> strings,
  				"tuning" -> defaultTuning,
  				"user" -> user.id)
  		.executeUpdate
  	Instrument(id, name, strings, defaultTuning, Some(user.id))
  }  
}