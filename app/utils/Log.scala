package utils

object log {
  def apply(s: String) = play.api.Logger.logger.info(s, "") 
}