package models

import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class Instrument(id: Int, name: String, strings: Int) {
  def toJson = Instrument.toJson(this)
}
object Instrument {


  implicit val json = Json.writes[Instrument]

  def toJson(ins: Instrument) = Json.toJson(ins)

  def fromRow(row: SqlRow) = Instrument(row[Int]("id"), row[String]("name"), row[Int]("strings"))

  def getAll(implicit con: java.sql.Connection, user: Option[User]) = 
  	user.fold(
  		SQL("SELECT * FROM instruments WHERE user_id IS NULL")()
  	)(user =>
  		SQL("SELECT * FROM instruments WHERE user_id = {user} OR user_id IS NULL")
  			.on("user" -> user.id)()
  	).map(fromRow)
  

}