package server

class Room(var name: String, private var password: String) {
  
  private val users = collection.mutable.Set[User]()
  private val messages = collection.mutable.Buffer[Message]()
  
  def addMessage(message: Message) = {
    val msg = message.content
    val user = message.user
    if (message.isValid && msg.startsWith("/") && msg.trim.length >= 2) {    //Command
      val components = msg.tail.split(" ")
      val command = components(0)
      val arguments = components.tail.mkString(" ")
      command match {
        case "join" => {          // /join roomname password
          if (users.contains(message.user)) {
            user.send(Main.constructSystemMessage("Already in room " + name + "!"))
          } else {
            val argumentList = arguments.split(" ").filter(!_.isEmpty)
            if (argumentList.length < 2) {
              user.send(Main.constructSystemMessage("Too few arguments! '/join roomname password'"))
            } else if (argumentList(0) != name) {
              user.send(Main.constructSystemMessage("Invalid roomname '" + argumentList(0) + "'."))
            } else {
              addUser(message.user, argumentList(1))
            }
          }
        }
        case "exit" => {        // /exit
          if (users.contains(message.user)) {
            user.send(Main.constructSystemMessage(":::005" + name))    //Exited room.
            removeUser(message.user)
          } else {
            user.send(Main.constructSystemMessage("Not in room " + name + "!"))
          }
        }
        case other: String => {
          user.send(Main.constructSystemMessage("Unknown command: '" + other + "'."))
        }
      }
    } else if (message.isValid && users.contains(message.user)) {
      messages += message
      val encodedMessage = Main.constructMessage(msg, user.username)
      users.foreach(_.send(encodedMessage))
    }
  }
  
  def announce(msg: String) = users.foreach(_.send(Main.constructMessage(msg, "Chatroom")))
  
  /**
   * Attempts to add a user to the chat room. If the password is correct,
   * this is done.
   */
  def addUser(user: User, passwordAttempt: String): Boolean = {
    if (passwordAttempt == password) {
      user.send(":::002:::" + name)    //Successfully joined room.
      users += user
      announce(user.username + " joined room.")
      true
    } else {
      user.send(Main.constructSystemMessage("Incorrect password for room " + name + "."))
      false
    }
  }
  
  /**
   * Removes a user from the room, if the user was in the room to begin with.
   */
  def removeUser(user: User) = {
    if (users.contains(user)) {
      users.remove(user)
      announce(user.username + " left room.")
    }
  }
  
  def changeName(newName: String) = {
    //TODO: Inform clients name was changed
    name = newName
    announce("Chatroom name was changed to " + newName + ".")
  }
  
  /**
   * Changes the room's password and kicks every user.
   * If the know the new password, they can rejoin the room.
   */
  def changePassword(newPassword: String) = {
    users.foreach(removeUser(_))
    password = newPassword
  }
  
  /**
   * Makes necessary preparations before the room is removed.
   */
  def prepareForRemoval() = {
    announce("The room was removed. You must join a new chat room if you want to keep chatting.")
  }
  
}

object Room {
  
}