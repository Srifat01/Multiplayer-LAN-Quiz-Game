package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// ClientHandler class implements runnbale so that instances of this class can be executed by separate threads
public class ClientHandler implements Runnable
{
    private final Socket socket; // socket object to communicate with the client and server
    private final int clientId;
    private PrintWriter out; // sending data to the client
    private BufferedReader in; // reading data from the client
    private String playerName;

    private static final String Test_Question = "QUESTION:Which Java class accepts incoming connection or clients?|Socket|ServerSocket|DatagramSocket|SocketChannel";

    private static final int Correct_Index = 1;

    public ClientHandler(Socket socket, int clientId)
    {
           this.socket = socket; // sets the socket object to communicate with the client and server from ClientHandler constructor from server class
           this.clientId = clientId;

    }
    @Override
    public void run() // separate thread is created for each client connection and the run method is called for each thread for runnable interface
    {
      System.out.println("[Thread->"+ clientId + "] Client connected form" + socket.getInetAddress());
      try
      {
        out = new PrintWriter(socket.getOutputStream(), true); // sending data to the client
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // reading data from the client
        
        String nameMsg = in.readLine(); // reads the name of the player sent by the client reads until a newline character
        if(nameMsg != null && nameMsg.startsWith("NAME:"))
        {
            playerName = nameMsg.substring(5).trim();

        }
        else
        {
            playerName = "Player" + clientId;
        }
        System.out.println("[Thread->" + clientId + "] Player registered: "+ playerName);
        out.println("WELCOME:"+ playerName);

        System.out.println("[Thread->" + clientId + "] Sending question to "+ playerName);
        out.println(Test_Question);


        String answer = in.readLine();
        System.out.println("[Thread->" + clientId +"] "+ playerName + " answered: "+ answer);

        if(answer != null && answer.startsWith("ANSWER:"))
        {
            try
            {
                int idx = Integer.parseInt(answer.substring(7).trim());
                if(idx == Correct_Index)
                {
                    out.println("RESULT:CORRECT");
                    System.out.println("[thread->" + clientId + "] "+ playerName + " CORRECT");

                }
                else
                {
                    out.println("RESULT:WRONG:" + Correct_Index);
                    System.out.println("[Thread->" + clientId + "] " + playerName + "WRONG");
                }

            }
            catch(NumberFormatException e)
            {
                out.println("RESULT:ERROR");
            }
           
        }
        else
        {
            out.println("RESULT:ERROR");
        }
       
      }
      catch(IOException e)
      {
          System.out.println("[Thread->" + clientId + "] Connection lost: " + e.getMessage());

      }
      finally
      {
          closeSocket();
      }
    }

    public void sendMessage(String message){
        if(out != null)
        {
            out.println(message);
        }
    }
    public String getPlayerName()
    {
        return playerName;
    }

    private void closeSocket()
    {
        try
        {
            if(socket != null && !socket.isClosed()) 
            {
               socket.close();
               System.out.println("[Thread->" + clientId + "] Socket closed for " + (playerName != null ? playerName : "unknown"));
            }
        }
        catch (IOException e)
        {
            System.out.println("[Thread->" + clientId + "] Error closing socket: "+ e.getMessage());

        }
        finally
        {
            Server.removeClient(this); // removes the client from the list of connected clients in the server class when the client disconnects or the socket is closed
        }
    }

}
