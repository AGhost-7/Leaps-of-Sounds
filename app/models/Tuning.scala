package models

case class Tuning(
		name: String, 
		values: String, 
		user: Option[Int],
		instrumentId: Int) extends JsonAble {
  def toJson = Tuning.toJson(this)
}
object Tuning extends CompWithUserRef[Tuning] {
  import play.api.libs.json.Json
  import anorm._
  import play.api.mvc._
  import play.api.db.DB

  implicit val parser = Json.writes[Tuning]
  
  val tableName = "tunings"

  def fromRow(row: SqlRow) = 
  	Tuning(row[String]("name"), row[String]("values"), row[Option[Int]]("user_id"), row[Int]("instrument"))
  
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