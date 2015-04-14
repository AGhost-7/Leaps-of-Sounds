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
  
  def fromRow(row: Row) = 
  	Instrument(row[Int]("id"), 
  			row[String]("name"), 
  			row[Int]("strings"), 
  			row[Long]("default_tuning"), 
  			row[Option[Int]]("user_id"))  
  			
  def fromRS(rs: scalikejdbc.WrappedResultSet): Instrument =
  	Instrument(
  			rs.int("id"), 
  			rs.string("name"), 
  			rs.int("strings"), 
  			rs.long("default_tuning"), 
  			rs.intOpt("user_id"))
  
  def validInput(name: String, strings: Int) =
  	strings < 16 && nameConstraint.findFirstIn(name).isDefined
  
  def update(id: Long, name: String, strings: Int, defaultTuning: Long, user: User)(implicit con: Connection) = {
  	
  	val instrument = Instrument.ofId(id)
  	
  	// You should only be able to change the instrument if it is user defined.
  	if(instrument.user.isDefined){
  		// going to need to update all of the tunings if there is a change
  		// to the number of strings on the instrument.
  		if(instrument.strings != strings){
  			
  			val tunings = Tuning
  					.ofInstrument(id)(con, Some(user))
  					.map { _.toValuesOfLength(strings) }
  			
  			val sql = SQL("""
  						UPDATE "tunings" 
  						SET values = {values}
  						WHERE id = {id} 
  					""")
  					
  			tunings.foldLeft(sql.asBatch) { (batch, tuning) => 
  				batch.addBatchParams(tuning.values, tuning.id) 
  			}.execute()
  			
  		}
  		
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
  	} else {
  		// instrument isn't user defined, therefore it remains unchanged. GUI 
  		// shouldn't allow the user to request a change to this either.
  		instrument
  	}
  }  
}