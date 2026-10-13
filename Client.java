import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <hostname> <port>");
            return;
        }
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);
        
        try {
            Socket socket = new Socket(serverAddress, port);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader (new InputStreamReader(System.in));
            
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                         System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                     System.out.println("Connection closed.");
                }
            }).start();
            
            String username = userInput.readLine();
            output.println(username);
            output.flush();
            
            String message;
            while ((message = userInput.readLine()) != null) {
                output.println(message);
                output.flush();
                if (message.equalsIgnoreCase("Bye")) {
                     break;
                }
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }
    }
}
