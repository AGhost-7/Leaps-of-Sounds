package models

import play.api.libs.json.Json
import utils.validation._
import utils._

case class Scale(id : Long, name: String, values: String, user: Option[Int])
		extends JsonAble {

	def toJson = Json.toJson(this)(Scale.jsFormat)
}

object Scale {

	//import play.api.Play.current

  import scala.concurrent._
  import play.api.libs.json.Json

  val tableName = "scales"

  val nameConstraint = """^[A-z1-9\s()_-]{3,}$""".r
  val valuesConstraint = "^((1[0-2]|[0-9])([,](1[0-2]|[0-9]))+)$".r

  implicit val jsFormat = Json.format[Scale]



	object async extends AsyncCompWithUserRef[Scale] {

		import scalikejdbc._
		import scalikejdbc.async._

		def tableName = Scale.tableName

		def fromRS(rs: scalikejdbc.WrappedResultSet): Scale =
			Scale(
				rs.long("id"),
				rs.string("name"),
				rs.string("values"),
				rs.intOpt("user_id"))

		def update(id: Long, name: String, values: String, user: User): Future[Int] =
			sql"""
				UPDATE scales
				SET name = $name, values = ${Csv.sort(values)}
				WHERE user_id = ${user.id}
					AND id = $id
				"""
				.update
				.future

		def insert(name: String, values: String, user: User): Future[Long] =
			sql"""
				INSERT INTO scales("name", "values", user_id)
				VALUES ($name, ${Csv.sort(values)}, ${user.id})
				RETURNING id
				"""
				.updateAndReturnGeneratedKey()
				.future

	}

  /** Verifies if name and values is valid. */
  def validInput(name: String, values: String): ModelValidation = {
		if(nameConstraint.findFirstIn(name).isEmpty) {
			MFailed("Name does not follow constraints.", "name", name)
		} else if(valuesConstraint.findFirstIn(values).isEmpty) {
			MFailed("Scale is invalid", "values", values)
		} else {
			val nums = values.split(",").map { _.toInt }
			if(nums.length < 3){
				MFailed("You need at least 3 values in your scale", "values", values)
			} else if(!nums.groupBy(identity).forall { case (v, arr) => arr.length == 1 }) {
				MFailed("You cannot have duplicate values in your scale.", "values", values)
			} else {
				MSuccess
			}
		}
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
