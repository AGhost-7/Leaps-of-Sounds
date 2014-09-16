package utils

/*
object Test extends App {
	val writer = Writer("this is a file.txt")
	writer.println("Hi")
	
	writer.println { puts =>
		List("one", "two", "three").foreach { number => puts(number) }
	}
}
*/
trait SimplePrintWriter {
	
	import java.io._
	
	def uri: String
	
	private lazy val writer = {
		val file = new java.io.File(uri)
		val parent = file.getParentFile()
		
		if(parent != null) file.getParentFile().mkdirs()
		if(!file.exists())	file.createNewFile()
		
		new PrintWriter(new BufferedWriter(new FileWriter(file, true)))
	}
	
	def print(data: String) = {
		writer.print(data)
		writer.flush
	}
	
	def println(data: String) = {
		writer.println(data)
		writer.flush
	}
	
	def print(func: ((String) => Unit) => Unit) {
		func(s => writer.print(s))
		writer.flush
	}
	
	def println(func: ((String) => Unit) => Unit) {
		func(s => writer.println(s))
		writer.flush
	}
	
	def toBuffer = new LazyBuffer(this)
	
	def apply(data: String) = this.println(data)
	
	def apply(func: ((String) => Unit) => Unit) = this.println(func)
}

case class Writer(dir: String) extends SimplePrintWriter {
	def uri = dir
}
/*
object bufferTest extends App {
	val writer = Writer("hello world.txt")
	val buff = writer.toBuffer
	buff("much")
	buff("wow")
	buff.done
}
 */
sealed class LazyBuffer(lzFile: SimplePrintWriter) {
	private val buf = new StringBuffer
	
	def append(data: Any) = {
		buf.append(data)
		this
	}
	
	def +=(data: Any) = {
		buf.append(data)
		this
	}
	
	def done = {
		lzFile.print(buf.toString)
		lzFile
	}
	
	def apply(data: Any) = append(data)
}