import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainMenuView menu = new MainMenuView(primaryStage);

        primaryStage.setTitle("Main Menu");
        primaryStage.setScene(menu.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
