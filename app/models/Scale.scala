package models

case class Scale(name: String, values: String)
object Scale {
	import play.api.Play.current
	import anorm._
	import play.api.db.DB
	import scala.concurrent._
	import ExecutionContext.Implicits.global
	
	def fromRow(row: anorm.SqlRow) = Scale(row[String]("name"), row[String]("values"))
	
	def getAll(implicit con: java.sql.Connection) = 
		future {
				SQL("SELECT `name`, `values` FROM scales")()
					.map(fromRow)
					.toList
		}
}
