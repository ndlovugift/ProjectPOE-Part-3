# QuickChat

A simple messaging app that runs in the console. I built it for my PROG5121 (Programming 1A) portfolio of evidence, using Java and NetBeans.

You register, log in, and then you can send messages, store them for later, or throw them away. Part 3 adds a menu for looking through the messages you stored.

## What it can do

**Part 1 – Login**
- Register with a username, password and South African cell number
- Username needs an underscore and can be at most 5 characters
- Password needs 8+ characters, a capital letter, a number and a special character
- Cell number must start with +27

**Part 2 – Sending messages**
- Every message gets a random 10-digit ID
- Messages can be up to 250 characters
- The recipient's number must start with an international code, like +27
- Each message gets a hash, for example `00:0:HITONIGHT` (first two ID digits, message number, first and last word)
- Choose to **send**, **disregard** or **store** each message
- Stored messages are saved in `messages.json`

**Part 3 – Stored messages**
- The app keeps arrays for sent, disregarded and stored messages, plus message IDs and hashes
- Stored messages are read back from `messages.json` when you log in
- Menu option **4 – Stored Messages** lets you:
  - see the sender and recipient of every stored message
  - see the longest stored message
  - search for a message by its ID
  - search for all messages to one recipient
  - delete a message using its hash
  - print a full report of the stored messages
- Menu option **2** shows a report of everything you've sent

## How to run it

1. Open the project in NetBeans.
2. Right-click `QuickChat.java` (in `src/quickchat`) and choose **Run File**.
3. Follow the prompts in the Output window.

Example details you can register with:
- Username: `am_1`
- Password: `Ch&&sec@ke99!`
- Cell number: `+27838968976`

## Unit tests

The tests are in `test/quickchat/MessageTest.java` and use JUnit 4. They cover the message checks from Part 2 and the arrays, search, delete and report features from Part 3, using the test data from the brief.

In NetBeans, right-click `MessageTest.java` and choose **Test File**.

## GitHub Actions

The workflow in `.github/workflows/TestJava.yml` compiles the code and runs all the unit tests every time I push. A green tick on the Actions tab means all the tests passed.

## Project layout

```
src/quickchat/    Login.java, Message.java, QuickChat.java
test/quickchat/   MessageTest.java
lib/              JUnit and Hamcrest jar files
.github/workflows TestJava.yml
```

## Reference

Oracle (2024) *Java SE API documentation*. Available at: https://docs.oracle.com/en/java/javase/17/docs/api/ (I used the Pattern, Matcher and Files classes for reading the JSON file).



