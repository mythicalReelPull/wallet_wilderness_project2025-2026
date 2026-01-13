package org.example;

import org.example.exception.InsufficientBalanceException;
import org.example.exception.InvalidTransactionException;

import java.util.HashMap;
import java.util.Map;

public class Player {

    private String name;
    private String profileName;
    private double balance;
    private double savings;
    private int freedomScore;
    private boolean trackingEnabled;
    private int stress;
    private Map<String, Double> categorySpending;
    private ExpenseTracker tracker;
    private String sessionId;

    public Player(String name, double startingBalance) {
        this.name = name;
        this.balance = startingBalance;
        this.savings = 0;
        this.freedomScore = 0;
        this.trackingEnabled = false;
        this.stress = 0;
        this.categorySpending = new HashMap<String, Double>();
        this.tracker = new ExpenseTracker();
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public double getSavings() {
        return savings;
    }

    public int getFreedomScore() {
        return freedomScore;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public int getStress() {
        return stress;
    }

    public void addStress(int amount) {
        this.stress += amount;
        if (this.stress < 0) {
            this.stress = 0;
        }
    }

    public void enableTracking() {
        this.trackingEnabled = true;
    }

    public void addFreedomScore(int points) {
        this.freedomScore += points;
    }

    public void spend(double amount) {
        this.balance -= amount;

        // Stress logic based on amount
        if (amount >= 100) {
            addStress(2);
        } else if (amount >= 40) {
            addStress(1);
        }

        // Tracking buffers stress
        if (trackingEnabled) {
            addStress(-1);
        }

        if (this.balance < 0) {
            addStress(2);
        }


    }


    public void save(double amount) throws InvalidTransactionException {
        if (amount == 0) {
            throw new InvalidTransactionException("Cannot save zero lei.");
        }

        this.savings += amount;
        if (amount < 0) {
            this.stress++;
        }

        tracker.addTransaction(new Transaction("savings", Math.abs(amount), amount > 0 ? "save" : "spend", sessionId, profileName));
    }

    public void spendCategory(String category, double amount)
            throws InsufficientBalanceException {

        if (amount > balance) {
            throw new InsufficientBalanceException(
                    "Not enough balance to spend " + amount + " lei"
            );
        }

        spend(amount); //strress logic handled in spend method

        categorySpending.put(
                category,
                categorySpending.getOrDefault(category, 0.0) + amount
        );

        tracker.addTransaction(
                new Transaction(category, amount, "spend", sessionId, profileName)

        );
    }


    public Map<String, Double> getCategorySpending() {
        return categorySpending;
    }

    public ExpenseTracker getTracker() {
        return tracker;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
