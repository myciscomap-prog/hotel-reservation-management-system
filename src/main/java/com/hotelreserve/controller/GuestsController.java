package com.hotelreserve.controller;

import com.hotelreserve.dao.GuestRepository;
import com.hotelreserve.dao.SqliteGuestRepository;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class GuestsController {

    @FXML private TextField searchField;
    @FXML private TableView<Guest> table;
    @FXML private TableColumn<Guest, String> nameColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, String> idColumn;
    @FXML private TableColumn<Guest, Void> actionsColumn;

    private final GuestRepository guestRepository = new SqliteGuestRepository();

    @FXML
    public void initialize() {
        setupColumns();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        refreshTable();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdNumber()));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final HBox box = new HBox(6, editButton);

            {
                editButton.setOnAction(e -> onEditGuest(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void refreshTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<Guest> filtered = guestRepository.findAll().stream()
                .filter(g -> search.isBlank() || g.getFullName().toLowerCase().contains(search))
                .toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void onAddGuest() {
        openGuestDialog(null);
    }

    private void onEditGuest(Guest guest) {
        openGuestDialog(guest);
    }

    private void openGuestDialog(Guest existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreserve/fxml/guest_form.fxml"));
            Parent formRoot = loader.load();
            GuestFormController formController = loader.getController();
            if (existing != null) {
                formController.loadForEdit(existing);
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Add Guest" : "Edit Guest");
            dialog.getDialogPane().setContent(formRoot);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            AtomicReference<Guest> builtGuest = new AtomicReference<>();
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    builtGuest.set(formController.buildGuest());
                } catch (ValidationException ex) {
                    formController.showError(ex.getMessage());
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK && builtGuest.get() != null) {
                guestRepository.save(builtGuest.get());
                refreshTable();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not open guest form", e);
        }
    }
}
