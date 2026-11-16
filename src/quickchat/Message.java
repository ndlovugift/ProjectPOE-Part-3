package quickchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    public static final int MAX_LENGTH = 250;
    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;

    private static final Pattern RECIPIENT = Pattern.compile("^\\+[0-9]{1,3}[0-9]{9,10}$");
    private static final Random RANDOM = new Random();

    // ---- Part 3 arrays (filled while the program runs, nothing is hard-coded) ----
    private static final List<Message> SENT = new ArrayList<>();        // Sent Messages
    private static final List<Message> DISREGARDED = new ArrayList<>(); // Disregarded Messages
    private static final List<Message> STORED = new ArrayList<>();      // Stored Messages (read from JSON)
    private static final List<Message> ALL = new ArrayList<>();         // every message, same order as the two arrays below
    private static final List<String> MESSAGE_IDS = new ArrayList<>();    // Message ID array (parallel to ALL)
    private static final List<String> MESSAGE_HASHES = new ArrayList<>(); // Message Hash array (parallel to ALL)

    private static String currentSender = "Developer";
    private static File storageFile = new File("messages.json");

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String message;
    private final String messageHash;
    private final String sender;

    public Message(int messageNumber, String recipient, String message) {
        this(generateMessageID(), messageNumber, recipient, message);
    }

    public Message(String messageID, int messageNumber, String recipient, String message) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.messageHash = createMessageHash();
        this.sender = currentSender;
    }

    private Message(String messageID, int messageNumber, String sender, String recipient, String message) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.messageHash = createMessageHash();
        this.sender = sender;
    }

    public static String generateMessageID() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public String checkRecipientCell() {
        if (recipient != null && RECIPIENT.matcher(recipient).matches()) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        int length = message == null ? 0 : message.length();
        if (length <= MAX_LENGTH) return "Message ready to send.";
        return "Message exceeds " + MAX_LENGTH + " characters by " + (length - MAX_LENGTH)
                + ", please reduce the size.";
    }

    public String createMessageHash() {
        String prefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String[] words = (message == null ? "" : message.trim()).split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    public String SentMessage(int choice) {
        switch (choice) {
            case SEND:
                SENT.add(this);
                register(this);
                return "Message successfully sent.";
            case DISREGARD:
                DISREGARDED.add(this);
                register(this);
                return "Press 0 to delete the message.";
            case STORE:
                STORED.add(this);
                register(this);
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message m : SENT) sb.append(m.details()).append(System.lineSeparator());
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return SENT.size();
    }

    public static void storeMessage() {
        storeMessage(storageFile);
    }

    public static void storeMessage(File file) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < STORED.size(); i++) {
            sb.append(STORED.get(i).toJson());
            sb.append(i < STORED.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]\n");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Could not store message: " + e.getMessage());
        }
    }

    public String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + esc(messageID) + "\",\n"
                + "    \"messageHash\": \"" + esc(messageHash) + "\",\n"
                + "    \"sender\": \"" + esc(sender) + "\",\n"
                + "    \"recipient\": \"" + esc(recipient) + "\",\n"
                + "    \"message\": \"" + esc(message) + "\"\n"
                + "  }";
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public String details() {
        return "Message ID: " + messageID + "\nMessage Hash: " + messageHash
                + "\nSender: " + sender + "\nRecipient: " + recipient + "\nMessage: " + message + "\n";
    }

    public static void resetState() {
        SENT.clear();
        DISREGARDED.clear();
        STORED.clear();
        ALL.clear();
        MESSAGE_IDS.clear();
        MESSAGE_HASHES.clear();
    }

    // ================= PART 3 =================

    /** Adds a message to the ID and hash arrays so every array stays in step. */
    private static void register(Message m) {
        ALL.add(m);
        MESSAGE_IDS.add(m.messageID);
        MESSAGE_HASHES.add(m.messageHash);
    }

    public static void setSender(String name) { currentSender = name; }
    public static void setStorageFile(File file) { storageFile = file; }

    /**
     * Reads the JSON file written by storeMessage() back into the Stored Messages array.
     * Messages already in the array (same message ID) are skipped so nothing is duplicated.
     *
     * Reference: Oracle (2024) Java SE API - java.util.regex.Pattern and Matcher.
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
     * Reference: Oracle (2024) Java SE API - java.nio.file.Files.readAllBytes.
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
     */
    public static int loadStoredMessages(File file) {
        if (!file.exists()) return 0;
        String json;
        try {
            json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not read stored messages: " + e.getMessage());
            return 0;
        }
        int loaded = 0;
        // each stored message is one { ... } block in the file
        Matcher block = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL).matcher(json);
        while (block.find()) {
            String item = block.group();
            String id = field(item, "messageID");
            if (id == null || MESSAGE_IDS.contains(id)) continue;
            String hash = field(item, "messageHash");
            String sender = field(item, "sender");
            Message m = new Message(id, numberFromHash(hash), sender == null ? "Developer" : sender,
                    field(item, "recipient"), field(item, "message"));
            STORED.add(m);
            register(m);
            loaded++;
        }
        return loaded;
    }

    /** Pulls the value of one "name": "value" pair out of a JSON block. */
    private static String field(String block, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(block);
        if (!m.find()) return null;
        return m.group(1).replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
                .replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int numberFromHash(String hash) {
        try {
            return Integer.parseInt(hash.split(":")[1]);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** Text of every sent message (the Sent Messages array). */
    public static List<String> sentMessageTexts() { return texts(SENT); }

    public static List<String> disregardedMessageTexts() { return texts(DISREGARDED); }

    public static List<String> storedMessageTexts() { return texts(STORED); }

    public static List<String> messageIDs() { return new ArrayList<>(MESSAGE_IDS); }

    public static List<String> messageHashes() { return new ArrayList<>(MESSAGE_HASHES); }

    private static List<String> texts(List<Message> list) {
        List<String> out = new ArrayList<>();
        for (Message m : list) out.add(m.message);
        return out;
    }

    /** 2a. Sender and recipient of every stored message. */
    public static String displaySenderAndRecipient() {
        if (STORED.isEmpty()) return "There are no stored messages.";
        StringBuilder sb = new StringBuilder();
        for (Message m : STORED) {
            sb.append("Sender: ").append(m.sender).append(" | Recipient: ").append(m.recipient)
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /** Longest message across every message captured (sent, stored and disregarded). */
    public static String longestMessage() { return longestOf(ALL); }

    /** 2b. Longest stored message. */
    public static String longestStoredMessage() { return longestOf(STORED); }

    private static String longestOf(List<Message> list) {
        String longest = "";
        for (Message m : list) {
            if (m.message.length() > longest.length()) longest = m.message;
        }
        return longest;
    }

    /** Looks up a message by ID using the Message ID array (null if not found). */
    public static Message findByMessageID(String id) {
        int index = MESSAGE_IDS.indexOf(id);
        return index == -1 ? null : ALL.get(index);
    }

    /** 2c. Search for a message ID and display the recipient and message. */
    public static String searchByMessageID(String id) {
        Message m = findByMessageID(id);
        if (m == null) return "No message found with ID " + id + ".";
        return "Recipient: " + m.recipient + "\nMessage: " + m.message;
    }

    /** 2d. All messages sent or stored for one recipient. */
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (Message m : ALL) {
            if (m.recipient.equals(recipient) && !DISREGARDED.contains(m)) found.add(m.message);
        }
        return found;
    }

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
}