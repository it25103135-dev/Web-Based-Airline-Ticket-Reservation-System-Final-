# Lanka Wings Airlines — Ticket Reservation

**Function:** Book Ticket / Ticket Reservation

This package contains the Ticket Reservation function with a simple passenger flow and a dedicated ticket-administration panel.

## Passenger workflow
1. Sign in as a passenger.
2. Search or browse future flights.
3. Select a flight and an available seat.
4. Enter passenger name and passport/ID.
5. The system validates the flight, seat and duplicate-passenger rules and creates a unique PNR.
6. Open **My Reservations**.
7. For an unpaid reservation, click **Mark Paid & View Ticket** once.
8. The system changes the reservation to `CONFIRMED / PAID`, creates the e-ticket and immediately opens it.
9. The passenger can view, print or save the ticket.

There is no card-entry screen or transaction-processing module in this package. `PaymentStatus` is only used as a simple PAID/UNPAID reservation flag because the existing database model already contains that field.

## Admin panel
The ADMIN account is redirected to `/admin/tickets`.

Admin can:
- View all issued tickets.
- Search ticket records.
- Edit the passenger name, passport/ID and seat printed on an issued ticket.
- The linked reservation is updated in the same transaction so the ticket and reservation stay consistent.
- Delete an issued ticket document.
- Deleting a ticket does **not** delete the reservation.

## Validation and safety
- Seat-layout validation.
- Active-seat uniqueness per flight.
- Active passenger/passport uniqueness per flight.
- Future-flight and non-cancelled-flight checks before marking paid/issuing a ticket.
- Admin-only protection for `/admin/*`.
- CSRF checks for POST actions.
- Parameterized SQL.
- Login rate limiting and password hashing.

## Included core code
- Models: `Flight`, `Booking`, `Ticket`, `User`.
- DAOs: `FlightDAO`, `BookingDAO`, `TicketDAO`, minimal `UserDAO`.
- Passenger servlets: flight search, reservation creation, reservations list, mark-paid action, ticket view, dashboard.
- Admin servlet: `AdminTicketServlet`.
- Passenger pages plus `admin-tickets.jsp`.
- Dedicated SQL Server database script with both passenger and admin demo accounts.

## Deliberately excluded
Refunds, general booking cancellation, flight administration, account administration, feedback, loyalty/promotions, reporting and notification management are not part of this isolated function.

## Setup
1. Run `sql/TicketReservationDB.sql` in SQL Server.
2. If needed, run `sql/CREATE_SQL_LOGIN_Lankawings.sql`.
3. Use Java 17, Maven and Tomcat 10.1+.
4. Build with `mvn clean package`.
5. Deploy `target/LankaWings-TicketReservation.war`.

Default DB settings: database `LankaWingsTicketReservationDB`, user `Lankawings`, password `Lankawings123`. Override with `LW_DB_URL`, `LW_DB_USER`, `LW_DB_PASSWORD` or JVM properties `lw.db.url`, `lw.db.user`, `lw.db.password`.

## Demo accounts
Passenger:
- Username: `passenger`
- Password: `Passenger123`

Administrator:
- Username: `admin`
- Password: `Lankawings@admin`

## Demo checks
- Log in as `passenger`.
- `LW-DEMO-PEND` starts as `PENDING / UNPAID`; click **Mark Paid & View Ticket** and confirm it immediately becomes viewable as an e-ticket.
- Create another reservation and test the same one-click flow.
- Try to reserve the same active seat twice; it must be rejected.
- Log in as `admin` and open the Ticket Admin panel.
- Edit the seeded ticket's passenger/seat data and save it.
- Delete a ticket; confirm the ticket disappears while its reservation remains.
- Log back in as the passenger; if a paid reservation has no ticket, **Create & View Ticket** recreates it.

## Viva scope
Be ready to explain flight discovery, seat availability, validation, duplicate-seat protection, PNR generation, the one-click PAID state change, e-ticket creation, admin edit/delete behavior, transaction boundaries, authorization, CSRF protection and parameterized SQL.
