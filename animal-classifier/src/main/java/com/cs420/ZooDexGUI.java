package com.cs420;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

public class ZooDexGUI extends Application {

    private boolean isScanning = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ZooDex (JavaFX Edition)");

        StackPane root = new StackPane();
        root.getStyleClass().add("root");

        // 1. MAIN UI PANE (hidden at first)
        HBox mainUI = createMainUI();
        mainUI.setOpacity(0); // Hidden
        root.getChildren().add(mainUI);

        // 2. LOADING SCREEN
        VBox loadingScreen = createLoadingScreen();
        root.getChildren().add(loadingScreen);

        Scene scene = new Scene(root, 800, 480);
        scene.getStylesheets().add(getClass().getResource("/styles/zoodex.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Boot Sequence Simulation (3 Seconds)
        Timeline bootTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), loadingScreen);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                root.getChildren().remove(loadingScreen);
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), mainUI);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }));
        bootTimer.play();
    }

    private VBox createLoadingScreen() {
        VBox loadingScreen = new VBox(20);
        loadingScreen.setAlignment(Pos.CENTER);
        loadingScreen.getStyleClass().add("loading-screen");

        try {
            InputStream is = getClass().getResourceAsStream("/images/Attach0.png");
            if (is != null) {
                Image image = new Image(is);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(400);
                imageView.setPreserveRatio(true);
                loadingScreen.getChildren().add(imageView);
            }
        } catch (Exception e) {
            System.err.println("Failed to load Attach0.png!");
            e.printStackTrace();
        }

        Text loadingText = new Text("BOOTING ZOODEX SYSTEM...");
        loadingText.getStyleClass().add("loading-text");
        loadingScreen.getChildren().add(loadingText);

        return loadingScreen;
    }

    private HBox createMainUI() {
        HBox mainUI = new HBox();
        mainUI.getStyleClass().add("pokedex-bg");

        // LEFT PANEL
        VBox leftPanel = new VBox(20);
        leftPanel.getStyleClass().add("left-panel");
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // LEDs
        HBox ledContainer = new HBox(10);
        ledContainer.setAlignment(Pos.TOP_LEFT);
        Region ledBigBlue = new Region();
        ledBigBlue.getStyleClass().add("led-big-blue");
        Region ledRed = new Region();
        ledRed.getStyleClass().addAll("led-small", "led-red");
        Region ledYellow = new Region();
        ledYellow.getStyleClass().addAll("led-small", "led-yellow");
        Region ledGreen = new Region();
        ledGreen.getStyleClass().addAll("led-small", "led-green");
        ledContainer.getChildren().addAll(ledBigBlue, ledRed, ledYellow, ledGreen);

        // Camera
        VBox cameraBezel = new VBox();
        cameraBezel.getStyleClass().add("camera-bezel");
        VBox.setVgrow(cameraBezel, Priority.ALWAYS);
        StackPane cameraScreen = new StackPane();
        cameraScreen.getStyleClass().add("camera-screen");
        VBox.setVgrow(cameraScreen, Priority.ALWAYS);
        Text placeholder = new Text("CAMERA FEED OFFLINE");
        placeholder.getStyleClass().add("placeholder-text");

        Region scanLine = new Region();
        scanLine.getStyleClass().add("scan-line");
        scanLine.setPrefHeight(5);
        scanLine.setMaxHeight(5);
        scanLine.setVisible(false);
        StackPane.setAlignment(scanLine, Pos.TOP_CENTER);
        cameraScreen.getChildren().addAll(placeholder, scanLine);
        cameraBezel.getChildren().add(cameraScreen);

        // Button
        HBox btnContainer = new HBox();
        btnContainer.setAlignment(Pos.CENTER);
        Button scanBtn = new Button("SCAN");
        scanBtn.getStyleClass().add("scan-btn");
        btnContainer.getChildren().add(scanBtn);

        leftPanel.getChildren().addAll(ledContainer, cameraBezel, btnContainer);

        // HINGE //
        Region hinge = new Region();
        hinge.getStyleClass().add("hinge");

        // RIGHT PANEL
        VBox rightPanel = new VBox(20);
        rightPanel.getStyleClass().add("right-panel");
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Data Screen
        VBox dataScreen = new VBox(10);
        dataScreen.getStyleClass().add("data-screen");
        Text animalName = new Text("UNKNOWN");
        animalName.getStyleClass().add("animal-name");

        StackPane typeContainer = new StackPane();
        typeContainer.setAlignment(Pos.CENTER_LEFT);
        Text typeText = new Text("");
        typeText.getStyleClass().add("type-badge");
        typeText.setVisible(false);
        typeContainer.getChildren().add(typeText);

        Text animalDesc = new Text("Awaiting scan data...");
        animalDesc.getStyleClass().add("animal-desc");
        animalDesc.setWrappingWidth(300);

        dataScreen.getChildren().addAll(animalName, typeContainer, animalDesc);
        dataScreen.setPrefHeight(200);

        rightPanel.getChildren().add(dataScreen);
        mainUI.getChildren().addAll(leftPanel, hinge, rightPanel);

        // SCAN LOGIC
        TranslateTransition scanAnim = new TranslateTransition(Duration.seconds(2), scanLine);
        scanAnim.setFromY(0);
        scanAnim.setToY(220); // Rough pixel height inside the camera frame
        scanAnim.setCycleCount(Timeline.INDEFINITE);
        scanAnim.setAutoReverse(false);

        scanBtn.setOnAction(e -> {
            if (isScanning)
                return;
            isScanning = true;

            scanLine.setVisible(true);
            scanAnim.playFromStart();

            animalName.setText("SCANNING...");
            animalDesc.setText("Analyzing structural data...");
            typeText.setVisible(false);

            Timeline mockBackendDelay = new Timeline(new KeyFrame(Duration.seconds(4), ev -> {
                scanAnim.stop();
                scanLine.setVisible(false);

                animalName.setText("Red Panda");
                typeText.setText("TYPE: MAMMAL");
                typeText.setVisible(true);

                String descText = "A small arboreal mammal native to the eastern Himalayas. It has reddish-brown fur and a long, shaggy tail.";
                animalDesc.setText("");

                // Typewriter effect
                Timeline typewriter = new Timeline();
                for (int i = 0; i < descText.length(); i++) {
                    final int idx = i;
                    typewriter.getKeyFrames().add(
                            new KeyFrame(Duration.millis(20 * i), event -> {
                                animalDesc.setText(animalDesc.getText() + descText.charAt(idx));
                            }));
                }
                typewriter.setOnFinished(f -> isScanning = false);
                typewriter.play();
            }));
            mockBackendDelay.play();
        });

        return mainUI;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
