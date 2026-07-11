package com.hotelreserve.controller;

import com.hotelreserve.dao.GuestRepository;
import com.hotelreserve.dao.ReservationRepository;
import com.hotelreserve.dao.RoomRepository;
import com.hotelreserve.dao.SqliteGuestRepository;
import com.hotelreserve.dao.SqliteReservationRepository;
import com.hotelreserve.dao.SqliteRoomRepository;
import com.hotelreserve.exception.RoomUnavailableException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.Room;
import com.hotelreserve.service.PricingEngine;
import com.hotelreserve.service.ReservationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class ReservationsController {

    @FXML private ComboBox<Guest> guestCombo;
    @FXML private ComboBox<Room> roomCombo;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private TextField numberOfGuestsField;
    @FXML private Label quoteLabel;
    @FXML private Label bookingErrorLabel;

    @FXML private TableView<Reservation> table;
    @FXML private TableColumn<Reservation, String> guestColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, String> datesColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

    private final RoomRepository roomRepository = new SqliteRoomRepository();
    private final GuestRepository guestRepository = new SqliteGuestRepository();
    private final ReservationRepository reservationRepository =
            new SqliteReservationRepository(guestRepository, roomRepository);
    private final ReservationService reservationService = new ReservationService(reservationRepository, roomRepository);
    private final PricingEngine pricingEngine = new PricingEngine();

    @FXML
    public void initialize() {
        setupColumns();
        setupGuestCombo();
        setupRoomCombo();
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshAvailableRooms());
        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshAvailableRooms());
        roomCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateQuote());

        refreshAvailableRooms();
        refreshTable();
    }

    private void setupGuestCombo() {
        guestCombo.setItems(FXCollections.observableArrayList(guestRepository.findAll()));
        guestCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Guest guest) {
                return guest == null ? "" : guest.getFullName() + " (" + guest.getPhone() + ")";
            }

            @Override
            public Guest fromString(String string) {
                return null;
            }
        });
    }

    private void setupRoomCombo() {
        roomCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room room) {
                return room == null ? "" : room.getRoomNumber() + " — " + room.roomType()
                        + " (GHS " + String.format("%.2f", room.getBaseRate()) + "/night)";
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void onRefreshRooms() {
        refreshAvailableRooms();
    }

    private void refreshAvailableRooms() {
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            roomCombo.setItems(FXCollections.observableArrayList());
            quoteLabel.setText("Choose valid check-in/check-out dates to see available rooms.");
            return;
        }

        List<Room> available = roomRepository.findAll().stream()
                .filter(r -> reservationService.checkAvailability(r, checkIn, checkOut))
                .toList();
        roomCombo.setItems(FXCollections.observableArrayList(available));
        updateQuote();
    }

    private void updateQuote() {
        Room room = roomCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();
        if (room == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            quoteLabel.setText("Select a room and valid dates to see a price quote.");
            return;
        }
        try {
            PricingEngine.PriceBreakdown quote = pricingEngine.computeQuote(room, checkIn, checkOut);
            quoteLabel.setText(String.format(
                    "%d night(s) — Subtotal GHS %.2f, Discount GHS %.2f, Weekend Surcharge GHS %.2f — Total GHS %.2f",
                    quote.nights(), quote.subtotal(), quote.discount(), quote.surcharge(), quote.total()));
        } catch (ValidationException e) {
            quoteLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void onBookReservation() {
        bookingErrorLabel.setText("");
        Guest guest = guestCombo.getValue();
        Room room = roomCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (guest == null) {
            bookingErrorLabel.setText("Please choose a guest");
            return;
        }
        if (room == null) {
            bookingErrorLabel.setText("Please choose a room");
            return;
        }

        int numberOfGuests;
        try {
            numberOfGuests = Integer.parseInt(numberOfGuestsField.getText().trim());
        } catch (NumberFormatException e) {
            bookingErrorLabel.setText("Number of guests must be a valid number");
            return;
        }

        try {
            reservationService.bookReservation(guest, room, checkIn, checkOut, numberOfGuests);
            refreshAvailableRooms();
            refreshTable();
            numberOfGuestsField.clear();
        } catch (ValidationException | RoomUnavailableException e) {
            bookingErrorLabel.setText(e.getMessage());
        }
    }

    private void setupColumns() {
        guestColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuest().getFullName()));
        roomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom().getRoomNumber()));
        datesColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckInDate() + " to " + data.getValue().getCheckOutDate()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button checkInButton = new Button("Check In");
            private final Button checkOutButton = new Button("Check Out");
            private final Button cancelButton = new Button("Cancel");
            private final HBox box = new HBox(6, checkInButton, checkOutButton, cancelButton);

            {
                checkInButton.setOnAction(e -> onCheckIn(getTableView().getItems().get(getIndex())));
                checkOutButton.setOnAction(e -> onCheckOut(getTableView().getItems().get(getIndex())));
                cancelButton.setOnAction(e -> onCancel(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(reservationRepository.findAll()));
    }

    private void onCheckIn(Reservation reservation) {
        try {
            reservationService.checkIn(reservation.getId());
            refreshTable();
            refreshAvailableRooms();
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    private void onCheckOut(Reservation reservation) {
        try {
            reservationService.checkOut(reservation.getId());
            refreshTable();
            refreshAvailableRooms();
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    private void onCancel(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel reservation #" + reservation.getId() + " for " + reservation.getGuest().getFullName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Cancel Reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.cancelReservation(reservation.getId());
                    refreshTable();
                    refreshAvailableRooms();
                } catch (ValidationException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}
