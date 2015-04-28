

/** Just a rambling on something to propagate error messages while using
	* futures underneath. For comprehensions rule.
	*/



import scala.concurrent.{Future, Promise}
import scala.util.{Success, Failure}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

// Transaction Unit

trait TUnit [+A] {

	protected val underlying: Future[Transaction[A]]

	def map[B](f: A => B): TUnit[B]

	def flatMap[B](f: A => TUnit[B]): TUnit[B]

	def unwrap[B](f: Transaction[A] => B): Future[B]
}

object TUnit {

	def apply[A](ft: Future[Transaction[A]]): TUnit[A] = new TUnit[A] {

		protected val underlying = ft

		def map[B](f: A => B): TUnit[B] = {
			val ft2 = ft.map {
				case TResult(t) => TResult(f(t))
				case err: TError => err
			}
			apply(ft2)
		}

		def flatMap[B](become: A => TUnit[B]): TUnit[B] = {
			val ft2 = ft.flatMap {
				case TResult(t) =>
					become(t).underlying
				case err: TError =>
					Future.successful(err)
			}
			apply(ft2)
		}

		def unwrap[B](f: Transaction[A] => B) : Future[B] = ft.map(f)

	}
}

sealed trait Transaction[+A]
case class TResult[A](a : A) extends Transaction[A]
case class TError(message: String, field: Option[String] = None) extends Transaction[Nothing]
/*

import java.sql.SQLException
object test extends play.api.mvc.Controller {

	def getUserId: TUnit[Int] = {
		// suppose its an async DB query
		val ft = Future.successful(TResult(0))
			.recover{
				case SQLException => TError("User id could not be found.")
			}
		TUnit(ft)
	}

	def mkInstrument(userId: Int): TUnit[Int] = {
		val ft = Future.successful(TResult(0))
		TUnit(ft)
	}

	def incrementUserInstrumentCount: TUnit[Unit] = {
		val ft = Future.successful(TError("You have reached the maximum count"))
		TUnit(ft)
	}

	val result = for {
		userId <- getUserId
		insId <- mkInstrument(userId)
		inc <- incrementUserInstrumentCount
	} yield(insId)

	val response = result.unwrap{
		case TResult(instrumentId) => Ok("")
		case TError(message, _ ) => BadRequest("")
	}
}
*/