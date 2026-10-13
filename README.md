# CSCI 4311 
# Socket Programming

**Author: Aicel Cabuenas**


## Language and Version
- Java (JDK 17 or later recommended; tested with OpenJDK)
- No external libraries or build tools required

## Dependencies
- None. Only the Java standard library is used:
  java.io, java.net, java.util

## Files
- Server.java - multithreaded chat server
- Client.java - chat client with a background receiver thread

## Build Commands
Compile both files from the project folder:

    javac Server.java Client.java

## Run Commands
Start the server first (port 8989 is hard-coded in Server.java):

    java Server

Start each client in its own terminal window:

    java Client <hostname> <port>

Example:

    java Client localhost 8989

## How to Use
1. Start the server, then start one or more clients.
2. Enter a username when prompted (3-16 characters, letters, digits,
   underscores, or hyphens; must be unique).
3. Type a message and press Enter to broadcast it to everyone.
4. Supported commands:
   - Bye      - leave the chat and close the connection
   - AllUsers - show a sorted list of active users (only to you)
   - Log      - replay the full chat history (only to you)

## Design Assumptions
- The protocol is line-based: every message ends with a newline character.
- The transport is TCP, so delivery is reliable and ordered.
- The server port is fixed at 8989; only the client takes command-line arguments.
- Usernames are case-sensitive and are released when the user disconnects.
- Messages longer than 500 characters are rejected, and blank messages are ignored.

## Known Limitations
- The message log is kept in memory only, so it is lost when the server restarts.
- The message length limit is checked after the special commands, so a very long
  line is still read fully into memory before it is rejected.
- If a client drops unexpectedly (the socket throws an IOException instead of
  reaching the normal cleanup code), the disconnect notice is not broadcast,
  because the handler's username field is shadowed by the local variable
  inside run().
- There is no private messaging and no way to remove a user from the server side.
