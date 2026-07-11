# Hotel Reservation Management System

A JavaFX desktop application for a small hotel's front desk: manage rooms across three
categories, register guests, book and track reservations through their full lifecycle
(pending → confirmed → checked-in → checked-out / cancelled), and generate itemized
invoices with length-of-stay discounts and weekend surcharges baked into the pricing.

Built for **INF811D: Object-Oriented Programming** (MSc Information Technology, University
of Cape Coast, College of Distance Education).

## Login Details

The app opens on a front-desk sign-in screen. Credentials are hardcoded for this coursework
build (no user-management system was in scope):

- **Username:** `frontdesk`
- **PIN:** `1234`

## Features

- **Rooms** — Standard/Deluxe/Suite room hierarchy with per-type amenities and nightly rate, filterable table, add/edit dialog, one-click maintenance toggle
- **Guests** — add, edit, and search guests by name
- **Reservations** — pick a guest and date range, see only rooms actually available for that range, a live price quote before you commit, then book/check-in/check-out/cancel from the reservations table
- **Pricing Engine** — length-of-stay discount (5% at 3+ nights, 10% at 7+ nights) stacked with a 15%-per-night weekend surcharge computed by walking each night of the stay
- **Billing** — generate an itemized invoice for any reservation and record payments (cash, card, mobile money, bank transfer) against it, with balance-due tracking
- **Dashboard** — room occupancy counts by status, today's check-ins and check-outs

## Tech Stack

- Java 21, JavaFX 21 (`javafx-maven-plugin`)
- SQLite via JDBC (`org.xerial:sqlite-jdbc`) — embedded, no server required
- FXML + Controller pattern, JUnit 5 for the domain/service test suite

## Object-Oriented Design

| Concept | Where |
|---|---|
| **Encapsulation** | Private fields with validating getters/setters in `Room`, `Guest`, `Reservation`, `Payment` |
| **Inheritance** | `Room` (abstract) → `StandardRoom`, `DeluxeRoom`, `SuiteRoom` |
| **Polymorphism** | `Room.nightlyRate()`/`amenities()` overridden per subtype; each `dao` repository interface implemented by a single SQLite class |
| **Abstraction** | Abstract `Room` class; repository interfaces (`dao` package) hide SQLite/JDBC details from services and controllers |
| **Exception Handling** | Checked `ValidationException` and `RoomUnavailableException`, unchecked `DataAccessException`, a global uncaught-exception handler in `App` |
| **Collections** | `ObservableList` backing every `TableView`/`ComboBox`; `Map<RoomStatus, Long>` occupancy summary via `Collectors.groupingBy` |
| **Event-Driven GUI** | Button `onAction` handlers, live availability/quote recalculation on date/room selection changes |

See `docs/OOP_MAPPING.md` for the full breakdown with file references.

## Project Structure

```
src/main/java/com/hotelreserve/
├── App.java               # JavaFX entry point
├── model/                  # Room hierarchy, Guest, Reservation, Payment, enums
├── exception/               # ValidationException, DataAccessException, RoomUnavailableException
├── dao/                    # Repository interfaces + SQLite implementations
├── service/                # PricingEngine, ReservationService, BillingService
└── controller/              # JavaFX FXML controllers (Login, Main, Dashboard, Rooms, Guests, Reservations, Billing)
src/main/resources/com/hotelreserve/{fxml,css}/
src/test/java/com/hotelreserve/    # JUnit 5 tests (model + service layers, in-memory fake repositories)
docs/OOP_MAPPING.md         # OOP concept → code mapping
Technical_Report.md         # Full project report
```

## Running It

**Requirements:** JDK 21+, Maven 3.9+ (or use your IDE's bundled tooling — IntelliJ IDEA
Community detects the Maven project automatically).

```bash
mvn javafx:run
```

or double-click `run.bat` on Windows.

The first launch creates a local `hotelreserve.db` SQLite file (ignored by git) and seeds a
starter set of 8 rooms spanning all three room types.

To run the test suite:

```bash
mvn test
```

or double-click `test.bat`.

> **Note:** if you hit a `PKIX path building failed` / certificate error running Maven on
> Windows, it's usually antivirus/TLS-inspection software your JDK doesn't trust by default.
> Add this to your environment and retry: `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`

## Screenshots

Screenshots will be added here after a manual run of the application (this build was done
in a headless environment without a display).

| Login | Dashboard |
|---|---|
| _to be added_ | _to be added_ |

| Rooms | Reservations |
|---|---|
| _to be added_ | _to be added_ |

| Billing | Guests |
|---|---|
| _to be added_ | _to be added_ |

## GitHub Upload Commands

```bash
git init
git add .
git commit -m "Initial commit: Hotel Reservation Management System"
git remote add origin https://github.com/myciscomap-prog/hotel-reservation-management-system.git
git branch -M main
git push -u origin main
```

Repository: https://github.com/myciscomap-prog/hotel-reservation-management-system

## Author

Senior Mike — MSc Information Technology, University of Cape Coast
