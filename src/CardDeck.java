// CardDeck.java

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
     * Returns the contents of the deck as a space-separated string.
     * Synchronized to avoid ConcurrentModificationException if another thread
     * tries to add or draw cards while this iterator is traversing the list.
     * 
     * @return a string representation of the deck's contents.
     */
    public synchronized String contentsAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).getValue());
            if (i < cards.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
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
