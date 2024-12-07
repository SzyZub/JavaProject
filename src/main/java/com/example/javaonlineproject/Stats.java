package com.example.javaonlineproject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class Stats {
    private Runnable onBack;
    private Runnable onDisconnect;
    private UserInfo user;
    private Thread disconnectThread;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Stage primaryStage;


    private Button createBackButton() {
        Button backButton = new Button("Back");
        backButton.setFont(new Font(16.0));
        backButton.setOnAction(_ -> backButton());
        return backButton;
    }

/*    private HBox createHBox(VBox left, VBox right){
        HBox organizer = new HBox(12);
        organizer.setPadding(new Insets(8, 8, 8, 100));
        organizer.setSpacing(20);
        HBox.setHgrow(organizer, Priority.ALWAYS);
    }*/

    private VBox createVBoxRight() {
        VBox organizer = new VBox(12);
        organizer.setPrefSize(280, 210);
        organizer.setPadding(new Insets(40, 8, 40, 20));
        organizer.setAlignment(Pos.BASELINE_RIGHT);
        return organizer;
    }

    private VBox createVBoxLeft() {
        VBox organizer = new VBox(12);
        organizer.setPrefSize(280, 210);
        organizer.setPadding(new Insets(40, 8, 40, 20));
        organizer.setAlignment(Pos.BASELINE_LEFT);
        return organizer;
    }

    private BorderPane createManager(VBox organizer, VBox organizer2) {
        BorderPane root = new BorderPane();
        root.setPrefSize(900, 600);
        root.setCenter(organizer);
        root.setLeft(organizer2);
        root.setStyle("-fx-background-color: #1A1A1A;");
        return root;
    }
    private void manageScene(BorderPane manager) {
        Scene scene = new Scene(manager);
        primaryStage.setTitle("Stats");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void displayMatchHistory(List<MatchHistoryData> matchHistory, VBox organizer) {
        if (matchHistory == null || matchHistory.isEmpty()) {
            organizer.getChildren().add(new Label("No match history found."));
        } else {
            for (MatchHistoryData match : matchHistory) {
                String matchDetails = "Date: " + match.getDate() +
                        " | Player: " + match.getPlayer1username() +
                        " | Enemy: " + match.getPlayer2username() +
                        " | Result: " + match.getResult();
                Label matchLabel = new Label(matchDetails);
                matchLabel.setFont(new Font(14.0));
                matchLabel.setTextFill(Color.WHITE);
                matchLabel.setAlignment(Pos.CENTER_RIGHT);
                organizer.getChildren().add(matchLabel);
            }
        }
    }
    private void receiveMatchHistoryFromServer(VBox organizer) {
        user.getUserOutput().sendMessage("GETMATCHHISTORY");
        String message = user.getUserInput().receiveMessage();
        if (message.startsWith("MATCHHISTORY: ")) {
            String matchHistoryJson = message.substring("MATCHHISTORY: ".length());
            System.out.println(matchHistoryJson);//DEBUG
            try {
                List<MatchHistoryData> matchHistory = objectMapper.readValue(matchHistoryJson, new TypeReference<>() {
                });
                displayMatchHistory(matchHistory, organizer);
            } catch (IOException e) {
                System.err.println("Error parsing match history: " + e.getMessage());
                organizer.getChildren().add((new Label("Match history parse ERROR")));
            }
        } else {
            System.out.println("Message does not contain match history: " + message);
            organizer.getChildren().add(new Label("No match history found."));
        }
    }
    private void receiveStatsFromServer(VBox organizer) {
        user.getUserOutput().sendMessage("GETSTATS");
        String message = user.getUserInput().receiveMessage();
        System.out.println(message);
        if (message.startsWith("STATS:")) {
            String statsJson = message.substring("STATS:".length());
            System.out.println(statsJson); //debug
            try {
                StatsData statsData = objectMapper.readValue(statsJson, StatsData.class);
                displayStats(Collections.singletonList(statsData), organizer);
            } catch (IOException e) {
                System.err.println("Error Parsing stats: " + e.getMessage());
                organizer.getChildren().add((new Label("Match history parse ERROR")));
            }
        } else {
            System.out.println("Message does not contain statsData " + message);
            organizer.getChildren().add(new Label("No match history found."));
        }
    }
    private void displayStats(List<StatsData> statsData, VBox organizer) {
        if (statsData == null || statsData.isEmpty()) organizer.getChildren().add((new Label("No stats found.")));
        else {
            for (StatsData stats : statsData) {
                String statsDetails = "You: " + stats.getUsername() +
                        "   " + stats.getWins() +
                        "-" + stats.getDraws() +
                        "-" + stats.getLosses();
                Label statsLabel = new Label(statsDetails);
                statsLabel.setFont(new Font(34));
                statsLabel.setTextFill(Color.RED);
                organizer.getChildren().add(statsLabel);
            }
        }
    }
    private void receiveBestPlayersFromServer(VBox organizer) {
        user.getUserOutput().sendMessage("GETBESTPLAYERS");
        String message = user.getUserInput().receiveMessage();
        if (message.startsWith("BESTPLAYERS:")) {
            String statsJson = message.substring("BESTPLAYERS:".length());
            System.out.println(statsJson);
            try {
                if (!statsJson.startsWith("[")) {
                    statsJson = "[" + statsJson + "]";
                }
                List<StatsData> bestPlayer = objectMapper.readValue(statsJson, new TypeReference<List<StatsData>>() {});
                displayBestPlayers(bestPlayer, organizer);
            } catch (IOException e) {
                System.err.println("Error Parsing stats: " + e.getMessage());
            }
        } else {
            System.out.println("Message does not contain statsData " + message);
        }
    }

    private void displayBestPlayers(List<StatsData> bestplayer, VBox organizer) {
        if (bestplayer == null || bestplayer.isEmpty())
            organizer.getChildren().add((new Label("No best player found.")));
        else {
            for (StatsData player : bestplayer) {
                String statsDetails = "Username: " + player.getUsername() +
                        " |Wins: " + player.getWins() +
                        " |Draws" + player.getDraws() +
                        " |Loses" + player.getLosses();
                Label statsLabel = new Label(statsDetails);
                statsLabel.setFont(new Font(28));
                statsLabel.setTextFill(Color.BLUE);
                organizer.getChildren().add(statsLabel);
            }
        }
    }
    public void start(Stage primaryStage, UserInfo user) {
        this.user = user;
        this.primaryStage = primaryStage;
        Button backButton = createBackButton();
        VBox organizer = createVBoxRight();
        VBox organizer2 = createVBoxLeft();
        BorderPane manager = createManager(organizer, organizer2);
        organizer.getChildren().add(backButton);
        receiveStatsFromServer(organizer);
        receiveMatchHistoryFromServer(organizer);
        receiveBestPlayersFromServer(organizer2);
        manageScene(manager);
        checkForDisconnect();
    }


    private void checkForDisconnect() {
        Runnable disconnectChecker = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                String move = user.getUserInput().receiveMessage();
                if (move == null) continue;
                else if (move.equals("SOCKETERROR")) {
                    Platform.runLater(Stats.this::disconnect);
                    return;
                }
            }
        };
        disconnectThread = new Thread(disconnectChecker);
        disconnectThread.setDaemon(true);
        disconnectThread.start();
    }
    private void backButton() {
        disconnectThread.interrupt();
        try {
            disconnectThread.join();
        } catch (InterruptedException _) {}
        onBack.run();
    }
    private void disconnect() {
        disconnectThread.interrupt();
        try {
            disconnectThread.join();
        } catch (InterruptedException _) {}
        user.closeConnection();
        onDisconnect.run();
    }
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }
}
