package org.example.gameplayController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.NetworkManager;
import org.example.profile.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameController {

    private final Stage stage;
    private final GameContext ctx; // Renamed from 'context' to 'ctx' to match your methods

    private javafx.scene.control.TextField categoryField = new javafx.scene.control.TextField();
    private javafx.scene.control.TextField amountField = new javafx.scene.control.TextField();
    private javafx.scene.control.TextField descriptionField = new javafx.scene.control.TextField();

    private final MenuController menuController;
    private final GameFlowController gameFlowController;
    private final HistoryController historyController;
    private final AuthController authController;


    private final NetworkManager networkManager = new NetworkManager();
    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public GameController(Stage stage) {
        this.stage = stage;

        this.ctx = new GameContext(); // Initialize as ctx

        // Pass 'ctx' to all sub-controllers
        this.authController = new AuthController(stage, ctx, this);
        this.menuController = new MenuController(stage, ctx, this);
        this.gameFlowController = new GameFlowController(stage, ctx, this);
        this.historyController = new HistoryController(stage, ctx, this);
    }

    /* ---------- APP FLOW ---------- */

    public void start() {
        authController.showLogin();
    }

    public void showMainMenu() {
        menuController.showMainMenu();
    }

    public void showIntro() {
        menuController.showIntro();
    }

    public void resumeGame() {
        gameFlowController.resumeGame();
    }


    /* ---------- GAME ---------- */

    public void startGame(Profile profile) {
        ctx.currentProfile = profile;
        gameFlowController.startGame(profile);
    }

    /* ---------- HISTORY ---------- */

    public void showProfileHistory(Profile profile) {
        ctx.browsingProfileId = profile.getId();
        historyController.showSessionHistoryForProfile(profile.getId());
    }

    public void showSessionDetails(String sessionId) {
        historyController.showSessionDetails(sessionId);
    }


    /* ---------- PROFILE SELECTION ---------- */

    public void showProfileSelection() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        Label title = new Label("Welcome, " + ctx.currentUser.getUsername());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> profileList = new ListView<>();

        // --- UPDATED: Get profiles from Server ---
        List<Profile> userProfiles = new ArrayList<>();
        String response = networkManager.sendRequest("GET_PROFILES|" + ctx.currentUser.getId());

        if (response != null && response.startsWith("SUCCESS")) {
            try {
                String jsonPart = response.split("\\|")[1];
                ObjectMapper mapper = new ObjectMapper();
                // We use TypeReference because we are fetching a List, not a single object
                userProfiles = mapper.readValue(jsonPart, new TypeReference<List<Profile>>(){});

                for (Profile p : userProfiles) {
                    profileList.getItems().add(p.getName());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        final List<Profile> finalProfiles = userProfiles; // Needed for the lambda below

        Button selectButton = new Button("Select Profile");
        selectButton.setOnAction(e -> {
            String selectedName = profileList.getSelectionModel().getSelectedItem();
            if (selectedName != null) {
                ctx.currentProfile = finalProfiles.stream()
                        .filter(p -> p.getName().equals(selectedName))
                        .findFirst()
                        .orElse(null);
                showMainMenu();
            }
        });

        Button createButton = new Button("Create New Profile");
        // Pass 'this' or relevant data to the dialog
        createButton.setOnAction(e -> showCreateProfileDialog(profileList, finalProfiles));

        root.getChildren().addAll(title, new Label("Select your profile:"), profileList, selectButton, createButton);
        stage.setScene(new Scene(root, 600, 600));
    }


    private void showCreateProfileDialog(ListView<String> listView, List<Profile> listData) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Profile");
        dialog.setHeaderText("Enter profile name:");

        dialog.showAndWait().ifPresent(name -> {
            // 1. Ask the Server to create it
            String request = "CREATE_PROFILE|" + ctx.currentUser.getId() + "|" + name;
            String response = networkManager.sendRequest(request);

            if (response != null && response.startsWith("SUCCESS")) {
                try {
                    // 2. Turn the server's response back into a Profile object
                    String jsonPart = response.split("\\|")[1];
                    Profile newProfile = new ObjectMapper().readValue(jsonPart, Profile.class);

                    // 3. Update the UI
                    listData.add(newProfile);
                    listView.getItems().add(name);
                } catch (Exception ex) {
                    System.err.println("Failed to parse profile JSON: " + ex.getMessage());
                }
            }
        });
    }

    private void clearFields() {
        categoryField.clear();
        amountField.clear();
        descriptionField.clear();
    }

    private void showError(String message) {
        // This fulfills Requirement 9 by showing validation errors to the user
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshTransactionList() {
        // Requirement 6: This is where you'd refresh the UI after a DB change
        System.out.println("Transaction saved! Reloading view...");
    }

    private void handleAddTransaction() {
        String category = categoryField.getText();
        String amountStr = amountField.getText();
        String description = descriptionField.getText();

        // Requirement 9: Basic UI-level validation
        if (category.isEmpty() || amountStr.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // Prepare the message for the server
        String request = String.format("ADD_TRANSACTION|%d|%s|%s|%s",
                ctx.currentProfile.getId(),
                amountStr,
                category,
                description
        );

        // Send to server via NetworkManager (Requirement 4: Sockets)
        String response = networkManager.sendRequest(request);

        if (response.startsWith("SUCCESS")) {
            refreshTransactionList(); // Reload the list from the DB
            clearFields();
        } else {
            showError("Server error: " + response);
        }
    }


}