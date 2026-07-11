package com.hotelreserve.controller;

import com.hotelreserve.dao.GuestRepository;
import com.hotelreserve.dao.ReservationRepository;
import com.hotelreserve.dao.RoomRepository;
import com.hotelreserve.dao.SqliteGuestRepository;
import com.hotelreserve.dao.SqliteReservationRepository;
import com.hotelreserve.dao.SqliteRoomRepository;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.service.ReservationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class DashboardController {

    @FXML private FlowPane summaryCardsContainer;
    @FXML private ListView<String> checkInsList;
    @FXML private ListView<String> checkOutsList;

    private final RoomRepository roomRepository = new SqliteRoomRepository();
    private final GuestRepository guestRepository = new SqliteGuestRepository();
    private final ReservationRepository reservationRepository =
            new SqliteReservationRepository(guestRepository, roomRepository);
    private final ReservationService reservationService = new ReservationService(reservationRepository, roomRepository);

    @FXML
    public void initialize() {
        renderOccupancyCards();
        renderTodayLists();
    }

    private void renderOccupancyCards() {
        Map<RoomStatus, Long> occupancy = reservationService.occupancyByStatus();
        summaryCardsContainer.getChildren().clear();
        for (RoomStatus status : RoomStatus.values()) {
            long count = occupancy.getOrDefault(status, 0L);
            summaryCardsContainer.getChildren().add(buildCard(status.name(), String.valueOf(count)));
        }
    }

    private VBox buildCard(String label, String value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(160);
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("card-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("card-value");
        card.getChildren().addAll(labelNode, valueNode);
        return card;
    }

    private void renderTodayLists() {
        var checkIns = reservationService.todaysCheckIns().stream().map(this::describe).toList();
        var checkOuts = reservationService.todaysCheckOuts().stream().map(this::describe).toList();
        checkInsList.setItems(FXCollections.observableArrayList(checkIns.isEmpty() ? java.util.List.of("No check-ins today") : checkIns));
        checkOutsList.setItems(FXCollections.observableArrayList(checkOuts.isEmpty() ? java.util.List.of("No check-outs today") : checkOuts));
    }

    private String describe(Reservation reservation) {
        return reservation.getGuest().getFullName() + " — Room " + reservation.getRoom().getRoomNumber()
                + " (" + reservation.getStatus() + ")";
    }
}
