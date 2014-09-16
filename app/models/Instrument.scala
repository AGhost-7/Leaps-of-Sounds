package models

case class Instrument(id: Int, name: String, strings: Int) {
	def toJson = Instrument.toJson(this)
}
object Instrument {
	import play.api.libs.json.Json
	import anorm._
	import play.api.db.DB
	import play.api.Play.current
	
	implicit val json = Json.writes[Instrument]
	
	def toJson(ins: Instrument) = Json.toJson(ins)
	
	def fromRow(row:SqlRow) = Instrument(row[Int]("id"), row[String]("name"), row[Int]("strings"))
	
	def getAll = 
		DB.withConnection { implicit con =>
			SQL("""SELECT * FROM instruments""")().map(fromRow).toList
		}
	
}