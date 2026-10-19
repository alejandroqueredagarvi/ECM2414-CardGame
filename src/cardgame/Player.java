// Player.java

package cardgame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Represents a player in the card game simulation.
 * Each player runs in its own thread, interacting with adjacent card decks 
 * in a ring topology.
 */
public class Player implements Runnable {

    private final int playerID;
    private final List<Card> hand;
    private final CardDeck leftDeck;   // Deck i (draw deck)
    private final CardDeck rightDeck;  // Deck i+1 (discard deck)
    private final Random random;
    private final String fileName;

   
    private static volatile boolean gameOver = false;
    private static volatile int winnerID = -1; //Global variable across all player threads

    /**
     * Constructs a new Player with an ID 
     * @param playerID the unique identifier for the player.
     * @param leftDeck to draw from.
     * @param rightDeck to discard to.
     */
    public Player(int playerID, CardDeck leftDeck, CardDeck rightDeck) {
        this.playerID = playerID;
        this.hand = new LinkedList<>();
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.random = new Random();
        this.fileName = "player" + playerID + "_output.txt";
        
    }

    /**
     * Adds a card to the player's hand.
     * 
     * @param card the card to add.
     */
    public void addCardToHand(Card card) {
        this.hand.add(card);
    }

    /**
     * Returns a copy of the player's hand.
     * Creates a new ArrayList containing the same card references to protect internal state 
     * encapsulation and prevent ConcurrentModificationException across threads.
     * 
     * @return a new List containing the cards in the player's hand.
     */
    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * Gets the player's ID.
     *
     * @return the ID.
     */
    public int getPlayerID() {
        return playerID;
    }

    /**
     * Checks if all 4 cards in the hand have the same face value.
     * 
     * @return true if the player has a winning hand, false otherwise.
     */
    public boolean checkWinCondition() {
        if (hand.size() < 4) {
            return false;
        }
        int value = hand.get(0).getValue(); //get the value of the first card
        for (Card card : hand) {
            if (card.getValue() != value) { // if a card has a different value, return false
                return false;
            }
        }
        return true;
    }

    /**
     * Selects a non-preferred card to discard.
     * The preferred denomination matches the player's ID.
     * Doesn't need to be synchronized because it's only called within a synchronized block in run().
     * 
     * @return the card selected for discarding.
     */
    private Card chooseCardToDiscard() {  
        List<Card> nonPreferred = new ArrayList<>();
        for (Card card : hand) {
            if (card.getValue() != playerID) {
                nonPreferred.add(card);
            }
        }

        // If all cards in hand are preferred, pick randomly from full hand
        if (nonPreferred.isEmpty()) {
            return hand.get(random.nextInt(hand.size()));
        }

        // Pick a random non-preferred card 
        return nonPreferred.get(random.nextInt(nonPreferred.size()));
    }

    /**
     * Handles winning operations: updating global flags, printing to terminal,
     * and writing winning details to the output file.
     * 
     * @throws IOException
     */
    private void declareWin() throws IOException {
        synchronized (Player.class) {
            if (gameOver) {
                return; // In the case two players win simultaneously, the first to acquire the lock will declare victory.
            }
            gameOver = true;
            winnerID = playerID;
        }
        System.out.println("player " + playerID + " wins");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("player " + playerID + " wins");
            writer.newLine();
            writer.write("player " + playerID + " exits");
            writer.newLine();
            writer.write("player " + playerID + " final hand: " + getHandString());
            writer.newLine();
        }
    }

    /**
     * Draws a card from the left deck and discards a card from the hand to the right deck atomically.
     * 
     * @throws IOException
     */
    public void drawAndDiscard() throws IOException {
         // We want to avoid circular wait conditions, so we always acquire locks in order of increasing deck ID.
                CardDeck lock1, lock2;
                if ( leftDeck.getId() < rightDeck.getId() ) {
                    lock1 = leftDeck;
                    lock2 = rightDeck;
                } else {
                    lock1 = rightDeck;  
                    lock2 = leftDeck;
                } 

                // Variable for output text
                String logMessage = null;
                    
                synchronized (lock1) {
                    synchronized (lock2) {
                        Card drawnCard = leftDeck.drawCard();
                        if (drawnCard != null) {
                            hand.add(drawnCard);
                            Card discardCard = chooseCardToDiscard();
                            hand.remove(discardCard);
                            rightDeck.addCard(discardCard);
                            
                            // Construct the log message to write to the output file after releasing locks
                            logMessage = "player " + playerID + " draws a " + drawnCard.getValue() + " from deck " + leftDeck.getId() + "\n" +
                                        "player " + playerID + " discards a " + discardCard.getValue() + " to deck " + rightDeck.getId() + "\n" +
                                        "player " + playerID + " current hand is " + getHandString();
                        } else {
                            return; // Skip this turn if the left deck is empty
                        }
                    }
                }
                
                // Write the log message to the output file after releasing locks
                if (logMessage != null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                        writer.write(logMessage);
                        writer.newLine();
                    }
                    if (checkWinCondition()) {
                        declareWin();
                    }
                }
    }

    /**
     * Returns a string representation of the player's current hand.
     * 
     * @return a space-separated string of card values in the hand.
     */
    private String getHandString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : hand) {
            sb.append(c.getValue()).append(" ");
        }
        return sb.toString().trim(); // Remove the last space for cleaner output
    }

    /**
     * Resets global state variables for test suites.
     */
    public static void resetGame() {
        gameOver = false;
        winnerID = -1;
    }

    /**
     * Main thread execution method for the player. 
     */
    @Override
    public void run() {
        try {
            // Write initial hand to output file opening and closing the stream
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("player " + playerID + " initial hand " + getHandString());
                writer.newLine();
            }

            // Check if the initial hand is already a winning hand
            if (checkWinCondition()) {
                declareWin();
                return;
            }

            // Main gameplay loop - sin escritores globales que retengan el I/O
            while (!gameOver) {
                drawAndDiscard();
                Thread.sleep(10); // Ensures true concurrent execution without starving other threads
            }

            // Handle game exit when another player wins
            if (gameOver && winnerID != playerID) {
                // Abre el archivo una última vez para el mensaje de derrota
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                    writer.write("player " + winnerID + " has informed player " + playerID + " that player " + winnerID + " has won");
                    writer.newLine();
                    writer.write("player " + playerID + " exits");
                    writer.newLine();
                    writer.write("player " + playerID + " hand: " + getHandString());
                    writer.newLine();
                }
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}