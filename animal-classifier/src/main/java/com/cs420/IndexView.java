package com.cs420;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class IndexView {

    private Scene scene;

    public IndexView(Stage stage) {
        Label title = new Label("Animal Index");

        Label animalNameLabel = new Label("Select an animal");
        animalNameLabel.setId("animalNameLabel");

        Label animalBioLabel = new Label("Its name and bio will appear here.");
        animalBioLabel.setWrapText(true);
        animalBioLabel.setId("animalBioLabel");

        ImageView detailImageView = new ImageView();
        detailImageView.setFitWidth(200);
        detailImageView.setPreserveRatio(true);

        VBox detailPanel = new VBox(15, detailImageView, animalNameLabel, animalBioLabel);
        detailPanel.setPadding(new Insets(20));
        detailPanel.setAlignment(Pos.TOP_CENTER);
        detailPanel.setPrefWidth(250);
        detailPanel.setId("detailPanel");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        grid.setId("animalGrid");

        File capturesDir = new File("captures");
        File[] files = capturesDir.exists() ? capturesDir.listFiles((dir, name) -> name.endsWith(".jpg")) : new File[0];

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File imgFile = files[i];
                Button animalButton = new Button();
                animalButton.setPrefSize(120, 120);

                ImageView thumbnail = new ImageView(new Image(imgFile.toURI().toString(), 100, 100, true, true));
                animalButton.setGraphic(thumbnail);

                String baseName = imgFile.getName().substring(0, imgFile.getName().lastIndexOf('.'));
                File txtFile = new File(capturesDir, baseName + ".txt");

                String animalName = "Unknown";
                String animalBio = "No data available.";

                if (txtFile.exists()) {
                    try {
                        List<String> lines = Files.readAllLines(txtFile.toPath());
                        if (lines.size() >= 1)
                            animalName = lines.get(0);
                        if (lines.size() >= 2)
                            animalBio = lines.get(1);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                final String selectedName = animalName;
                final String selectedBio = animalBio;
                final String imgUri = imgFile.toURI().toString();

                animalButton.setOnAction(e -> {
                    detailImageView.setImage(new Image(imgUri));
                    animalNameLabel.setText(selectedName);
                    animalBioLabel.setText(selectedBio);
                });

                int col = i % 4;
                int row = i / 4;
                grid.add(animalButton, col, row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> stage.setScene(new MainMenuView(stage).getScene()));

        VBox leftSection = new VBox(15, title, scrollPane, backButton);
        leftSection.setAlignment(Pos.CENTER);
        leftSection.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setCenter(leftSection);
        root.setRight(detailPanel);

        scene = new Scene(root, 900, 550);
        applyTheme();
    }

    private void applyTheme() {
        scene.getStylesheets().clear();

        java.net.URL css = getClass().getResource("/styles/" + ThemeManager.getThemeFile());
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("Theme file not found: " + ThemeManager.getThemeFile());
        }
    }

    public Scene getScene() {
        return scene;
    }
}
