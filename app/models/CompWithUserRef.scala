package models

import play.api._
import play.api.libs.json.Json
import anorm._


/**
 * Simple type for bounds. Didn't use duck typing since its reflective AFAIK.
 */
trait JsonAble { def toJson: play.api.libs.json.JsValue }

/**
 * Companion with user reference
 * 
 * Contains all generic utility methods for the companion objects of
 * classes which depend on the user model.
 */
trait CompWithUserRef [A <: JsonAble] {
	
	// ----------------
	// Abstract Members
	
	implicit def parser: play.api.libs.json.Writes[A]
	
	def tableName: String
	
	def fromRow(row: SqlRow): A
	
	// ----------------
	// Concrete Members
	
	def toJson(obj: A) = Json.toJson(obj)
	
	def countForUser(user: User)(implicit con: java.sql.Connection) =
		SQL(s"""
			SELECT Count(*) As cnt FROM "$tableName"
			WHERE user_id = {user}
		""")
			.on("table" -> tableName,
					"user" -> user.id)()
			.head[Int]("cnt")
	
	def ofPageForUser(page: Int, user: User)(implicit con: java.sql.Connection) = 
		SQL(s"""
			SELECT * FROM "$tableName"
			WHERE user_id = {user}
			LIMIT {start}, 10
		""").on("user" -> user.id,
				"start" -> (page-1) * 10)()
			.map(fromRow)
	
	def pageAsJson(page: Int, user: User)(implicit con: java.sql.Connection) =
		Json.toJson(ofPageForUser(page, user))
	
	/**
	 * Returns all scales that the user should be able to read.
	 */
	def getAll(implicit con: java.sql.Connection, user: Option[User]) = 
  	user.fold(
  		SQL(s"""
  			SELECT * FROM "$tableName" 
  			WHERE user_id IS NULL
  		""")
  			.on("table" -> tableName)()
  	)(user =>
  		SQL(s"""
  			SELECT * FROM "$tableName" 
  			WHERE user_id = {user} OR user_id IS NULL
  		""")
  			.on("user" -> user.id)()
  	).map(fromRow)
  
  /**
   * Returns only the user defined rows
   */
  def ofUser(user: User)(implicit con: java.sql.Connection) = 
  	SQL(s"""
  		SELECT * FROM "$tableName"
  		WHERE user_id = {user}
  	""")
	  	.on("user" -> user.id)()
	  	.map(fromRow)
}





