package models

import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current


case class Instrument(id: Int, name: String, strings: Int, user: Option[Int]) extends JsonAble {
  def toJson = Instrument.toJson(this)
}

object Instrument extends CompWithUserRef[Instrument] {
  implicit val parser = Json.writes[Instrument]
  
  val tableName = "instruments"
  	
  def fromRow(row: SqlRow) = 
  	Instrument(row[Int]("id"), row[String]("name"), row[Int]("strings"), row[Option[Int]]("user_id"))  
}