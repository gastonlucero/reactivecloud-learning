package cl.reactivecloud.storage

import cl.reactivecloud.commons.{Login, User}

/**
  * Created by papelacho on 2016-10-24.
  */
class TokenValidator {
  def validate(login:Login):Option[User] = if (login.token.equals("wenaleke")) Option(User("user")) else Option.empty
}
