package server

import java.io._
import java.net._

class Listener(port: Int) {
  
  //val userThreads = collection.mutable.Set[Thread]()
  
  val welcomeSocket = new ServerSocket(port)
  
  while (true) {
    println("!")
    val newSocket = welcomeSocket.accept()
    println("?")
    val userThread: Thread = new Thread(new Runnable {
      //var active = true
      def run() = {
        println("Starting new thread.")
        //User listener thread code//
        val userOption = User(newSocket)
        if (userOption.isDefined) {
          println("Username was: " + userOption.get.username)
          var active = true
          while (active) {
            try {
              if (!userOption.get.isOnline) throw new Error("User is no longer online.")
              println("Waiting for input from user " + userOption.get.username)
              val input = userOption.get.receiveLine()
              if (!input.isDefined) throw new Error("User.receiveLine() returned None.")
              println("Input received: " + input.get)
              //if (input == null || input == "") throw new Error("Received null or empty input!")  //Client side must make sure no such outputs are sent.
              val time = System.currentTimeMillis()
              val message = new Message(input.get, time, userOption.get)
              if (!message.isValid) {
                userOption.get.send(":::004\n")
              }
            } catch {
              case e: Throwable => {
                println("===Error===: " + e.getClass.toString + " /// " + e.getMessage())
                active = false
              }
            }
          }
          //Terminating connection.
          Main.users.remove(userOption.get)
          Main.rooms.foreach(_.removeUser(userOption.get))
        }
        println("Terminating thread.")
        //
      }
    })
    //userThreads += userThread
    userThread.start()
    
  }
  
  /*def removeInactives(): Unit = {
    val inactiveThread = userThreads.find(!_.isAlive)
    if (!inactiveThread.isDefined) {
      userThreads.remove(inactiveThread.get)
      removeInactives()
    }
  }*/
  
  /*class UserThread(runnable: Runnable) extends Thread(runnable) {
    var active = true
  }*/
  
}
