package utils

object HtmlRandomizer {
	def randStr = {
		val charList = 'a' to 'z'
		(1 to (Math.random() * 10).toInt + 3)
			.map { x =>  
				charList((Math.random() * 26).toInt)
			}.mkString
	}
}