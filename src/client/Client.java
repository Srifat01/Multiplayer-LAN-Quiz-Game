package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client
{

    private static final String server_ipaddress = "localhost";
    private static final int PORT = 5000;

    private static final String myname = "Nadya";

    public static void main(String[] args)
    {

        String name = (args.length > 0) ? args[0] : myname;

        System.out.println("< LAN Quiz Game — Client (Week 5) >");
        System.out.println("Connecting as [" + name + "] to " + server_ipaddress + ":" + PORT + "...");

        try(Socket socket = new Socket(server_ipaddress, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
        Scanner scanner = new Scanner(System.in)) 
        {   // Scanner is used to read user input from the console.

            System.out.println("Connected!\n");

            out.println("NAME:" + name);
            System.out.println("Sent: NAME:" + name);

            String welcome = in.readLine(); // reads the welcome message from the server with the player's name.
            System.out.println("Server: " + welcome);
            String line;
            while((line = in.readLine()) != null) // taking the input from the server and processing it.
            {

                if(line.startsWith("QUESTION:")) 
                {
                    String body = line.substring(9);
                    String[] parts = body.split("\\|");

                    if(parts.length == 5)
                    {
                        System.out.println("\n--- Question ---");
                        System.out.println(parts[0]);
                        System.out.println("0) " + parts[1]);
                        System.out.println("1) " + parts[2]);
                        System.out.println("2) " + parts[3]);
                        System.out.println("3) " + parts[4]);
                        System.out.println("----------------");
                        System.out.print("Your answer (0-3): ");

                        String answer = scanner.nextLine(); // reads the answer from the user input in the console.
                        out.println("ANSWER:" + answer);
                        System.out.println("Sent: ANSWER:" + answer);
                    }
                }else if(line.startsWith("RESULT:"))
                {   // if the server sends a result message, it checks if the result is correct or wrong and prints the appropriate message to the console.
                    if(line.equals("RESULT:CORRECT"))
                    {
                        System.out.println("[" + name + "] Correct answer!");
                    }else if(line.startsWith("RESULT:WRONG"))
                    {
                        System.out.println("[" + name + "] Incorrect answer! (" + line + ")");
                    }
                    else 
                    {
                        System.out.println("Server message: " + line); 
                    }

                }
                else if(line.startsWith("SCORES:"))
                {
                    System.out.println(">> Scoreboard: " + line.substring(7));

                }
                else if(line.startsWith("END:"))
                {
                    System.out.println("\nGame over! Winner: " + line.substring(4));
                    break;
                }
            }

            System.out.println("\nSession complete");

        }
        catch(IOException e) 
        {
            System.out.println("Could not connect: " + e.getMessage()); // if the socket is not created, prints the error message to the console.
        }
    }
}