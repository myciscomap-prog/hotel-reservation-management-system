package com.hotelreserve.controller;

import com.hotelreserve.dao.RoomRepository;
import com.hotelreserve.dao.SqliteRoomRepository;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RoomsController {

    @FXML private ComboBox<RoomStatus> filterStatusCombo;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private TableView<Room> table;
    @FXML private TableColumn<Room, String> numberColumn;
    @FXML private TableColumn<Room, String> typeColumn;
    @FXML private TableColumn<Room, String> floorColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, String> rateColumn;
    @FXML private TableColumn<Room, Void> actionsColumn;

    private final RoomRepository roomRepository = new SqliteRoomRepository();

    @FXML
    public void initialize() {
        setupColumns();
        filterStatusCombo.setItems(FXCollections.observableArrayList(RoomStatus.values()));
        filterStatusCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomStatus status) {
                return status == null ? "All Statuses" : status.name();
            }

            @Override
            public RoomStatus fromString(String string) {
                return null;
            }
        });
        filterTypeCombo.setItems(FXCollections.observableArrayList("All Types", "STANDARD", "DELUXE", "SUITE"));
        filterTypeCombo.setValue("All Types");
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        filterTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        refreshTable();
    }

    private void setupColumns() {
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().roomType()));
        floorColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getFloor())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        rateColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("GHS %.2f", data.getValue().getBaseRate())));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button maintenanceButton = new Button("Toggle Maintenance");
            private final HBox box = new HBox(6, editButton, maintenanceButton);

            {
                editButton.setOnAction(e -> onEditRoom(getTableView().getItems().get(getIndex())));
                maintenanceButton.setOnAction(e -> onToggleMaintenance(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void refreshTable() {
        List<Room> all = roomRepository.findAll();
        RoomStatus statusFilter = filterStatusCombo.getValue();
        String typeFilter = filterTypeCombo.getValue();

        List<Room> filtered = all.stream()
                .filter(r -> statusFilter == null || r.getStatus() == statusFilter)
                .filter(r -> typeFilter == null || typeFilter.equals("All Types") || r.roomType().equals(typeFilter))
                .toList();

        table.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void onResetFilters() {
        filterStatusCombo.setValue(null);
        filterTypeCombo.setValue("All Types");
        refreshTable();
    }

    @FXML
    public void onAddRoom() {
        openRoomDialog(null);
    }

    private void onEditRoom(Room room) {
        openRoomDialog(room);
    }

    private void onToggleMaintenance(Room room) {
        try {
            room.setStatus(room.getStatus() == RoomStatus.MAINTENANCE ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE);
            roomRepository.update(room);
            refreshTable();
        } catch (Exception e) {
            showError("Could not update room status: " + e.getMessage());
        }
    }

    private void openRoomDialog(Room existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreserve/fxml/room_form.fxml"));
            Parent formRoot = loader.load();
            RoomFormController formController = loader.getController();
            if (existing != null) {
                formController.loadForEdit(existing);
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Add Room" : "Edit Room");
            dialog.getDialogPane().setContent(formRoot);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            AtomicReference<Room> builtRoom = new AtomicReference<>();
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    builtRoom.set(formController.buildRoom());
                } catch (ValidationException ex) {
                    formController.showError(ex.getMessage());
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK && builtRoom.get() != null) {
                roomRepository.save(builtRoom.get());
                refreshTable();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not open room form", e);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}
