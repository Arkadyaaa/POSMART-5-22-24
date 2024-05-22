package main;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/pos";
        String username = "root";
        String password = "";

        // Attempt to connect to the database
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            launch(args);
        } catch (SQLException e) {
            // If connection fails, print an error message
            System.err.println("Failed to connect to the database!");

            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText(null);
                alert.setContentText("Database not connected.");
                alert.showAndWait();
            });
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Application icon
        InputStream stream = getClass().getResourceAsStream("/resources/icon.png");
        if (stream != null) {
            Image icon = new Image(stream);
            primaryStage.getIcons().add(icon);
        } else {
            System.out.println("Resource not found!");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("POS.fxml"));
        Parent root = loader.load();

        root.requestFocus();
        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("POSmart Point of Sale");
        primaryStage.show();
    }
}
