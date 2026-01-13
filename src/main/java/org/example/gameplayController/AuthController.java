package org.example.gameplayController;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.auth.*;

public class AuthController {

    private final Stage stage;
    private final GameContext ctx;
    private final GameController app;

    public AuthController(Stage stage, GameContext ctx, GameController app) {
        this.stage = stage;
        this.ctx = ctx;
        this.app = app;
    }

    public void showLogin() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        // This padding ensures the elements aren't hugging the top/bottom
        root.setStyle("-fx-padding: 50; -fx-background-color: #ffffff;");

        // 1. THE TITLE
        Label title = new Label("WALLET WILDERNESS");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 2. THE INPUTS (Set Max Width so they don't stretch)
        TextField username = new TextField();
        username.setPromptText("Username");
        username.setMaxWidth(220);
        username.setStyle("-fx-pref-height: 30px;");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setMaxWidth(220);
        password.setStyle("-fx-pref-height: 30px;");

        Label error = new Label();
        error.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // 3. THE BUTTONS
        Button login = new Button("Login");
        login.setMinWidth(220);
        login.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        Button register = new Button("Register New Account");
        register.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-underline: true;");

        register.setOnAction(e -> {
            String userText = username.getText();
            String passText = password.getText();

            if (userText.isEmpty() || passText.isEmpty()) {
                error.setText("Please enter details to register.");
                return;
            }

            String request = "REGISTER|" + userText + "|" + passText;
            String response = app.getNetworkManager().sendRequest(request);

            if (response != null && response.startsWith("SUCCESS")) {
                error.setText("Registered! Now click Login.");
                error.setStyle("-fx-text-fill: green;");
            } else {
                String msg = (response != null) ? response.split("\\|")[1] : "Server unreachable";
                error.setText(msg);
            }
        });

        // Add everything to the VBox
        root.getChildren().addAll(title, username, password, error, login, register);

        // --- THE CRITICAL PART: RENDER THE SCENE ---
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show(); // <--- If this isn't here, you see nothing!

        // LOGIN LOGIC (Shortened for clarity)
        login.setOnAction(e -> {
            String request = "LOGIN|" + username.getText() + "|" + password.getText();
            String response = app.getNetworkManager().sendRequest(request);

            if (response != null && response.startsWith("SUCCESS")) {
                try {
                    String userJson = response.split("\\|")[1];
                    ctx.currentUser = new ObjectMapper().readValue(userJson, User.class);
                    app.showProfileSelection(); // Move to next screen
                } catch (Exception ex) {
                    error.setText("Login failed: JSON Error");
                }
            } else {
                error.setText(response != null ? response.split("\\|")[1] : "Server Offline");
            }
        });


    }



}
