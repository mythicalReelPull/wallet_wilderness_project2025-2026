package org.example.db;

import org.example.auth.User;
import org.example.profile.Profile;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlRepository {

    // --- USER METHODS (Requirement 7: Insert) ---
    public void saveUser(User user) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void saveProfile(Profile profile) {
        String sql = "INSERT INTO profiles(id, userId, name) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, profile.getId());
            pstmt.setLong(2, profile.getUserId());
            pstmt.setString(3, profile.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    public List<Profile> findProfilesByUserId(long userId) {
        List<Profile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM profiles WHERE userId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                profiles.add(new Profile(rs.getLong("id"), rs.getLong("userId"), rs.getString("name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return profiles;
    }

    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Mapping DB columns to your User object
                return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Save Transaction
    public void saveTransaction(long profileId, double amount, String category, String description) {
        String sql = "INSERT INTO transactions (profile_id, amount, category, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, profileId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, category);
            pstmt.setString(4, description);

            pstmt.executeUpdate(); // Requirement 7: Data insertion
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}