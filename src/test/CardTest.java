// CardTest.java

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Card class.
 */
public class CardTest {
    /**
     * Tests the constructor with a valid value and checks if the value is set correctly.
     */
    @Test
    public void testConstructorValidValue() {
        Card c = new Card(3);
        assertEquals(3, c.getValue());
    }

    /**
     * Tests the constructor with an invalid value and expects an IllegalArgumentException to be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidValue() {
        new Card(-1);
    }

    /**
     * Tests that a card value can be converted to a string correctly using the toString method.
     */
    @Test
    public void testToString() {
        Card c = new Card(5);
        assertEquals("5", c.toString());
    }
}