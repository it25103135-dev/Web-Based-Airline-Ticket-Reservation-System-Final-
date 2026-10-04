# Lanka Wings Airlines — Flight Management

**Function:** Manage Flight Schedule / Flight Management

This ZIP is intentionally isolated to the Flight Management function described in the design document. It contains the schedule-management workflow and only the minimum shared dependencies needed to run it.

## Implemented workflow
1. Reservation Manager signs in.
2. Open the Flight Management dashboard.
3. Create a new schedule or edit an existing flight.
4. Validate flight number, route, dates/times, fare, aircraft and seat capacity.
5. Check that the same aircraft is not assigned to an overlapping active flight.
6. Save and publish the validated schedule.
7. When a flight is delayed, cancelled or rescheduled, create flight-change notification records for affected active bookings.
8. Prevent unsafe deletion when bookings already exist; use `CANCELLED` status instead.

## Included function code
- `Flight` — flight schedule domain model.
- `FlightDAO` — search, CRUD, booked-seat count, schedule conflict checks, capacity protection and safe delete.
- `AdminFlightServlet` — create/update/delete/status-management controller.
- `FlightServlet` — read-only published schedule search.
- `DashboardServlet` — Flight Management statistics and next departures only.
- `NotificationDAO` — **integration-only** insert of flight-change notifications; no general notification feature is included.
- Flight Management JSPs, common security fragments and flight-only JavaScript validation.

## Shared dependencies kept only because this function needs them
- `DBConnection` — SQL Server access.
- `LoginServlet`, `LogoutServlet`, `UserDAO`, `User`, `SecurityFilter` — authentication/session and `RESERVATION_MANAGER` authorization only. Registration, profile editing, user CRUD and role-management screens are excluded.
- Minimal `Bookings` database rows are used only to calculate booked seats, prevent unsafe delete/capacity reduction, and locate users affected by schedule changes. There is no booking-management servlet or JSP.
- Common CSS and error pages.

## Deliberately excluded
Ticket reservation, booking-management UI, seat selection, payments, refunds, user management, feedback, loyalty/promotions, reports and general notification management.

## Schedule conflict rule
An aircraft conflicts with another non-cancelled flight when:

`existing departure < proposed arrival` **and** `existing arrival > proposed departure`.

Create/update runs the conflict check inside a serializable SQL transaction and uses SQL Server update/hold locks during the check. This prevents two overlapping schedules for the same aircraft from being accepted concurrently by this prototype.

## Setup
1. Run `sql/FlightManagementDB.sql` in SQL Server.
2. If needed, run `sql/CREATE_SQL_LOGIN_Lankawings.sql`.
3. Use Java 17, Maven and Tomcat 10.1+.
4. Build with: `mvn clean package`
5. Deploy: `target/LankaWings-FlightManagement.war`

Default DB settings are `localhost:1433`, database `LankaWingsFlightDB`, user `Lankawings`, password `Lankawings123`. Override them with `LW_DB_URL`, `LW_DB_USER`, `LW_DB_PASSWORD` or JVM properties `lw.db.url`, `lw.db.user`, `lw.db.password`.

## Demo login
- Username: `flightmanager`
- Password: `Lankawings@admin`

## Demo checks
- Create a valid future flight and verify it appears in the published schedule.
- Try creating another flight with the **same aircraft and overlapping time**; it must be rejected as a scheduling conflict.
- Edit total seats below the current booked count of a seeded booked flight; it must be rejected.
- Try deleting `LW101`; it has a dependency booking and must not be physically deleted.
- Change a booked flight to `DELAYED` or `CANCELLED`; a `FLIGHT_UPDATE` notification row should be inserted for the affected passenger.

## Viva scope
Be ready to explain the Reservation Manager actor, create/update/cancel schedule workflow, scheduling-conflict decision, route/date/time/aircraft/capacity validation, booked-seat protection, safe deletion rule, role-based authorization, CSRF protection, parameterized SQL, transaction isolation/locking, and why Bookings/Notifications exist only as minimum integration dependencies rather than separate functions.
