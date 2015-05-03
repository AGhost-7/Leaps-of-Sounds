package models

import play.api.libs.json.Json
import anorm._
import java.sql.Connection


/** Simple type for bounds. */
trait JsonAble {
	def toJson: play.api.libs.json.JsValue
}


/** Companion with user reference
 * 
 *  Contains all generic utility methods for the companion objects of
 *  classes which depend on the user model.
 */
trait CompWithUserRef [A <: JsonAble] {
	
	def tableName: String
	
	def fromRow(row: Row): A

	def countForUser(user: User)(implicit con: Connection) =
		SQL(s"""
			SELECT Count(*) As cnt FROM "$tableName"
			WHERE user_id = {user}
		""")
			.on("table" -> tableName,
					"user" -> user.id)()
			.head[Int]("cnt")
	
	def ofPageForUser(page: Int, user: User)(implicit con: Connection) = 
		SQL(s"""
			SELECT * FROM "$tableName"
			WHERE user_id = {user}
			LIMIT {start}, 10
		""").on("user" -> user.id,
				"start" -> (page-1) * 10)()
			.map(fromRow)
	
	//def pageAsJson(page: Int, user: User)(implicit con: Connection) =
	//	Json.toJson(ofPageForUser(page, user))
	
	/** Returns all scales that the user should be able to read. */
	def getAll(implicit con: Connection, user: Option[User]) = 
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
  
  /** Returns only the user defined rows */
  def ofUser(user: User)(implicit con: Connection): Stream[A] =
  	SQL(s"""
  		SELECT * FROM "$tableName"
  		WHERE user_id = {user}
  	""")
	  	.on("user" -> user.id)()
	  	.map(fromRow)
	 
	 /** Generic removal method for single entry. */
	 def remove(id: Long, user: User)(implicit con: Connection) =
		 SQL(s"""
				DELETE FROM "$tableName"
				WHERE id = {id}
					AND user_id = {user}
		 """)
		 	.on("id" -> id,
		 			"user" -> user.id)
		 	.executeUpdate
	
	/** Generic Scalar SQL select. */
	def ofId(id: Long)(implicit con: Connection) = 
		SQL(s"""
			SELECT * FROM "$tableName"
			WHERE id = {id}
		""")
			.on("id" -> id)()
			.map(fromRow)
			.head
}





