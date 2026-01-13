package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Interfaces.Storable;
import Interfaces.Trackable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ExpenseTracker implements Trackable, Storable {

    private List<Transaction> transactions;

    public ExpenseTracker() {
        this.transactions = new ArrayList<>();
    }

    /* =========================================================
       TRANSACTIONS
       ========================================================= */

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    /**
     * ✅ Correctly filters transactions by profileId
     */
    public List<Transaction> getTransactionsforProfile(long profileId) {
        return transactions.stream()
                .filter(t -> t.getProfileId() == profileId)
                .collect(Collectors.toList());
    }

    public double getTotalForCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /* =========================================================
       TRACKABLE
       ========================================================= */

    @Override
    public double calculateTotalSpent() {
        return transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("spend"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public int getTransactionCount() {
        return transactions.size();
    }

    /* =========================================================
       BINARY STORAGE (LEGACY)
       ========================================================= */

    @Override
    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(transactions);
            System.out.println("📁 Data saved to " + filename);
        } catch (IOException e) {
            System.out.println("⚠️ Error saving data: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            transactions = (List<Transaction>) in.readObject();
            System.out.println("Data loaded from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    /* =========================================================
       JSON STORAGE (ACTIVE)
       ========================================================= */

    public void saveToJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fileName);
        List<Transaction> allHistory = new ArrayList<>();

        try {
            // 1. Load existing history if file exists
            if (file.exists()) {
                Transaction[] existing = mapper.readValue(file, Transaction[].class);
                allHistory.addAll(Arrays.asList(existing));
            }

            // 2. Add ONLY the new transactions from this session
            // (Prevent duplicates by checking if they are already in the file)
            for (Transaction t : this.transactions) {
                if (!allHistory.contains(t)) {
                    allHistory.add(t);
                }
            }

            // 3. Write everything back to the file
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, allHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromJson(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Transaction[] loaded =
                    mapper.readValue(Paths.get(filename).toFile(), Transaction[].class);

            transactions.clear();
            transactions.addAll(Arrays.asList(loaded));

            System.out.println("✅ Transactions loaded from JSON file " + filename);

        } catch (Exception e) {
            System.out.println("⚠️ Failed to load JSON: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
}
