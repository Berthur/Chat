package server

/**
 * This class represents a message from client to server.
 * This class processes the message and finds its corresponding room.
 * The standard form of a message is << code:::roomname:::msg >>
 * where 'msg' can be either an actual chat message or a command for
 * the chat room.
 */
class Message(rawString: String, val time: Long, val user: User) {
  private val msgComponents = rawString.split(":::").map(_.trim)
  var isValid = false
  var content = ""
  if (msgComponents.length >= 3 && Main.MESSAGECODES.contains(msgComponents(0))) {
    val roomname = msgComponents(1)
    content = msgComponents.drop(2).mkString(":::")
    val roomOption = Main.rooms.find(_.name == roomname)
    if (roomOption.isDefined) {
      isValid = true
      roomOption.get.addMessage(this)
    } else {
      user.send(":::003")
    }
  }
}