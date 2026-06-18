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

## Project Structure (so far)

```
Multiplayer-LAN-Quiz-Game/
├── src/
│   ├── main/
│   │   └── Main.java
│   ├── model/
│   │   ├── Player.java
│   │   └── Question.java
│   └── util/
│       └── QuestionLoader.java
├── out/              ← compiled .class files
└── data/
    └── questions.txt
```

---

*Submitted to: Vashkar Kar (VK), Lecturer, CSE — NUBT Khulna*