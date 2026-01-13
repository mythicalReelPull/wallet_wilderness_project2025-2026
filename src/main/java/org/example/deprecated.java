//package org.example;
//
//
//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//import org.example.exception.InsufficientBalanceException;
//import org.example.exception.InvalidTransactionException;
//import org.example.profile.JsonProfileRepository;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//public class deprecated {
//
//    private static final String GREEN = "#2ECC71";
//    private static final String BLUE  = "#3498DB";
//    private static final String GOLD  = "#F1C40F";
//    private static final String RED   = "#E74C3C";
//
//    private Stage stage;
//    private Player player;
//    private GameState currentState = GameState.INTRO;
//
//
//    private String currentProfile;
//    private String browsingProfile;
//
//    private final Random random = new Random();
//
//    private boolean sessionSaved = false;
//
//
//    private void saveSessionIfNeeded() {
//        if (player != null && !sessionSaved) {
//            player.getTracker().saveToJson("transactions.json");
//            sessionSaved = true;
//        }
//    }
//
//
//
//    public deprecated(Stage stage) {
//        this.stage = stage;
//    }
//
//    public void showMainMenu() {
//        VBox root = new VBox(15);
//        root.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Wallet Wilderness");
//        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
//
//        Label subtitle = new Label("An RPG about money, choices, and freedom");
//
//        Button newGameBtn = new Button("▶ Start New Game");
//        newGameBtn.setOnAction(e -> showIntro());
//
//        VBox profilesBox = new VBox(8);
//        profilesBox.setAlignment(Pos.CENTER);
//        profilesBox.setFillWidth(true);
//
//        Label profilesLabel = new Label("👤 Profiles");
//        profilesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
//
//        profilesBox.getChildren().add(profilesLabel);
//
//        Set<String> profiles = loadProfiles();
//
//        if (profiles.isEmpty()) {
//            profilesBox.getChildren().add(new Label("No profiles yet"));
//        } else {
//            for (String profile : profiles) {
//                Button profileBtn = new Button("• " + profile);
//                profileBtn.setOnAction(e -> {
//                    browsingProfile = profile;
//                    showSessionHistoryForProfile(profile);
//                });
//                profilesBox.getChildren().add(profileBtn);
//            }
//        }
//
//        Button exitBtn = new Button("❌ Exit");
//        exitBtn.setOnAction(e -> stage.close());
//
//        root.getChildren().addAll(
//                title,
//                subtitle,
//                newGameBtn,
//                profilesBox,
//                exitBtn
//        );
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//
//    private Set<String> loadProfiles() {
//        try {
//            ExpenseTracker tracker = new ExpenseTracker();
//            tracker.loadFromJson("transactions.json");
//
//            return tracker.getTransactions().stream()
//                    .map(Transaction::getProfileName)
//                    .collect(Collectors.toSet());
//
//        } catch (Exception e) {
//            return new HashSet<>();
//        }
//    }
//
//
//
//
//    private Map<String, List<Transaction>> loadSessionsForProfile(String profile) {
//        try {
//            ExpenseTracker tracker = new ExpenseTracker();
//            tracker.loadFromJson("transactions.json");
//
//            return tracker.getTransactions().stream()
//                    .filter(t -> t.getProfileName().equals(profile))
//                    .collect(Collectors.groupingBy(Transaction::getSessionId));
//
//        } catch (Exception e) {
//            return new HashMap<>();
//        }
//    }
//
//
//
//
//    public void showIntro() {
//        // Root layout
//        VBox root = new VBox(15);
//        root.setAlignment(Pos.CENTER);
//
//        // UI elements
//        Label title = new Label("Wallet Wilderness");
//        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//
//        Label subtitle = new Label("An RPG about money, choices, and freedom");
//
//        TextField nameField = new TextField();
//        nameField.setStyle("-fx-prompt-text-fill: white;");
//        nameField.setPromptText("Enter your name");
//
//        Button startBtn = new Button("Start Adventure");
//
//        // Button action
//        startBtn.setOnAction(e -> {
//            String name = nameField.getText().trim();
//
//            if (name.isEmpty()) {
//                subtitle.setText("Please enter your name to continue.");
//                return;
//            }
//
//            startGame(name);
//        });
//
//        // Add elements to layout
//        root.getChildren().addAll(title, subtitle, nameField, startBtn);
//
//        // Create scene
//        Scene scene = new Scene(root, 600, 600);
//
//        // Attach scene to stage
//        stage.setTitle("Wallet Wilderness");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    private void startGame(String name) {
//        // Roll dice for starting balance
//        int die1 = random.nextInt(6) + 1;
//        int die2 = random.nextInt(6) + 1;
//        int startingBalance = 230 + (die1 + die2) * 10;
//
//        // Initialize player
//        currentProfile = name;
//        sessionSaved = false;
//        player = new Player(name, startingBalance);
//        player.setProfileName(currentProfile);
//        player.setSessionId("session_" + System.currentTimeMillis());
//
//
//        addInitTransactionIfNeeded(startingBalance);
//        // Show dice roll screen
//        showDiceRollScreen(die1, die2, startingBalance);
//    }
//
//    private void addInitTransactionIfNeeded(double startingBalance) {
//        boolean exists = player.getTracker().getTransactions().stream()
//                .anyMatch(t ->
//                        t.getType().equals("init") &&
//                                t.getSessionId().equals(player.getSessionId())
//                );
//
//        if (!exists) {
//            player.getTracker().addTransaction(
//                    new Transaction(
//                            "starting_balance",
//                            startingBalance,
//                            "init",
//                            player.getSessionId(),
//                            currentProfile
//                    )
//            );
//        }
//    }
//
//    public void showProfileSelection() {
//        VBox root = new VBox(15);
//        root.setAlignment(Pos.CENTER);
//        root.setStyle("-fx-padding: 20;");
//
//        Label title = new Label("Welcome, " + ctx.currentUser.getUsername());
//        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
//
//        ListView<String> profileList = new ListView<>();
//
//        // We use the Repository to find profiles belonging to this user
//        JsonProfileRepository profileRepo = new JsonProfileRepository();
//        List<org.example.profile.Profile> userProfiles = profileRepo.findByUserId(ctx.currentUser.getId());
//
//        // Add profile names to the list
//        for (org.example.profile.Profile p : userProfiles) {
//            profileList.getItems().add(p.getName());
//        }
//
//        Button selectButton = new Button("Select Profile");
//        selectButton.setOnAction(e -> {
//            String selectedName = profileList.getSelectionModel().getSelectedItem();
//            if (selectedName != null) {
//                // Find the actual profile object based on the name
//                ctx.currentProfile = userProfiles.stream()
//                        .filter(p -> p.getName().equals(selectedName))
//                        .findFirst()
//                        .orElse(null);
//
//                showMainMenu(); // Move to the game menu now that a profile is chosen
//            }
//        });
//
//        Button createButton = new Button("Create New Profile");
//        createButton.setOnAction(e -> showCreateProfileDialog(profileRepo, profileList, userProfiles));
//
//        root.getChildren().addAll(title, new Label("Select your profile:"), profileList, selectButton, createButton);
//        stage.setScene(new Scene(root, 400, 500));
//    }
//
//    // Helper method to handle creating a new profile
//    private void showCreateProfileDialog(JsonProfileRepository repo, ListView<String> listView, List<org.example.profile.Profile> listData) {
//        TextInputDialog dialog = new TextInputDialog();
//        dialog.setTitle("New Profile");
//        dialog.setHeaderText("Enter profile name:");
//
//        dialog.showAndWait().ifPresent(name -> {
//            org.example.profile.Profile newProfile = repo.create(ctx.currentUser.getId(), name);
//            listData.add(newProfile);
//            listView.getItems().add(name);
//        });
//    }
//
//
//    private void showDiceRollScreen(int die1, int die2, int startingBalance) {
//
//        VBox root = new VBox(20);
//        root.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Rolling the dice...");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        // Dice images
//        ImageView diceView1 = new ImageView();
//        ImageView diceView2 = new ImageView();
//        diceView1.setFitWidth(100);
//        diceView1.setFitHeight(100);
//        diceView2.setFitWidth(100);
//        diceView2.setFitHeight(100);
//
//        HBox diceBox = new HBox(20, diceView1, diceView2);
//        diceBox.setAlignment(Pos.CENTER);
//
//        Label balanceLabel = new Label("Starting balance: ???");
//        balanceLabel.setStyle("-fx-font-size: 18px;");
//
//        Button continueBtn = new Button("Continue");
//        continueBtn.setDisable(true);
//        continueBtn.setOnAction(e -> showDay1());
//
//        root.getChildren().addAll(title, diceBox, balanceLabel, continueBtn);
//
//        Scene scene = new Scene(root, 500, 400);
//        stage.setScene(scene);
//        stage.show();
//
//
//        Timeline rollAnimation = new Timeline(
//                new KeyFrame(Duration.millis(100), e -> {
//                    int temp1 = random.nextInt(6) + 1;
//                    int temp2 = random.nextInt(6) + 1;
//
//                    System.out.println("Rolling: " + temp1 + ", " + temp2);
//
//                    diceView1.setImage(loadDiceImage(temp1));
//                    diceView2.setImage(loadDiceImage(temp2));
//                })
//        );
//
//
//        rollAnimation.setCycleCount(10); // ~1 second total
//
//        rollAnimation.setOnFinished(e -> {
//            diceView1.setImage(loadDiceImage(die1));
//            diceView2.setImage(loadDiceImage(die2));
//            title.setText("🎲 Dice Rolled!");
//            balanceLabel.setText("Starting balance: " + startingBalance + " lei");
//            continueBtn.setDisable(false);
//        });
//
//        rollAnimation.play();
//    }
//
//    private Map<String, List<Transaction>> loadSessions() {
//        try {
//            ExpenseTracker tracker = new ExpenseTracker();
//            tracker.loadFromJson("transactions.json");
//
//            return tracker.getTransactions().stream()
//                    .collect(Collectors.groupingBy(Transaction::getSessionId));
//
//        } catch (Exception e) {
//            return new HashMap<>();
//        }
//    }
//
//    public void showSessionHistory() {
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(15);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("📂 Previous Sessions");
//        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//
//        Map<String, List<Transaction>> sessions = loadSessions();
//
//        if (sessions.isEmpty()) {
//            content.getChildren().add(new Label("No previous sessions found."));
//        } else {
//            for (String sessionId : sessions.keySet()) {
//                Button sessionBtn = new Button("Session: " + sessionId);
//                sessionBtn.setOnAction(e -> showSessionDetails(sessionId, browsingProfile));
//                content.getChildren().add(sessionBtn);
//            }
//        }
//
//        Button backBtn = new Button("⬅ Back");
//        backBtn.setOnAction(e -> showMainMenu());
//
//        content.getChildren().add(backBtn);
//
//        root.setCenter(content);
//        stage.setScene(new Scene(root, 700, 550));
//        stage.show();
//    }
//
//    public void showSessionDetails(String sessionId, String profileName) {
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(10);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Session Review");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label sessionLabel = new Label("Session ID: " + sessionId);
//
//        TextArea history = new TextArea();
//        history.setEditable(false);
//        history.setPrefHeight(300);
//
//        Map<String, List<Transaction>> sessions =
//                loadSessionsForProfile(profileName);
//
//        List<Transaction> txs = sessions.getOrDefault(sessionId, Collections.emptyList());
//
//        double totalSpent = 0;
//
//        for (Transaction t : txs) {
//            history.appendText(t.toString() + "\n");
//            if (t.getType().equalsIgnoreCase("spend")) {
//                totalSpent += t.getAmount();
//            }
//        }
//
//        Label summary = new Label(
//                "Total transactions: " + txs.size() +
//                        "\nTotal spent: " + totalSpent + " lei"
//        );
//
//        Button backBtn = new Button("⬅ Back");
//        backBtn.setOnAction(e -> {
//            if (profileName == null || profileName.isEmpty()) {
//                showSessionHistory();
//            } else {
//                showSessionHistoryForProfile(profileName);
//            }
//        });
//
//        content.getChildren().addAll(title, sessionLabel, history, summary, backBtn);
//
//        root.setCenter(content);
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//
//    private Image loadDiceImage(int value) {
//        String path = "/images/dice" + value + ".png";
//        System.out.println("Loading image: " + path);
//
//        if (getClass().getResource(path) == null) {
//            System.out.println("IMAGE NOT FOUND: " + path);
//            return null;
//        }
//
//        return new Image(getClass().getResourceAsStream(path));
//    }
//
//    private VBox createHUD() {
//        VBox hud = new VBox(5);
//        hud.setStyle(
//                "-fx-background-color: rgba(0,0,0,0.6);" +
//                        "-fx-padding: 10;" +
//                        "-fx-border-radius: 5;" +
//                        "-fx-background-radius: 5;"
//        );
//
//        // HUD only visible if tracking is ON
//        if (!player.isTrackingEnabled()) {
//            return hud;
//        }
//
//        Label trackingLabel = new Label("📊 Tracking: ON");
//        Label balanceLabel = new Label("💰 Balance: " + player.getBalance() + " lei");
//        Label savingsLabel = new Label("🏦 Savings: " + player.getSavings() + " lei");
//        Label stressLabel = new Label("😰 Stress: " + getStressText());
//
//        trackingLabel.setStyle("-fx-text-fill: white;");
//        balanceLabel.setStyle("-fx-text-fill: white;");
//        savingsLabel.setStyle("-fx-text-fill: white;");
//        stressLabel.setStyle("-fx-text-fill: white;");
//
//        hud.getChildren().addAll(
//                trackingLabel,
//                balanceLabel,
//                savingsLabel,
//                stressLabel
//        );
//
//        //Button profileBtn = new Button("📜 Profile");
//        //profileBtn.setOnAction(e -> showProfileMenu());
//
//        //hud.getChildren().add(profileBtn);
//
//        return hud;
//    }
//
//    private String getStressText() {
//        int stress = player.getStress();
//
//        if (stress <= 0) return "Calm";
//        if (stress == 1) return "Slightly tense";
//        if (stress == 2) return "Anxious";
//        return "Overwhelmed";
//    }
//
//    private boolean attemptAndCheck(Runnable action) {
//        try {
//            action.run();
//        } catch (Exception ex) {
//            showFailureEnding(
//                    "You tried to make a financial decision\n" +
//                            "that you couldn’t actually afford.\n\n" +
//                            "Without clear limits, choices stop being real options."
//            );
//            return false;
//        }
//
//        return !checkGameOver();
//    }
//
//    public void showDay1() {
//        currentState = GameState.DAY1;
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Day 1 – Campus Café");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label story = new Label(
//                "Classes just ended, and your stomach reminds you\n" +
//                        "that you skipped breakfast.\n\n" +
//                        "The campus café is buzzing with people laughing,\n" +
//                        "ordering food, and tapping cards without thinking.\n\n" +
//                        "Your friends smile:\n" +
//                        "“Come on, you deserve something nice today.”"
//        );
//        story.setWrapText(true);
//        story.setMaxWidth(420);
//
//        Button option1 = new Button("Fancy meal and drink (40 lei)");
//        Button option2 = new Button("Simple snack (15 lei)");
//        Button option3 = new Button("Check budget first (start tracking)");
//
//        option1.setOnAction(e -> {
//            if (!attemptAndCheck(() -> player.spend(40))) return;
//            showDay2();
//        });
//
//
//        option2.setOnAction(e -> {
//            if (!attemptAndCheck(() -> {
//                try {
//                    player.spendCategory("food", 15);
//                } catch (InsufficientBalanceException ex) {
//                    throw new RuntimeException(ex);
//                }
//                player.addFreedomScore(1);
//            })) return;
//
//            showDay2();
//        });
//
//
//        option3.setOnAction(e -> {
//            player.enableTracking();
//            player.addFreedomScore(2);
//
//            if (!attemptAndCheck(() -> {
//                try {
//                    player.spendCategory("food", 15);
//
//                } catch (InsufficientBalanceException ex) {
//                    throw new RuntimeException(ex);
//                }
//            })) return;
//
//            showDay2();
//        });
//
//
//        content.getChildren().addAll(title, story, option1, option2, option3);
//
//        root.setTop(createHUD());
//        root.setCenter(content);
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    public void showDay2() {
//        currentState = GameState.DAY2;
//        BorderPane root = new BorderPane();
//
//
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Day 2 – Online Sale");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label story = new Label(
//                "While scrolling through your phone, a notification pops up.\n\n" +
//                        "🔥 FLASH SALE 🔥\n" +
//                        "Limited time. Huge discounts. Everyone is buying.\n\n" +
//                        "You weren’t planning to spend today...\n" +
//                        "but the deals are very convincing."
//        );
//        story.setWrapText(true);
//        story.setMaxWidth(450);
//        story.setStyle("-fx-font-size: 16px;");
//
//        content.getChildren().addAll(title, story);
//
//
//        if (player.isTrackingEnabled()) {
//
//            Button opt1 = new Button("Check expenses, buy small accessory (40 lei)");
//            Button opt2 = new Button("Ignore plan, buy expensive gadget (150 lei)");
//            Button opt3 = new Button("Skip sale, save 100 lei");
//
//            opt1.setOnAction(e -> {
//                if (!attemptAndCheck(() -> {
//                    try {
//                        player.spendCategory("shopping", 40);
//                    } catch (InsufficientBalanceException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                    player.addFreedomScore(2);
//                })) return;
//
//                showDay3();
//            });
//
//
//            opt2.setOnAction(e -> {
//                if (!attemptAndCheck(() -> {
//                    try {
//                        player.spendCategory("shopping", 150);
//                    } catch (InsufficientBalanceException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                })) return;
//                showDay3();
//            });
//
//
//            opt3.setOnAction(e -> {
//                if (!attemptAndCheck(() -> {
//                    try {
//                        player.save(100);
//                    } catch (InvalidTransactionException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                    player.addFreedomScore(3);
//                })) return;
//
//                showDay3();
//            });
//
//
//            content.getChildren().addAll(opt1, opt2, opt3);
//
//        } else {
//
//            Button opt1 = new Button("Buy expensive gadget (150 lei) – YOLO");
//            Button opt2 = new Button("Buy small accessory (40 lei)");
//            Button opt3 = new Button("Start tracking, buy nothing");
//
//            opt1.setOnAction(e -> {
//                if (!attemptAndCheck(() -> player.spend(150))) return;
//                showDay3();
//            });
//
//
//            opt2.setOnAction(e -> {
//                if (!attemptAndCheck(() -> {
//                    player.spend(40);
//                    player.addFreedomScore(1);
//                })) return;
//
//                showDay3();
//            });
//
//
//            opt3.setOnAction(e -> {
//                player.enableTracking();
//                player.addFreedomScore(2);
//                showDay3();
//            });
//
//            content.getChildren().addAll(opt1, opt2, opt3);
//        }
//
//
//        root.setTop(createHUD());   // HUD appears only if tracking ON
//        root.setCenter(content);
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    public void showDay3() {
//        currentState = GameState.DAY3;
//        BorderPane root = new BorderPane();
//
//        // ---- Main content ----
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Day 3 – The Unexpected Bill");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label story = new Label(
//                "Just as you start feeling confident about your week,\n" +
//                        "your phone vibrates.\n\n" +
//                        "It’s your landlord.\n\n" +
//                        "“There was a recalculation of building expenses.\n" +
//                        "You owe an additional 120 lei.”\n\n" +
//                        "There’s no avoiding this one."
//        );
//        story.setWrapText(true);
//        story.setMaxWidth(450);
//        story.setStyle("-fx-font-size: 16px;");
//
//        Label resultLabel = new Label();
//        resultLabel.setWrapText(true);
//        resultLabel.setMaxWidth(450);
//        resultLabel.setStyle("-fx-font-size: 15px;");
//
//        Button handleBtn = new Button("Deal with the bill");
//
//        handleBtn.setOnAction(e -> {
//
//            if (player.getSavings() >= 120) {
//                // ---- Pay from savings ----
//                try {
//                    player.save(-120);
//                    player.addFreedomScore(3);
//
//                    resultLabel.setText(
//                            "You pay the bill from your savings.\n\n" +
//                                    "It hurts, but your daily budget remains untouched.\n" +
//                                    "Preparedness buys peace of mind."
//                    );
//                } catch (Exception ex) {
//                    resultLabel.setText(ex.getMessage());
//                }
//
//            } else {
//
//                if (!attemptAndCheck(() -> {
//                    try {
//                        player.spendCategory("bills", 120);
//                    } catch (InsufficientBalanceException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                })) return;
//
//
//                if (player.isTrackingEnabled()) {
//                    player.addFreedomScore(1);
//                    resultLabel.setText(
//                            "You knew this week was tight.\n\n" +
//                                    "Because you were tracking your expenses,\n" +
//                                    "the bill is stressful — but not shocking."
//                    );
//                } else {
//
//                    resultLabel.setText(
//                            "This bill completely catches you off guard.\n\n" +
//                                    "No warning. No preparation.\n" +
//                                    "Stress spikes."
//                    );
//                }
//            }
//
//            handleBtn.setDisable(true);
//
//            Button continueBtn = new Button("Continue");
//            continueBtn.setOnAction(ev -> showDay4());
//
//            content.getChildren().add(continueBtn);
//            root.setTop(createHUD()); // refresh HUD after changes
//        });
//
//        content.getChildren().addAll(title, story, handleBtn, resultLabel);
//
//        // ---- Layout ----
//        root.setTop(createHUD());   // HUD only visible if tracking ON
//        root.setCenter(content);
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    public void showDay4() {
//        currentState = GameState.DAY4;
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = new Label("Day 4 – An Unexpected Turn");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label story = new Label(
//                "By the fourth day, you feel the weight of the week.\n\n" +
//                        "Something unexpected happens - a broken pipe inside your apartment. :(\n\n" +
//                        "It’s not a disaster — but how you react now\n" +
//                        "will decide how the week ends."
//        );
//        story.setWrapText(true);
//        story.setMaxWidth(450);
//        story.setStyle("-fx-font-size: 16px;");
//
//        Label resultLabel = new Label();
//        resultLabel.setWrapText(true);
//        resultLabel.setMaxWidth(450);
//        resultLabel.setStyle("-fx-font-size: 15px;");
//
//        // ---- Choices ----
//        Button opt1 = new Button("Take the risk and fix it fast (20 lei)");
//        Button opt2 = new Button("Look for a expensive workaround (50 lei)");
//        Button opt3 = new Button("Do nothing and delay the problem");
//
//        opt1.setOnAction(e -> {
//            if (!attemptAndCheck(() -> {
//                try {
//                    player.spendCategory("misc", 20);
//                } catch (InsufficientBalanceException ex) {
//                    throw new RuntimeException(ex);
//                }
//                player.addStress(1);
//            })) return;
//
//            resultLabel.setText(
//                    "You want the job to be done.\n\n" +
//                            "A cheaper workaround works just fine.\n" +
//                            "Being mindful pays off."
//            );
//
//            finishDay(content, root);
//        });
//
//
//
//        opt2.setOnAction(e -> {
//            if (!attemptAndCheck(() -> {
//                try {
//                    player.spendCategory("misc", 50);
//                    player.addFreedomScore(2);
//                } catch (InsufficientBalanceException ex) {
//                    throw new RuntimeException(ex);
//                }
//            })) return;
//
//            resultLabel.setText(
//                "You take a moment to think.\n\n" +
//                "Calling Vericu its the only workaround you could find \n" +
//                "in such short notice.\n\n" +
//                "At least it won't cause any problems in the future! (you hope)"
//            );
//
//            finishDay(content, root);
//        });
//
//        opt3.setOnAction(e -> {
//            player.addStress(2);
//
//            resultLabel.setText(
//                    "You decide to ignore the issue for now.\n\n" +
//                            "It doesn’t cost money today,\n" +
//                            "but the stress lingers (and you dont have a working sink now)."
//            );
//            finishDay(content, root);
//        });
//
//        content.getChildren().addAll(
//                title,
//                story,
//                opt1,
//                opt2,
//                opt3,
//                resultLabel
//        );
//
//        root.setTop(createHUD());
//        root.setCenter(content);
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    private void finishDay(VBox content, BorderPane root) {
//        content.getChildren().removeIf(n -> n instanceof Button);
//
//        Button finishBtn = new Button("Finish the week");
//        finishBtn.setOnAction(e -> showSummary());
//
//        content.getChildren().add(finishBtn);
//        root.setTop(createHUD()); // refresh HUD
//    }
//
//    private Label createEndingTitle(String text, String color) {
//        Label title = new Label(text);
//        title.setStyle(
//                "-fx-font-size: 28px;" +
//                        "-fx-font-weight: bold;" +
//                        "-fx-text-fill: " + color + ";"
//        );
//        return title;
//    }
//
//
//    private boolean checkGameOver() {
//        if (player.getBalance() < 0) {
//            showFailureEnding(
//                    "You ran out of money.\n\n" +
//                            "Without a clear picture of your expenses,\n" +
//                            "small decisions piled up faster than expected."
//            );
//            return true;
//        }
//
//        if (player.getBalance() < 20 && !player.isTrackingEnabled()) {
//            showFailureEnding(
//                    "You are almost out of money.\n\n" +
//                            "Without tracking your expenses,\n" +
//                            "you no longer feel in control of your choices."
//            );
//            return true;
//        }
//
//        return false;
//    }
//
//    private void showFailureEnding(String reason) {
//        currentState = GameState.FAILURE;
//        saveSessionIfNeeded();
//
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label title = createEndingTitle("🔴 The Burned-Out", RED);
//
//
//
//        Label message = new Label(reason);
//        message.setWrapText(true);
//        message.setMaxWidth(450);
//        message.setStyle("-fx-font-size: 16px;");
//
//        Label lesson = new Label(
//                "This ending isn’t a punishment.\n\n" +
//                        "It shows how quickly financial stress\n" +
//                        "can build when spending isn’t visible.\n\n" +
//                        "Tracking expenses gives you\n" +
//                        "time, clarity, and options."
//        );
//
//        lesson.setWrapText(true);
//        lesson.setMaxWidth(450);
//
//        Button menuBtn = new Button("Return to Main Menu");
//        menuBtn.setOnAction(e -> showMainMenu());
//
//
//        content.getChildren().addAll(title, message, lesson, menuBtn);
//
//        root.setCenter(content);
//
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    public void showProfileMenu() {
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(15);
//        content.setAlignment(Pos.CENTER_LEFT);
//        content.setStyle("-fx-padding: 20;");
//
//        Label title = new Label("📜 Player Profile");
//        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
//
//        Label info = new Label(
//                "Name: " + player.getName() + "\n" +
//                        "Session ID: " + player.getSessionId() + "\n" +
//                        "Tracking: " + (player.isTrackingEnabled() ? "ON" : "OFF")
//        );
//        info.setStyle("-fx-font-size: 15px;");
//
//        Label historyTitle = new Label("Transaction History");
//        historyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
//
//        VBox historyBox = new VBox(5);
//
//        // 🔁 Only show transactions from THIS session
//        player.getTracker().getTransactions().stream()
//                .filter(t -> t.getSessionId().equals(player.getSessionId()))
//                .forEach(t -> {
//                    Label entry = new Label(formatTransaction(t));
//                    historyBox.getChildren().add(entry);
//                });
//
//        if (historyBox.getChildren().isEmpty()) {
//            historyBox.getChildren().add(
//                    new Label("No transactions recorded yet.")
//            );
//        }
//
//        Button backBtn = new Button("⬅ Back");
//        backBtn.setOnAction(e -> showSessionHistoryForProfile(browsingProfile)); // or store last scene
//
//        content.getChildren().addAll(
//                title,
//                info,
//                historyTitle,
//                historyBox,
//                backBtn
//        );
//
//        root.setCenter(content);
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//    private void resumeGame() {
//        if (player == null) {
//            showMainMenu();
//            return;
//        }
//
//        switch (currentState) {
//            case DAY1 -> showDay1();
//            case DAY2 -> showDay2();
//            case DAY3 -> showDay3();
//            case DAY4 -> showDay4();
//            case SUMMARY -> showSummary();
//            case FAILURE -> showFailureEnding("This session already ended.");
//            default -> showIntro();
//        }
//    }
//
//
//
//    private String formatTransaction(Transaction t) {
//        String typeIcon = t.getType().equalsIgnoreCase("spend") ? "💸" : "💾";
//
//        return typeIcon + " " +
//                t.getType().toUpperCase() +
//                ": " + t.getAmount() + " lei [" + t.getCategory() + "]";
//    }
//
//
//    public void showSummary() {
//        currentState = GameState.SUMMARY;
//        saveSessionIfNeeded();
//
//        player.getTracker().saveToJson("transactions.json");
//
//        BorderPane root = new BorderPane();
//
//        VBox content = new VBox(20);
//        content.setAlignment(Pos.CENTER);
//
//        Label endingTitle;
//        Label conclusion = new Label();
//        conclusion.setWrapText(true);
//        conclusion.setMaxWidth(520);
//        conclusion.setStyle("-fx-font-size: 16px;");
//
//        Label stats = new Label(
//                "Final balance: " + player.getBalance() + " lei\n" +
//                        "Savings: " + player.getSavings() + " lei\n" +
//                        "Freedom score: " + player.getFreedomScore()
//        );
//        stats.setStyle("-fx-font-size: 16px;");
//
//        // ---- ENDING LOGIC ----
//        if (!player.isTrackingEnabled()) {
//            endingTitle = createEndingTitle("🟡 The Drifter", GOLD);
//
//            conclusion.setText(
//                    "You made it through the week,\n" +
//                            "but without a clear picture of your money.\n\n" +
//                            "Decisions felt reactive, not intentional.\n\n" +
//                            "Tracking expenses doesn’t limit freedom.\n" +
//                            "It reveals it."
//            );
//
//        } else if (player.getFreedomScore() >= 6) {
//            endingTitle = createEndingTitle("🟢 The Planner", GREEN);
//
//            conclusion.setText(
//                    "You stayed aware of your spending\n" +
//                            "and chose what truly mattered.\n\n" +
//                            "Because you tracked your expenses,\n" +
//                            "you weren’t guessing — you were deciding.\n\n" +
//                            "That’s real financial freedom."
//            );
//
//        } else {
//            endingTitle = createEndingTitle("🔵 The Learner", BLUE);
//
//            conclusion.setText(
//                    "You didn’t make every decision perfectly,\n" +
//                            "but tracking helped you avoid chaos.\n\n" +
//                            "Awareness isn’t about being flawless.\n" +
//                            "It’s about learning faster."
//            );
//        }
//
//        Button menuBtn = new Button("Return to Main Menu");
//        menuBtn.setOnAction(e -> showMainMenu());
//
//
//        content.getChildren().addAll(
//                endingTitle,
//                stats,
//                conclusion,
//                menuBtn
//        );
//
//
//        root.setCenter(content);
//        stage.setScene(new Scene(root, 600, 600));
//        stage.show();
//    }
//
//
//
//
//}
