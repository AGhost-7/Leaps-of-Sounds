package models

import play.api.libs.json.Json
import anorm._
import play.api.mvc._
import scala.concurrent.Future

case class Instrument(id: Long, name: String, strings: Int, defaultTuning: Long, user: Option[Int]) extends JsonAble {
  def toJson = Json.toJson(this)(Instrument.jsFormat)
}

object Instrument {
  implicit val jsFormat = Json.format[Instrument]
  
  val tableName = "instruments"
  val nameConstraint = """^[A-z1-9\s()_-]{3,}$""".r
  


	object async extends AsyncCompWithUserRef[Instrument] {
		import scalikejdbc._
		import scalikejdbc.async._

		val tableName = Instrument.tableName

		def fromRS(rs: scalikejdbc.WrappedResultSet): Instrument =
			Instrument(
				rs.int("id"),
				rs.string("name"),
				rs.int("strings"),
				rs.long("default_tuning"),
				rs.intOpt("user_id"))

		def insert(name: String, strings: Int, tuningName: String, tuningValues: String, user: User): Future[(Long, Long)] =
			sql"""
				SELECT * FROM insert_instrument(
		 		$name, $strings, ${user.id}, $tuningName, $tuningValues)
			 	"""
				.map { rs =>
					(rs.long("id_one"), rs.long("id_two"))
				}
				.single
				.future
				.map { _.get }

		def update(id: Long, name: String, strings: Int, defaultTuning: Long, user: User): Future[Int] = {
			// update all of the tunings if needed, values might need to be filled or
			// removed to fit within the size of the instrument
			val updated = for {
				instrument <- Instrument.async.ofId(id)
				if(instrument.get.strings != strings
					&& instrument.get.user == user.id)
				tunings <- Tuning.async.ofInstrument(id)(Some(user))
			} yield {
				val sqlBatch = SQL(
					"""
					UPDATE tunings
					SET values = ?
					WHERE id = ?
					""")

				val updates = for(tuning <- tunings) yield {
					val shrunk = tuning.toValuesOfLength(strings)
					sqlBatch
						.bind(shrunk.values, shrunk.id)
						.update
						.future
				}

				Future.fold(updates)(true){
					(ok, rows) => if(rows == 1 && ok) true else false
				}
			}
			updated.flatMap { ok =>
				sql"""
				UPDATE instruments
		 		SET name = $name, strings = $strings, default_tuning = $defaultTuning
		 		WHERE id = $id AND user_id = ${user.id}
				"""
					.update
					.future
			}

		}
	}
  
  def validInput(name: String, strings: Int) =
  	strings < 16 && nameConstraint.findFirstIn(name).isDefined

}