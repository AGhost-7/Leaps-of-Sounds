package models

case class Scale(id : Long,name: String, values: String, user: Option[Int]) extends JsonAble {
	def toJson = Scale.toJson(this)
}

object Scale extends CompWithUserRef[Scale] {

	import play.api.Play.current
  import anorm._
  import play.api.db.DB
  import scala.concurrent._
  import play.api.libs.json.Json
  
  val tableName = "scales"
  	
  val nameConstraint = """^[A-z1-9\s]{4,}$""".r
  val valuesConstraint = "^((1[0-2]|[0-9])([,](1[0-2]|[0-9]))+)$".r
  
  implicit val parser = Json.writes[Scale]
  
  def fromRow(row: anorm.SqlRow) = Scale(row[Long]("id"), row[String]("name"), row[String]("values"), row[Option[Int]]("user_id"))
  
  /**
   * Verifies if name and values is valid.
   */
  def validInput(name: String, values: String) = {
		
		/*def checkValuesOrder(arr: Array[Int], i: Int = 1): Boolean = {
			if(arr(i - 1) < arr(1)) 
				if(i < arr.length - 1) checkValuesOrder(arr, i + 1)
				else true
			else false
		}
			
		println(checkValuesOrder(values.split(",").map { _.toInt }))*/
		
  	if(nameConstraint.findFirstIn(name) != None
  			&& valuesConstraint.findFirstIn(values) != None) 
  		values
				.split(",")
				.map { _.toInt }
				.toSeq
				.foldLeft((0, true)) { case ((last, valid), e) => 
					if(!valid) (e, false)  
					else if(last < e) (e, true)
					else (e, false)
				}._2
  	else false
	}
	
	/**
	 * Takes care of only inserting the new entry, and returns a Scale Object.
	 */
  def insert(user: User, name: String, values: String)(implicit con: java.sql.Connection) = {
		val id = SQL("""
			INSERT INTO "scales"(name, values, user_id)
			VALUES({name}, {values}, {user})
		""")
			.on("name" -> name,
					"values" -> values,
					"user" -> user.id)
			.executeInsert()
					
		Scale(id.get, name, values, Some(user.id))
	}
  
  def update(user: User, id: Int, name: String, values: String)(implicit con: java.sql.Connection) = {
  	val n = SQL("""
  			UPDATE "scales"
  			SET "name" = {name}, "values" = {values}
  			WHERE user_id = {user}
  	""")
  		.on("user" -> user.id,
  				"name" -> name,
  				"values" -> values)
  		.executeUpdate
  	println(n)
  	
  	Scale(id, name, values, Some(user.id))
  }
  
  val intervals = Map(
		  "1" -> "1",
			"2" -> "2b",
			"3" -> "2",
			"4" -> "b3",
			"5" -> "3",
			"6" -> "4",
			"7" -> "b5",
			"8" -> "5",
			"9" -> "b6",
			"10" -> "6",
			"11" -> "b7",
			"12" -> "7"		
		 )
	
	val jsIntervals = Json.toJson(intervals).toString
	
	
}
