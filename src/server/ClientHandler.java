package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable // The runnable interface is implemented for handling multiple clients by assingning each client to a separate thread.
{
    private final Socket socket;
    private final int clientId;
    private final GameManager gameManager;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private boolean registered = false; // WHY: so closeSocket() only tells GameManager to remove a player that was actually added

    public ClientHandler(Socket socket, int clientId, GameManager gameManager)
    {
        this.socket = socket;
        this.clientId = clientId;
        this.gameManager = gameManager;
    }

    @Override
    public void run() 
    {    
        System.out.println("[Thread-" + clientId + "] Client connected from " + socket.getInetAddress());
        try
        {
            out = new PrintWriter(socket.getOutputStream(), true); // auto-flush enabled for sending messages to the client.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // reading the input stream from the client socket.

            String nameMsg = in.readLine(); // reades the players name that was sent from the client.
            playerName = (nameMsg != null && nameMsg.startsWith("NAME:")) ? nameMsg.substring(5).trim() : "";

            boolean accepted = gameManager.registerPlayer(playerName, clientId, this);
            if(!accepted)
            {
                String reason = playerName.isEmpty() ? "Name cannot be empty" : "Name already taken";
                sendMessage("REJECTED:" + reason);
                System.out.println("[Thread-" + clientId + "] Registration rejected: " + reason);
                return; // skip the read loop entirely 
            }

            registered = true;
            System.out.println("[Thread-" + clientId + "] Player registered: " + playerName);
            sendMessage("WELCOME:" + playerName); // sends a welcome message to the client with the player's name.

            String line;
            while((line = in.readLine()) != null) // reading inputs from the client.
            {
                if(line.startsWith("ANSWER:"))
                {
                    try
                    {
                        int idx = Integer.parseInt(line.substring(7).trim());
                        System.out.println("[Thread-" + clientId + "] " + playerName + " answered: " + line);
                        gameManager.submitAnswer(playerName, idx);
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("[Thread-" + clientId + "] Bad answer from " + playerName + ": " + line);
                    }
                }
            }
        }
        catch(IOException e) // if the socket is not created.
        {
            System.out.println("[Thread-" + clientId + "] Connection lost: " + e.getMessage());
        }
        finally
        {
            closeSocket(); // removes everything related to the client when the client disconnects.
        }
    }

    public synchronized void sendMessage(String message)
    {
        if(out != null)
        {
            out.println(message); // if the output stream is not null, sends the message to the client.
        }
    }

    public String getPlayerName()
    {
        return playerName;
    }

    private void closeSocket()
    {
        try
        {   // if the socket is not null and not closed, closes the socket and prints a message to the console.
            if(socket != null && !socket.isClosed())
            {
                socket.close();
                System.out.println("[Thread-" + clientId + "] Socket closed for " + (playerName != null && !playerName.isEmpty() ? playerName : "unknown"));
            }
        }
        catch(IOException e) // occurs when the socket is not closed properly.
        {
            System.out.println("[Thread-" + clientId + "] Error closing socket: " + e.getMessage());
        }
        finally
        {
            Server.removeClient(this);
            if(registered)
            {
                gameManager.removePlayer(playerName);
            }
        }
    }
}