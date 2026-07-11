package com.hotelreserve;

import com.hotelreserve.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Something went wrong: " + throwable.getMessage() + "\n\nYou can keep using the system, but please double-check your last action.");
            alert.setHeaderText("Unexpected Error");
            alert.showAndWait();
        });

        try {
            DatabaseManager.initSchema();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreserve/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/com/hotelreserve/css/styles.css").toExternalForm());

            stage.setTitle("Hotel Reservation Management System");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The application could not start: " + e.getMessage());
            alert.setHeaderText("Startup Failed");
            alert.showAndWait();
            throw new IllegalStateException("Failed to start Hotel Reservation Management System", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
