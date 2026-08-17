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

  
    private static final List<ClientHandler> connected_usrs = Collections.synchronizedList(new ArrayList<>()); // keeps tracck of all the clients
    private static int clientcounter = 0;

    public static void main(String[] args)
    {

        System.out.println("=== LAN Quiz Game — Server (Week 4) ===");
        System.out.println("Starting server on port " + PORT + "...");
        System.out.println("Waiting for clients. Max players: " + max_usr);
        System.out.println("(Press Ctrl+C to stop the server)\n");
        // server is listening on this port number for clients making a connectuon request
        try(ServerSocket serverSocket = new ServerSocket(PORT)) 
        {

      
            while(true)
            {

              
                Socket clientSocket = serverSocket.accept(); // program halts here until a client connects (blocking call)
                clientcounter++;

              
                if(connected_usrs.size() >= max_usr)
                {
                 System.out.println("Server full. Refusing client ID: " + clientcounter);
                 clientSocket.close();
                 continue;
                }

                System.out.println("New client connecting-> assigning ID " + clientcounter);

                ClientHandler handler = new ClientHandler(clientSocket, clientcounter); // accepts the socket object from accept method and client number each object is used for communication with the client
                connected_usrs.add(handler);

                Thread thread = new Thread(handler); // passing the instance of ClientHandler as it implements Runnable, and creating a new thread for every client connection
                thread.setName("ClientThread-" + clientcounter);
                thread.start(); // thread calls the run method of ClientHandler class

                System.out.println("Thread started for client " + clientcounter + ". Total connected: " + connected_usrs.size());
            }

        }
        catch(IOException e)
        {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void removeClient(ClientHandler handler) // removes the client from the list as they disconnects or the server closes the connection
    {
        connected_usrs.remove(handler);
        System.out.println("SERVER: client " + clientcounter + "-> disconnected");
        System.out.println("Client removed --> Total active connections: " + connected_usrs.size() + "\n");
    }
}