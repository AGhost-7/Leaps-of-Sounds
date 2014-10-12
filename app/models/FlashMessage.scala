package models
import play.api.mvc._
case class FlashMessage (msgClass: String, msg:String){

}
object FlashMessage{
  def getAll (implicit request: Request[AnyContent]) = 
    List("warningMsg", 
        "successMsg",
        "activeMsg",
        "dangerMsg",
        "infoMsg")
      .flatMap { flashVarName => request.flash.get(flashVarName) match {
          case Some(flashValue) => 
            val className = flashVarName.substring(0, flashVarName.length - 3)
            Some(FlashMessage(className, flashValue))
          case None => None
        }
      }
}