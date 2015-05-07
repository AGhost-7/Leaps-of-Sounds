

package utils

import play.api.libs.json._

package object pipe {
  implicit class AnyExtensionOps[A](val x: A) extends AnyVal {
    def |>[B](f: A => B): B = f(x)
  }
}

package validation {

  sealed trait ModelValidation {
    def toJson: JsValue
  }

  /** The succesful result from the transaction */
  final case object MSuccess extends ModelValidation {
    def toJson = JsObject(Nil)
  }

  /** Resulting error along with the field name which failed. */
  final case class MFailed[A](message: String, fieldName: String, fieldValue: String)
      extends ModelValidation {
    def toJson = Json.obj(
        "message" -> message,
        "fieldName" -> fieldName,
        "fieldValue" -> fieldValue)
  }

  final case class MGlobalFailed(message: String) extends ModelValidation {
    def toJson = Json.obj("message" -> message)
  }


}
