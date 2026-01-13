package org.example.auth;

public class User {
    private long id;
    private String username;
    private String password; // This was likely missing or named differently

    // Constructor
    public User(long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Default constructor for Jackson/ObjectMapper (Requirement 4)
    public User() {}

    // Getters
    public long getId() { return id; }
    public String getUsername() { return username; }

    // This provides the "password" symbol that SqlRepository and ClientHandler need
    public String getPassword() { return password; }

    // Setters (Needed for ObjectMapper)
    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}