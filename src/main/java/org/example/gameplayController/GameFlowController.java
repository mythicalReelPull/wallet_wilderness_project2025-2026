package org.example.gameplayController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.exception.InsufficientBalanceException;
import org.example.exception.InvalidTransactionException;
import org.example.GameState;
import org.example.Player;
import org.example.Transaction;
import org.example.profile.Profile;

import java.util.Random;

public class GameFlowController {

    private static final String GREEN = "#2ECC71";
    private static final String BLUE  = "#3498DB";
    private static final String GOLD  = "#F1C40F";
    private static final String RED   = "#E74C3C";

    private final Stage stage;
    private final GameContext ctx;
    private final GameController app;

    private final Random random = new Random();

    public GameFlowController(Stage stage, GameContext ctx, GameController app) {
        this.stage = stage;
        this.ctx = ctx;
        this.app = app;
    }

    public void startGame(Profile profile) {
        ctx.currentProfile = profile;

        ctx.sessionSaved = false;

        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        int balance = 230 + (die1 + die2) * 10;

        ctx.player = new Player(profile.getName(), balance);
        ctx.player.setSessionId("session_" + System.currentTimeMillis());

        addInitTransactionIfNeeded(balance);

        ctx.currentState = GameState.INTRO;
        showDiceRollScreen(die1, die2, balance);
    }

    /* =========================================================
       DICE ROLL
       ========================================================= */

    private void showDiceRollScreen(int die1, int die2, int startingBalance) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Rolling the dice...");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ImageView dice1 = new ImageView();
        ImageView dice2 = new ImageView();
        dice1.setFitWidth(100);
        dice1.setFitHeight(100);
        dice2.setFitWidth(100);
        dice2.setFitHeight(100);

        HBox diceBox = new HBox(20, dice1, dice2);
        diceBox.setAlignment(Pos.CENTER);

        Label balanceLabel = new Label("Starting balance: ???");

        Button continueBtn = new Button("Continue");
        continueBtn.setDisable(true);
        continueBtn.setOnAction(e -> showDay1());

        root.getChildren().addAll(title, diceBox, balanceLabel, continueBtn);

        stage.setScene(new Scene(root, 500, 400));
        stage.show();

        Timeline roll = new Timeline(
                new KeyFrame(Duration.millis(100), e -> {
                    dice1.setImage(loadDiceImage(random.nextInt(6) + 1));
                    dice2.setImage(loadDiceImage(random.nextInt(6) + 1));
                })
        );

        roll.setCycleCount(10);
        roll.setOnFinished(e -> {
            dice1.setImage(loadDiceImage(die1));
            dice2.setImage(loadDiceImage(die2));
            title.setText("🎲 Dice Rolled!");
            balanceLabel.setText("Starting balance: " + startingBalance + " lei");
            continueBtn.setDisable(false);
        });

        roll.play();
    }

    private Image loadDiceImage(int value) {
        String path = "/images/dice" + value + ".png";
        if (getClass().getResource(path) == null) return null;
        return new Image(getClass().getResourceAsStream(path));
    }

    /* =========================================================
       HUD
       ========================================================= */

    private VBox createHUD() {
        VBox hud = new VBox(5);
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 10;");

        if (ctx.player == null || !ctx.player.isTrackingEnabled()) return hud;

        hud.getChildren().addAll(
                label("📊 Tracking: ON"),
                label("💰 Balance: " + ctx.player.getBalance() + " lei"),
                label("🏦 Savings: " + ctx.player.getSavings() + " lei"),
                label("😰 Stress: " + getStressText())
        );

        return hud;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white;");
        return l;
    }

    private String getStressText() {
        int stress = ctx.player.getStress();
        if (stress <= 0) return "Calm";
        if (stress == 1) return "Slightly tense";
        if (stress == 2) return "Anxious";
        return "Overwhelmed";
    }

    /* =========================================================
       GAME FLOW HELPERS
       ========================================================= */

    private boolean attemptAndCheck(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            showFailureEnding(
                    "You tried to make a financial decision\n" +
                            "that you couldn’t actually afford.\n\n" +
                            "Without clear limits, choices stop being real options."
            );
            return false;
        }
        return !checkGameOver();
    }

    private void addInitTransactionIfNeeded(double balance) {
        boolean exists = ctx.player.getTracker().getTransactions().stream()
                .anyMatch(t ->
                        t.getType().equals("init") &&
                                t.getSessionId().equals(ctx.player.getSessionId())
                );

        if (!exists) {
            ctx.player.getTracker().addTransaction(
                    new Transaction(
                            "starting_balance",
                            balance,
                            "init",
                            ctx.player.getSessionId(),
                            ctx.currentProfile.getName(),
                            ctx.currentProfile.getId()
                    )
            );
        }
    }

    private void saveSessionIfNeeded() {
        if (!ctx.sessionSaved && ctx.player != null) {
            // This should point to a central history file
            ctx.player.getTracker().saveToJson("all_transactions.json");
            ctx.sessionSaved = true;
        }
    }

    public void resumeGame() {
        if (ctx.player == null) {
            app.showMainMenu();
            return;
        }

        switch (ctx.currentState) {
            case DAY1 -> showDay1();
            case DAY2 -> showDay2();
            case DAY3 -> showDay3();
            case DAY4 -> showDay4();
            case SUMMARY -> showSummary();
            case FAILURE -> showFailureEnding("This session already ended.");
            default -> showDay1();
        }
    }


    public void showDay1() {
        ctx.currentState = GameState.DAY1;

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Day 1 – Campus Café");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label story = new Label(
                "Classes just ended, and your stomach reminds you\n" +
                        "that you skipped breakfast.\n\n" +
                        "The campus café is buzzing with people laughing,\n" +
                        "ordering food, and tapping cards without thinking.\n\n" +
                        "Your friends smile:\n" +
                        "“Come on, you deserve something nice today.”"
        );
        story.setWrapText(true);
        story.setMaxWidth(420);

        Button option1 = new Button("Fancy meal and drink (40 lei)");
        Button option2 = new Button("Simple snack (15 lei)");
        Button option3 = new Button("Check budget first (start tracking)");

        option1.setOnAction(e -> {
            if (!attemptAndCheck(() -> ctx.player.spend(40))) return;
            showDay2();
        });

        option2.setOnAction(e -> {
            if (!attemptAndCheck(() -> {
                try {
                    ctx.player.spendCategory("food", 15);
                } catch (InsufficientBalanceException ex) {
                    throw new RuntimeException(ex);
                }
                ctx.player.addFreedomScore(1);
            })) return;
            showDay2();
        });

        option3.setOnAction(e -> {
            ctx.player.enableTracking();
            ctx.player.addFreedomScore(2);

            if (!attemptAndCheck(() ->
                    {
                        try {
                            ctx.player.spendCategory("food", 15);
                        } catch (InsufficientBalanceException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            )) return;

            showDay2();
        });

        content.getChildren().addAll(
                title, story, option1, option2, option3
        );

        root.setTop(createHUD());
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }


    public void showDay2() {
        ctx.currentState = GameState.DAY2;

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Day 2 – Online Sale");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label story = new Label(
                "While scrolling through your phone, a notification pops up.\n\n" +
                        "🔥 FLASH SALE 🔥\n" +
                        "Limited time. Huge discounts. Everyone is buying.\n\n" +
                        "You weren’t planning to spend today...\n" +
                        "but the deals are very convincing."
        );
        story.setWrapText(true);
        story.setMaxWidth(450);
        story.setStyle("-fx-font-size: 16px;");

        content.getChildren().addAll(title, story);

        if (ctx.player.isTrackingEnabled()) {

            Button opt1 = new Button("Check expenses, buy small accessory (40 lei)");
            Button opt2 = new Button("Ignore plan, buy expensive gadget (150 lei)");
            Button opt3 = new Button("Skip sale, save 100 lei");

            opt1.setOnAction(e -> {
                if (!attemptAndCheck(() -> {
                    try {
                        ctx.player.spendCategory("shopping", 40);
                    } catch (InsufficientBalanceException ex) {
                        throw new RuntimeException(ex);
                    }
                    ctx.player.addFreedomScore(2);
                })) return;
                showDay3();
            });

            opt2.setOnAction(e -> {
                if (!attemptAndCheck(() ->
                        {
                            try {
                                ctx.player.spendCategory("shopping", 150);
                            } catch (InsufficientBalanceException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                )) return;
                showDay3();
            });

            opt3.setOnAction(e -> {
                if (!attemptAndCheck(() -> {
                    try {
                        ctx.player.save(100);
                    } catch (InvalidTransactionException ex) {
                        throw new RuntimeException(ex);
                    }
                    ctx.player.addFreedomScore(3);
                })) return;
                showDay3();
            });

            content.getChildren().addAll(opt1, opt2, opt3);

        } else {

            Button opt1 = new Button("Buy expensive gadget (150 lei) – YOLO");
            Button opt2 = new Button("Buy small accessory (40 lei)");
            Button opt3 = new Button("Start tracking, buy nothing");

            opt1.setOnAction(e -> {
                if (!attemptAndCheck(() -> ctx.player.spend(150))) return;
                showDay3();
            });

            opt2.setOnAction(e -> {
                if (!attemptAndCheck(() -> {
                    ctx.player.spend(40);
                    ctx.player.addFreedomScore(1);
                })) return;
                showDay3();
            });

            opt3.setOnAction(e -> {
                ctx.player.enableTracking();
                ctx.player.addFreedomScore(2);
                showDay3();
            });

            content.getChildren().addAll(opt1, opt2, opt3);
        }

        root.setTop(createHUD());
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }


    public void showDay3() {
        ctx.currentState = GameState.DAY3;

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Day 3 – The Unexpected Bill");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label story = new Label(
                "Just as you start feeling confident about your week,\n" +
                        "your phone vibrates.\n\n" +
                        "It’s your landlord.\n\n" +
                        "“There was a recalculation of building expenses.\n" +
                        "You owe an additional 120 lei.”\n\n" +
                        "There’s no avoiding this one."
        );
        story.setWrapText(true);
        story.setMaxWidth(450);
        story.setStyle("-fx-font-size: 16px;");

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(450);

        Button handleBtn = new Button("Deal with the bill");

        handleBtn.setOnAction(e -> {

            if (ctx.player.getSavings() >= 120) {
                try {
                    ctx.player.save(-120);
                } catch (InvalidTransactionException ex) {
                    throw new RuntimeException(ex);
                }
                ctx.player.addFreedomScore(3);

                resultLabel.setText(
                        "You pay the bill from your savings.\n\n" +
                                "Preparedness buys peace of mind."
                );
            } else {
                if (!attemptAndCheck(() ->
                        {
                            try {
                                ctx.player.spendCategory("bills", 120);
                            } catch (InsufficientBalanceException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                )) return;

                if (ctx.player.isTrackingEnabled()) {
                    ctx.player.addFreedomScore(1);
                    resultLabel.setText(
                            "Because you were tracking your expenses,\n" +
                                    "the bill is stressful — but not shocking."
                    );
                } else {
                    resultLabel.setText(
                            "This bill completely catches you off guard.\n\n" +
                                    "Stress spikes."
                    );
                }
            }

            handleBtn.setDisable(true);

            Button continueBtn = new Button("Continue");
            continueBtn.setOnAction(ev -> showDay4());
            content.getChildren().add(continueBtn);

            root.setTop(createHUD());
        });

        content.getChildren().addAll(title, story, handleBtn, resultLabel);
        root.setTop(createHUD());
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }


    public void showDay4() {
        ctx.currentState = GameState.DAY4;

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Day 4 – An Unexpected Turn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label story = new Label(
                "By the fourth day, you feel the weight of the week.\n\n" +
                        "Something unexpected happens - a broken pipe inside your apartment.\n\n" +
                        "It’s not a disaster — but how you react now\n" +
                        "will decide how the week ends."
        );
        story.setWrapText(true);
        story.setMaxWidth(450);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(450);

        Button opt1 = new Button("Take the risk and fix it fast (20 lei)");
        Button opt2 = new Button("Look for an expensive workaround (50 lei)");
        Button opt3 = new Button("Do nothing and delay the problem");

        opt1.setOnAction(e -> {
            if (!attemptAndCheck(() -> {
                try {
                    ctx.player.spendCategory("misc", 20);
                } catch (InsufficientBalanceException ex) {
                    throw new RuntimeException(ex);
                }
                ctx.player.addStress(1);
            })) return;

            resultLabel.setText("Being mindful pays off.");
            finishDay(content, root);
        });

        opt2.setOnAction(e -> {
            if (!attemptAndCheck(() -> {
                try {
                    ctx.player.spendCategory("misc", 50);
                } catch (InsufficientBalanceException ex) {
                    throw new RuntimeException(ex);
                }
                ctx.player.addFreedomScore(2);
            })) return;

            resultLabel.setText("An expensive but reliable solution.");
            finishDay(content, root);
        });

        opt3.setOnAction(e -> {
            ctx.player.addStress(2);
            resultLabel.setText("Stress lingers as the problem waits.");
            finishDay(content, root);
        });

        content.getChildren().addAll(
                title, story, opt1, opt2, opt3, resultLabel
        );

        root.setTop(createHUD());
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }


    public void showSummary() {
        ctx.currentState = GameState.SUMMARY;
        saveSessionIfNeeded();
        ctx.player.getTracker().saveToJson("transactions.json");

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label endingTitle;
        Label conclusion = new Label();
        conclusion.setWrapText(true);
        conclusion.setMaxWidth(520);

        Label stats = new Label(
                "Final balance: " + ctx.player.getBalance() + " lei\n" +
                        "Savings: " + ctx.player.getSavings() + " lei\n" +
                        "Freedom score: " + ctx.player.getFreedomScore()
        );

        if (!ctx.player.isTrackingEnabled()) {
            endingTitle = createEndingTitle("🟡 The Drifter", GOLD);
            conclusion.setText("Tracking reveals freedom.");
        } else if (ctx.player.getFreedomScore() >= 6) {
            endingTitle = createEndingTitle("🟢 The Planner", GREEN);
            conclusion.setText("You chose with intention.");
        } else {
            endingTitle = createEndingTitle("🔵 The Learner", BLUE);
            conclusion.setText("Awareness grows through mistakes.");
        }

        Button menuBtn = new Button("Return to Main Menu");
        menuBtn.setOnAction(e -> app.showMainMenu());

        content.getChildren().addAll(endingTitle, stats, conclusion, menuBtn);
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }

    private Label createEndingTitle(String text, String color) {
        Label title = new Label(text);
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + color + ";"
        );
        return title;
    }

    private boolean checkGameOver() {
        if (ctx.player.getBalance() < 0) {
            showFailureEnding("You ran out of money.");
            return true;
        }
        if (ctx.player.getBalance() < 20 && !ctx.player.isTrackingEnabled()) {
            showFailureEnding("You lost control of your finances.");
            return true;
        }
        return false;
    }

    private void showFailureEnding(String reason) {
        ctx.currentState = GameState.FAILURE;
        saveSessionIfNeeded();

        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = createEndingTitle("🔴 The Burned-Out", RED);
        Label message = new Label(reason);
        message.setWrapText(true);
        message.setMaxWidth(450);

        Button menuBtn = new Button("Return to Main Menu");
        menuBtn.setOnAction(e -> app.showMainMenu());

        content.getChildren().addAll(title, message, menuBtn);
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 600));
        stage.show();
    }

    private void finishDay(VBox content, BorderPane root) {
        content.getChildren().removeIf(n -> n instanceof Button);

        Button finishBtn = new Button("Finish the week");
        finishBtn.setOnAction(e -> showSummary());

        content.getChildren().add(finishBtn);
        root.setTop(createHUD()); // refresh HUD
    }

}
