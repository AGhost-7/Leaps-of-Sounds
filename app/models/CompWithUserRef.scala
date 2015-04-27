package models

import play.api._
import play.api.libs.json.Json
import anorm._
import java.sql.Connection
import scalikejdbc.WrappedResultSet

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
	
	def fromRow(row: Row): A
	
	def fromRS(rs: scalikejdbc.WrappedResultSet): A
	
	// ----------------
	// Concrete Members
	
	def toJson(obj: A) = Json.toJson(obj)
	
	object async {
		
		import scalikejdbc._
		import scalikejdbc.async._
		import scala.concurrent.Future
		import play.api.libs.concurrent.Execution.Implicits._
		import utils.sql._
		
		private implicit val mapper = fromRS _
		
		def countForUser(user: User)(implicit ses: AsyncDBSession): Future[Int] = 
			SQL(s"""SELECT count(*) As Cnt FROM "$tableName" WHERE user_id = ?""")
				.bind(user.id)
				.map { _.int("Cnt") }
				.single
				.future
				.map { _.get }
				
		def ofPageForUser(page: Int, user: User)(implicit ses: AsyncDBSession): Future[List[A]] =
			SQL(s"""
				SELECT * FROM "$tableName"
				WHERE user_id = ?
				LIMIT ?, 10
			""")
				.bind(user.id, (page - 1) * 10)
				.as[A]
				.list
				.future

		/** Returns all scales that the user should be able to read. */
		def all(implicit ses: AsyncDBSession, userOpt: Option[User]): Future[List[A]] = 
			userOpt.fold { 
				SQL(s"""
					SELECT * FROM "$tableName" 
					WHERE user_id IS NULL
				""")
			} { user => 
				SQL(s"""
					SELECT * FROM "$tableName" 
					WHERE user_id = ? OR user_id IS NULL
				""").bind(user.id)
			}.as[A].list.future

		/** Returns only the user defined rows */
		def allOfUser(user: User)(implicit ses: AsyncDBSession): Future[List[A]] =
			SQL(s"""SELECT * FROM "$tableName" WHERE user_id = ?""")
				.bind(user.id)
				.as[A]
				.list
				.future
	}
	
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
	
	def pageAsJson(page: Int, user: User)(implicit con: Connection) =
		Json.toJson(ofPageForUser(page, user))
	
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
  def ofUser(user: User)(implicit con: Connection) = 
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





