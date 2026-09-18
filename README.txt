=================================================
 Multiplayer LAN Quiz Game - README
 CSE 2216 - Software Development I
 Md. Shoibe Hossain Rifat - ID: 11240321705 - Section 4A
=================================================

-------------------------------------------------
1. HOW TO RUN THE SERVER
-------------------------------------------------

From the project root folder (the one containing src/, data/, out/):

  Step 1 - Compile:
    javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/client/Scoreboard.java src/client/ClientUI.java src/main/Main.java

  Step 2 - Run the server:
    java -cp out server.Server

  You should see:
    === LAN Quiz Game - Server ===
    Starting server on port 5000...
    Loaded 40 questions.
    Waiting for clients. Max players: 8
    (Press Ctrl+C to stop the server)
    Type START and press Enter once players have joined.

  IMPORTANT: always run this from the project ROOT, not from inside src/
  or out/. The server loads data/questions.txt using a path relative to
  wherever you launch it from.

  Alternatively, if you have the packaged JAR files:
    java -jar QuizHolic-Server.jar
  (data/questions.txt must be in the same folder as this JAR.)

-------------------------------------------------
2. HOW CLIENTS CONNECT
-------------------------------------------------

There are TWO different client programs. Either can connect to the same
server, and you can mix both kinds of clients in the same game.

  A) GUI client (ClientUI) - recommended, has a real window:

     java -cp out client.ClientUI
       (or: java -jar QuizHolic-Client.jar)

     A window titled "QuizHolic" opens. On the login screen:
       - Type your player name
       - Type the server's IP address (leave as "localhost" if the
         server is running on this SAME computer; otherwise type the
         host machine's LAN IP, e.g. 192.168.0.113)
       - Click "Connect"

  B) Console client (Client.java) - text-only, runs in a terminal:

     java -cp out client.Client <yourName> <serverIP>

     Example:
       java -cp out client.Client Alice 192.168.0.113

     If you omit the arguments, it defaults to name "Nadya" and
     IP "localhost".

  FINDING THE SERVER'S LAN IP (needed only if connecting from a
  different physical device on the same Wi-Fi):

     Linux:   ip a          (look under your Wi-Fi interface, e.g. wlo1)
     Windows: ipconfig
     Mac:     ifconfig

  If a second device cannot connect, the host machine's firewall may be
  blocking the port. On Fedora/Linux, run on the HOST machine:
     sudo firewall-cmd --add-port=5000/tcp

-------------------------------------------------
3. STARTING THE GAME
-------------------------------------------------

  - Have all players connect first. Each one will see "Waiting for
    host to start..." (GUI) or a similar message (console).
  - The HOST (whoever is running the server) types START in the
    SERVER's terminal window and presses Enter.
  - The first question is sent to everyone at that moment.

  A name that is empty, or already taken by another connected player,
  will be rejected with a message explaining why - just reconnect with
  a different name.

-------------------------------------------------
4. CONTROLS DURING THE GAME
-------------------------------------------------

  GUI client (ClientUI):
    - Click one of the 4 answer buttons to submit your answer.
    - Once you click, all 4 buttons lock until the next question.
    - Correct answers flash GREEN; wrong answers flash RED, and the
      button that WAS correct also turns green so you can see it.
    - A live scoreboard (highest score first) is always visible.
    - A countdown timer shows seconds remaining for the current
      question - if it reaches 0, the round moves on even if you
      haven't answered.

  Console client (Client.java):
    - When a question appears, type a single digit 0, 1, 2, or 3
      and press Enter to answer.
    - Everything else (scores, correct/wrong, time remaining) is
      printed automatically - no other input is needed.

  Server console (host only):
    - Type START and press Enter to begin the game once players
      have joined.
    - Press Ctrl+C to shut the server down.

-------------------------------------------------
5. END OF GAME
-------------------------------------------------

  Once every question has been used, all clients display:
    "Game over! Winner: <name>"

  The server also appends the final scores (highest first) to
  data/highscores.txt, so past games' results are never overwritten -
  the file becomes a running history over time.

-------------------------------------------------
6. PROJECT STRUCTURE (for reference)
-------------------------------------------------

  src/model/    - Player, Question (core data classes)
  src/util/     - QuestionLoader (reads data/questions.txt)
  src/server/   - Server, ClientHandler, GameManager
  src/client/   - Client (console), ClientUI (GUI), Scoreboard
  src/main/     - Main (Week 1/2 standalone test harness only)
  data/         - questions.txt, highscores.txt
  out/          - compiled .class files (generated by javac, not
                  submitted as source)

  See README.md for the full week-by-week development history.

=================================================
 Submitted to: Vashkar Kar (VK), Lecturer, CSE - NUBT Khulna
=================================================
