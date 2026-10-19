// CardDeckTest.java

package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import cardgame.Card;
import cardgame.CardDeck;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the CardDeck class.
 */
public class CardDeckTest {
    /**
     * Clean up any generated output files after each test to maintain a clean test environment.
     * 
     * @throws IOException 
     */
    @AfterEach
    public void tearDown() throws IOException {
        // Removes the file generated in testDeckOutputFileContent()
        Files.deleteIfExists(Path.of("deck1_output.txt"));
    }

    /**
     * Tests that the deck ID is set correctly upon creation.
     */
    @Test
    public void testValidDeckId() {
        CardDeck d = new CardDeck(1);
        assertEquals(1, d.getId());
    }

    /**
     * Tests that creating a deck with an invalid ID (negative or zero) throws an IllegalArgumentException.
     */
    @Test
    public void testInvalidDeckIds() {
        int[] invalidIds = {0, -1, -10};

        for (int id : invalidIds) {
            try {
                new CardDeck(id);
                fail("Should throw IllegalArgumentException for non-positive id: " + id);
            } catch (IllegalArgumentException e) {
                // Expected exception, test continues
            }
        }
    }

    /**
     * Tests that adding and drawing cards from the deck works correctly and changes the size of the deck as expected.
     */
    @Test
    public void testAddDrawCard() {
        // Check adding
        CardDeck d = new CardDeck(1);
        Card c = new Card(3);
        d.addCard(c);
        assertEquals(1, d.getSize());

        // Check drawing
        Card drawnCard = d.drawCard();
        assertEquals(c, drawnCard);
        assertEquals(0, d.getSize());
    }

    /**
     * Tests that adding a null card to the deck throws an IllegalArgumentException.
     */
    @Test
    public void testAddNullCard() {
        CardDeck d = new CardDeck(1);
        assertThrows(IllegalArgumentException.class, () -> {
            d.addCard(null);
        });
    }

    /**
     * Tests that drawing a card from an empty deck returns null.
     */
    @Test
    public void testDrawFromEmptyDeck() {
        CardDeck d = new CardDeck(1);
        assertNull(d.drawCard());
    }

    /**
     * Tests the FIFO behavior of the deck by adding multiple cards and drawing them in order.
     */
    @Test
    public void testDeckFIFOBehavior() {
        CardDeck d = new CardDeck(1);
        Card c1 = new Card(1);
        Card c2 = new Card(2);
        Card c3 = new Card(3);

        d.addCard(c1);
        d.addCard(c2);
        d.addCard(c3);

        assertEquals(c1, d.drawCard());
        assertEquals(c2, d.drawCard());
        assertEquals(c3, d.drawCard());
    }

    /**
     * Tests that the deck maintains the integrity of card references when extracting the last card.
     */
    @Test
    public void testDeckReferenceIntegrity() {
        CardDeck d = new CardDeck(1);
        Card c1 = new Card(7);
        Card c2 = new Card(3);

        // Add a card, draw it and check that the reference is maintained
        d.addCard(c1);
        assertEquals(c1, d.drawCard());

        // Verify that drawing a card now returns null since the deck is empty
        assertNull(d.drawCard());

        // Check the integrity of the reference by adding again and drawing after the deck had been emptied
        d.addCard(c2);
        assertEquals(c2, d.drawCard());
        assertNull(d.drawCard());
    }

    /**
     * Tests that the contents of the deck can be represented as a string correctly.
     */
    @Test
    public void testContentsAsString() {
        CardDeck d = new CardDeck(1);
        Card c1 = new Card(3);
        Card c2 = new Card(5);
        d.addCard(c1);
        d.addCard(c2);

        assertEquals("3 5", d.contentsAsString());
    }

    /**
     * Tests the thread-safety of CardDeck by concurrent adding from multiple threads.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS) // Timeout to assure liveness
    public void testConcurrentAdd() throws InterruptedException {
        final CardDeck d = new CardDeck(1);
        final int numThreads = 10;
        final int cardsPerThread = 100;
        Thread[] threads = new Thread[numThreads];
        final AtomicBoolean exceptionThrown = new AtomicBoolean(false);

        // Create threads that add cards to the deck concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < cardsPerThread; j++) {
                        d.addCard(new Card(threadIndex * cardsPerThread + j));
                    }
                } catch (Exception e) {
                    exceptionThrown.set(true);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        /**
         * Check that no exceptions were thrown during the concurrent adding of cards.
         * This way, if the count is not as expected, we can be sure that it is due to a concurrency issue and not an exception being thrown.
         */
        assertFalse(exceptionThrown.get());
        // Check that the total number of cards in the deck is as expected
        assertEquals(numThreads * cardsPerThread, d.getSize());
    }

   /**
     * Tests the thread-safety of CardDeck by concurrent drawing from multiple threads.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS) // Timeout to assure liveness
    public void testConcurrentDraw() throws InterruptedException {
        final CardDeck d = new CardDeck(1);
        final int numThreads = 10;
        final int cardsPerThread = 100;
        final int totalCards = numThreads * cardsPerThread;

        // Pre-fill the deck with cards
        for (int i = 0; i < totalCards; i++) {
            d.addCard(new Card(i));
        }

        Thread[] threads = new Thread[numThreads];
        final AtomicInteger totalCardsDrawn = new AtomicInteger(0);

        // Create threads that draw cards from the deck concurrently
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                while (true) {
                    Card c = d.drawCard();
                    if (c == null) {
                        break; // Deck exhausted for this thread (and in general)
                    }
                    // If a card was drawn, increment the total count of cards drawn
                    totalCardsDrawn.incrementAndGet();
                }
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        // Check that the total number of cards drawn is equal to the total number of cards added
        assertEquals(totalCards, totalCardsDrawn.get());
        // Check that the deck is now empty
        assertEquals(0, d.getSize());
        // Check that subsequent draws will now return null safely
        assertNull(d.drawCard());
    }

    /**
     * Tests the thread-safety of CardDeck by concurrent adding and drawing from multiple threads.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS) // Timeout to assure liveness
    public void testConcurrentAddAndDraw() throws InterruptedException {
        final CardDeck d = new CardDeck(1);
        final int numAdders = 5;
        final int numDrawers = 5;
        final int cardsPerAdder = 100;
        final int totalTargetCards = numAdders * cardsPerAdder;

        final AtomicInteger totalDrawnCount = new AtomicInteger(0);
        Thread[] adders = new Thread[numAdders];
        Thread[] drawers = new Thread[numDrawers];

        // Create adder threads that will add cards concurrently
        for (int i = 0; i < numAdders; i++) {
            final int threadIndex = i;
            adders[i] = new Thread(() -> {
                for (int j = 0; j < cardsPerAdder; j++) {
                    d.addCard(new Card(threadIndex * cardsPerAdder + j));
                }
            });
            adders[i].start();
        }

        // Create drawer threads that will attempt to draw cards concurrently
        for (int i = 0; i < numDrawers; i++) {
            drawers[i] = new Thread(() -> {
                while (totalDrawnCount.get() < totalTargetCards) {
                    Card c = d.drawCard();
                    // There is no break here as the adder threads may still be adding cards
                    if (c != null) {
                        totalDrawnCount.incrementAndGet();
                    } else {
                        // If no card was drawn, yield to allow adder threads to add cards
                        Thread.yield();
                    }
                }
            });
            drawers[i].start();
        }

        // Wait for all adder threads to finish adding cards
        for (Thread adder : adders) {
            adder.join();
        }

        // Wait for all drawer threads to finish drawing cards
        for (Thread drawer : drawers) {
            drawer.join();
        }

        // Check that all added cards were successfully drawn
        assertEquals(totalTargetCards, totalDrawnCount.get());
        // Check that the deck is now empty after all operations
        assertEquals(0, d.getSize());
    }

    /**
     * Tests that the output file for the deck is generated correctly and contains the expected content.
     */
    @Test
    public void testDeckOutputFileContent() throws IOException {
        CardDeck deck = new CardDeck(1);
        
        deck.addCard(new Card(5));
        deck.addCard(new Card(12));

        // This method would be called by CardGame once the game has finished
        deck.writeOutput();

        // Check that the output file was created
        Path path = Path.of("deck1_output.txt");
        assertTrue(Files.exists(path), "deck1_output.txt must be generated upon execution.");

        // Read the output file and verify its not empty
        List<String> lines = Files.readAllLines(path);
        assertFalse(lines.isEmpty(), "The output file should not be empty.");

        // Check the format of the contents
        assertEquals("deck1 contents: 5 12", lines.get(0),
                "The output file should contain the correct deck contents.");
    }
}