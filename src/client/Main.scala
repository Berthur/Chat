package client

import java.io._
import java.net._

/**
 * The main object for the client.
 */
object Main {
  
  val ADDRESS = "localhost"    // TODO: Fill IP in here. Could be added as a launch parameter in the future.
  val PORT = 1337              // TODO: Change this accordingly. Could be added as a launch parameter in the future.
  val MESSAGECODE = "JCHATV1"
  var currentRoom: Option[String] = None
  var username = ""
  var isOnline = false
  
  def main(args: Array[String]) = {
    
    println("======= Welcome to this humble chat app. =======")
    println()
    while (username.isEmpty) {
      println("Please enter your username:")
      val input = scala.io.StdIn.readLine("> ").trim
      var valid = true
      if (input.length < 3) println("Username too short."); valid = false
      if (input.length >= 64) println("Username too long."); valid = false
      if (input.find(!_.isLetterOrDigit).isDefined) println("Username may only contain (valid) letters and digits."); valid = false
      if (valid) println("Welcome, " + input + "!"); username = input
    }
    println("_____________")
    //println("Would you like to connect to the default server? [Y/N]")
    //val input = scala.io.StdIn.readLine().trim.toLowerCase
    //if (input == "y") {
    println("Attempting to connect to server...")
    try {
      val clientSocket = new Socket(ADDRESS, PORT)
      val outToServer = new DataOutputStream(clientSocket.getOutputStream())
      val inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"))
      println("Server found...")
      val listenerThread = new Thread(new Runnable {
        def run() = startListener(inFromServer)
      })
      listenerThread.start()
      startPrompt(outToServer)
    } catch {
      case e: Throwable => {
        println("Connection failed. Error: " + e.getMessage())
      }
    }
    //}
    println("Exiting.")
  }
  
  def startPrompt(outToServer: DataOutputStream) = {
    while (true) {
      println("=====")          //Display helpful info.
      
      //Connecting:
      while (!isOnline) {
        //val input = scala.io.StdIn.readLine("Join chat? [Y/N]").trim.toLowerCase
        //if (input == "y") {
        outToServer.writeBytes(MESSAGECODE + ":::" + username + ":::\n")
        Thread.sleep(500)
        //}
        //else System.exit(0)
      }
      
      //
      println("In order to start chatting, you need to join a chat room.")
      while (isOnline && !currentRoom.isDefined) {
        //TODO: Request list of chat rooms.
        println("Please enter the name of the chat room you would like to join:")
        val roomName = scala.io.StdIn.readLine("> ")
        println("Room password:")
        val roomPw = scala.io.StdIn.readLine("> ")
        if (roomName.isEmpty || roomPw.isEmpty || (roomName+"."+roomPw).contains(" ") || (roomName+"."+roomPw).contains(":::")) {
          println("Invalid room name or password.")
        } else {
          outToServer.writeBytes(MESSAGECODE + ":::" + roomName + ":::/join " + roomName + " " + roomPw + "\n")
        }
        Thread.sleep(1000)
      }
      
      println("======= |" + currentRoom.get + "| =======")
      while (isOnline && currentRoom.isDefined) {
        val input = scala.io.StdIn.readLine().trim
        if (input.isEmpty) {}
        else if (input.contains(":::")) println("Invalid text.")
        else {
          val wasCommand = executeCommand(input)
          if (!wasCommand) {
            outToServer.writeBytes(MESSAGECODE + ":::" + currentRoom.get + ":::" + input + "\n")
          }
        }
      }
    }
  }
  
  def startListener(inFromServer: BufferedReader) = {
    while (true) {
      val in = inFromServer.readLine().trim
      if (in == ":::") println("[unknown response from server]")
      else if (in.startsWith(":::")) {
        in.split(":::")(1) match {
          case "001" => {
            if (!isOnline) {
              isOnline = true
              println("Connected successfully.")
            }
          }
          case "002" => {
            val roomName = in.split(":::")(2)
            currentRoom = Some(roomName)
            println("Successfully joined room " + roomName + ".")
          }
          case "003" => {
            println("No room with such name exists.")
          }
          case "004" => {    //General error
            //do nothing
          }
          case "005" => {    //Exited room
            val roomName = in.split(":::")(2)
            currentRoom = None
            println("You have exited room " + roomName + ".")
          }
          case _ => {
            println("[unknown response from server]")
          }
        }
      }
      else {
        val msgComponents = in.split(":::")
        if (msgComponents.length < 2) println("[unknown response from server]")
        else {
          val timeTag = msgComponents(0)
          val sender = msgComponents(1)
          val message = if (msgComponents.length >= 3) msgComponents(2) else ""
          println("[" + timeTag + " " + sender + "] " + message)
        }
      }
      //println("--" + in + "--")    //Debugging
    }
  }
  
  def executeCommand(input: String): Boolean = {
    //TODO: Intercept commands.
    false
  }
  
}