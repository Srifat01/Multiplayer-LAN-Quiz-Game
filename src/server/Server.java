package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Server
{

    private static final int PORT = 5000;
    private static final int max_usr = 8; 

  
    private static final List<ClientHandler> connected_usrs = Collections.synchronizedList(new ArrayList<>());
    private static int clientcounter = 0;

    public static void main(String[] args)
    {

        System.out.println("=== LAN Quiz Game — Server (Week 4) ===");
        System.out.println("Starting server on port " + PORT + "...");
        System.out.println("Waiting for clients. Max players: " + max_usr);
        System.out.println("(Press Ctrl+C to stop the server)\n");

        try(ServerSocket serverSocket = new ServerSocket(PORT))
        {

      
            while(true)
            {

              
                Socket clientSocket = serverSocket.accept();
                clientcounter++;

              
                if(connected_usrs.size() >= max_usr)
                {
                 System.out.println("Server full. Refusing client ID: " + clientcounter);
                 clientSocket.close();
                 continue;
             }

                System.out.println("New client connecting... assigning ID " + clientcounter);

                ClientHandler handler = new ClientHandler(clientSocket, clientcounter);
                connected_usrs.add(handler);

                Thread thread = new Thread(handler);
                thread.setName("ClientThread-" + clientcounter);
                thread.start();

                System.out.println("Thread started for client " + clientcounter
                        + ". Total connected: " + connected_usrs.size());
            }

        } catch(IOException e)
        {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void removeClient(ClientHandler handler)
    {
        connected_usrs.remove(handler);
        System.out.println("Client removed .> Total active connections: " + connected_usrs.size());
    }
}