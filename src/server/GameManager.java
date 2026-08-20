package server;

import java.util.*;
import model.Player;
import model.Question;

public class GameManager
{
    private final List<Question> questions;
    private int currentQuestionIndex = 0;

    
    private final Map<String, Player> players = new LinkedHashMap<>(); // instant name lookup about players using their name.
    
    private final Map<String, ClientHandler> handlers = new LinkedHashMap<>(); // instant name lookup about handlers using their name (as handlers contain the socket and connection info)
   
    private final Set<String> answeredThisRound = new HashSet<>(); // avoid answer duplication in the same round, store names of players who have answered this round.

    private boolean gameOver = false;

    public GameManager(List<Question> questions)
    {
        this.questions = new ArrayList<>(questions); // taking the copy of questions.txt from the server by calling this constructor so that it has its own copy of the questions.
    }
    
    // synchronized to avoid racing issues when multiple threads (ClientHandler) calls this method simultaneously.
    public synchronized void registerPlayer(String name, int id, ClientHandler handler)
    {
        players.put(name, new Player(name, id)); // creating player obj and storing in playerMap, so that the player obj for the particular player can be later retreived and modified.
        handlers.put(name, handler); // storing the handler obj for the particular player in the handlerMap, so that the handler obj for the particular player can be later retreived and modified.

        System.out.println("[GameManager] " + name + " joined. Total players: " + players.size());

        if(gameOver)
        {
            handler.sendMessage("END:" + getWinnerName());
            return; // stops when the game is over.
        }

        if(currentQuestionIndex < questions.size())
        {
            handler.sendMessage(formatQuestion(questions.get(currentQuestionIndex))); // sending the current question to the newly joined player, in the format of network message to the handler then the socket will send it to the client.
        }
        broadcastScores(); // score display current.
    }
    public synchronized void submitAnswer(String name, int answerIndex)
    {
        if(gameOver || currentQuestionIndex >= questions.size())
        {
            return; // stops when the game is over.
        }

        Player player = players.get(name); // takes the obj from the PlayerMap using the name to do score calculation and update the score of the player.
        ClientHandler handler = handlers.get(name); // takes the obj from the HandlerMap using the name to send the result of the answer to the player.
        if(player == null || handler == null) return;


        if(answeredThisRound.contains(name)) return; // avoid answer duplication in the same round

        Question current = questions.get(currentQuestionIndex); // getting the current question obj from the questions list.
        boolean correct = current.Iscorrect(answerIndex);

        if(correct)
        {   // if correct answer, add 10 to score and send results to the player.
            player.addScore(10);
            handler.sendMessage("RESULT:CORRECT");
        }
        else
        {
            handler.sendMessage("RESULT:WRONG:" + current.getAnsIndex());
        }

        answeredThisRound.add(name); // adding name of the player who answered now and avoid answer duplication in the same round.

        // Round only advances once EVERY currently connected player has answered
        if (answeredThisRound.size() >= handlers.size())
        {
            advanceRound();
        }
    }
    private void advanceRound()
    {
        broadcastScores();
        currentQuestionIndex++;
        answeredThisRound.clear(); // clearing the previous round, with a clean slate for next one.

        if(currentQuestionIndex < questions.size())
        {
            broadcastMessage(formatQuestion(questions.get(currentQuestionIndex))); // sending the next question to all players in the format of network message to the handler then the socket will send it to the client.
        }
        else
        {
            endGame();
        }
    }

    private void endGame()
    {
        gameOver = true; // was set false initially, now set to true to indicate the game is over.
        String winner = getWinnerName(); // in case of tie, the first player to reach the score will be the winner.
        broadcastMessage("END:" + winner);
        System.out.println("[GameManager] Game over. Winner: " + winner);
    }

    private String getWinnerName() // comapres all player obj to get the winner if any.
    {
        return players.values().stream()
                .max(Comparator.comparingInt(Player::getScore))
                .map(Player::getName)
                .orElse("NONE");
    }
    public synchronized void removePlayer(String name)
    {
        players.remove(name);
        handlers.remove(name);
        answeredThisRound.remove(name);
        System.out.println("[GameManager] " + name + " disconnected. Remaining: " + players.size());

        // after a player left if everyone has answered this round, advance to the next question.
        if(!gameOver && !handlers.isEmpty() && answeredThisRound.size() >= handlers.size())
        {
            advanceRound();
        }
    }

    private void broadcastMessage(String msg)
    {
        for(ClientHandler handler : handlers.values())
        {
            handler.sendMessage(msg); // using loop goes through all the handlers and sends the message to each player.
        }
    }

    private void broadcastScores()
    {
        List<String> parts = new ArrayList<>(); // creating a list to store the scores of all players in the format of "name=score" for each player.
        for(Player p : players.values())
        {
            parts.add(p.getName() + "=" + p.getScore()); // iterate and add the name and score fo players.
        }
        broadcastMessage("SCORES:" + String.join(",", parts)); // joins the list using commas and SCORES: as prefix, and sends it to player console.
    }

    private String formatQuestion(Question q) // foramts the questions in the format of "QUESTION:question|option1|option2|option3|option4" to send to the client.
    {
        return "QUESTION:" + q.getS() + "|" + String.join("|", q.getOpt());
    }
}