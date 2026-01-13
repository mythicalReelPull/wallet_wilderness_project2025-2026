package org.example;

import org.example.exception.InsufficientBalanceException;
import org.example.exception.InvalidTransactionException;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class GameLogic {

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();
    private Player player;
    private Properties config = new Properties();

    public void start() {
        loadConfig();
        intro();
        scene1CampusCafe();
        scene2OnlineSale();
        scene3UnexpectedBill();
        scene4RandomEvent();
        showSummary();
        scanner.close();
    }

    private void loadConfig() {
        try {
            config.load(new FileInputStream("config.properties"));
            System.out.println("⚙️ Config loaded: save.mode = " + config.getProperty("save.mode"));
        } catch (Exception e) {
            System.out.println("⚠️ Could not load config: " + e.getMessage());
        }
    }

    private void intro() {
        System.out.println("=== Wallet Wilderness ===");
        System.out.print("Enter your name, explorer: ");
        String name = scanner.nextLine();

        System.out.println("Let's roll the dice to determine your weekly budget!");
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        int extra = (die1 + die2) * 10;
        int startingBalance = 150 + extra;

        System.out.println("🎲 You rolled a " + die1 + " and a " + die2 + ".");
        System.out.println("Your starting weekly balance is: " + startingBalance + " lei!");
        player = new Player(name, startingBalance);

        String sessionId = "session_" + System.currentTimeMillis();
        player.setSessionId(sessionId);
        System.out.println("📎 Session ID: " + sessionId);

        try {
            String loadMode = config.getProperty("save.mode", "ser");
            if ("json".equalsIgnoreCase(loadMode)) {
                player.getTracker().loadFromJson("transactions.json");
            } else {
                player.getTracker().loadFromFile("transactions.ser");
            }
            System.out.println("📂 Previous transactions loaded from " + loadMode);
        } catch (Exception e) {
            System.out.println("⚠️ Could not load previous data: " + e.getMessage());
        }
    }

    private void scene1CampusCafe() {
        System.out.println("\n--- Day 1: The Campus Café ---");
        System.out.println("You’re hungry after class. Your friends invite you to:");
        System.out.println("1) Get a fancy meal and drink (40 lei).");
        System.out.println("2) Get a simple snack (15 lei).");
        System.out.println("3) Check your budget and then decide (start tracking).");

        int choice = readChoice(1, 3);

        switch (choice) {
            case 1:
                player.spend(40);
                System.out.println("You enjoy a big meal. It was nice… but pricey.");
                break;
            case 2:
                try {
                    player.spendCategory("food", 15);
                    player.addFreedomScore(1);
                    System.out.println("You keep it simple and spend less.");
                } catch (InsufficientBalanceException e) {
                    System.out.println("⚠️ " + e.getMessage());
                    player.addStress(1);
                }
                break;
            case 3:
                player.enableTracking();
                player.addFreedomScore(2);
                System.out.println("You open your expense tracking app and see your whole week at a glance.");
                System.out.println("You decide to go for the simple snack to stay on budget (15 lei).");
                try {
                    player.spendCategory("food", 15);
                } catch (InsufficientBalanceException e) {
                    System.out.println("⚠️ " + e.getMessage());
                    player.addStress(1);
                }
                break;
        }

        showStatus();
        waitForEnter();
    }

    private void scene2OnlineSale() {
        System.out.println("\n--- Day 2: The Online Sale ---");
        System.out.println("You see a flash sale on gadgets.");

        if (player.isTrackingEnabled()) {
            System.out.println("1) Check your tracked expenses, then decide.");
            System.out.println("2) Ignore your plan and buy the expensive gadget (150 lei).");
            System.out.println("3) Skip the sale and save for something that truly matters (add 100 lei to savings).");

            int choice = readChoice(1, 3);

            switch (choice) {
                case 1:
                    System.out.println("You open your tracker and realize this week is tighter than you thought.");
                    try {
                        player.spendCategory("shopping", 40);
                        player.addFreedomScore(2);
                    } catch (InsufficientBalanceException e) {
                        System.out.println("⚠️ " + e.getMessage());
                        player.addStress(1);
                    }
                    break;
                case 2:
                    try {
                        player.spendCategory("shopping", 150);
                    } catch (InsufficientBalanceException e) {
                        System.out.println("⚠️ " + e.getMessage());
                        player.addStress(2);
                    }
                    break;
                case 3:
                    try {
                        player.save(100);
                        player.addFreedomScore(3);
                    } catch (InvalidTransactionException e) {
                        System.out.println("⚠️ " + e.getMessage());
                    }
                    break;
            }
        } else {
            System.out.println("1) Buy the expensive gadget (150 lei) – YOLO.");
            System.out.println("2) Only buy a small accessory (40 lei).");
            System.out.println("3) Start tracking your expenses before buying anything.");

            int choice = readChoice(1, 3);

            switch (choice) {
                case 1:
                    player.spend(150);
                    break;
                case 2:
                    player.spend(40);
                    player.addFreedomScore(1);
                    break;
                case 3:
                    player.enableTracking();
                    player.addFreedomScore(2);
                    System.out.println("After seeing your budget, you decide not to buy anything right now.");
                    break;
            }
        }

        showStatus();
        waitForEnter();
    }

    private void scene3UnexpectedBill() {
        System.out.println("\n--- Day 3: The Unexpected Bill ---");
        System.out.println("Your landlord messages you: some building cost got recalculated.");
        System.out.println("You must pay an unexpected 120 lei.");

        if (player.getSavings() >= 120) {
            try {
                player.save(-120);
                System.out.println("Because you have savings, you just pay the bill from your savings.");
                player.addFreedomScore(3);
            } catch (InvalidTransactionException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        } else {
            System.out.println("You don’t have enough savings, so this comes from your current balance.");
            try {
                player.spendCategory("bills", 120);
            } catch (InsufficientBalanceException e) {
                System.out.println("⚠️ " + e.getMessage());
                player.addStress(2);
            }

            if (player.isTrackingEnabled()) {
                System.out.println("At least you knew this week was tight thanks to your tracker.");
                player.addFreedomScore(1);
            } else {
                System.out.println("You’re surprised and stressed because you didn’t see this coming.");
            }
        }

        showStatus();
        waitForEnter();
    }

    private void scene4RandomEvent() {
        System.out.println("\n--- Day 4: The Unexpected Event ---");
        int event = random.nextInt(2);

        if (event == 0) {
            System.out.println("🎉 Surprise! You sold an old textbook online and made 30 lei.");
            try {
                player.save(30);
                player.addFreedomScore(1);
            } catch (InvalidTransactionException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        } else {
            System.out.println("😞 Oh no! Your bike tire blew out. You need to pay 50 lei for repairs.");
            try {
                player.spendCategory("transport", 50);
            } catch (InsufficientBalanceException e) {
                System.out.println("⚠️ " + e.getMessage());
                player.addStress(2);
            }
        }

        showStatus();
        waitForEnter();
    }

    public Player getPlayer() {
        return player;
    }

    private void showSummary() {
        System.out.println("\n=== Week Summary for " + player.getName() + " ===");
        System.out.println("Current balance: " + player.getBalance() + " lei");
        System.out.println("Savings:        " + player.getSavings() + " lei");
        System.out.println("Freedom score:  " + player.getFreedomScore());
        System.out.println("Stress level:   " + player.getStress());
        System.out.println("Tracking used:  " + (player.isTrackingEnabled() ? "YES" : "NO"));

        if (player.isTrackingEnabled()) {
            System.out.println("\nSpending breakdown by category:");
            for (Map.Entry<String, Double> entry : player.getCategorySpending().entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue() + " lei");
            }
        }

        try {
            String saveMode = config.getProperty("save.mode", "ser");
            if ("json".equalsIgnoreCase(saveMode)) {
                player.getTracker().saveToJson("transactions.json");
            } else {
                player.getTracker().saveToFile("transactions.ser");
            }
            System.out.println("💾 Transactions saved using mode: " + saveMode);
        } catch (Exception e) {
            System.out.println("⚠️ Could not save data: " + e.getMessage());
        }

        showTransactionHistory();
    }

    private void showTransactionHistory() {
        System.out.println("\n=== Transaction History (This Session) ===");
        String sessionId = player.getSessionId();

        boolean found = false;
        for (Transaction t : player.getTracker().getTransactions()) {
            if (sessionId.equals(t.getSessionId())) {
                System.out.println("- " + t);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No transactions recorded for this session.");
        }
    }


    private int readChoice(int min, int max) {
        while (true) {
            System.out.print("Choose (" + min + "-" + max + "): ");
            String line = scanner.nextLine();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid option. Please enter a number between " + min + " and " + max + ".");
        }
    }

    private void showStatus() {
        if (!player.isTrackingEnabled()) {
            return; // Skip showing status if tracking is off
        }

        System.out.println("\n[Status]");
        System.out.println("Balance: " + player.getBalance() + " lei");
        System.out.println("Savings: " + player.getSavings() + " lei");
        System.out.println("Freedom score: " + player.getFreedomScore());
        System.out.println("Stress: " + player.getStress());
        System.out.println("Tracking: " + (player.isTrackingEnabled() ? "ON" : "OFF"));
    }



    private void waitForEnter() {
        System.out.print("\n(Press Enter to continue...)");
        scanner.nextLine();
    }
}
