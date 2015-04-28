package models


import scalikejdbc._
import scalikejdbc.async._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import utils.sql._

/** Asynchronous Model Companion Object
	*
  * Base structure of companion objects used for the models.
	*/
trait AsyncModelComp [A <: JsonAble]{

	def fromRS(rs: scalikejdbc.WrappedResultSet): A

	def tableName: String

	type Con = AsyncDBSession

	protected def db = AsyncDB.sharedSession

	protected implicit val mapper = fromRS _
}

/** Asynchronous Model's Companion Object with User Reference
	*
  * Some generic queries to use across multiple models.
	*/
trait AsyncCompWithUserRef [A <: JsonAble] extends AsyncModelComp[A] {

	def countForUser(user: User)(implicit ses: Con = db): Future[Int] =
		SQL(s"""SELECT count(*) As Cnt FROM "$tableName" WHERE user_id = ?""")
			.bind(user.id)
			.map { _.int("Cnt") }
			.single
			.future
			.map { _.get }

	def ofPageForUser(page: Int, user: User)(implicit ses: Con = db): Future[List[A]] =
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
	def all(implicit userOpt: Option[User], ses: Con = db): Future[List[A]] =
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
	def allOfUser(user: User)(implicit ses: Con = db): Future[List[A]] =
		SQL(s"""SELECT * FROM "$tableName" WHERE user_id = ?""")
			.bind(user.id)
			.as[A]
			.list
			.future

	def ofId(id: Long)(implicit ses: Con = db): Future[Option[A]] =
		SQL(s"""SELECT * FROM "$tableName" WHERE id = ?""")
			.bind(id)
			.as[A]
			.single
			.future

	/** Removes the user's entry. */
	def remove(id: Long, user: User)(implicit ses: Con = db): Future[Int] =
		SQL(s"""DELETE FROM "$tableName" WHERE id = ? AND user_id = ?""")
			.bind(id, user.id)
			.update
			.future
}