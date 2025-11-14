package application;

import database.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        DBUtil.initializeDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        primaryStage.setTitle("Food Inventory - Login");
        Scene scene = new Scene(root, 800, 500);
        URL cssUrl = getClass().getResource("/styles.css");
        System.out.println("styles.css URL = " + cssUrl);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            // Fallback visual style so you can see an immediate change even if CSS fails to load
            root.setStyle("-fx-font-family: 'Comic Sans MS', 'Segoe UI', sans-serif; -fx-background-color: linear-gradient(to bottom right, #fff0f6, #e6f7ff);");
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
