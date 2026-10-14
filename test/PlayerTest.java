import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Tests essential behaviors, invariants, discard strategies, and output file generation.
 */
public class PlayerTest {

    @BeforeEach
    public void setUp() {
        // Reset static game state before each test to guarantee test isolation
        Player.resetGame();
    }

    /**
     * Tests that cards can be added to the hand.
     *
     * @throws IOException
     */
    @Test
    public void testAddingToHand() throws IOException {
        
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player p = new Player(1, left, right);

        // Add cards.
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(3));
        p.addCardToHand(new Card(4));

        assertEquals(4, p.getHand().size(), "Player's hand should contain 4 cards after adding.");
    }

    /**
     * Test the winning hand detection (four identical cards).
     *
     *
     * @throws IOException
     */
    @Test
    public void testHasWinningHand() throws IOException {
      
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player p = new Player(1, left, right);

        // Add four cards with the same value to simulate a winning hand.
        for (int i = 0; i < 4; i++) {
            p.addCardToHand(new Card(5));
        }
        assertTrue(p.checkWinCondition(), "Player should have a winning hand with four identical cards.");
    }
    
     /**
     * Test the preferred card discard strategy. The player should discard the non-preferred card when possible.
     */
    @Test
    public void testPreferredCardStrategy() {

        CardDeck left = new CardDeck(3);
        CardDeck right = new CardDeck(4);
        Player p = new Player(2, left, right);

        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(5)); // Only non-preferred card

        // Choose card to discard
        left.addCard(2);
        p.drawAndDiscard(); // Simulate a turn where the player draws and discards
        Card discarded = right.drawCard();
        assertNotNull(discarded, "The discarded card should not be null.");
        assertEquals(5, discarded.getValue(), "Player should discard card 5 (non-preferred) to keep preferred cards (2).");
    }
    /**
     * Discard strategy when hand contains only preferred cards
     */
    @Test

    public void testDiscardWhenAllCardsArePreferred() {
        CardDeck left = new CardDeck(3);
        CardDeck right = new CardDeck(4);
        Player p = new Player(1, left, right);
        // Temporary hand with only preferred cards
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(1));

        left.addCard(1);
        p.drawAndDiscard(); // Simulate a turn where the player draws and discards
        Card discarded = right.drawCard();
        assertNotNull(discarded, "Player must select a card even if all cards are preferred to prevent game stagnation.");
        assertEquals(1, discarded.getValue(), "Discarded card value should be 1.");
    }
    /**
     * Draw and discard action maintains 4 cards in hand: It's atomic
     */
    @Test
    public void testAtomicDrawAndDiscardHandInvariant() throws IOException {
       
        CardDeck left = new CardDeck(3);
        CardDeck right = new CardDeck(4);
        Player p = new Player(1, left, right);
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(1));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(3));

        // Add a card to the left deck to draw from
        left.addCard(new Card(9));

        // Execute atomic turn
        p.drawAndDiscard();

        // Verify invariant and deck state
        assertEquals(4, p.getHand().size(), "Hand must retain exactly 4 cards after atomic draw-and-discard.");
        assertEquals(1, right.getSize(), "Right deck (right) should receive the discarded card.");
    }
    /**
     * Tests that the output file is generated correctly and contains expected content
     */
    @Test
    public void testOutputFileCreation() throws IOException {
        CardDeck left = new CardDeck(3);
        CardDeck right = new CardDeck(4);
        Player p = new Player(1, left, right);
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(2));
        p.addCardToHand(new Card(2));

        // Run player thread to simulate immediate win execution
        Thread player = new Thread(p);
        player.start();

        try {
            player.join(2000); // Wait for thread completion
        } catch (InterruptedException e) {
            fail("Player thread was interrupted.");
        }
        File file = new File("player1_output.txt");
        assertTrue(file.exists(), "player1_output.txt must be generated upon execution.");

        List<String> lines = Files.readAllLines(file.toPath());
        assertFalse(lines.isEmpty(), "Player output file should not be empty.");
        assertTrue(lines.get(0).contains("player 1 initial hand"), "First line must record initial hand.");
    }
}