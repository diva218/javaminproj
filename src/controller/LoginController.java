package controller;

import database.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Session;
import model.User;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private ChoiceBox<String> roleChoice;

    @FXML
    private void initialize() {
        roleChoice.getItems().addAll("NGO", "SUPPLIER");
        roleChoice.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String role = roleChoice.getValue();
        if (username.isEmpty()) {
            username = role.equals("NGO") ? "ngo1" : "supplier1"; // simple default
        }
        try {
            DBUtil.ensureUser(username, role);
        } catch (Exception ex) {
            // ignore for simplicity
        }
        Session.setCurrentUser(new User(username, role));
        try {
            String fxml = role.equals("NGO") ? "/NgoHome.fxml" : "/SupplierHome.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(role.equals("NGO") ? "NGO Dashboard" : "Supplier Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
