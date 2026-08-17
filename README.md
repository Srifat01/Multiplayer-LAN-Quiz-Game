# Multiplayer LAN Quiz Game
**CSE 2216 — Software Development I**
Md. Shoibe Hossain Rifat · ID: 11240321705 · Section 4A
 
---
 
## Week 1 — OOP Foundations & Data Model
 
### What was done
Designed and implemented the two core data model classes that the entire game is built on.
 
### Files
| File | Package | Purpose |
|------|---------|---------|
| `Player.java` | `model` | Represents a player — stores name, ID, and score |
| `Question.java` | `model` | Represents a quiz question — stores text, 4 options, correct answer index |
| `Main.java` | `main` | Test entry point — creates objects, tests all methods |
 
### Key concepts applied
- **Encapsulation** — all fields are private, accessed through getters/setters
- **Constructor validation** — `Question` throws `IllegalArgumentException` if options ≠ 4 or index out of 0–3 range
- **Controlled score updates** — `addScore()` ignores negative values to prevent accidental score reduction
- **toString()** override on both classes for readable console output
### How to run
```bash
javac model/Player.java model/Question.java main/Main.java
java -cp out main.Main
```
 
### Sample output
```
Player ID: 1, Name: Eagle, Score: 17
Question: What is the default port for HTTP?
A) 80   <-- correct
B) 443
C) 8080
D) 21
```
 
---
 
## Week 2 — File Handling & Question Loader
 
### What was done
Built a file loader that reads quiz questions from a plain text file into memory as `Question` objects, with full error handling for missing files and malformed lines.
 
### Files
| File | Package | Purpose |
|------|---------|---------|
| `QuestionLoader.java` | `util` | Reads `questions.txt`, parses each line, returns `ArrayList<Question>` |
| `questions.txt` | `data/` | Quiz questions covering Java, C, C++, DSA, and ML |
| `Main.java` | `main` | Updated — tests the loader, missing file case, and Week 1 carry-over |
 
### Question file format
One question per line, 6 fields separated by `|`:
```
<question text>|<option A>|<option B>|<option C>|<option D>|<correctIndex>
```
Example:
```
What is the default port for HTTP?|80|443|8080|21|0
```
Blank lines and lines starting with `#` are skipped.
 
### Key concepts applied
- **BufferedReader + FileReader** — efficient line-by-line file reading
- **Try-with-resources** — file closes automatically even if an error occurs
- **Checked exceptions** — `IOException` declared on `loadQuestions()` and handled in `Main`
- **Fault-tolerant parsing** — a malformed line is skipped with a warning, loading continues
- **ArrayList** — used over arrays because question count is unknown at compile time
### How to run
```bash
javac model/Player.java model/Question.java util/QuestionLoader.java main/Main.java
java -cp out main.Main
```
 
### Sample output
```
Testing Question Loader
.....Loading Questions from data/questions.txt
Loaded-> N questions.
...
-> Loading a fake file to see if try catch works as expected
Caught expected error-> data/nonexistant.txt (No such file or directory)
Week 1 demo checking.....
Player ID: 1, Name: Eagle, Score: 17
```
 
---
 
## Week 3 — Java Sockets (Single Client Connection)
 
### What was done
Built the first working communication between two separate Java programs over a network socket. The server sends a question, the client receives and displays it, sends back an answer, and the server checks and responds with the result.
 
### Files
| File | Package | Purpose |
|------|---------|---------|
| `Server.java` | `server` | Starts ServerSocket on port 5000, accepts one client, sends question, checks answer |
| `Client.java` | `client` | Connects to server, receives and displays question, sends answer, shows result |
 
### Message protocol defined this week
All messages are plain strings terminated by a newline. Both sides parse them by splitting on `:`.
 
| Message | Direction | Meaning |
|---------|-----------|---------|
| `QUESTION:text\|A\|B\|C\|D` | Server → Client | Sends the question and 4 options |
| `ANSWER:2` | Client → Server | Player's answer (index 0–3) |
| `RESULT:CORRECT` | Server → Client | Answer was right |
| `RESULT:WRONG:1` | Server → Client | Answer was wrong, correct index given |
 
### Key concepts applied
- **ServerSocket** — listens on port 5000, blocks on `accept()` until a client connects
- **Socket** — client-side connection, initiated with server IP and port
- **PrintWriter** — sends text to the other side
- **BufferedReader** — reads text from the other side
- **Try-with-resources** — both sockets close automatically when done
- **Protocol design** — structured message format so both sides know how to parse what they receive
### How to run
Requires **two terminals open at the same time**.
 
**Terminal 1 — start server first:**
```bash
javac server/Server.java
java server.Server
```
 
**Terminal 2 — then run client:**
```bash
javac client/Client.java
java client.Client
```
 
### Sample output
 
**Server terminal:**
```
Server is running. Waiting for a client to connect...
Client connected from: /127.0.0.1
Sending question to client...
Received from client: ANSWER:1
Result: CORRECT! Client answered index 1
Session complete. Closing connection.
```
 
**Client terminal:**
```
Connected to server!
 
--- Question ---
Which Java class accepts incoming connections?
A) Socket
B) ServerSocket
C) DatagramSocket
D) SocketChannel
----------------
Sending answer: ANSWER:1  (index 1)
Server says: RESULT:CORRECT
Your answer was CORRECT!
```
 
---
 
## Week 4 — Multithreading & Multiple Clients
 
### What was done
Upgraded the server from handling one client at a time to accepting several clients concurrently, each on its own thread. This is the piece that turns the project from a two-program demo into an actual multiplayer server.
 
### Files
| File | Package | Purpose |
|------|---------|---------|
| `ClientHandler.java` | `server` | Implements `Runnable` — owns the full lifecycle of one connected client (read name, send welcome, send question, read answer, send result, close) |
| `Server.java` | `server` | Updated — loops on `accept()`, spawns a new `Thread(handler)` per client instead of handling one and exiting, tracks connected clients in a list, enforces a max player cap |
| `Client.java` | `client` | Updated — sends `NAME:` before anything else and reads back `WELCOME:` |
 
### Message protocol additions this week
| Message | Direction | Meaning |
|---------|-----------|---------|
| `NAME:Alice` | Client → Server | Sent immediately on connect, before any question |
| `WELCOME:Alice` | Server → Client | Server acknowledges and confirms the name it registered |
 
### Key concepts applied
- **`Runnable` interface** — `ClientHandler` implements `run()`, so each client's whole exchange (name → question → answer → result → close) lives on its own thread instead of blocking the server's main loop
- **`while(true)` accept loop** — `Server.java` no longer exits after one client; it keeps calling `accept()` and spinning up a new thread for every connection until `max_usr` is reached
- **Thread-per-client model** — a blocking call like `readLine()` only blocks the thread that called it. While one client is sitting there deciding on an answer, the server keeps accepting and serving everyone else
- **Synchronized client list** — `connected_usrs` is wrapped with `Collections.synchronizedList(...)` since multiple threads (the accept loop and each client's own thread on disconnect) touch it
- **Max client cap** — new connections are refused once `connected_usrs.size()` reaches `max_usr` (8), instead of accepting unlimited clients
### Bugs found and fixed this week
Two issues surfaced during testing with multiple clients, both now fixed:
 
1. **Disconnected clients were never removed from the server's list.** `ClientHandler.closeSocket()` closed the socket but never called `Server.removeClient(this)`, so `connected_usrs` only ever grew, even for clients long gone. Fixed by calling `Server.removeClient(this)` in a `finally` block inside `closeSocket()`, so it runs on every exit path.
2. **Client was sending its own name as the answer instead of an index.** `Client.java` sent `ANSWER:` followed by the player's name string instead of the numeric answer index, which made `Integer.parseInt()` fail on the server every time and always return `RESULT:ERROR`. Fixed by sending the actual answer index instead.
### How to run
Requires **three or more terminals** to see the multithreading behavior — one server, two or more clients.
 
**Terminal 1 — start the server:**
```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/main/Main.java
java -cp out server.Server
```
 
**Terminal 2, 3, ... — run a client per player:**
```bash
java -cp out client.Client
```
 
### Sample output
 
**Server terminal:**
```
=== LAN Quiz Game — Server (Week 4) ===
Starting server on port 5000...
Waiting for clients. Max players: 8
(Press Ctrl+C to stop the server)
New client connecting... assigning ID 1
Thread started for client 1. Total connected: 1
[Thread-1] Client connected from /127.0.0.1
[Thread-1] Player registered: Nadya
[Thread-1] Sending question to Nadya
[Thread-1] Nadya answered: ANSWER:1
[Thread-1] Socket closed for Nadya
Client removed .> Total active connections: 0
```
 
**Client terminal:**
```
< LAN Quiz Game — Client (Week 4) >
Connecting as [Nadya] to localhost:5000...
Connected!
Sent: NAME:Nadya
Server: WELCOME:Nadya
Received: QUESTION:Which Java class accepts incoming connection or clients?|Socket|ServerSocket|DatagramSocket|SocketChannel
--- Question ---
Which Java class accepts incoming connection or clients?
A) Socket
B) ServerSocket
C) DatagramSocket
D) SocketChannel
----------------
Sending: ANSWER:1
Server result: RESULT:CORRECT
[Nadya] Correct answer!
Session complete
```
 
---
 
## Project Structure (so far)
 
```
Multiplayer-LAN-Quiz-Game/
├── src/
│   ├── main/
│   │   └── Main.java
│   ├── model/
│   │   ├── Player.java
│   │   └── Question.java
│   ├── util/
│   │   └── QuestionLoader.java
│   ├── server/
│   │   ├── Server.java
│   │   └── ClientHandler.java
│   └── client/
│       └── Client.java
├── out/              ← compiled .class files
└── data/
    └── questions.txt
```
 
---
 
*Submitted to: Vashkar Kar (VK), Lecturer, CSE — NUBT Khulna*
