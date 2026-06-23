package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
    private static final String SERVER_ADD = "localhost";
    private static final int SERVER_PORT = 5000;

    private static final int My_Answer = 1;

    public static void main(String[] args)
    {
        System.out.println("---LAN Quiz Game---Client side --");
        System.out.println("Connection to server at "+ SERVER_ADD + ".....");

        try (Socket socket = new Socket(SERVER_ADD, SERVER_PORT))
        {
            System.out.println("Connected to server ! ");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = in.readLine();
            System.out.println("Received from server: "+ response);
            
            if(response != null && response.startsWith("QUESTION:"))
            {
                String body = response.substring(9);
                String[] parts = body.split("\\|");

                if(parts.length == 5)
                {
                   System.out.println("---Question----");
                   System.out.println(parts[0]);

                   System.out.println("A) "+ parts[1]);
                   System.out.println("B) "+ parts[2]);
                   System.out.println("C) "+ parts[3]);
                   System.out.println("D) "+ parts[4]);   
                }
            }
            String answerMessage = "ANSWER:"+ My_Answer;
            System.out.println("Sending answer: " + answerMessage+ " (index "+ My_Answer+ ")");
            out.println(answerMessage);
            out.flush();
            
            String result = in.readLine();
            System.out.println("Server says: "+ result);

            if(result != null && result.equals("RESULT:CORRECT"))
            {
                System.out.println("Your answer was Correct!! ");
            }
            else if(result != null && result.startsWith("RESULT:WRONG"))
            {
                String correctIdx = result.split(":").length > 2 ? result.split(":")[2] : "?";
                System.out.println("Wrong answer, correct answer index: "+ correctIdx);
            }
            System.out.println(" Session complete. ");
        }
        catch (IOException e)
        {
            System.out.println("Could not connect to server: "+ e.getMessage());
            System.out.println("Make sure the server is running first. ");
        }
    }
}