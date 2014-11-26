package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.db.DB
import play.api.Play.current
import anorm._

import models.{Scale, User}

/**
 * Standard CRUD operations for Scales data.
 */
object Scales extends Controller {

	def update(id: Int, name: String, values: String) = Action { implicit request =>
		User.fromSession.map { user =>
			if(Scale.validInput(name, values))
				DB.withConnection { implicit con =>
					Ok(Json.toJson(Scale.update(user, id, name, values)))
				}
			else
				BadRequest
		}.getOrElse(Unauthorized)
	}
	
	def delete(id: Int) = Action { implicit request =>
		User.fromSession.map { user =>
			DB.withConnection { implicit con =>
				SQL("""
					DELETE FROM "scales" 
					WHERE user_id = {user}
						AND id = {id}
				""")
					.on("user" -> user.id, "id" -> id)
					.executeUpdate()
					
				Ok("Removed")
			}
		}.getOrElse(Unauthorized)
		
	}
	
	def add(name: String, values: String) = Action { implicit request =>
  	if(Scale.validInput(name, values))
	  	User.fromSession.map { user => 
	  		DB.withTransaction { implicit con =>
	  			Ok(Json.toJson(Scale.insert(user, name, values)))
	  		}
	  	}.getOrElse(Unauthorized)
  	else
  		BadRequest("Invalid input.")
  }
	
	def list(set: Int) = Action { implicit request =>
		User.fromSession.map { user =>
			DB.withConnection { implicit con => 
  			Ok(Scale.pageAsJson(set, user))
  		}
		}.getOrElse(Unauthorized)
	}
	
	def all = Action { implicit request =>
		implicit val user = User.fromSession
		DB.withConnection { implicit con =>
			Ok(Json.toJson(Scale.getAll))
		}
	}
	
	/*def jsRoutes = Action { implicit request =>
		import routes.javascript._
		Ok(Routes.javascriptRouter("routes")(
      routes.javascript.Scales.list,
      routes.javascript.Scales.add,
      routes.javascript.Scales.delete,
      routes.javascript.Scales.all,
      routes.javascript.Scales.update
		))
	}*/
	
}





















