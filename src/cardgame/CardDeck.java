// CardDeck.java

package cardgame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread-safe class that represents a deck of cards.
 * Uses FIFO: draws from the front and adds to the back.
 */
public class CardDeck {
    private final int id;
    private final List<Card> cards;

    /**
     * Constructs a CardDeck with a specified id.
     * 
     * @param id the identifier for the deck, must be positive.
     * @throws IllegalArgumentException if the id is not positive.
     */
    public CardDeck(int id) throws IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("Deck id must be positive");
        }
        this.id = id;
        this.cards = new LinkedList<>();
    }

    /**
     * Returns the identifier for the deck.
     * No synchronization is needed as the id is immutable after construction.
     * 
     * @return the deck id.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the number of cards currently in the deck.
     * Synchronized to ensure that the data is up to date.
     * 
     * @return the size of the deck.
     */
    public synchronized int getSize() {
        return cards.size();
    }

    /**
     * Adds a card to the deck.
     * Synchronized to ensure thread safety.
     * 
     * @param c the card to add, must not be null.
     * @throws IllegalArgumentException if the card is null.
     */
    public synchronized void addCard(Card c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("Cannot add null card to deck"); 
        } else {
            cards.add(c);
        }
    }

    /**
     * Draws a card from the deck.
     * Synchronized to ensure thread safety.
     * 
     * @return the drawn card, or null if the deck is empty.
     */
    public synchronized Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        } else {
            // FIFO: remove from the front of the list
            return cards.remove(0);
        }
    }

    /**
     * Writes the current contents of the deck to an output file named "deck<ID>_output.txt".
     * Synchronized to ensure that the data is up to date.
     * 
     * @throws IOException
     */
    public synchronized void writeOutput() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("deck" + id + "_output.txt"))) {
            writer.write("deck" + id + " contents: " + contentsAsString());
            writer.newLine(); // End of line for better readability in the output file
        }
    }

    /**
     * Returns the contents of the deck as a space-separated string.
     * Synchronized to avoid ConcurrentModificationException if another thread
     * tries to add or draw cards while this iterator is traversing the list.
     * 
     * @return a string representation of the deck's contents.
     */
    public synchronized String contentsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb.append(c.getValue()).append(" ");
        }
        return sb.toString().trim(); // Remove the last space for cleaner output
    }

    /**
     * Returns a string representation of the deck, including its id and contents.
     * Synchronized to ensure that the data is up to date.
     * 
     * @return a string representation of the deck.
     */
    @Override
    public synchronized String toString() {
        return "Deck " + id + ": " + contentsAsString();
    }
}
