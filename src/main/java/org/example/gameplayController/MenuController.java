package org.example.gameplayController;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.profile.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuController {

    private final Stage stage;
    private final GameContext ctx;
    private final GameController app;
    private final ObjectMapper mapper = new ObjectMapper();

    public MenuController(Stage stage, GameContext ctx, GameController app) {
        this.stage = stage;
        this.ctx = ctx;
        this.app = app;
    }

    /* =========================================================
       MAIN MENU (Fetching profiles from Server)
       ========================================================= */

    public void showMainMenu() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Wallet Wilderness");
        title.getStyleClass().add("title");

        VBox profilesBox = new VBox(8);
        profilesBox.setAlignment(Pos.CENTER);

        // --- UPDATED: Ask Server for profiles ---
        String response = app.getNetworkManager().sendRequest("GET_PROFILES|" + ctx.currentUser.getId());
        List<Profile> profiles = new ArrayList<>();

        if (response != null && response.startsWith("SUCCESS")) {
            try {
                String jsonPart = response.split("\\|")[1];
                profiles = Arrays.asList(mapper.readValue(jsonPart, Profile[].class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (profiles.isEmpty()) {
            profilesBox.getChildren().add(new Label("No profiles yet"));
        } else {
            for (Profile profile : profiles) {
                Button btn = new Button("• " + profile.getName());
                btn.setOnAction(e -> {
                    ctx.currentProfile = profile;
                    app.showProfileHistory(ctx.currentProfile);
                });
                profilesBox.getChildren().add(btn);
            }
        }

        Button newGameBtn = new Button("▶ Start New Game");
        newGameBtn.setOnAction(e -> app.startGame(ctx.currentProfile));

        Button exitBtn = new Button("❌ Exit");
        exitBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, newGameBtn, profilesBox, exitBtn);
        stage.setScene(new Scene(root, 600, 600));
    }

    /* =========================================================
       INTRO / CREATE PROFILE (Sending to Server)
       ========================================================= */

    public void showIntro() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Choose a name for your profile");
        TextField nameField = new TextField();
        Button startBtn = new Button("Start Adventure");

        startBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;

            // --- THE FIX: Send to Server instead of local Repo ---
            String request = "CREATE_PROFILE|" + ctx.currentUser.getId() + "|" + name;
            String response = app.getNetworkManager().sendRequest(request);

            if (response != null && response.startsWith("SUCCESS")) {
                try {
                    String jsonPart = response.split("\\|")[1];
                    Profile newProfile = mapper.readValue(jsonPart, Profile.class);

                    ctx.currentProfile = newProfile;
                    app.startGame(newProfile);
                } catch (Exception ex) {
                    subtitle.setText("Error reading server data.");
                }
            } else {
                subtitle.setText("Server error creating profile.");
            }
        });

        root.getChildren().addAll(new Label("Create Profile"), subtitle, nameField, startBtn);
        stage.setScene(new Scene(root, 600, 600));
    }
}