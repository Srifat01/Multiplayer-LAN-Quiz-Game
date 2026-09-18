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
    private static final int PORT = 5000;
    private static final int LOAD_DELAY = 1000; // ms before next question, so results are visible first

    private static final Color GREEN = new Color(46, 139, 87);
    private static final Color RED = new Color(200, 50, 50);

    private final CardLayout cards = new CardLayout(); // used so that no other screen is visible except this one
    private final JPanel cardPanel = new JPanel(cards);

    private PrintWriter out;
    private String playerName;
    private int lastClicked = -1;

    private JTextField nameField;
    private JTextField ipField; // lets the player connect to a real LAN address without recompiling
    private JButton connectBtn;
    private JLabel statusLabel;

    private JLabel questionLabel, feedbackLabel, timerLabel;
    private JButton[] answerBtns;
    private Color defaultBtnColor;
    private Scoreboard scoreboard;

    public ClientUI()
    {
        super("QuizHolic");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 750);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);

        cardPanel.add(buildLoginScreen(), "login");
        cardPanel.add(buildGameScreen(), "game");
        add(cardPanel);
        cards.show(cardPanel, "login"); // stack the login screen over the game screen.
    }

    //Login Screen>

    private JPanel buildLoginScreen()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Enter your name to join QuizHolic", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        nameField = new JTextField();
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        nameField.setPreferredSize(new Dimension(300, 40));
        nameField.setHorizontalAlignment(JTextField.CENTER);

        JLabel ipLabel = new JLabel("Server IP (leave as localhost if same machine)", SwingConstants.CENTER); // ip label for the player to enter the server IP address, default is localhost.
        ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        ipField = new JTextField("localhost");
        ipField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        ipField.setPreferredSize(new Dimension(300, 36));
        ipField.setHorizontalAlignment(JTextField.CENTER);

        connectBtn = new JButton("Connect");
        connectBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        connectBtn.setPreferredSize(new Dimension(300, 40));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(RED);

        connectBtn.addActionListener(e ->
        {
            String name = nameField.getText().trim();
            String ip = ipField.getText().trim();
            if (name.isEmpty())
            { 
                statusLabel.setText("Please enter a name."); 
                return; 
            }
            if (ip.isEmpty())
            {
                statusLabel.setText("Please enter a server IP.");
                return;
            }
            connectBtn.setEnabled(false); // this prevents the player from clicking the connect button multiple times while the connection is being established.
            statusLabel.setForeground(Color.GRAY);
            statusLabel.setText("Connecting...");
            new Thread(() -> connectToServer(name, ip)).start(); // connect to server in a separate thread
        });
        // add components to the panel with proper spacing and alignment
        gbc.gridy = 0; panel.add(title, gbc);
        gbc.gridy = 1; panel.add(nameField, gbc);
        gbc.gridy = 2; panel.add(ipLabel, gbc);
        gbc.gridy = 3; panel.add(ipField, gbc);
        gbc.gridy = 4; panel.add(connectBtn, gbc);
        gbc.gridy = 5; panel.add(statusLabel, gbc);

        return panel; 
    }

    //Game Screen>

    private JPanel buildGameScreen()
    {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        questionLabel = new JLabel("Waiting for host to start...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        timerLabel = new JLabel(" ", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(questionLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.SOUTH);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        answerBtns = new JButton[4];
        for(int i = 0; i < 4; i++)
        {
            int index = i;
            JButton btn = new JButton("Option " + i);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
            btn.setEnabled(false);
            btn.addActionListener(e -> sendAnswer(index)); 
            answerBtns[i] = btn;  // saving button references fir later use in showQuestion() and showResult()
            grid.add(btn); // adds the button to the grid panel, which is a 2x2 layout for the answer buttons.
        }
        defaultBtnColor = answerBtns[0].getBackground(); 
        panel.add(grid, BorderLayout.CENTER);

        feedbackLabel = new JLabel(" ", SwingConstants.CENTER); 
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        scoreboard = new Scoreboard(); // update the scoreboard with the latest scores received from the server.
        JScrollPane scoreScrollPane = new JScrollPane(scoreboard);
        scoreScrollPane.setPreferredSize(new Dimension(0, 130)); // keeps scoreboard proportionally scaled
        scoreScrollPane.setBorder(BorderFactory.createTitledBorder("Live Scoreboard"));

        JPanel bottom = new JPanel(new BorderLayout(5, 8)); 
        bottom.add(feedbackLabel, BorderLayout.NORTH);
        bottom.add(scoreScrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel; // returns the game screen panel to be added to the card layout in the constructor.
    }

    //Networking>

    private void connectToServer(String name, String serverIp)
    {
        try
        {
            Socket socket = new Socket(serverIp, PORT); 
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            playerName = name;
            out.println("NAME:" + playerName); 

            String line;
            while ((line = in.readLine()) != null) // read messages from the server until the connection is closed
            {
                String msg = line;
                SwingUtilities.invokeLater(() -> handleMessage(msg)); 
            }
        }
        catch (IOException e)
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
        out.println("ANSWER:" + index);  
        setButtonsEnabled(false);
        feedbackLabel.setForeground(Color.GRAY);
        feedbackLabel.setText("Answer sent, waiting for other players...");
    }

    //Handling the messages received from the server and updating the UI accordingly.

    private void handleMessage(String line)
    {
        if (line.startsWith("REJECTED:"))
        {
            statusLabel.setForeground(RED);
            statusLabel.setText(line.substring(9));
            connectBtn.setEnabled(true);
        }
        else if (line.startsWith("WELCOME:"))
        {
            cards.show(cardPanel, "game");
            setTitle("QuizHolic — " + playerName);
        }
        else if (line.startsWith("QUESTION:")) scheduleQuestion(line.substring(9));
        else if (line.startsWith("TIME:")) timerLabel.setText("Time left: " + line.substring(5) + "s");
        else if (line.startsWith("RESULT:")) showResult(line);
        else if (line.startsWith("SCORES:")) scoreboard.update(line.substring(7));
        else if (line.startsWith("END:")) showEnd(line.substring(4));
    }

    private void scheduleQuestion(String body) 
    {
        Timer t = new Timer(LOAD_DELAY, e -> showQuestion(body));
        t.setRepeats(false);
        t.start();
    }

    private void showQuestion(String body)
    {
        String[] p = body.split("\\|");
        if (p.length != 5) return;

        questionLabel.setText("<html><div style='text-align:center;'>" + p[0] + "</div></html>"); 
        for (int i = 0; i < 4; i++) 
        {
            answerBtns[i].setText(p[i + 1]);
            answerBtns[i].setBackground(defaultBtnColor);
        }
        setButtonsEnabled(true);
        feedbackLabel.setText(" ");
        lastClicked = -1;
    }

    private void showResult(String line)
    {
        boolean correct = line.equals("RESULT:CORRECT");
        feedbackLabel.setForeground(correct ? GREEN : RED);
        feedbackLabel.setText(correct ? "Correct!" : "Wrong — " + line); 

        if (lastClicked >= 0) answerBtns[lastClicked].setBackground(correct ? GREEN : RED); 

        if(!correct)
        {
            try
            {
                int rightIndex = Integer.parseInt(line.substring(line.lastIndexOf(':') + 1).trim());
                answerBtns[rightIndex].setBackground(GREEN); 
            }
            catch(NumberFormatException ignored) {}
        }
    }

    private void showEnd(String winner) 
    {
        setButtonsEnabled(false);
        questionLabel.setText("Game over!");
        timerLabel.setText(" ");
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