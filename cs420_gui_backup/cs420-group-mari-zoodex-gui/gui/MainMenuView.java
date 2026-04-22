import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuView {

    private Scene scene;

    public MainMenuView(Stage stage) {
        Label title = new Label("Main Menu");

        Button button1 = new Button("Open Camera");
        Button button2 = new Button("Second Screen");
        Button toggleThemeButton = new Button("Toggle Theme");
        Button exitButton = new Button("Exit Application");

        button1.setOnAction(e -> stage.setScene(new CameraView(stage).getScene()));
        button2.setOnAction(e -> stage.setScene(new IndexView(stage).getScene()));

        toggleThemeButton.setOnAction(e -> {
            ThemeManager.toggleTheme();
            applyTheme();
        });

        exitButton.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, button1, button2, toggleThemeButton, exitButton);
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 800, 480);
        applyTheme();
    }

    private void applyTheme() {
        scene.getStylesheets().clear();

        var css = getClass().getResource(ThemeManager.getThemeFile());
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