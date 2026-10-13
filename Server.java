import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final Set<String> users = Collections.synchronizedSet(new HashSet<>());
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>()); 
    private static final List<String> messageLog = Collections.synchronizedList(new ArrayList<>());
    
    public static void main (String[] args) {
        int port = 8989;
        try (ServerSocket serverSocket = new ServerSocket (port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept ();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());  
        }
    }
    
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String endpoint;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.endpoint = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }
        
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                String username = null;
                while (username == null) {
                    out.println("Server: Enter your username:");
                    String input = in.readLine();
                    if (input == null) { 
                       socket.close(); return; 
                    }   
                    
                    input = input.trim();
                    if (!input.matches("[A-Za-z0-9_-]{2,16}")) {
                        System.out.println("Registration rejected: " + input + " from" + endpoint);
                        out.println("Server: Invalid username. Use 3-16 letters, digits, underscores, or hyphens.");
                        continue;
                     }
    
                     synchronized (users) {
                         if (users.contains(input)) {
                             System.out.println("Registration rejected:" + input + " from " + endpoint); 
                             out.println("Server: Username already exists. Enter another username:");
                             continue;
                         }
                         users.add(input);     // register only when valid AND unused
                     }
                     username = input;
                }
                
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
                
                System.out.println("Client connected: " + endpoint);
                System.out.println("User registered: " + username + "from " + endpoint);
                broadcastMessage("Server: Welcome " + username );
                
                String message;
                while ((message = in.readLine()) != null) {
                     System.out.println("Received from " + username + ": " + message);
                     
                     if (message.equals("Bye")) {
                         broadcastMessage("Server: Goodbye " + username);
                         break;
                         
                     } else if (message.equals("AllUsers")) { 
                         List<String> sortedUsers = new ArrayList<> (users);
                         Collections.sort(sortedUsers);
                         out.println("Server: Active users: " + String.join(", ", sortedUsers)); 
                     } else if (message.equalsIgnoreCase("Log")) {
                         out.println("Server: " + username + " requested the Log");
                         synchronized (messageLog) {
                             for (String entry : messageLog) {
                                 out.println(entry);
                             } 
                         }
                     } else if (message.length() > 500) {
                         out.println("Server: Message too long");
                     } else if (message.trim().isEmpty()) {
                         // ignore whitespace-only messages
                     } else {
                         messageLog.add(username + ": " + message); 
                         broadcastMessage(username + ": " + message);
                     }
                } 
                
                //Cleanup when user disconnects
                boolean wasRegistered = false;
                synchronized (users) {
                    wasRegistered = users.remove(username);
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                
                if (wasRegistered) {
                    broadcastMessage("Server: " + username + " disconnected");
                }
                
                socket.close();
                System.out.println("Client disconnected: " + endpoint);
            } catch (IOException e) {
                if (username != null) {
                    broadcastMessage("Server: " + username + " disconnected");
                }
                
            }        
        }
        
        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                    writer.flush();
                }
            }
        }
    } 
    
}
