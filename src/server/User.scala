package server

import java.io._
import java.net._

/**
 * Represents a user, as viewed from the server. Users should be unique by username.
 * A user is only authenticated by socket connection while online, and once offline
 * the user no longer exists.
 */
class User(userSocket: Socket, val username: String, inFromClient: BufferedReader, outToClient: DataOutputStream) {
  Main.users.add(this)
  
  private var online = true
  //private val inFromClient = new BufferedReader(new InputStreamReader(userSocket.getInputStream()))
  //private val outToClient = new DataOutputStream(userSocket.getOutputStream())
  
  /**
   * Block on recieving a message (as line) from user.
   */
  def receiveLine(): Option[String] = {
    var result: Option[String] = None
    if (online) {
      try {
        result = Some(inFromClient.readLine())
      } catch {
        case _: Throwable => {
          online = false
        }
      }
    }
    result
  }
  
  /**
   * This is the principal method for sending a message back to
   * the user. The standard form of such message is:
   * < sender:::timestamp:::msg >
   */
  def send(msg: String): Unit = {
    if (online) {
      try {
        outToClient.writeBytes(msg + "\n")
      } catch {
        case _: Throwable => {
          online = false
        }
      }
    }
  }
  
  //def ping() = {}
  
  /**
   * Find out if the user is currently considered online by the server.
   */
  def isOnline = online
  
  /**
   * Disconnect the user from the server.
   */
  def disconnect() = {
    Main.users.remove(this)
    send(Main.constructSystemMessage("You have been disconnected."))
    userSocket.close()
    online = false
  }
      
      
}

/**
 * A constructor object for class User. NB! Its apply method returns a user wrapped
 * in an option, only defined if construction was successful.
 */
object User {
  
  /**
   * Constructs a User, wrapped in an option, from the given user socket.
   * A valid user is constructed if and only if a correct connection message
   * is received. This message is of the form:
   * < code:::username:::parameters >
   * and is only valid if the code is correct and the username is not already
   * taken.
   */
  def apply(userSocket: Socket): Option[User] = {
    val inFromClient = new BufferedReader(new InputStreamReader(userSocket.getInputStream(), "UTF-8"))
    val outToClient = new DataOutputStream(userSocket.getOutputStream())
    try {
      val connectionMessage = inFromClient.readLine()
      val messageComponents = connectionMessage.split(":::").map(_.trim)
      if (messageComponents.length < 2) {    //Invalid message
        return None
      } else if (!Main.MESSAGECODES.contains(messageComponents(0))) {    //Invalid version
        outToClient.writeBytes("Invalid message code. The client version might need to be updated.\n")
        return None
      } else {
        val username = messageComponents(1)
        if (Main.users.find(_.username == username).isDefined) {
          outToClient.writeBytes("Username already in use.\n")
          return None
        } else {
          outToClient.writeBytes(":::001\n")      //Connected successfully.
          return Some(new User(userSocket, username, inFromClient, outToClient))
        }
      }
      None
    } catch {
      case _: Throwable => {
        return None
      }
    }
  }
  
}