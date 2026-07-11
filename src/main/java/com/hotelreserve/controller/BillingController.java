package com.hotelreserve.controller;

import com.hotelreserve.dao.GuestRepository;
import com.hotelreserve.dao.PaymentRepository;
import com.hotelreserve.dao.ReservationRepository;
import com.hotelreserve.dao.RoomRepository;
import com.hotelreserve.dao.SqliteGuestRepository;
import com.hotelreserve.dao.SqlitePaymentRepository;
import com.hotelreserve.dao.SqliteReservationRepository;
import com.hotelreserve.dao.SqliteRoomRepository;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.PaymentMethod;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.service.BillingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class BillingController {

    @FXML private ComboBox<Reservation> reservationCombo;
    @FXML private TextArea invoiceArea;
    @FXML private ComboBox<PaymentMethod> methodCombo;
    @FXML private TextField amountField;
    @FXML private Label paymentErrorLabel;

    private final RoomRepository roomRepository = new SqliteRoomRepository();
    private final GuestRepository guestRepository = new SqliteGuestRepository();
    private final ReservationRepository reservationRepository =
            new SqliteReservationRepository(guestRepository, roomRepository);
    private final PaymentRepository paymentRepository = new SqlitePaymentRepository(reservationRepository);
    private final BillingService billingService = new BillingService(paymentRepository);

    @FXML
    public void initialize() {
        reservationCombo.setItems(FXCollections.observableArrayList(reservationRepository.findAll()));
        reservationCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reservation reservation) {
                return reservation == null ? "" : "#" + reservation.getId() + " — " + reservation.getGuest().getFullName()
                        + " — Room " + reservation.getRoom().getRoomNumber();
            }

            @Override
            public Reservation fromString(String string) {
                return null;
            }
        });
        methodCombo.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        reservationCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshInvoice());
    }

    @FXML
    public void onGenerateInvoice() {
        refreshInvoice();
    }

    private void refreshInvoice() {
        Reservation reservation = reservationCombo.getValue();
        if (reservation == null) {
            invoiceArea.setText("Select a reservation to view its invoice.");
            return;
        }
        try {
            BillingService.Invoice invoice = billingService.generateInvoice(reservation);
            invoiceArea.setText(formatInvoice(invoice));
        } catch (ValidationException e) {
            invoiceArea.setText("Could not generate invoice: " + e.getMessage());
        }
    }

    private String formatInvoice(BillingService.Invoice invoice) {
        Reservation r = invoice.reservation();
        var breakdown = invoice.breakdown();
        return String.format("""
                INVOICE — Reservation #%d
                Guest: %s (%s)
                Room: %s (%s)
                Stay: %s to %s (%d night(s))

                Subtotal:          GHS %.2f
                Length-of-stay discount: -GHS %.2f
                Weekend surcharge:  +GHS %.2f
                --------------------------------
                Total:              GHS %.2f
                Amount Paid:        GHS %.2f
                Balance Due:        GHS %.2f
                """,
                r.getId(), r.getGuest().getFullName(), r.getGuest().getPhone(),
                r.getRoom().getRoomNumber(), r.getRoom().roomType(),
                r.getCheckInDate(), r.getCheckOutDate(), breakdown.nights(),
                breakdown.subtotal(), breakdown.discount(), breakdown.surcharge(), breakdown.total(),
                invoice.amountPaid(), invoice.balanceDue());
    }

    @FXML
    public void onRecordPayment() {
        paymentErrorLabel.setText("");
        Reservation reservation = reservationCombo.getValue();
        if (reservation == null) {
            paymentErrorLabel.setText("Please select a reservation first");
            return;
        }
        PaymentMethod method = methodCombo.getValue();
        if (method == null) {
            paymentErrorLabel.setText("Please choose a payment method");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            paymentErrorLabel.setText("Amount must be a valid number");
            return;
        }

        try {
            billingService.recordPayment(reservation, amount, method);
            refreshInvoice();
            amountField.clear();
            Alert done = new Alert(Alert.AlertType.INFORMATION, "Payment of GHS " + String.format("%.2f", amount) + " recorded.");
            done.setHeaderText(null);
            done.showAndWait();
        } catch (ValidationException e) {
            paymentErrorLabel.setText(e.getMessage());
        }
    }
}
