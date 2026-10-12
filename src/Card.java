// Card.java

/**
 * Thread-safe class that represents a card with a non-negative integer value.
 */
public class Card {
    private final int value;

    /**
     * Card constructor that takes an integer value.
     * 
     * @param value the value of the card, must be non-negative.
     * @throws IllegalArgumentException if the value is negative.
     */
    public Card(int value) throws IllegalArgumentException {
        if (value < 0) {
            throw new IllegalArgumentException("Card value must be non-negative");
        }
        this.value = value;
    }

    /**
     * Returns the value of the card.
     * 
     * @return the value of the card.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a string representation of the card.
     * 
     * @return a string representation of the card.
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
