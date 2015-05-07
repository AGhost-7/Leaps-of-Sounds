package models

import java.sql.Connection
import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import play.api.db.DB
import java.sql.Connection
import scala.annotation.tailrec

import utils.sql._
import scala.concurrent.Future

case class Tuning(
		id: Long,
		name: String, 
		values: String, 
		user: Option[Int],
		instrumentId: Long) extends JsonAble {

  def toJson = Json.toJson(this)(Tuning.jsFormat)
  
  /** Takes the current Tuning object and creates a new Tuning with a values of
   *  a different length. Optional fill argument will specify what the value will
   *  be if we're increasing the number of strings on the tuning.
   */
  def toValuesOfLength(length: Int, fill: Int = 0) = {
  	@tailrec
  	def resize(values: List[Int], push: Int, accu: List[Int] = Nil): List[Int] = 
  		if(push == 0) accu
  		else values match {
  			// we still have some elements left in the list, so we keep in taking
  			// from it and put it in the new list.
  			case v :: ls => resize(ls, push - 1, accu :+ v)
  			// There are no more elements left in the list, so we use the filler
  			// value instead.
  			case _ => resize(Nil, push - 1, accu :+ fill)
  		}
  	
  	val vals = values.split(",").map { _.toInt }.toList
  	
  	val newVal = resize(vals, length)
  	
  	Tuning(id, name, newVal.mkString(","), user, instrumentId)
  }
}

object Tuning extends { self =>
  
  implicit val jsFormat = Json.format[Tuning]


  
  val tableName = "tunings"


  			


	object async extends AsyncCompWithUserRef[Tuning] {

		import scalikejdbc._
		import scalikejdbc.async._

		def tableName = self.tableName

		def fromRS(rs: scalikejdbc.WrappedResultSet): Tuning =
			Tuning(
				rs.long("id"),
				rs.string("name"),
				rs.string("values"),
				rs.intOpt("user_id"),
				rs.int("instrument"))

		def ofInstrument(instrument: Long)(implicit userOpt: Option[User]): Future[Seq[Tuning]] =
			userOpt.fold {
				sql"""
					SELECT * FROM tunings
					WHERE instrument = ${instrument}
		 				AND user_id IS NULL
					"""
			} { user =>
				sql"""
					SELECT * FROM tunings
		 			WHERE instrument = ${instrument}
					AND (user_id IS NULL OR user_id = ${user.id})
		 		"""
			}.as[Tuning].list.future()(db)


		def insert(name: String, values: String, instrumentId: Long, user: User): Future[Long] =
			sql"""
				INSERT INTO tunings(name, values, instrument)
				VALUES ($name, $values, $instrumentId)
				RETURNING id
			"""
				.updateAndReturnGeneratedKey()
				.future

		def update(id: Long, name: String, values: String, instrumentId: Long, user: User): Future[Int] =
			sql"""
					UPDATE tunings
					SET name = $name, values = $values
					WHERE user_id = ${user.id} AND id = $id
				"""
				.update()
				.future

	}
  
	val nameConstraint = """^[A-z1-9\s()_-]{3,}$""".r
	val valuesConstraint = """^\d+([,]\d+)+$""".r
	
	def validInput(name: String, values: String) = 
		(nameConstraint.findFirstMatchIn(name).isDefined
				&& valuesConstraint.findFirstMatchIn(values).isDefined)
	

	
}