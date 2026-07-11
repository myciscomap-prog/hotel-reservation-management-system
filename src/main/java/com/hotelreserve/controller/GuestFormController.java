package com.hotelreserve.controller;

import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GuestFormController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField idNumberField;
    @FXML private Label errorLabel;

    private int editingId = 0;

    public void loadForEdit(Guest guest) {
        editingId = guest.getId();
        fullNameField.setText(guest.getFullName());
        emailField.setText(guest.getEmail());
        phoneField.setText(guest.getPhone());
        idNumberField.setText(guest.getIdNumber());
    }

    public Guest buildGuest() throws ValidationException {
        errorLabel.setText("");
        return new Guest(editingId, fullNameField.getText(), emailField.getText(), phoneField.getText(),
                idNumberField.getText());
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }
}
