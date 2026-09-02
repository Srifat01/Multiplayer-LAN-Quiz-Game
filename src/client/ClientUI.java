package client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class ClientUI extends JFrame
{
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 5000;
    private static final int LOAD_DELAY = 1000; // ms before next question, so results are visible first

    private static final Color GREEN = new Color(46, 139, 87);
    private static final Color RED = new Color(200, 50, 50);

    private final CardLayout cards = new CardLayout(); // used so that no other screen is viswible except thisn one this one is used for both login in and game screen
    private final JPanel cardPanel = new JPanel(cards);

    private PrintWriter out;
    private String playerName;
    private int lastClicked = -1;

    private JTextField nameField;
    private JButton connectBtn;
    private JLabel statusLabel;

    private JLabel questionLabel, feedbackLabel;
    private JButton[] answerBtns;
    private Color defaultBtnColor;
    private Scoreboard scoreboard;

    public ClientUI()
    {
        super("QuizHolic");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 550);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);

        cardPanel.add(buildLoginScreen(), "login");
        cardPanel.add(buildGameScreen(), "game");
        add(cardPanel);
        cards.show(cardPanel, "login"); // stack the login screen over the game screen.
    }

    private JPanel buildLoginScreen()
    {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 100, 80, 100)); 

        JLabel title= new JLabel("Enter your name to join QuizHolic", SwingConstants.CENTER);
        nameField = new JTextField();
        connectBtn =new JButton("Connect");
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(RED);

        connectBtn.addActionListener(e ->
        {
            String name = nameField.getText().trim();
            if(name.isEmpty())
            { 
                statusLabel.setText("Please enter a name."); return; 
            }
            connectBtn.setEnabled(false);
            statusLabel.setForeground(Color.GRAY);
            statusLabel.setText("Connecting...");
            new Thread(() -> connectToServer(name)).start(); // connect to the server in a separate thread to avoid blocking the UI
        });

        for(JComponent c : new JComponent[]{title, nameField, connectBtn, statusLabel}) // adding the components to the panel
        {
                panel.add(c);
        }
        return panel; // return the panel to the cardlayout.
    }
    private JPanel buildGameScreen()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        questionLabel = new JLabel("Waiting for the first question...", SwingConstants.CENTER);
        questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(questionLabel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        answerBtns = new JButton[4];
        for(int i = 0; i < 4; i++)
        {
            int index = i;
            JButton btn = new JButton("Option " + i);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
            btn.setEnabled(false);
            btn.addActionListener(e -> sendAnswer(index)); // send the answer to the server when clicked
            answerBtns[i] = btn; // store the button in the array for later reference
            grid.add(btn); // add the button to the grid panel
        }
        defaultBtnColor = answerBtns[0].getBackground(); // store the default button color to reset later
        panel.add(grid, BorderLayout.CENTER);

        feedbackLabel = new JLabel(" ", SwingConstants.CENTER); 
        scoreboard = new Scoreboard();

        JPanel bottom = new JPanel(new BorderLayout(5, 8)); 
        bottom.add(feedbackLabel, BorderLayout.NORTH);
        bottom.add(scoreboard, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }
    private void connectToServer(String name)
    {
        try
        {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            playerName = name;
            out.println("NAME:" + playerName);

            String line;
            while((line = in.readLine()) != null) // reads the message from the server, line by line, until the connection is closed
            {
                String msg = line;
                SwingUtilities.invokeLater(() -> handleMessage(msg)); // handleMessage runs on the EDT, so it can safely update the UI.
            }
        }
        catch(IOException e)
        {   
            SwingUtilities.invokeLater(() ->
            {
                statusLabel.setForeground(RED);
                statusLabel.setText("Could not connect: " + e.getMessage());
                connectBtn.setEnabled(true);
            });
        }
    }

    private void sendAnswer(int index)
    {
        lastClicked = index;
        out.println("ANSWER:" + index); // sending answer to server
        setButtonsEnabled(false);
        feedbackLabel.setForeground(Color.GRAY);
        feedbackLabel.setText("Answer sent, waiting for other players...");
    }
    private void handleMessage(String line)
    {
        if(line.startsWith("WELCOME:"))
        {
            cards.show(cardPanel, "game");
            setTitle("QuizHolic — " + playerName);
        }
        else if(line.startsWith("QUESTION:")) scheduleQuestion(line.substring(9));
        else if(line.startsWith("RESULT:")) showResult(line);
        else if(line.startsWith("SCORES:")) scoreboard.update(line.substring(7));
        else if(line.startsWith("END:")) showEnd(line.substring(4));
    }


    private void scheduleQuestion(String body) // delays the next question to 1000ms
    {
        Timer t = new Timer(LOAD_DELAY, e -> showQuestion(body));
        t.setRepeats(false);
        t.start();
    }

    private void showQuestion(String body)
    {
        String[] p = body.split("\\|");
        if(p.length != 5) return;

        questionLabel.setText("<html><div style='text-align:center;'>" + p[0] + "</div></html>"); // question is shown on the label used html tag to wrap the text and center it.
        for(int i = 0; i < 4; i++) // shows the options on the buttons and resets their color to default
        {
            answerBtns[i].setText(p[i + 1]);
            answerBtns[i].setBackground(defaultBtnColor);
        }
        // reset everything for the next question
        setButtonsEnabled(true);
        feedbackLabel.setText(" ");
        lastClicked = -1;
    }

    private void showResult(String line)
    {
        boolean correct = line.equals("RESULT:CORRECT");
        feedbackLabel.setForeground(correct ? GREEN : RED);
        feedbackLabel.setText(correct ? "Correct!" : "Wrong — " + line); // shows the answer on the feedback label.

        if(lastClicked >= 0) answerBtns[lastClicked].setBackground(correct ? GREEN : RED); // highlight the button that was clicked

        if(!correct)
        {
            try
            {
                int rightIndex = Integer.parseInt(line.substring(line.lastIndexOf(':') + 1).trim());
                answerBtns[rightIndex].setBackground(GREEN); // show which one WAS correct
            }
            catch(NumberFormatException ignored){}
        }
    }

    private void showEnd(String winner) // winner is shown
    {
        setButtonsEnabled(false);
        questionLabel.setText("Game over!");
        feedbackLabel.setForeground(GREEN);
        feedbackLabel.setText("Winner: " + winner);
    }

    private void setButtonsEnabled(boolean enabled)
    {
        for(JButton b : answerBtns)
            {
                b.setEnabled(enabled);
            }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}