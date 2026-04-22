import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class CameraView {
	
    private Scene scene;

    public CameraView(Stage stage) {
    	
    	Label cameraTitle = new Label("ZooDex View");
        Label cameraPlaceholder = new Label("Camera loading");
        
        Timeline trackingAnimation = new Timeline(
        	    new KeyFrame(Duration.seconds(0), e -> cameraPlaceholder.setText("Camera loading")),
        	    new KeyFrame(Duration.seconds(0.5), e -> cameraPlaceholder.setText("Camera loading.")),
        	    new KeyFrame(Duration.seconds(1.0), e -> cameraPlaceholder.setText("Camera loading..")),
        	    new KeyFrame(Duration.seconds(1.5), e -> cameraPlaceholder.setText("Camera loading..."))
        	);

        	// repeat forever
        	trackingAnimation.setCycleCount(Timeline.INDEFINITE);
        	trackingAnimation.play();
        
        StackPane cameraPane = new StackPane(cameraPlaceholder);
        cameraPane.setPrefSize(400, 250);
        cameraPane.setStyle(
            "-fx-border-color: gray;" +
            "-fx-border-width: 2;" +
            "-fx-background-color: #eeeeee;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );


        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> {
            stage.setScene(new MainMenuView(stage).getScene());
        });

        VBox centerBox = new VBox(15, cameraTitle, cameraPane, backButton);
        centerBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(centerBox);

        scene = new Scene(root, 800, 480);
        applyTheme();

        var css = getClass().getResource("style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
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
