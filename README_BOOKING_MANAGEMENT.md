# Lanka Wings — Booking Management

This is a clean, runnable Java/JSP/SQL Server project for the **Booking Management** functional area, centered on the documented use case **Book Flight on Behalf of Client**.

## What this project does

A Travel Agent can sign in, search upcoming flights, choose a registered passenger/client, select an available seat, enter passenger travel-document details, and create a reservation on that client's behalf. The booking is stored against the **client account** and also linked to the **Travel Agent account**, so the person receiving the journey is distinct from the person performing the booking.

The module also supports booking-management operations: role-scoped listing, editing passenger/seat details, cancellation, concurrency-safe seat checks, and an audit trail. A simple 5% commission amount is stored to demonstrate why the booking keeps the agent association.

## Functional boundaries

Included:
- Travel Agent login and role authorization
- Read-only flight search
- Registered-client selection
- Book flight on behalf of client
- Client ↔ booking ↔ Travel Agent association
- PNR generation
- Seat availability and duplicate-seat protection
- Duplicate passenger-document protection on the same flight
- Booking list/search/filter
- Edit booking details
- Cancel booking
- Booking audit entries
- Passenger read-only ownership view plus allowed booking-management actions
- Admin all-bookings view
- CSRF protection and session checks

Not included:
- user registration or user administration
- flight schedule administration
- payment gateway/card entry/payment processing
- refund processing
- ticket/e-ticket management
- feedback, loyalty or promotions

`PaymentStatus` exists only as an integration/status field. **There is no payment gateway or payment button in this project.** Cancelling a paid booking does not perform a refund; the UI explicitly states that refund processing is handled separately.

## Project structure

```text
src/main/java/com/lankawings/
  config/DBConnection.java
  dao/AccountDAO.java          # authentication + passenger/client lookup only
  dao/FlightDAO.java           # read-only flight dependency
  dao/BookingDAO.java          # owned booking business logic
  filter/SecurityFilter.java
  model/User.java
  model/Flight.java
  model/Booking.java
  servlet/LoginServlet.java
  servlet/LogoutServlet.java
  servlet/DashboardServlet.java
  servlet/FlightServlet.java
  servlet/AgentBookingServlet.java
  servlet/BookingServlet.java
  util/...

src/main/webapp/WEB-INF/views/
  login.jsp
  dashboard.jsp
  flights.jsp
  agent-book.jsp
  bookings.jsp

sql/
  CREATE_SQL_LOGIN_Lankawings.sql
  BookingManagementDB.sql
```

## Demo accounts

After running `BookingManagementDB.sql`:

| Role | Username | Password |
|---|---|---|
| Travel Agent | `agent1` | `Agent@123` |
| Passenger | `passenger1` | `Passenger@123` |
| Admin | `admin` | `Admin@123` |

A second passenger, `passenger2`, is also seeded for the Travel Agent client-selection list.

## Database setup

1. Open SQL Server Management Studio.
2. If the SQL login `Lankawings` does not exist, run:
   `sql/CREATE_SQL_LOGIN_Lankawings.sql`
3. Run:
   `sql/BookingManagementDB.sql`
4. Confirm database `LankaWingsBookingDB` was created.

Default JDBC settings in `DBConnection.java`:

```text
Database: LankaWingsBookingDB
SQL login: Lankawings
Password: Lankawings123
```

You can override these without editing source code:

```text
LW_DB_URL
LW_DB_USER
LW_DB_PASSWORD
```

or Java system properties:

```text
-Dlw.db.url=...
-Dlw.db.user=...
-Dlw.db.password=...
```

## Run with Tomcat 10+

Requirements:
- JDK 17 or newer
- Maven
- Tomcat 10 or newer
- Microsoft SQL Server

Build:

```bash
mvn clean package
```

Deploy the generated WAR from `target/LankaWings-BookingManagement.war` to Tomcat, then open the application URL and sign in using the demo accounts.

## Demonstration flow

1. Sign in as `agent1`.
2. Open **Flights**.
3. Click **Book for Client** on a flight.
4. Choose `passenger1` or `passenger2`.
5. Enter the passenger name and passport/ID.
6. Pick an available seat from the seat map.
7. Click **Create Booking**.
8. Open **Bookings** to see the generated PNR, client, agent, flight, seat and commission record.
9. Edit the passenger/seat details or cancel the booking.
10. Sign in as `passenger1` to see bookings associated with that passenger account.

## Viva / explanation points

- The Travel Agent and passenger are intentionally separate actors.
- `Bookings.UserID` identifies the client/passenger receiving the journey.
- `Bookings.AgentUserID` identifies the Travel Agent who performed the booking.
- The database transaction locks the relevant flight row before creating a booking, reducing double-booking risk.
- Active-seat and active-passenger-document indexes provide database-level protection in addition to server-side validation.
- The Travel Agent can manage only bookings where `AgentUserID` matches the signed-in account; passengers manage only their own client bookings; Admin can see all.
- Cancellation intentionally does **not** execute payment or refund logic because those are separate functional areas.
