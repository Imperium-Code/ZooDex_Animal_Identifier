package com.cs420;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class IndexView {

    private Scene scene;

    public IndexView(Stage stage) {
        Label title = new Label("Animal Index");

        Label animalNameLabel = new Label("Select an animal");
        animalNameLabel.setId("animalNameLabel");

        Label animalBioLabel = new Label("Its name and bio will appear here.");
        animalBioLabel.setWrapText(true);
        animalBioLabel.setId("animalBioLabel");

        VBox detailPanel = new VBox(15, animalNameLabel, animalBioLabel);
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

        // Placeholder animal data
        String[] animalNames = {
                "Fox", "Rabbit", "Owl", "Deer",
                "Bear", "Frog", "Turtle", "Squirrel",
                "Raccoon", "Hawk", "Skunk", "Mouse"
        };

        String[] animalBios = {
                "Foxes are clever mammals known for their fluffy tails and quick movements.",
                "Rabbits are small herbivores with long ears and strong back legs.",
                "Owls are birds of prey that are often active at night.",
                "Deer are graceful herbivores often found in forests and fields.",
                "Bears are large mammals that can be powerful, curious, and intelligent.",
                "Frogs are amphibians that live near water and have strong jumping legs.",
                "Turtles are reptiles with protective shells and slow, steady movements.",
                "Squirrels are energetic rodents often seen climbing trees.",
                "Raccoons are smart mammals known for their masked faces and dexterous paws.",
                "Hawks are sharp-eyed birds of prey that soar high in the sky.",
                "Skunks are mammals known for their black-and-white coloring and strong spray defense.",
                "Mice are tiny rodents that are quick and adaptable."
        };

        for (int i = 0; i < animalNames.length; i++) {
            Button animalButton = new Button();
            animalButton.setPrefSize(120, 120);
            animalButton.setWrapText(true);

            // Placeholder text for now
            animalButton.setText(animalNames[i]);

            final String selectedName = animalNames[i];
            final String selectedBio = animalBios[i];

            animalButton.setOnAction(e -> {
                animalNameLabel.setText(selectedName);
                animalBioLabel.setText(selectedBio);
            });

            int col = i % 4;
            int row = i / 4;
            grid.add(animalButton, col, row);
        }

        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> stage.setScene(new MainMenuView(stage).getScene()));

        VBox leftSection = new VBox(15, title, grid, backButton);
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
