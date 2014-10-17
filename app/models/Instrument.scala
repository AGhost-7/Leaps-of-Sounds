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

  def getAll(implicit con: java.sql.Connection, identity: Identity) =
    identity.id.flatMap { id =>
      id match {
      	case Known(value) =>
          future {
            SQL("SELECT * FROM instruments WHERE user_id = {user} OR IS NULL")
              .on("user" -> value)()
              .map(fromRow)
          }
      	case Unknown =>
      	  future {
            SQL("""SELECT * FROM instruments""")()
            .map(fromRow)
          }
      }
      
    }	
    

}