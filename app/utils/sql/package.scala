package utils
import scalikejdbc._

package object sql {
	implicit class sqlMappingAddons(sql: SQL[_, NoExtractor]) {
		// sql"SELECT * FROM users".as[User]
		def as[A](implicit mapper: WrappedResultSet => A) = sql.map(mapper)
	}
}