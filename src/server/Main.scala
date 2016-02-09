package server

/**
 * The main object for the server side application.
 */
object Main {
  
  val PORT = 1337          // TODO: Change this accordingly. Could be added as a launch parameter in the future.
  val MESSAGECODES = Array("JCHATV1")        // Valid initial codes
  
  val rooms = collection.mutable.Set[Room]()
  val users = collection.mutable.Set[User]()
  
  /**
   * Encodes the data into the message string.
   */
  def constructMessage(rawMsg: String, username: String) = {
    val msg = rawMsg.filter(c => !Array('\n').contains(c)).split(":::")(0)
    val time = java.time.LocalTime.now()
    //val time = System.currentTimeMillis()
    //val timestamp = (time / 3600000) + ":" + (time / 60000) + ":" + (time / 1000)
    val timestamp = time.toString.split('.')(0)
    timestamp + ":::" + username + ":::" + msg
  }
  
  /**
   * Constructs the message string of a system message.
   */
  def constructSystemMessage(msg: String) = constructMessage(msg, "System")
  
  /**
   * The serverside programs's main function.
   */
  def main(args: Array[String]) = {
    val listenerThread = new Thread(new Runnable {
      def run() = {
        new Listener(PORT)
      }
    })
    listenerThread.start()
    
    //Server console:
    while (true) {
      val rawInput = scala.io.StdIn.readLine()
      val input = rawInput.split(' ').filter(!_.isEmpty)
      if (input.isEmpty || rawInput.contains(":::")) println("Invalid command.")
      else {
        val command = input(0)
        command match {
          case "shutdown" => {                                        // <shutdown>
            println("Shutting down.")
            //TODO: Inform all users server is going down.
            System.exit(0)
          }
          case "rooms" => {                                           // <rooms>
            println("     ===== CHAT ROOMS: =====")
            if (rooms.isEmpty) {
              println("   |There are no chat rooms right now.|")
            } else {
              for (room <- rooms) {
                println("  " + room.name)
              }
            }
          }
          case "addroom" => {                                         // <addroom roomname roompw>
            if (input.length < 3) println("Too few arguments for command <addroom roomname roompw>.")
            else {
              val roomname = input(1)
              val roompw = input(2)
              if (rooms.find(_.name == roomname).isDefined) {
                println("Room " + roomname + " already exists!")
              } else {
                rooms.add(new Room(roomname, roompw))
                println("Room " + roomname + " added successfully.")
              }
            }
          }
          case "removeroom" => {                                      // <removeroom roomname>
            if (input.length < 2) println("Too few arguments for command <removeroom roomname>.")
            else {
              val roomname = input(1)
              val roomOption = rooms.find(_.name == roomname)
              if (roomOption.isDefined) {
                println("Are you sure you wish to permanently remove room " + roomname + "? [Y/N]")
                val response = scala.io.StdIn.readLine().trim.toLowerCase
                if (response == "y") {
                  roomOption.get.prepareForRemoval()
                  rooms.remove(roomOption.get)
                  println("Room " + roomname + " was removed successfully.")
                } else {
                  println("Request cancelled.")
                }
              } else {
                println("Room " + roomname + " does not exist!")
              }
            }
          }
          case _ => {
            println("Unknown command '" + command + "'.")
          }
        }
      }
    }
     //JCHATV1:::burt:::lol
  }
}