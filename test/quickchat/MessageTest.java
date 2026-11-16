package quickchat;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    private static final String MSG1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String MSG2 = "Hi Keegan, did you receive the payment?";

    // Part 3 test data (from the brief)
    private static final String T1 = "Did you get the cake?";
    private static final String T2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String T3 = "Yohoooo, I am at your gate.";
    private static final String T4 = "It is dinner time!";
    private static final String T5 = "Ok, I am leaving without you.";

    private Message m1, m2, m3, m4, m5;

    @Before
    public void setUp() throws IOException {
        // use a temporary file so the tests never overwrite the real messages.json
        File temp = File.createTempFile("quickchat-test", ".json");
        temp.deleteOnExit();
        Message.setStorageFile(temp);
        Message.resetState();
    }

    /** Fills the arrays with the five test messages from the brief. */
    private void loadTestData() {
        m1 = new Message("1000000001", 0, "+27834557896", T1);
        m2 = new Message("1000000002", 1, "+27838884567", T2);
        m3 = new Message("1000000003", 2, "+27834484567", T3);
        m4 = new Message("1000000004", 3, "0838884567", T4);
        m5 = new Message("1000000005", 4, "+27838884567", T5);
        m1.SentMessage(Message.SEND);
        m2.SentMessage(Message.STORE);
        m3.SentMessage(Message.DISREGARD);
        m4.SentMessage(Message.SEND);
        m5.SentMessage(Message.STORE);
    }

    @Test
    public void messageLengthSuccess() {
        Message m = new Message("0012345678", 0, "+27718693002", MSG1);
        assertEquals("Message ready to send.", m.checkMessageLength());
    }

    @Test
    public void messageLengthFailure() {
        Message m = new Message("0012345678", 0, "+27718693002", "a".repeat(260));
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.",
                m.checkMessageLength());
    }

    @Test
    public void recipientSuccess() {
        Message m = new Message("0012345678", 0, "+27718693002", MSG1);
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    public void recipientFailure() {
        Message m = new Message("0012345678", 1, "08575975889", MSG2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                m.checkRecipientCell());
    }

    @Test
    public void messageHashTestCase1() {
        Message m = new Message("0012345678", 0, "+27718693002", MSG1);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    public void messageHashesInLoop() {
        String[] texts = {MSG1, MSG2, "Thanks", "Hi, mate. Please call me back thanks!"};
        String[] expected = {"00:0:HITONIGHT", "00:1:HIPAYMENT", "00:2:THANKSTHANKS", "00:3:HITHANKS"};
        for (int i = 0; i < texts.length; i++) {
            Message m = new Message("0012345678", i, "+27718693002", texts[i]);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    @Test
    public void messageIdCreated() {
        Message m = new Message(0, "+27718693002", MSG1);
        assertTrue(m.checkMessageID());
        assertEquals(10, m.getMessageID().length());
    }

    @Test
    public void messageSentSend() {
        Message m = new Message("0012345678", 0, "+27718693002", MSG1);
        assertEquals("Message successfully sent.", m.SentMessage(Message.SEND));
    }

    @Test
    public void messageSentDisregard() {
        Message m = new Message("0098765432", 1, "08575975889", MSG2);
        assertEquals("Press 0 to delete the message.", m.SentMessage(Message.DISREGARD));
    }

    @Test
    public void messageSentStore() {
        Message m = new Message("0012345678", 0, "+27718693002", MSG1);
        assertEquals("Message successfully stored.", m.SentMessage(Message.STORE));
    }

    @Test
    public void totalMessagesAfterSendAndDisregard() {
        new Message("0012345678", 0, "+27718693002", MSG1).SentMessage(Message.SEND);
        new Message("0098765432", 1, "08575975889", MSG2).SentMessage(Message.DISREGARD);
        assertEquals(1, Message.returnTotalMessages());
    }

    // ================= PART 3 TESTS =================

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        loadTestData();
        assertEquals(Arrays.asList(T1, T4), Message.sentMessageTexts());
    }

    @Test
    public void disregardedAndStoredArraysPopulated() {
        loadTestData();
        assertEquals(Arrays.asList(T3), Message.disregardedMessageTexts());
        assertEquals(Arrays.asList(T2, T5), Message.storedMessageTexts());
    }

    @Test
    public void messageIdAndHashArraysPopulated() {
        loadTestData();
        assertEquals(Arrays.asList("1000000001", "1000000002", "1000000003", "1000000004", "1000000005"),
                Message.messageIDs());
        assertEquals(5, Message.messageHashes().size());
        assertEquals(m2.getMessageHash(), Message.messageHashes().get(1));
    }
}
