package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Question;
import util.QuestionLoader;


public class Server
{

    private static final int PORT = 5000;
    private static final int max_usr = 8;

    // creates a list of the connected client handlers, synchronized to avoid racing issues when multiple threads (ClientHandler) calls this method simultaneously.
    private static final List<ClientHandler> connected_usrs = Collections.synchronizedList(new ArrayList<>());
    private static int clientcounter = 0;

    public static void main(String[] args)
    {

        System.out.println("=== LAN Quiz Game — Server (Week 5) ===");
        System.out.println("Starting server on port " + PORT + "...");

        List<Question> questions; // list of questions to be loaded from the questions.txt file.
        try
        {
            questions = QuestionLoader.loadQuestions("data/questions.txt");
        }
        catch(IOException e) // if file is not found or any other IO exceptions.
        {
            System.out.println("Failed to load questions: " + e.getMessage());
            return;
        }

        if(questions.isEmpty()) 
        {
            System.out.println("No valid questions loaded. Check data/questions.txt");
            return;
        }

        GameManager gameManager = new GameManager(questions); // initalizing the obj and calling the constructor of GameManager class to load the questions and manage the game.

        System.out.println("Loaded " + questions.size() + " questions.");
        System.out.println("Waiting for clients. Max players: " + max_usr);
        System.out.println("(Press Ctrl+C to stop the server)\n");

        // entry point for server waiting for requests from clients.
        try(ServerSocket serverSocket = new ServerSocket(PORT)) 
        {


            while(true)
            {


                Socket clientSocket = serverSocket.accept(); // returns socket for the connected client.
                clientcounter++;


                if(connected_usrs.size() >= max_usr)
                {
                 System.out.println("Server full. Refusing client ID: " + clientcounter);
                 clientSocket.close();
                 continue;
                }

                System.out.println("New client connecting... assigning ID " + clientcounter);

                // creates an handler obj by initilizing constructor that is in ClientHandler.java and passes the arguments, so now the handler for the client has the socket is, and gameManager obj.
                ClientHandler handler = new ClientHandler(clientSocket, clientcounter, gameManager);
                
                connected_usrs.add(handler); // puts the handler obj into the list of active clients.

                Thread thread = new Thread(handler); // creates a new thread obj by passing handler obj.
                thread.setName("ClientThread-" + clientcounter); // sets an id for the thread obj.
                thread.start(); // now thread starts as it calls run method from the ClientHandler.java.

                System.out.println("Thread started for client " + clientcounter + ". Total connected: " + connected_usrs.size());
            }

        }catch(IOException e) // if the server socket is not created or any other IO exceptions.
        {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void removeClient(ClientHandler handler)
    {
        connected_usrs.remove(handler); // When client disconnects it removes the handler obj from the list of active clients.
        System.out.println("Client removed >> Total active connections: " + connected_usrs.size());
    }
}