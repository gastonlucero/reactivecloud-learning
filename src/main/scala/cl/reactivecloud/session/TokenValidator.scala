package cl.reactivecloud.session

import cl.reactivecloud.commons.Login

/**
  * Created by papelacho on 2016-10-24.
  */
class TokenValidator {
  def validate(login:Login):Option[String] = if (login.token.equals("wenaleke")) Option("user") else Option.empty
}
