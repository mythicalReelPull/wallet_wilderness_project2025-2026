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
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label error = new Label();

        Button login = new Button("Login");
        Button register = new Button("Register");

        login.setOnAction(e -> {
            // 1. Send the request via the network
            String request = "LOGIN|" + username.getText() + "|" + password.getText();
            String response = app.getNetworkManager().sendRequest(request);

            // 2. Parse the response
            if (response != null && response.startsWith("SUCCESS")) {
                try {
                    // The server sends "SUCCESS|{JSON_USER_DATA}"
                    String userJson = response.split("\\|")[1];
                    ObjectMapper mapper = new ObjectMapper();

                    // Reconstruct the User object from the JSON string the server sent
                    ctx.currentUser = mapper.readValue(userJson, User.class);

                    app.showProfileSelection();
                } catch (Exception ex) {
                    error.setText("Error parsing user data.");
                }
            } else {
                // The server sends "ERROR|Reason"
                String errorMsg = response != null ? response.split("\\|")[1] : "Server unreachable";
                error.setText(errorMsg);
            }
        });

        register.setOnAction(e -> {
            String response = app.getNetworkManager().sendRequest("REGISTER|" + username.getText() + "|" + password.getText());

            if (response != null && response.startsWith("SUCCESS")) {
                try {
                    String jsonPart = response.split("\\|")[1];
                    // Reconstruct the user object
                    ctx.currentUser = new ObjectMapper().readValue(jsonPart, User.class);

                    // Success! Move to profile selection
                    app.showProfileSelection();
                } catch (Exception ex) {
                    error.setText("Client Error: Could not parse server response.");
                }
            } else if (response != null && response.startsWith("ERROR")) {
                error.setText(response.split("\\|")[1]);
            } else {
                error.setText("Server did not respond.");
            }
        });

        root.getChildren().addAll(username, password, login, register, error);
        stage.setScene(new Scene(root, 400, 400));
        stage.show();
    }
}
