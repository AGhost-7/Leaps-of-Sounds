package models

import java.sql.Connection
import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import play.api.db.DB
import java.sql.Connection

case class Tuning(
		id: Long,
		name: String, 
		values: String, 
		user: Option[Int],
		instrumentId: Long) extends JsonAble {
  def toJson = Tuning.toJson(this)
}

object Tuning extends CompWithUserRef[Tuning] {
  

  implicit val parser = Json.writes[Tuning]
  
  val tableName = "tunings"

  def fromRow(row: SqlRow) = 
  	Tuning(row[Long]("id"), 
  			row[String]("name"), 
  			row[String]("values"), 
  			row[Option[Int]]("user_id"), 
  			row[Int]("instrument"))
  
  
	val nameConstraint = """^[A-z1-9\s()_-]{3,}$""".r
	val valuesConstraint = """^\d+([,]\d+)+$""".r
	
	def validInput(name: String, values: String) = 
		(nameConstraint.findFirstMatchIn(name).isDefined
				&& valuesConstraint.findFirstMatchIn(values).isDefined)
	
		
	def insert(name: String, values: String, instrumentId: Long, user: User)(implicit con: Connection) = {
		val id = SQL("""
			INSERT INTO "tunings"(name, values, instrument, user_id)
			VALUES ({name}, {values}, {instrument}, {user})
		""")
			.on("name" -> name,
					"values" -> values,
					"instrument" -> instrumentId,
					"user" -> user.id)
			.executeInsert()
		
		Tuning(id.get, name, values, Some(user.id), instrumentId)
	}
	
	def update(id: Long, name: String, values: String, instrumentId: Long, user: User)(implicit con: Connection) = {
		SQL("""
			UPDATE "scales"
			SET name = {name}, values = {values}
			WHERE id = {id} AND user_id = {user}
		""")
			.on("id" -> id,
					"name" -> name,
					"values" -> values,
					"user" -> user.id)
			.executeUpdate
			
		Tuning(id, name, values,Some(user.id), instrumentId)
	}

	def ofInstrument(instrumentId: Long)
  	(implicit con: Connection, 
    		request: Request[AnyContent],
    		user: Option[User]) = 
    		user.fold {
					SQL("""
						SELECT * FROM tunings 
						WHERE instrument = {instrument} 
							AND user_id IS NULL
					""")
						.on("instrument" -> instrumentId)()
				} { user => 
					SQL("""
						SELECT * FROM tunings
						WHERE instrument = {instrument}
							AND (user_id = {user} OR user_id IS NULL)
					""")
						.on("instrument" -> instrumentId,
								"user" -> user.id)()
				}.map(fromRow)
	
}