package com.hotelreserve.controller;

import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.DeluxeRoom;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.model.StandardRoom;
import com.hotelreserve.model.SuiteRoom;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RoomFormController {

    @FXML private TextField roomNumberField;
    @FXML private TextField floorField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField maxOccupancyField;
    @FXML private TextField baseRateField;
    @FXML private ComboBox<RoomStatus> statusCombo;
    @FXML private Label errorLabel;

    private int editingId = 0;

    @FXML
    public void initialize() {
        roomTypeCombo.setItems(FXCollections.observableArrayList("STANDARD", "DELUXE", "SUITE"));
        roomTypeCombo.setValue("STANDARD");
        statusCombo.setItems(FXCollections.observableArrayList(RoomStatus.values()));
        statusCombo.setValue(RoomStatus.AVAILABLE);
    }

    public void loadForEdit(Room room) {
        editingId = room.getId();
        roomNumberField.setText(room.getRoomNumber());
        floorField.setText(String.valueOf(room.getFloor()));
        roomTypeCombo.setValue(room.roomType());
        maxOccupancyField.setText(String.valueOf(room.getMaxOccupancy()));
        baseRateField.setText(String.valueOf(room.getBaseRate()));
        statusCombo.setValue(room.getStatus());
    }

    public Room buildRoom() throws ValidationException {
        errorLabel.setText("");
        String roomNumber = roomNumberField.getText();

        int floor;
        int maxOccupancy;
        double baseRate;
        try {
            floor = Integer.parseInt(floorField.getText().trim());
            maxOccupancy = Integer.parseInt(maxOccupancyField.getText().trim());
            baseRate = Double.parseDouble(baseRateField.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Floor, max occupancy, and base rate must be valid numbers");
        }

        RoomStatus status = statusCombo.getValue();
        String roomType = roomTypeCombo.getValue();

        return switch (roomType) {
            case "DELUXE" -> new DeluxeRoom(editingId, roomNumber, floor, status, maxOccupancy, baseRate);
            case "SUITE" -> new SuiteRoom(editingId, roomNumber, floor, status, maxOccupancy, baseRate);
            default -> new StandardRoom(editingId, roomNumber, floor, status, maxOccupancy, baseRate);
        };
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }
}
