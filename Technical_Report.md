# Technical Report

## 1. Title Page

**Hotel Reservation Management System**
A JavaFX Desktop Application for Front-Desk Room, Guest, Reservation, and Billing Management

Course: INF811D — Object-Oriented Programming
Programme: MSc Information Technology
Institution: University of Cape Coast, College of Distance Education
Author: Senior Mike
Date: July 2026

---

## 2. Introduction

I built this project to model something I could actually picture: a small hotel's front desk,
run by one clerk with one login, needing to keep track of which rooms are free, who is staying
in them, and who still owes money. Rather than picking an abstract "CRUD app" exercise, I
wanted the class hierarchy and the business rules to fall out of a real domain, so that the
OOP concepts INF811D asks for (encapsulation, inheritance, polymorphism, abstraction,
exception handling, collections, event-driven GUI) would have an obvious reason to exist
rather than being bolted on to satisfy a rubric line item.

The system is a Java 21 / JavaFX 21 desktop application backed by an embedded SQLite
database, built and run with Maven. It covers the full front-desk workflow: managing a
physical room inventory across three room categories, registering guests, booking
reservations against real availability, walking a reservation through its lifecycle
(check-in, check-out, cancellation), and billing guests with a pricing model that rewards
longer stays and charges more for weekend nights.

## 3. Problem Statement

Small hotels that don't run a commercial property-management system typically track rooms
and bookings on paper or in a spreadsheet. Both approaches make it easy to double-book a
room, lose track of which reservations are still active, and get pricing wrong when a stay
crosses a weekend or qualifies for a length-of-stay discount. The system needed to solve
three concrete problems: (1) prevent a room from being booked twice for overlapping dates,
(2) enforce that a reservation can only move through valid states (you cannot check out a
guest who never checked in), and (3) compute a stay's price consistently, including
discounts and surcharges, rather than leaving that arithmetic to a human every time.

## 4. Objectives of the System

- Model three distinct room categories (Standard, Deluxe, Suite) with different amenity sets and a shared pricing/validation contract, using inheritance and polymorphism rather than a single flat `Room` table with an `if/else` on type.
- Persist rooms, guests, reservations, and payments in a local SQLite database without requiring a database server.
- Prevent overlapping bookings for the same room through an explicit availability check backed by a real SQL date-range query.
- Enforce reservation state transitions (PENDING/CONFIRMED → CHECKED_IN → CHECKED_OUT, or → CANCELLED) so an invalid transition is rejected with a clear message instead of silently corrupting data.
- Compute an itemized price quote for any stay, combining a length-of-stay discount with a weekend-night surcharge, and reuse that same calculation for both the live "quote as you book" preview and the final invoice.
- Provide a usable JavaFX GUI for a non-technical front-desk user: a login gate, a sidebar-navigated shell, and five task-focused screens (Dashboard, Rooms, Guests, Reservations, Billing).
- Cover the domain and service layers with JUnit 5 tests that run without a real database, using in-memory fake repositories.

## 5. Scope of the System

In scope: room inventory management, guest registration, reservation booking with
availability checking, the check-in/check-out/cancel lifecycle, pricing computation, invoice
generation, and payment recording. A single hardcoded front-desk login gates access — there
is no multi-user account system, no role-based permissions, and no online guest-facing
booking portal. Reporting is limited to the dashboard's occupancy snapshot and today's
check-in/check-out lists; there is no historical revenue reporting or analytics dashboard.
Payment recording tracks amounts and methods but does not integrate with any real payment
gateway — this is a front-desk record-keeping tool, not a payment processor.

## 6. Methodology

I followed the same layered architecture I used on a sibling coursework project (SpendWise,
an expense tracker for the same module), because it had already proven itself: `model` for
domain objects that validate themselves, `exception` for the checked/unchecked exception
types those models throw, `dao` for repository interfaces plus their SQLite implementations,
`service` for business logic that depends only on the repository interfaces (never on SQLite
directly), and `controller` + FXML for the JavaFX presentation layer. I built the models and
their validation rules first, wrote model-level JUnit tests against them, then the DAO layer
and schema, then the two services (`PricingEngine` and `ReservationService`, later
`BillingService`), with service-level tests running against hand-written in-memory fake
repositories so the test suite never touches a real database file. Only once the domain and
service layers were verified did I wire up the FXML screens and controllers on top.

## 7. System Design

The domain model centers on `Room` as an abstract class with three concrete subtypes —
`StandardRoom`, `DeluxeRoom`, `SuiteRoom` — each overriding `nightlyRate()` and
`amenities()`. `Guest`, `Reservation`, and `Payment` are standalone validated classes;
`Reservation` holds references to a `Guest` and a `Room` and computes its own `nights()`.
Four enums (`RoomStatus`, `ReservationStatus`, `PaymentMethod`, `PaymentStatus`) constrain
state to a known set of values, matched exactly by `CHECK` constraints in the SQLite schema.

The persistence layer defines four repository interfaces (`RoomRepository`,
`GuestRepository`, `ReservationRepository`, `PaymentRepository`) so the service layer never
imports `java.sql.*`. `SqliteReservationRepository` reconstructs a `Reservation` by looking
up its `Guest` and `Room` through the other two repositories, and
`SqliteRoomRepository.build()` is the one place in the codebase with a switch-on-string,
turning the stored `room_type` column back into the correct `Room` subtype.

The service layer has three classes: `PricingEngine` (stateless — a pure function from room
and dates to a `PriceBreakdown` record), `ReservationService` (owns the booking/cancel/
check-in/check-out lifecycle and keeps `Room.status` in sync with `Reservation.status`), and
`BillingService` (turns a `PricingEngine` quote plus recorded payments into an `Invoice`
record). The GUI layer is a `BorderPane` shell (`MainController`) with a sidebar and a
`StackPane` content area that swaps in one of five FXML screens loaded via `FXMLLoader`.

## 8. Description of Classes and Methods

- **`Room` (abstract, `model/Room.java`)** — private fields `id`, `roomNumber`, `floor`, `status`, `maxOccupancy`, `baseRate`; validating setters that throw `ValidationException`; abstract `nightlyRate()`, `amenities()`, `roomType()`.
- **`StandardRoom` / `DeluxeRoom` / `SuiteRoom`** — concrete `Room` subtypes differing only in their amenity list and `roomType()` discriminator string.
- **`Guest`** — `fullName`, `email` (regex-validated), `phone`, `idNumber`, all required and validated in the constructor and every setter.
- **`Reservation`** — holds `Guest`, `Room`, `checkInDate`, `checkOutDate`, `numberOfGuests`, `status`, `createdAt`; validates that check-out is strictly after check-in and that `numberOfGuests` is between 1 and `room.getMaxOccupancy()`; `nights()` computes `ChronoUnit.DAYS.between(...)`.
- **`Payment`** — `reservation`, `amount` (must be positive), `method`, `status`, `paymentDate`.
- **`DatabaseManager`** — static `getConnection()` / `initSchema()`; creates all four tables with `CREATE TABLE IF NOT EXISTS` and seeds 8 sample rooms (idempotent — checks the row count first).
- **`Sqlite*Repository` classes** — each implements its interface with `PreparedStatement`/`Statement.RETURN_GENERATED_KEYS` for inserts, a private `query(sql, StatementBinder)` helper, and a private `mapRow(ResultSet)`.
- **`PricingEngine.computeQuote(Room, LocalDate, LocalDate)`** — returns a `PriceBreakdown(subtotal, discount, surcharge, total, nights)` record; walks each night of the stay with a `while` loop checking `DayOfWeek` for the surcharge, and applies a discount tier based on total nights.
- **`ReservationService`** — `checkAvailability`, `bookReservation` (throws `RoomUnavailableException` on conflict), `cancelReservation`, `checkIn`, `checkOut` (each validates the current `ReservationStatus` before transitioning), `occupancyByStatus()` (returns `Map<RoomStatus, Long>` via `Collectors.groupingBy`).
- **`BillingService`** — `generateInvoice(Reservation)`, `recordPayment(Reservation, double, PaymentMethod)`.
- **Controllers** — `LoginController`, `MainController`, `DashboardController`, `RoomsController` + `RoomFormController`, `GuestsController` + `GuestFormController`, `ReservationsController`, `BillingController` — each wires FXML `@FXML` fields to repository/service calls and translates checked exceptions into on-screen error labels or `Alert` dialogs.

## 9. GUI Design Explanation

The shell mirrors a typical admin console: a dark navy sidebar with gold accents on the left
(`.sidebar`, `.nav-button`, `.nav-button-active`) and a light content area on the right
(`.content-area`, `.screen`). I reused the structural CSS class names from my SpendWise
project (`.card`, `.card-label`, `.card-value`, `.primary-button`, `.secondary-button`,
`.form-error`) so the two projects' FXML stay consistent, just with a warmer, more
"hospitality" palette instead of SpendWise's teal. Every screen follows the same layout
pattern: a title label, a filter/action bar in a `.card`, then either a `TableView` or a
form. Add/edit operations for rooms and guests open as a `Dialog` built from a separate FXML
form fragment rather than a full-screen navigation, which keeps the front-desk clerk's place
in the underlying table. The Reservations screen is the most interactive: choosing a
different date range or room live-recomputes both the available-rooms list and the price
quote label, using `ChangeListener`s on the `DatePicker`/`ComboBox` value properties.

## 10. OOP Concepts Implemented

See `docs/OOP_MAPPING.md` for the full table with file references. In summary:
**Encapsulation** (private fields, validating setters throwing `ValidationException` across
every model class), **Inheritance** (`Room` → `StandardRoom`/`DeluxeRoom`/`SuiteRoom`),
**Polymorphism** (`nightlyRate()`/`amenities()` overridden per subtype; repository
interfaces each implemented by one SQLite class), **Abstraction** (abstract `Room`; `dao`
interfaces hiding JDBC from the rest of the app), **Exception Handling** (checked
`ValidationException`/`RoomUnavailableException`, unchecked `DataAccessException`, a global
`Thread.setUncaughtExceptionHandler` in `App.java`), **Collections** (`ObservableList` for
every table/combo, `Map<RoomStatus, Long>` for occupancy), and **Event-Driven GUI** (`onAction`
handlers and property-change listeners throughout the controller package).

## 11. Screenshots and Outputs

**Login** — front-desk sign-in screen (`screenshots/login.png`)

**Dashboard** — room occupancy summary (8 available, 0 reserved/occupied/maintenance) and
today's check-in/check-out lists (`screenshots/dashboard.png`)

**Rooms** — the seeded room inventory across all three types (Standard, Deluxe, Suite) with
status/type filters and per-room Edit/Toggle Maintenance actions (`screenshots/rooms.png`)

**Guests** — guest table with name search and Add Guest action (`screenshots/guests.png`)

**Reservations** — the booking form (guest, room, check-in/check-out dates, guest count,
live price quote) above the reservations table with check-in/check-out/cancel actions
(`screenshots/reservations.png`)

**Billing** — reservation selector, generated itemized invoice panel, and the payment
recording form (method + amount) (`screenshots/billing.png`)

These were captured by running `mvn javafx:run` locally on a Windows machine with a display,
signing in with the front-desk credentials, and walking through each screen.

## 12. GitHub Repository Link

https://github.com/myciscomap-prog/hotel-reservation-management-system

## 13. Challenges Encountered

The trickiest part of this build was the overlap-date query behind `checkAvailability`. My
first instinct was to check `check_in_date BETWEEN ? AND ?`, but that only catches
reservations that *start* inside the requested range — it misses the case where an existing
reservation fully contains the new range (e.g. someone already booked Aug 1–20 and a new
guest requests Aug 5–10). I had to switch to the standard interval-overlap condition,
`existing.check_in < new.check_out AND existing.check_out > new.check_in`, and wrote the
`ReservationServiceTest.bookingOverlappingDatesThrowsRoomUnavailable` /
`bookingNonOverlappingDatesAfterFirstStaySucceeds` tests specifically to pin that logic down
after a first draft let a contained-overlap booking through.

A smaller but real design decision was whether the length-of-stay discount and the weekend
surcharge should stack, and in what order. I chose to compute both against the same
`subtotal` (rather than applying the surcharge to an already-discounted rate) because that's
how a hotel's actual folio would read: room charge, then a length-of-stay discount line, then
a separate weekend-night line item, summed to a total. Writing `PricingEngineTest` against
concrete calendar dates (anchored to a specific January so I could hand-verify which days
were Friday/Saturday) caught an off-by-one in my first version of the weekend-counting loop,
which iterated `checkIn` to `checkOut` inclusive instead of exclusive on the end date and
overcounted the last night by one.

I also chose SQLite over a heavier database deliberately: this is a single-user, single-
machine front-desk tool, and a server-based database would add operational overhead (a
process to keep running, connection configuration) with no corresponding benefit for a
one-clerk hotel desk. The embedded file-based approach also makes the whole project trivially
runnable by a grader with just `mvn javafx:run` and nothing else installed.

## 14. Conclusion

The finished system meets the objectives I set out with: it prevents double-booking through
a real interval-overlap query rather than a naive date check, it enforces the reservation
state machine so a checked-out reservation cannot be checked out again, and it computes
pricing consistently by having both the live booking-time quote and the final invoice call
the exact same `PricingEngine.computeQuote` method rather than duplicating the arithmetic in
two places. Structuring the codebase around repository interfaces rather than concrete
SQLite classes also paid off directly during testing — every service test runs against an
in-memory fake and never touches the actual `hotelreserve.db` file, which is what let me get
to 34 passing JUnit tests without any test-database setup or teardown machinery.

## 15. Recommendations

If I were to extend this beyond the coursework scope, the first thing I'd add is proper
multi-user accounts with role-based access (manager vs. front-desk clerk) instead of the
single hardcoded login — that was a deliberate scope cut, not an oversight, since account
management wasn't part of the assignment brief. Second, the dashboard could be extended with
actual revenue reporting (by date range, by room type) now that `PaymentRepository` already
has everything needed to compute it. Third, the overlap-detection query works well at the
scale of one hotel's room count, but if this were ever adapted for a multi-property chain,
I'd want to add a database index on `reservations(room_id, check_in_date, check_out_date)`
rather than relying on a full-table scan.

## 16. References

- Oracle. *Java SE 21 Documentation.* https://docs.oracle.com/en/java/javase/21/
- OpenJFX. *JavaFX 21 Documentation.* https://openjfx.io/
- SQLite. *SQLite Documentation.* https://www.sqlite.org/docs.html
- Xerial. *sqlite-jdbc — SQLite JDBC Driver.* https://github.com/xerial/sqlite-jdbc
- JUnit Team. *JUnit 5 User Guide.* https://junit.org/junit5/docs/current/user-guide/
- Apache Maven. *Maven Documentation.* https://maven.apache.org/guides/
- University of Cape Coast, College of Distance Education. *INF811D: Object-Oriented Programming — Course Project Brief and Marking Rubric.* MSc Information Technology programme, 2026.

---

### Rubric Self-Assessment (informal, mapped to the stated 100-point marking scheme)

| Criterion | Marks | Where addressed |
|---|---|---|
| Functionality | 20 | Full booking/check-in/check-out/cancel lifecycle, availability checking, pricing, invoicing, payment recording — all covered by passing tests |
| GUI Design/UX | 15 | Consistent sidebar-shell layout, filter bars, dialogs for add/edit, live quote feedback |
| OOP Concepts | 20 | See Section 10 and `docs/OOP_MAPPING.md` |
| Operators/Control Structures | 10 | `PricingEngine`'s discount tier `if/else` and weekend `while` loop; state-transition guards in `ReservationService` |
| Methods/Modularization | 10 | Layered `model`/`exception`/`dao`/`service`/`controller` packages, no God classes |
| Event Handling/GUI Interaction | 10 | `onAction` handlers throughout; live listeners on Reservations screen |
| Exception Handling/Validation | 5 | Checked `ValidationException`/`RoomUnavailableException`, unchecked `DataAccessException`, global handler |
| GitHub Deployment/Documentation | 5 | README, this report, `docs/OOP_MAPPING.md` (repository link pending upload) |
| Technical Report | 5 | This document |
