package org.example.auth;

import org.example.db.SqlRepository; // Use the new merged repo
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class AuthService {

    // CHANGE: Point directly to SqlRepository to avoid "cannot find symbol"
    private final SqlRepository repo;

    public AuthService() {
        this.repo = new SqlRepository();
    }

    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }

    public static boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }

    public User login(String username, String password) {
        User user = repo.findUserByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // FIX: Use the matches() method instead of .equals()
        // This compares the raw input to the hashed version in the DB
        if (!matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

//    public User register(String username, String password) {
//        if (repo.findUserByUsername(username) != null) {
//            throw new RuntimeException("Username already exists");
//        }
//
//        // FIX: Hash the password BEFORE creating the User object
//        String securePassword = hash(password);
//
//        // Pass 'securePassword' (the hash) instead of 'password' (raw text)
//        User newUser = new User(0, username, securePassword);
//
//        repo.saveUser(newUser);
//        return newUser;
//    }

    public User register(String username, String password) {

        // 1. Check for null or empty strings
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Validation Error: Username cannot be empty.");
        }

        // 2. Check username length (e.g., minimum 3 characters)
        if (username.trim().length() < 3) {
            throw new RuntimeException("Validation Error: Username must be at least 3 characters.");
        }

        // 3. Check password strength (e.g., minimum 5 characters)
        if (password == null || password.length() < 5) {
            throw new RuntimeException("Validation Error: Password must be at least 5 characters.");
        }

        // 4. Check for illegal characters (Security: prevent basic script injection)
        if (username.contains("|") || username.contains("'")) {
            throw new RuntimeException("Validation Error: Username contains illegal characters.");
        }


        if (repo.findUserByUsername(username) != null) {
            throw new RuntimeException("System Error: Username already exists.");
        }

        String securePassword = hash(password);
        User newUser = new User(0, username, securePassword);

        repo.saveUser(newUser);
        return newUser;
    }
}