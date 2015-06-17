import play.api.libs.iteratee.{Done, Iteratee}
import play.api.mvc._
import play.api.mvc.Result
import scala.concurrent.ExecutionContext.Implicits.global


object Test extends Controller {
	//import play.api.mvc._
	import scala.concurrent.Future
	implicit class sesExtension(val ses: CookieBaker[Session]) extends AnyVal {
		def find(str: String): Future[Option[String]] = ???
	}
	def HasToken(action: String => EssentialAction): EssentialAction = EssentialAction { requestHeader =>
		val maybeToken = requestHeader.headers.get("session")

		val futureIteratee: Future[Iteratee[Array[Byte], SimpleResult]] = maybeToken map { token =>
			//This returns a future...
Session.find(token).map {
	case Some(session) => action(session)(requestHeader)
	case None => Done[Array[Byte], Result](Unauthorized("Invalid token"))
}.recover {
	case _ =>
		Done[Array[Byte], Result](Unauthorized("400 Error finding Security Token\n"))
}
		} getOrElse {
			Future.successful(Done[Array[Byte], Result](Unauthorized("401 No Security Token\n")))
		}

		Iteratee.flatten(futureIteratee)
	}
}
