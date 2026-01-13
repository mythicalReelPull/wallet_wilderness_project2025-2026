package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.gameplayController.GameController;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController(stage);
        controller.start();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
