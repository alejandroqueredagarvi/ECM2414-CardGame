import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Executable entry point for the card game simulation.
 * Manages user CLI prompts, pack validation, card dealing, and thread coordination.
 */
public class CardGame {

    private final int numPlayers;
    private final List<CardDeck> decks;
    private final List<Player> players;
    private final List<Thread> playerThreads;
    private final List<Integer> pack;

    /**
     * Initializes the CardGame with player count and card pack.
     *
     * @param numPlayers Number of players/decks in the game.
     * @param pack       List of integer values representing card denominations.
     */
    public CardGame(int numPlayers, List<Integer> pack) {
        this.numPlayers = numPlayers;
        this.pack = pack;
        this.decks = new ArrayList<>();
        this.players = new ArrayList<>();
        this.playerThreads = new ArrayList<>();

        // Initialize n card decks
        for (int i = 1; i <= numPlayers; i++) {
            decks.add(new CardDeck(i));
        }

        // Initialize n players in a circular ring topology
        for (int i = 1; i <= numPlayers; i++) {
            CardDeck leftDeck = decks.get(i - 1);
            CardDeck rightDeck = decks.get(i % numPlayers);
            players.add(new Player(i, leftDeck, rightDeck));
        }
    }
    /*
        Returns the number of players in the game.
        @return the number of players.
    */
   
    public int getNumPlayers() {
        return numPlayers;
    }
    /**
     * Gets a protective copy of the players list 
     * @return A new List containing the Player instances.
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(this.players);
    }

    /**
     * Gets a protective copy of the decks list
     *
     * @return A new List containing the CardDeck instances.
     */
    public List<CardDeck> getDecks() {
        return new ArrayList<>(this.decks);
    }
    /**
     * Distributes cards in round-robin order to player hands and decks.
     */
    public void distributeCards() {
        int packIndex = 0;

        // 1. Distribute 4 cards to each player round-robin
        for (int round = 0; round < 4; round++) {
            for (Player player : players) {
                player.addCardToHand(new Card(pack.get(packIndex)));
                packIndex++;   
            }
        }

        // 2. Distribute remaining 4*n cards to decks round-robin
        while (packIndex < pack.size()) {
            for (CardDeck deck : decks) {
                if (packIndex < pack.size()) {
                    deck.addCard(new Card(pack.get(packIndex)));
                    packIndex++;
                }
            }
        }
    }

    /**
     * Starts player threads and waits for all threads to terminate.
     */
    public void startGame() {
        for (Player player : players) {
            Thread thread = new Thread(player, "Player-" + player.getPlayerID());
            playerThreads.add(thread);
            thread.start();
        }

        // Wait for all player threads to finish execution
        for (Thread thread : playerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Generate output files for all decks upon game completion
        writeDeckOutputs();
    }

    /**
     * Writes final deck states to corresponding deckX_output.txt files.
     */
    private void writeDeckOutputs() {
        for (CardDeck deck : decks) {
            String filename = "deck" + deck.getId() + "_output.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write("deck" + deck.getId() + " contents: " + deck.toString());
            } catch (IOException e) {
                System.err.println("Error writing " + filename + ": " + e.getMessage());
            }
        }
    }

    /**
     * Reads and validates the pack file (must contain exactly 8*n non-negative integers).
     *
     * @param filepath   Path to the pack file.
     * @param numPlayers Number of players in current game.
     * @return List of card values, or null if invalid.
     */
    public static List<Integer> loadAndValidatePack(String filepath, int numPlayers) {
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        List<Integer> cards = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int val = Integer.parseInt(line);
                if (val < 0) {
                    return null; // Card values must be non-negative
                }
                cards.add(val);
            }
        } catch (Exception e) {
            return null;
        }

        // Validate pack size equals exactly 8 * n
        if (cards.size() != 8 * numPlayers) {
            return null;
        }

        return cards;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = 0;

        // Prompt user for player count until valid input is received
        while (true) {
            System.out.println("Please enter the number of players:");
            try {
                String input = scanner.nextLine().trim();
                n = Integer.parseInt(input);
                if (n > 0) {
                    break;
                }
                System.out.println("Invalid number of players. Must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }

        // Prompt user for valid pack file path
        List<Integer> pack = null;
        while (pack == null) {
            System.out.println("Please enter location of pack to load:");
            String filepath = scanner.nextLine().trim();
            pack = loadAndValidatePack(filepath, n);
            if (pack == null) {
                System.out.println("Invalid pack file. Please try again.");
            }
        }

        // Instantiate game, deal cards, and start execution
        CardGame game = new CardGame(n, pack);
        game.distributeCards();
        game.startGame();
        scanner.close();
    }
}
