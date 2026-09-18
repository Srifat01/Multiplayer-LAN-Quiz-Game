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
## Week 5 — Shared Game State (GameManager)

### What was done
Replaced each client's isolated one-question exchange with a single shared `GameManager` that every connected client plays against together — the same question, the same live scoreboard, and a round that only advances once everyone has answered.

### Files
| File | Package | Purpose |
|------|---------|---------|
| `GameManager.java` | `server` | New — central shared game state. Holds the question list, current question index, every player's score, and decides when a round advances or the game ends |
| `ClientHandler.java` | `server` | Updated — no longer owns any game logic. Reads `NAME:`/`ANSWER:` off the socket and forwards everything to `GameManager`; exposes `sendMessage()` so `GameManager` can push data out at any time |
| `Server.java` | `server` | Updated — loads `data/questions.txt` on startup and constructs one `GameManager`, shared across every `ClientHandler` it spawns |
| `Client.java` | `client` | Updated — now loops reading server messages instead of a single exchange, since the server pushes several message types over the life of the game |

### Message protocol additions this week
| Message | Direction | Meaning |
|---------|-----------|---------|
| `SCORES:Alice=20,Bob=10` | Server → Client (broadcast) | Live scoreboard, sent after every round |
| `END:Alice` | Server → Client (broadcast) | Sent once all questions are used up — names the winner |

### Key concepts applied
- **Shared mutable state across threads** — `GameManager` is one object that every `ClientHandler` thread calls into concurrently, instead of each thread keeping its own private state
- **`synchronized` methods** — `registerPlayer()`, `submitAnswer()`, and `removePlayer()` are all synchronized on the `GameManager` instance, so two players answering at the same instant can't corrupt shared data or double-advance a round
- **Broadcast pattern** — `GameManager` holds a `Map<String, ClientHandler>` so it can push a message to every connected client the moment a round ends, rather than waiting for each client to ask
- **Round-gating logic** — a round only advances once `answeredThisRound.size()` reaches the number of connected players, tracked with a `Set<String>` that resets every round
- **Reused identifiers instead of duplicated state** — `GameManager` reuses the `clientId` that `Server.java` already assigns per connection instead of keeping a second counter, so id assignment stays owned by one place

### Bug found and fixed this week
During a refactor to simplify `broadcastScores()`, the `formatQuestion()` method was accidentally deleted along with the class's closing brace, causing `cannot find symbol` compile errors. Fixed by restoring `formatQuestion()` and correcting the brace placement so it sits inside the class, before the final closing `}`.

### How to run
Requires **three or more terminals** — one server, one per player. Run all commands from the **project root** (not from inside `src/`), since `data/questions.txt` is loaded with a path relative to where `java` is launched.

**Terminal 1 — start the server:**
```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/main/Main.java
java -cp out server.Server
```

**Terminal 2, 3, ... — one client per player:**
```bash
java -cp out client.Client Alice
java -cp out client.Client Bob
```

The round only advances once **every** currently connected client has answered.

### Sample output

**Server terminal:**
```
=== LAN Quiz Game — Server (Week 5) ===
Starting server on port 5000...
Loaded 20 questions.
Waiting for clients. Max players: 8
(Press Ctrl+C to stop the server)
New client connecting... assigning ID 1
Thread started for client 1. Total connected: 1
[Thread-1] Client connected from /127.0.0.1
[Thread-1] Player registered: Alice
[GameManager] Alice joined. Total players: 1
New client connecting... assigning ID 2
Thread started for client 2. Total connected: 2
[Thread-2] Client connected from /127.0.0.1
[Thread-2] Player registered: Bob
[GameManager] Bob joined. Total players: 2
```

**Client terminal (Alice):**
```
< LAN Quiz Game — Client (Week 5) >
Connecting as [Alice] to localhost:5000...
Connected!
Sent: NAME:Alice
Server: WELCOME:Alice

--- Question ---
Which Java class accepts incoming connection or clients?
0) Socket
1) ServerSocket
2) DatagramSocket
3) SocketChannel
----------------
Your answer (0-3): 1
Sent: ANSWER:1
[Alice] Correct answer!
>> Scoreboard: Alice=10,Bob=0

--- Question ---
...
```

Once every question has been used, both clients print:
```
Game over! Winner: Alice
Session complete
```

---
## Week 6 — Java Swing UI (Client Side)

### What was done
Replaced the console-only client with a real graphical client, `ClientUI.java` — a login screen, a question panel with 4 clickable answer buttons, and a live scoreboard, all in a window titled **QuizHolic**. The console client (`Client.java`) is untouched and still works; `ClientUI` is a second, independent way to connect.

### Files
| File | Package | Purpose |
|------|---------|---------|
| `ClientUI.java` | `client` | New — Swing GUI client. Login screen (name + Connect button) that switches to a game screen (question label, 4 answer buttons, feedback text, scoreboard) once connected |

### Key concepts applied
- **`JFrame`, `JPanel`, `JLabel`, `JButton`, `JTextField`** — the base Swing components the whole UI is built from
- **`BorderLayout` and `GridLayout`** — `BorderLayout` arranges the game screen into NORTH/CENTER/SOUTH regions; `GridLayout` arranges the 4 answer buttons into a 2×2 grid and stacks the login screen's fields vertically
- **`CardLayout`** — lets one window switch between the login screen and the game screen without opening a second window
- **Background thread for networking** — opening the socket and reading `in.readLine()` are both blocking calls. Running them directly on a button's click handler (which fires on the Event Dispatch Thread) would freeze the whole window, so `connectToServer()` runs on its own `Thread`
- **`SwingUtilities.invokeLater()` and the EDT** — Swing forbids touching any GUI component from any thread except the Event Dispatch Thread. Every server message received on the background thread is handed to `handleMessage()` via `invokeLater(...)`, which schedules it to actually run on the EDT instead

### Bug found and fixed this week
When the *last* player to answer in a round triggers the next question, `GameManager` broadcasts the new `QUESTION:` almost instantly after that player's `RESULT:`. The green/red answer highlight was being wiped by the new question before it was visible on screen. Fixed with a short delay (`javax.swing.Timer`, ~1 second) between receiving a new question and actually displaying it, giving the previous result time to be seen.

### How to run
Same server as previous weeks — `ClientUI` just replaces `client.Client` as the thing you run per player.

```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/client/ClientUI.java src/main/Main.java
java -cp out server.Server        # Terminal 1
java -cp out client.ClientUI      # Terminal 2, 3, ... — opens a window instead of using the console
```

### Sample output
No console output on the client side — a window opens showing a name field and a **Connect** button. After connecting, the question appears with 4 clickable buttons; clicking one shows **Correct!** in green or **Wrong** in red, along with the live scoreboard.

---

## Week 7 — Score Tracking + Leaderboard

### What was done
Pulled the scoreboard out of `ClientUI` into its own reusable `Scoreboard.java` component that sorts players highest-score-first, and added persistent score history: every finished game's final scores are appended to `data/highscores.txt` on the server.

### Files
| File | Package | Purpose |
|------|---------|---------|
| `Scoreboard.java` | `client` | New — a self-contained `JPanel` that parses a `SCORES:` string, sorts it descending by score, and displays a ranked list (`1. Alice — 20`). `ClientUI` just calls `scoreboard.update(...)` and doesn't know or care how the sorting/formatting works |
| `GameManager.java` | `server` | Updated — `endGame()` now calls `saveHighScores()`, which writes every player's final score, sorted highest-first, to `data/highscores.txt` |

### Key concepts applied
- **Encapsulation / single-responsibility** — sorting and display logic for the scoreboard live entirely inside `Scoreboard`, not scattered through `ClientUI`
- **`Comparator` as a lambda** — `entries.sort((a, b) -> b.getValue() - a.getValue())` sorts descending by score
- **Writing files, not just reading them** — `FileWriter("data/highscores.txt", true)` is the first time this project writes to disk instead of only reading (`QuestionLoader` in Week 2 only ever read)
- **Append mode** — the `true` flag on `FileWriter` means each game's results are added to the file, not overwriting the previous game's, so it becomes a running history over time
- **Visual feedback on answer buttons** — clicking an answer flashes it green or red immediately; if wrong, the button that *was* correct also lights up green, tightened to exactly a 1-second flash before the round moves on

### How to run
Same as Week 6 — `Scoreboard.java` is a new required file in the compile command.

```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/client/Scoreboard.java src/client/ClientUI.java src/main/Main.java
java -cp out server.Server
java -cp out client.ClientUI
```

After a full game finishes, check the results file:
```bash
cat data/highscores.txt
```

### Sample output

**`data/highscores.txt` after a game:**
```
=== Tue Aug 18 21:50:12 2026 | Winner: Alice ===
Alice=50
Bob=30

```

**Scoreboard panel in the client window:**
```
1. Alice — 50
2. Bob — 30
```

---

## Week 8 — Lobby + Full Game Flow

### What was done
Wired together everything built so far into a complete start-to-finish game flow: players connect and wait in a lobby, the host starts the game with a console command, each question now has a visible 15-second countdown, and disconnects/end-of-game were confirmed to already work correctly under the new flow.

### Files
| File | Package | Purpose |
|------|---------|---------|
| `GameManager.java` | `server` | Updated — added a `gameStarted` flag that gates the first question from being sent until the host starts the game; added a 15-second per-question countdown using `java.util.Timer`, broadcasting `TIME:<seconds>` every second and force-advancing the round if it reaches zero |
| `Server.java` | `server` | Updated — spawns a background thread that reads console input; typing `START` and pressing Enter calls `gameManager.startGame()` |
| `ClientUI.java` | `client` | Updated — placeholder text changed to `"Waiting for host to start..."`; added a `timerLabel` that displays the countdown as it arrives |

### Message protocol additions this week
| Message | Direction | Meaning |
|---------|-----------|---------|
| `TIME:12` | Server → Client (broadcast) | Seconds remaining in the current question, sent once per second |

### Key concepts applied
- **Console input on a separate thread** — the server's main thread is permanently occupied inside the `accept()` loop, so reading `START` from the console requires its own background `Thread` running concurrently
- **`java.util.Timer` / `TimerTask`** — schedules the countdown tick to run once per second on its own background thread, separate from any `ClientHandler` thread. This is the server-side equivalent of `javax.swing.Timer` used on the client — same idea (delayed/repeated execution), different toolkit, since the server has no EDT
- **Synchronizing across yet another thread** — the timer's `tick()` method reads and writes the same shared state (`currentQuestionIndex`, `answeredThisRound`) that `submitAnswer()` and `advanceRound()` touch. Marking `tick()` `synchronized` prevents a countdown reaching zero at the exact moment the last player answers from advancing the round twice
- **Graceful disconnect handling** — confirmed still correct from Week 5: `GameManager.removePlayer()` removes a disconnected player without crashing the server; a small additional check (`gameStarted &&`) stops a lobby-stage disconnect from ever triggering a round-advance that shouldn't be possible before the game has begun
- **End-of-game broadcast** — confirmed still correct from Week 5/7: `END:<winner>` is sent to every connected client once the last question is used up

### How to run
Same compile command as Week 7.

```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/client/Scoreboard.java src/client/ClientUI.java src/main/Main.java
java -cp out server.Server        # Terminal 1
java -cp out client.ClientUI      # Terminal 2, 3, ...
```

Connect however many clients you want first — they'll all sit on **"Waiting for host to start..."** — then, in the **server terminal**, type:
```
START
```
and press Enter to begin the game for everyone at once.

### Sample output

**Server terminal:**
```
=== LAN Quiz Game — Server (Week 8) ===
Starting server on port 5000...
Loaded 20 questions.
Waiting for clients. Max players: 8
(Press Ctrl+C to stop the server)
Type START and press Enter once players have joined.
New client connecting... assigning ID 1
Thread started for client 1. Total connected: 1
[GameManager] Alice joined. Total players: 1
New client connecting... assigning ID 2
Thread started for client 2. Total connected: 2
[GameManager] Bob joined. Total players: 2
START
[GameManager] Game started by host.
```

**Client window:** shows "Waiting for host to start..." until `START` is typed, then the first question appears along with a live "Time left: 15s" countdown that ticks down each second.

---

## Week 9 — LAN Testing + Bug Fixes

### What was done
Took the game off a single machine and onto a real Wi-Fi network. The server address, hardcoded to `localhost` since Week 3, became something entered at runtime instead. Server-side validation was added so an empty or duplicate player name is rejected rather than silently accepted. The question bank was expanded to meet the 30+ target, and the project was packaged into standalone runnable JAR files.

### Files
| File | Package | Purpose |
|------|---------|---------|
| `GameManager.java` | `server` | Updated — `registerPlayer()` now returns `boolean` instead of `void`. Returns `false` for an empty name or a name already in use |
| `ClientHandler.java` | `server` | Updated — checks `registerPlayer()`'s return value; on rejection sends `REJECTED:<reason>` and closes the connection without entering the read loop |
| `Client.java` | `client` | Updated — server address is now an optional command-line argument, defaulting to `localhost` |
| `ClientUI.java` | `client` | Updated — login screen gained a server IP field |
| `data/questions.txt` | `data/` | Expanded from 20 to 40 questions |

### Message protocol additions this week
| Message | Direction | Meaning |
|---------|-----------|---------|
| `REJECTED:<reason>` | Server → Client | Sent instead of `WELCOME:` when a name is empty or already taken |

### Key concepts applied
- **Runtime configuration instead of hardcoded values** — the server address moved from a compiled-in constant to something read at connect time
- **Changing a method's return type to communicate outcome** — `registerPlayer()` going from `void` to `boolean`
- **Defense in depth** — the GUI already stops an empty name client-side, but the server validates independently too, since the console client had no such check
- **Packaging compiled classes into a runnable JAR** — `jar cfe QuizHolic-Client.jar client.ClientUI -C out .`
- **The line between code and data at runtime** — `QuestionLoader` reads `data/questions.txt` from disk, not from inside the JAR, so the server JAR still needs `data/` sitting next to it

### Issues found during real LAN testing
1. Connections were refused with no error until the host's firewall was opened for port 5000.
2. The server JAR failed to load questions on a second device because `data/` hadn't been copied alongside it.
3. The wrong JAR (server instead of client) was copied to a client-only device — resolved by checking `unzip -p <file>.jar META-INF/MANIFEST.MF` for the actual `Main-Class:`.

None of these were code bugs — all three were deployment/environment issues invisible during single-machine testing.

### How to run
```bash
javac -d out src/model/Player.java src/model/Question.java src/util/QuestionLoader.java src/server/GameManager.java src/server/ClientHandler.java src/server/Server.java src/client/Client.java src/client/Scoreboard.java src/client/ClientUI.java src/main/Main.java

jar cfe QuizHolic-Server.jar server.Server -C out .
jar cfe QuizHolic-Client.jar client.ClientUI -C out .

java -jar QuizHolic-Server.jar        # host machine, with data/ alongside
java -jar QuizHolic-Client.jar        # any device, same Wi-Fi
```

---

## Week 10 — Documentation, Cleanup & Final Submission

### What was done
Closed out the project: added Javadoc to every class and method, cleaned up real clutter and naming inconsistencies found in the actual source, wrote a plain-text `README.txt` covering setup and controls, and prepared the project for submission.

### Files
Every `.java` file in the project received Javadoc — no files were added or removed structurally this week, only documented and tidied.

### Cleanup found and fixed
1. **Stray compiled `.class` files inside `src/`** — 7 leftover files (`Player.class`, `Question.class`, `ClientHandler.class`, `GameManager.class`, `Server.class`, `QuestionLoader.class`, `Client.class`) had been sitting in the source tree since earlier weeks. This is the exact same clutter that caused the Java-version mismatch error back in Week 6 (`javac`/`java` picking up a stale class instead of a freshly compiled one). Removed entirely — compiled output belongs only in `out/`.
2. **Naming inconsistencies fixed for consistency:**
   - `Question.Iscorrect()` → `isCorrect()` — Java method names start lowercase; the capital `I` was a typo that had persisted since Week 5.
   - `Question`'s field/parameter `AnsIndex` → `ansIndex` — fields and parameters are camelCase, not PascalCase; having both `AnsIndex` (field) and `ansIndex` (a local parameter elsewhere) in the same class was genuinely confusing to read.
   - `Player.getID()`/`setID()` → `getId()`/`setId()` — for consistency with the camelCase `id`/`clientId` naming used everywhere else in the project. Confirmed unused externally before renaming, so this was a zero-risk change.
3. **Imports organized** — `GameManager.java`'s `import java.util.*;` wildcard replaced with explicit imports, matching every other file's style.
4. **Debug print statements** — every `System.out.println` across all 9 files was reviewed individually. None were found to be leftover debugging noise; all serve either as direct player feedback (console client) or as the demonstration logging relied on since Week 4 to show multithreading actually working during a live demo. Nothing was removed here — flagged explicitly rather than deleting something that's actually load-bearing for a viva demonstration.

### Known remaining style inconsistency (not changed)
`Question.java` and `ClientUI.java` use K&R brace style (`{` on the same line), while every other file uses Allman style (`{` on its own line). This is purely cosmetic and doesn't affect behavior — left as-is since `ClientUI.java`'s current structure was already deliberately settled on in an earlier revision, and reformatting brace style project-wide risked a large, purely cosmetic diff for no functional benefit.

### How to run
Identical to Week 9 — no compile command or protocol changes this week.

### Final playthrough checklist
Run through this once, end to end, before submitting:
- [ ] Server starts cleanly, loads 40 questions
- [ ] Two clients (GUI, console, or one of each) connect with different names
- [ ] A duplicate name is correctly rejected
- [ ] Host types `START`; lobby screens transition to the first question on both clients
- [ ] Both players answer at least one question; scoreboard updates correctly on both
- [ ] Let one question's countdown expire without answering — round still advances
- [ ] Play through to the final question; both clients show the correct winner
- [ ] `data/highscores.txt` has a new entry appended after the game ends
- [ ] Disconnect one client mid-game (close the window) — server logs it and doesn't crash

### Submission
The project folder is zipped for submission, excluding `.git/`, `.idea/`, `.vscode/`, and `out/` (all regenerable or irrelevant to grading) — `src/`, `data/`, both READMEs, the packaged JARs, and the license/workplan files are included.

---


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
│   │   ├── ClientHandler.java
│   │   └── GameManager.java
│   └── client/
│       ├── Client.java
│       ├── ClientUI.java
│       └── Scoreboard.java
├── out/              ← compiled .class files
└── data/
    ├── questions.txt
    └── highscores.txt   ← created/appended to automatically after each game
```

---

*Submitted to: Vashkar Kar (VK), Lecturer, CSE — NUBT Khulna*
