package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client{

    private static final String server_ipaddress = "localhost";
    private static final int PORT = 5000;

    private static final String myname = "Nadya";
    private static final int myanswer = 1; //correct index of answer

    public static void main(String[] args) {

        String name = (args.length > 0) ? args[0] : myname;

        System.out.println("< LAN Quiz Game — Client (Week 4) >");
        System.out.println("Connecting as [" + name + "] to " + server_ipaddress + ":" + PORT + "...");

        try(Socket socket = new Socket(server_ipaddress, PORT)) {

            System.out.println("Connected!\n");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("NAME:" + name);
            System.out.println("Sent: NAME:" + name);

            String welcome = in.readLine();
            System.out.println("Server: " + welcome);

            String raw = in.readLine();
            System.out.println("\nReceived: " + raw);

            if (raw != null && raw.startsWith("QUESTION:")) {
                String body = raw.substring(9);
                String[] parts = body.split("\\|");

                if (parts.length == 5) {
                    System.out.println("\n--- Question ---");
                    System.out.println(parts[0]);
                    System.out.println("A) " + parts[1]);
                    System.out.println("B) " + parts[2]);
                    System.out.println("C) " + parts[3]);
                    System.out.println("D) " + parts[4]);
                    System.out.println("----------------");
                }
            }

            String answerMsg = "ANSWER:" + myanswer;
            System.out.println("\nSending: " + answerMsg);
            out.println(answerMsg);

            String result = in.readLine();
            System.out.println("Server result: " + result);

            if(result != null && result.equals("RESULT:CORRECT"))
            {
                System.out.println("[" + name + "] Correct answer!");
            }
            else if(result != null && result.startsWith("RESULT:WRONG"))
            {
                System.out.println("[" + name + "] Incorrect answer!");
            }
            else
            {
                System.out.println("[" + name + "] Server message: " + result);
            }

            System.out.println("\nSession complete");

        }
        catch(IOException e)
        {
            System.out.println("Could not connect: " + e.getMessage());
            System.out.println("Is the server running?");
        }
    }
}