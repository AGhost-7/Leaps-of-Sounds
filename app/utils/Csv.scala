package utils


object Csv {

	def sort(str: String): String =
		str.split(",").map { _.toInt }.sorted.mkString(",")
}
