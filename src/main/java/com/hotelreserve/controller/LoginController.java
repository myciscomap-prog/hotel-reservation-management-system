package com.hotelreserve.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Simple front-desk login gate. Credentials are intentionally hardcoded for this coursework
 * project (no user-management system was in scope) — see README "Login Details".
 */
public class LoginController {

    private static final String VALID_USERNAME = "frontdesk";
    private static final String VALID_PIN = "1234";

    @FXML private TextField usernameField;
    @FXML private PasswordField pinField;
    @FXML private Label errorLabel;

    @FXML
    public void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pin = pinField.getText() == null ? "" : pinField.getText().trim();

        if (!VALID_USERNAME.equals(username) || !VALID_PIN.equals(pin)) {
            errorLabel.setText("Invalid username or PIN. Try again.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreserve/fxml/main.fxml"));
            Parent root = loader.load();
            var stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            var scene = usernameField.getScene();
            scene.setRoot(root);
            stage.setTitle("Hotel Reservation Management System — Front Desk");
        } catch (IOException e) {
            throw new IllegalStateException("Could not load main application shell", e);
        }
    }
}
