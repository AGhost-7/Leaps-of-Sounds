package models

case class Instrument(id: Int, name: String, strings: Int) {
  def toJson = Instrument.toJson(this)
}
object Instrument {
  import play.api.libs.json.Json
  import anorm._
  import play.api.db.DB
  import play.api.Play.current
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  implicit val json = Json.writes[Instrument]

  def toJson(ins: Instrument) = Json.toJson(ins)

  def fromRow(row: SqlRow) = Instrument(row[Int]("id"), row[String]("name"), row[Int]("strings"))

  def getAll(implicit con: java.sql.Connection) =
    future {
      SQL("""SELECT * FROM instruments""")()
        .map(fromRow)
        .toList
    }

}