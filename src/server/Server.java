package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    private static final int PORT = 5000; // hardcoded for now
    private static final String TEST_QUESTION = "QUESTION:Which transport layer protocol is preferred for real-time video streaming or online multiplayer gaming where minimizing latency is more vital than ensuring every single packet arrives?|UDP|TLS|TCP|HTTP";

    private static final int ANS_INDEX = 1; // correct answer for the question
    public static void main(String[] args)
    {
        System.out.println("Server connection Checking-<<<");
        System.out.println("Started on Port:"+ PORT+"......");


    try(ServerSocket serverSoc = new ServerSocket(PORT))
    {
         System.out.println("Server is listening now------");

         Socket clientSoc = serverSoc.accept();
         System.out.println("User connected IP: "+ clientSoc.getInetAddress());

         PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
          
             System.out.println("Sending Test Questions to the User----");
             out.println(TEST_QUESTION);
             System.out.println("Waiting for Users Answer now----");

             String usrAns = input.readLine();
             System.out.println("Answer received: "+usrAns);

             if(usrAns != null && usrAns.startsWith("ANSWER:"))
             {
                    String cutAns = usrAns.substring(7);
                    try
                    {
                        int ansIndex = Integer.parseInt(cutAns.trim());
                        if(ansIndex == ANS_INDEX)
                        {
                            System.out.println("Correct Answer ");
                            out.println("RESULT:CORRECT");
                        }
                        else
                        {
                            System.out.println("Incorrect Answer!!"+" Correct ANSWER: "+ ANS_INDEX);
                            out.println("RESULT:WRONG CORRECT_ANSWER-> " + ANS_INDEX);
                        }   
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("It shall be numerical integer" + cutAns);
                        out.println("ERROR!!!");
                    }
             }
             else
             {
                System.out.println("Invalid answer format-<< "+ usrAns);
                out.println("Invalid answer format ");
             }
             System.out.println("...End of Session....");
             clientSoc.close();

    }
    catch(IOException e)
    {
        System.out.println("Server error<<<<< "+ e.getMessage());
    }
    System.out.println("Server is closed now-----");
    }

}