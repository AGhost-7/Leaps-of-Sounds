package models

case class Tuning(name: String, values: String, instrumentId: Option[Int] = None) {
  def toJson = Tuning.toJson(this)
}
object Tuning {
  import play.api.libs.json.Json
  import anorm._
  import play.api.mvc._
  import play.api.db.DB
  import play.api.Play.current
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  implicit val json = Json.writes[Tuning]

  def toJson(tun: Tuning) = Json.toJson(tun)

  def fromRow(row: SqlRow) = Tuning(row[String]("name"), row[String]("values"))

  def ofInstrument(instrument: String)
    (implicit con: java.sql.Connection, 
    		request: Request[AnyContent],
    		user: Option[User]) =
    	user.fold(
    		SQL("""
	        SELECT * FROM tunings 
    			WHERE instrument=(
	      		SELECT id FROM instruments WHERE name={inst}
    			)
	      """)
        	.on("inst" -> instrument)()
    		)(user =>
    			SQL("""
		        SELECT * FROM tunings 
    				WHERE instrument=(
		      		SELECT id FROM instruments WHERE name={inst}
    				)
    					AND (
    						user_id IS NULL
    							OR user_id = {user}
    					)
		      """)
	        	.on("inst" -> instrument,
	        			"user" -> user.id
	        	)()
    		).map(fromRow)
    
}