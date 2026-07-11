# OOP Concept Mapping

How each core object-oriented programming concept is expressed in this codebase.

| Concept | Where |
|---|---|
| **Encapsulation** | Private fields with validating getters/setters across `model/Room`, `model/Guest`, `model/Reservation`, `model/Payment` — e.g. `Room.setBaseRate(double)` and `Guest.setEmail(String)` reject invalid values by throwing `ValidationException` instead of allowing bad state |
| **Inheritance** | `Room` (abstract) → `StandardRoom`, `DeluxeRoom`, `SuiteRoom` — each subclass extends the shared fields/behaviour (`roomNumber`, `floor`, `status`, `maxOccupancy`, `baseRate`) and supplies its own pricing/amenity policy |
| **Polymorphism** | `Room.nightlyRate()` and `Room.amenities()` are overridden per subtype and invoked polymorphically wherever a `Room` reference is used (`PricingEngine.computeQuote`, `RoomsController` table rendering); `RoomRepository`/`GuestRepository`/`ReservationRepository`/`PaymentRepository` interfaces are each implemented by a single `Sqlite*Repository` class, letting services depend on the interface rather than the concrete SQLite type |
| **Abstraction** | Abstract `Room` class hides the differences between room categories behind `nightlyRate()`/`amenities()`; the `dao` repository interfaces hide all SQL/JDBC detail from the `service` and `controller` layers — `ReservationService` never sees a `Connection` or `PreparedStatement` |
| **Exception Handling** | Custom checked exceptions `ValidationException` (bad domain data) and `RoomUnavailableException` (booking conflict) force calling code to handle failure explicitly; `DataAccessException` (unchecked) wraps `SQLException` at the JDBC boundary in every `Sqlite*Repository`; a global `Thread.setUncaughtExceptionHandler` in `App.java` catches anything unhandled and shows a JavaFX `Alert` instead of crashing |
| **Collections** | `List<Room>`/`List<Reservation>`/`List<Payment>` returned by every repository; `ObservableList` (via `FXCollections`) backs every `TableView`/`ComboBox` in the controllers; `ReservationService.occupancyByStatus()` uses `Collectors.groupingBy(Room::getStatus, Collectors.counting())` to build a `Map<RoomStatus, Long>` for the dashboard |
| **Event-Driven GUI** | `onAction` handlers on every `Button` (`#onBookReservation`, `#onRecordPayment`, `#onLogin`, ...); `ChangeListener`s on `ComboBox`/`DatePicker` value properties in `ReservationsController` recompute room availability and the live price quote as the front-desk clerk changes dates; `MainController` swaps the visible screen via a `StackPane` and highlights the active nav button |

## Where each requirement lives

- **Room type hierarchy**: `src/main/java/com/hotelreserve/model/Room.java`, `StandardRoom.java`, `DeluxeRoom.java`, `SuiteRoom.java`
- **Custom exceptions**: `src/main/java/com/hotelreserve/exception/`
- **Persistence (SQLite via JDBC)**: `src/main/java/com/hotelreserve/dao/`
- **Business logic**: `src/main/java/com/hotelreserve/service/PricingEngine.java`, `ReservationService.java`, `BillingService.java`
- **GUI**: `src/main/java/com/hotelreserve/controller/`, `src/main/resources/com/hotelreserve/fxml/`, `src/main/resources/com/hotelreserve/css/styles.css`
- **Tests**: `src/test/java/com/hotelreserve/model/`, `service/`, `dao/` (in-memory fake repositories, no real SQLite in unit tests)
