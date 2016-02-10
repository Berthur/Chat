# Chat
My first experiment with coding network stuff. This is a simple client-server, room-based chat application.

What it does:
- Allows multiple users to connect to one server simultaneously, from all corners of the world.
- Allows users to enter different chat rooms and chat live to other users in the same room
- Allows the administrator to list, create and remove chat rooms with their respective passwords, live

What it does not:
- Use any p2p technology
- Have a graphical user interface
- Use any sort of authentication or encryption - it is plain TCP as this is only an experimental chat app

What there is direct support for but has yet to be implemented:
- User commands
- Better interface


# Suggested way to run the application
The server-side source code files can be turned into a Java jar file using the main function in the Launch.java file to launch the program. The client-side code is all contained by one file and can more easily be run directly. It too comes with a Java launcher class if preferred.
