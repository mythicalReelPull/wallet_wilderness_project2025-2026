package org.example.server;

import org.example.auth.AuthService;
import org.example.auth.User;
import org.example.profile.Profile;
import org.example.db.SqlRepository; // Our new combined repo
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final SqlRepository repository; // ONE repo for everything
    private final ObjectMapper mapper = new ObjectMapper();
    private final AuthService authService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.authService = new AuthService();
        this.repository = new SqlRepository();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received: " + request);
                String[] parts = request.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        // Expecting: LOGIN|username|password
                        handleLogin(parts[1], parts[2], out);
                        break;
                    case "REGISTER":
                        // Expecting: REGISTER|username|password
                        handleRegister(parts[1], parts[2], out);
                        break;
                    case "GET_PROFILES":
                        // Expecting: GET_PROFILES|userId
                        handleGetProfiles(Long.parseLong(parts[1]), out);
                        break;
                    case "CREATE_PROFILE":
                        // Expecting: CREATE_PROFILE|userId|profileName
                        handleCreateProfile(Long.parseLong(parts[1]), parts[2], out);
                        break;
                    case "ADD_TRANSACTION":
                        // Expecting: ADD_TRANSACTION|profileId|amount|category|description
                        repository.saveTransaction(
                                Long.parseLong(parts[1]),
                                Double.parseDouble(parts[2]),
                                parts[3],
                                parts[4]
                        );
                        out.println("SUCCESS|Transaction saved");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }

    private void handleCreateProfile(long userId, String name, PrintWriter out) {
        // REQUIREMENT 9: Profile name validation
        if (name == null || name.trim().isEmpty() || name.length() > 20) {
            out.println("ERROR|Profile name must be between 1 and 20 characters.");
            return;
        }

        try {
            long profileId = System.currentTimeMillis();
            Profile newProfile = new Profile(profileId, userId, name.trim());
            repository.saveProfile(newProfile);
            out.println("SUCCESS|" + mapper.writeValueAsString(newProfile));
        } catch (Exception e) {
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleLogin(String username, String password, PrintWriter out) {
        try {

            User user = authService.login(username, password);
            out.println("SUCCESS|" + mapper.writeValueAsString(user));
        } catch (Exception e) {

            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleRegister(String username, String password, PrintWriter out) {
        try {
            // Validation happens inside this call now
            User newUser = authService.register(username, password);
            out.println("SUCCESS|User " + newUser.getUsername() + " registered successfully.");
        } catch (RuntimeException e) {
            // REQUIREMENT 9: Show the validation error on the UI/Screen
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleGetProfiles(long userId, PrintWriter out) {
        try {
            // Combined repository handles profiles too
            List<Profile> profiles = repository.findProfilesByUserId(userId);
            out.println("SUCCESS|" + mapper.writeValueAsString(profiles));
        } catch (Exception e) {
            out.println("ERROR|" + e.getMessage());
        }
    }
}