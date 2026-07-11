package com.hotelreserve.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button navDashboard;
    @FXML private Button navRooms;
    @FXML private Button navGuests;
    @FXML private Button navReservations;
    @FXML private Button navBilling;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        load("dashboard.fxml", navDashboard);
    }

    @FXML
    public void showRooms() {
        load("rooms.fxml", navRooms);
    }

    @FXML
    public void showGuests() {
        load("guests.fxml", navGuests);
    }

    @FXML
    public void showReservations() {
        load("reservations.fxml", navReservations);
    }

    @FXML
    public void showBilling() {
        load("billing.fxml", navBilling);
    }

    private void load(String fxmlFile, Button activeButton) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/hotelreserve/fxml/" + fxmlFile));
            contentArea.getChildren().setAll(node);
            highlight(activeButton);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load view: " + fxmlFile, e);
        }
    }

    private void highlight(Button activeButton) {
        List<Button> navButtons = List.of(navDashboard, navRooms, navGuests, navReservations, navBilling);
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-active");
        }
        activeButton.getStyleClass().add("nav-button-active");
    }
}
