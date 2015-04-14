package utils
import scalikejdbc._

package object sql {
	implicit class sqlMappingAddons(sql: SQL[_, NoExtractor]) {
		def as[A](implicit mapper: WrappedResultSet => A) = sql.map(mapper)
	}
}