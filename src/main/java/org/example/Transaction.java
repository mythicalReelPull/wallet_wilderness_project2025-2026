package org.example;

import java.io.Serializable;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private long profileId;
    private String profileName; // <--- ADD THIS
    private String category;
    private double amount;
    private String type;
    private String sessionId;

    public Transaction() {}

    public Transaction(String category, double amount, String type,
                       String sessionId, String profileName) {
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.sessionId = sessionId;
        this.profileName = profileName;
        // Since we don't have an ID here, we can set it to a default like 0 or -1
        this.profileId = -1;
    }


    public Transaction(String category, double amount, String type,
                       String sessionId, String profileName, long profileId) {
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.sessionId = sessionId;
        this.profileName = profileName;
        this.profileId = profileId;
    }


    public String getProfileName() {
        return this.profileName;
    }

    public long getProfileId() {
        return profileId;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return type.toUpperCase() + ": " + amount + " lei [" + category + "] (Session: " + sessionId + ")";
    }
}