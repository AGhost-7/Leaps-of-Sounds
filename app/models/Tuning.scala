package models

case class Tuning(name: String, values: String, instrumentId: Option[Int] = None) {
	def toJson = Tuning.toJson(this)
}
object Tuning {
	import play.api.libs.json.Json
	import anorm._
	import play.api.db.DB
	import play.api.Play.current
	
	implicit val json = Json.writes[Tuning]
	
	def toJson(tun: Tuning) = Json.toJson(tun)
	
	def fromRow(row:SqlRow) = Tuning(row[String]("name"), row[String]("values"))
	
	def ofInstrument(instrument: String) = 
		DB.withConnection { implicit con =>
			SQL("""
				SELECT * FROM tunings 
				WHERE instrument=(
					SELECT id FROM instruments WHERE name={inst}
					)
			""")
			.on("inst" ->instrument)()
				.map(fromRow)
				.toList
		}
	
}