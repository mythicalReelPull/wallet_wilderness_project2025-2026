package org.example.gameplayController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryController {

    private final Stage stage;
    private final GameContext ctx;
    private final GameController app;
    private final ObjectMapper mapper = new ObjectMapper();

    public HistoryController(Stage stage, GameContext ctx, GameController app) {
        this.stage = stage;
        this.ctx = ctx;
        this.app = app;
    }

    public void showSessionHistoryForProfile(long profileId) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        Label title = new Label("Past Sessions");
        ListView<String> sessionList = new ListView<>();

        List<Transaction> allTransactions = loadAllTransactions();

        // 1. Get unique IDs (filtering by the current profile ID)
        Set<String> uniqueSessions = allTransactions.stream()
                .filter(t -> t.getProfileId() == profileId)
                .map(Transaction::getSessionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 2. Format and add to the list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");
        for (String sid : uniqueSessions) {
            try {
                long ts = Long.parseLong(sid.replace("session_", ""));
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
                sessionList.getItems().add(sid + " (" + date.format(formatter) + ")");
            } catch (Exception ex) {
                sessionList.getItems().add(sid); // Fallback
            }
        }

        // 3. THE BUTTON LOGIC GOES HERE
        Button viewDetails = new Button("View Session Details");
        viewDetails.setOnAction(e -> {
            String selectedItem = sessionList.getSelectionModel().getSelectedItem();
            if (selectedItem != null && !selectedItem.isEmpty()) {
                // This takes "session_123456" from "session_123456 (Jan 12, 2026...)"
                String actualSessionId = selectedItem.split(" ")[0];
                showSessionDetails(actualSessionId);
            }
        });

        Button backBtn = new Button("Back to Menu");
        backBtn.setOnAction(e -> app.showMainMenu());

        root.getChildren().addAll(title, sessionList, viewDetails, backBtn);
        stage.setScene(new Scene(root, 600, 600));
    }

    public void showSessionDetails(String sessionId) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        Label title = new Label("Details for: " + sessionId);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);

        List<Transaction> sessionData = loadAllTransactions().stream()
                .filter(t -> t.getSessionId().equals(sessionId))
                .collect(Collectors.toList());


        StringBuilder sb = new StringBuilder();
        double totalSpent = 0;
        for (Transaction t : sessionData) {
            sb.append(String.format("[%s] %s: %.2f lei\n", t.getType(), t.getCategory(), t.getAmount()));
            if (!t.getType().equals("init")) totalSpent += t.getAmount();
        }
        sb.append("\n------------------\n");
        sb.append("Total Expenses: ").append(totalSpent).append(" lei");

        detailsArea.setText(sb.toString());

        Button backBtn = new Button("Back to History");
        backBtn.setOnAction(e -> showSessionHistoryForProfile(ctx.currentProfile.getId()));

        root.getChildren().addAll(title, detailsArea, backBtn);
        stage.setScene(new Scene(root, 600, 600));
    }

    private List<Transaction> loadAllTransactions() {
        try {
            File file = new File("all_transactions.json");
            if (!file.exists()) return new ArrayList<>();
            return Arrays.asList(mapper.readValue(file, Transaction[].class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}