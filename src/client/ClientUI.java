package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class ClientUI extends JFrame
{
// server connection details
private static final String SERVER_IP = "localhost";
private static final int PORT = 5000;


// target dimensions for dynamic scaling
private static final int BASE_WIDTH = 700;
private static final int BASE_HEIGHT = 500;

// brief delay before rendering the next question after feedback
private static final int NEXT_QUESTION_DELAY_MS = 1200;

// visual styles for answer feedback
private static final Color CORRECT_COLOR = new Color(210, 245, 220);
private static final Color WRONG_COLOR = new Color(250, 210, 210);
private static final Color CORRECT_TEXT = new Color(0, 120, 40);
private static final Color WRONG_TEXT = new Color(180, 30, 30);

//network
private Socket socket;
private PrintWriter out;
private String playerName;

// screens
private final CardLayout cardLayout = new CardLayout();
private final JPanel cardPanel = new JPanel(cardLayout);
private static final String LOGIN_CARD = "login";
private static final String GAME_CARD = "game";

//login screen cmp
private JLabel loginTitleLabel;
private JTextField nameField;
private JButton connectButton;
private JLabel loginStatusLabel;

//screen cmp
private JLabel questionLabel;
private JButton[] answerButtons;
private Color defaultButtonBg;
private JLabel feedbackLabel;
private JTextArea scoreboardArea;
private JLabel scoreboardTitleLabel;
private int lastClickedIndex = -1;

public ClientUI()
{
    super("QuizHolic");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(BASE_WIDTH + 50, BASE_HEIGHT + 50);
    setMinimumSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
    setLocationRelativeTo(null);

    // register screens in the card layout container
    cardPanel.add(buildLoginPanel(), LOGIN_CARD);
    cardPanel.add(buildGamePanel(), GAME_CARD);
    add(cardPanel);
    cardLayout.show(cardPanel, LOGIN_CARD);

    // dynamically adjust font sizes when window is resized
    addComponentListener(new ComponentAdapter()
    {
        @Override
        public void componentResized(ComponentEvent e) { rescale(); }
    });
    rescale(); // applying the initial scale before any resize happens
}


private void rescale()
{
    // Calculate uniform scale factor based on container dimensions
    double scale = Math.min(getWidth() / (double) BASE_WIDTH, getHeight() / (double) BASE_HEIGHT);

    // Clamp the zoom factor to prevent extreme text sizing
    scale = Math.max(0.75, Math.min(scale, 2.5));

    if(loginTitleLabel != null)
    {
        setScaledFont(loginTitleLabel, 18, scale, true);
        setScaledFont(nameField, 16, scale, false);
        setScaledFont(connectButton, 16, scale, true);
        setScaledFont(loginStatusLabel, 13, scale, false);
    }
    if(questionLabel != null)
    {
        setScaledFont(questionLabel, 20, scale, true);
        setScaledFont(feedbackLabel, 15, scale, true);
        setScaledFont(scoreboardTitleLabel, 14, scale, true);
        setScaledFont(scoreboardArea, 14, scale, false);
        for (JButton btn : answerButtons) setScaledFont(btn, 16, scale, false);
    }
}

private void setScaledFont(JComponent c, int baseSize, double scale, boolean bold)
{
    // Preserve monospace font for alignment in the scoreboard
    String family = (c == scoreboardArea) ? "Monospaced" : "SansSerif";
    c.setFont(new Font(family, bold ? Font.BOLD : Font.PLAIN, (int) (baseSize * scale)));
}
private JPanel buildLoginPanel()
{
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.insets = new Insets(10, 60, 10, 60);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    loginTitleLabel = new JLabel("Enter your name to join QuizHolic", SwingConstants.CENTER);
    nameField = new JTextField();
    nameField.setPreferredSize(new Dimension(200, 36));
    connectButton = new JButton("Connect");
    connectButton.setPreferredSize(new Dimension(200, 40));
    loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
    loginStatusLabel.setForeground(WRONG_TEXT);

    connectButton.addActionListener((ActionEvent e) ->
    {
        String name = nameField.getText().trim();
        if (name.isEmpty())
        {
            loginStatusLabel.setText("Please enter a name.");
            return;
        }
        // Prevent duplicate clicks while connecting asynchronously
        connectButton.setEnabled(false);
        loginStatusLabel.setForeground(new Color(90, 90, 90));
        loginStatusLabel.setText("Connecting...");
        new Thread(() -> connectToServer(name)).start();
    });

    int row = 0;
    for (JComponent c : new JComponent[]{loginTitleLabel, nameField, connectButton, loginStatusLabel})
    {
        gbc.gridy = row++;
        panel.add(c, gbc);
    }
    return panel;
}
private JPanel buildGamePanel()
{
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    questionLabel = new JLabel("Waiting for the first question...", SwingConstants.CENTER);
    panel.add(questionLabel, BorderLayout.NORTH);

    JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
    answerButtons = new JButton[4];
    for (int i = 0; i < 4; i++)
    {
        final int answerIndex = i;
        JButton btn = new JButton("Option " + i);
        btn.addActionListener((ActionEvent e) -> sendAnswer(answerIndex));
        btn.setEnabled(false); // disabled until a question actually arrives
        answerButtons[i] = btn;
        buttonGrid.add(btn);
    }
    defaultButtonBg = answerButtons[0].getBackground(); // all 4 share the same default color
    panel.add(buttonGrid, BorderLayout.CENTER);

    feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
    scoreboardTitleLabel = new JLabel("Scoreboard");
    scoreboardArea = new JTextArea(5, 20);
    scoreboardArea.setEditable(false);

    JPanel scorePanel = new JPanel(new BorderLayout(4, 4));
    scorePanel.add(scoreboardTitleLabel, BorderLayout.NORTH);
    scorePanel.add(new JScrollPane(scoreboardArea), BorderLayout.CENTER);

    // group status feedback and live scores in the bottom section
    JPanel bottom = new JPanel(new BorderLayout(5, 8));
    bottom.add(feedbackLabel, BorderLayout.NORTH);
    bottom.add(scorePanel, BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);

    return panel;
}

private void connectToServer(String name)
{
    try
    {
        socket = new Socket(SERVER_IP, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        playerName = name;
        out.println("NAME:" + playerName);

        // listen continuously for incoming network commands
        String line;
        while((line =in.readLine()) != null)
        {
            final String msg = line;
            // dispatch updates back to the Swing event dispatch thread
            SwingUtilities.invokeLater(() -> handleServerMessage(msg));
        }
    }
    catch(IOException e)
    {
        SwingUtilities.invokeLater(() ->
        {
            loginStatusLabel.setForeground(WRONG_TEXT);
            loginStatusLabel.setText("Could not connect: " + e.getMessage());
            connectButton.setEnabled(true);
        });
    }
}

private void sendAnswer(int index)
{
    lastClickedIndex = index; // needed later by highlightLastClicked() when RESULT is here
    if(out != null) out.println("ANSWER:" + index);
    // lock choices to prevent double submission
    setAnswerButtonsEnabled(false);
    feedbackLabel.setForeground(new Color(90, 90, 90));
    feedbackLabel.setText("Answer sent, waiting for other players...");
}


private void handleServerMessage(String line)
{
    // parse protocol prefix and route to the corresponding UI update
    if(line.startsWith("WELCOME:"))
    {
        cardLayout.show(cardPanel, GAME_CARD);
        setTitle("QuizHolic — " + playerName);
    }
    else if(line.startsWith("QUESTION:")) scheduleNextQuestion(line.substring("QUESTION:".length()));
    else if(line.startsWith("RESULT:")) showResult(line);
    else if(line.startsWith("SCORES:")) updateScores(line.substring("SCORES:".length()));
    else if(line.startsWith("END:")) showEndGame(line.substring("END:".length()));
}
private void scheduleNextQuestion(String body)
{
    // delay rendering new options so players can process the previous result
    Timer timer = new Timer(NEXT_QUESTION_DELAY_MS, e -> updateQuestion(body));
    timer.setRepeats(false);
    timer.start();
}

private void updateQuestion(String body)
{
    String[] parts = body.split("\\|");
    if (parts.length != 5) return;

    questionLabel.setText("<html><div style='text-align:center;'>" + parts[0] + "</div></html>");
    for(int i = 0; i < 4; i++)
    {
        answerButtons[i].setText(parts[i + 1]);
        answerButtons[i].setBackground(defaultButtonBg);
    }
    setAnswerButtonsEnabled(true);
    feedbackLabel.setText(" ");
    lastClickedIndex = -1;
}

private void showResult(String line)
{
    if(line.equals("RESULT:CORRECT"))
    {
        feedbackLabel.setForeground(CORRECT_TEXT);
        feedbackLabel.setText("Correct!");
        highlightLastClicked(true);
    }
    else if(line.startsWith("RESULT:WRONG:"))
    {
        feedbackLabel.setForeground(WRONG_TEXT);
        feedbackLabel.setText("Wrong — " + line);
        highlightLastClicked(false);

        try
        {
            // highlight which answer was actually the correct one
            int correctIndex = Integer.parseInt(line.substring("RESULT:WRONG:".length()).trim());
            if(correctIndex >= 0 && correctIndex < answerButtons.length)
            {
                answerButtons[correctIndex].setBackground(CORRECT_COLOR);
            }
        }
        catch(NumberFormatException ignored)
        {
           
        }
    }
}

private void highlightLastClicked(boolean wasCorrect)
{
    if(lastClickedIndex >= 0 && lastClickedIndex < answerButtons.length)
    {
        answerButtons[lastClickedIndex].setBackground(wasCorrect ? CORRECT_COLOR : WRONG_COLOR);
    }
}

private void updateScores(String body)
{
    // rebuild score display from comma separated server data
    StringBuilder sb = new StringBuilder();
    for(String entry : body.split(","))
    {
        if(!entry.isEmpty()) sb.append(entry).append("\n");
    }
    scoreboardArea.setText(sb.toString());
}

private void showEndGame(String winner)
{
    setAnswerButtonsEnabled(false);
    questionLabel.setText("Game over!");
    feedbackLabel.setForeground(CORRECT_TEXT);
    feedbackLabel.setText("Winner: " + winner);
}

private void setAnswerButtonsEnabled(boolean enabled)
{
    for(JButton btn : answerButtons) btn.setEnabled(enabled);
}

public static void main(String[] args)
{ 
    SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
}



}