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
| `questions.txt` | `data/` | 40 quiz questions covering Java, C, C++, DSA, and ML |
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
Loaded-> 40 questions.
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
│   │   └── Server.java
│   └── client/
│       └── Client.java
├── out/              ← compiled .class files
└── data/
    └── questions.txt
```

---

*Submitted to: Vashkar Kar (VK), Lecturer, CSE — NUBT Khulna*
