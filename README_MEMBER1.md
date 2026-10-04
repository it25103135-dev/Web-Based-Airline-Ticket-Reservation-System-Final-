# Lanka Wings Airlines — Member 1: Payment Management

**Assigned member:** Athapaththu A.M.S.T (IT25103039)  
**Assigned function:** Online Payment / Payment Management

This ZIP is intentionally isolated to Member 1's function. It contains:
- secure online card-payment simulation;
- payment input validation (cardholder, card brand/Luhn, expiry, CVV);
- successful/failed transaction recording;
- booking payment-status update after successful payment;
- payment transaction history;
- generated transaction reference/receipt confirmation;
- administrator payment-history deletion already present in the original payment module.

## What is shared only because Payment Management needs it
- `DBConnection` — database access.
- `LoginServlet`, `LogoutServlet`, `UserDAO`, `User` and `SecurityFilter` — minimal authentication/session infrastructure only; there is no registration, profile editing, role management or user administration.
- `BookingDAO` and `Booking` — read-only lookup of an existing booking and a list of unpaid bookings so checkout can start. There is no booking creation, editing, cancellation or reservation management.
- common JSP fragments, CSS/JS and error pages.

## Deliberately removed
Flight management, reservation creation, booking management, ticket management/e-ticket CRUD, refunds, feedback, notifications, user-management CRUD, admin flight screens and unrelated dashboards.

> Payment success updates `Bookings.PaymentStatus` to `PAID` and `BookingStatus` to `CONFIRMED` because that is the integration result of payment. Ticket generation is not included; it belongs to the separate ticket/reservation member module.

## Setup
1. SQL Server: run `sql/PaymentManagementDB.sql`.
2. Run `sql/CREATE_SQL_LOGIN_Lankawings.sql` if the `Lankawings` SQL login does not already exist.
3. Java 17 + Maven + Tomcat 10.1+.
4. Build: `mvn clean package`.
5. Deploy `target/LankaWings-PaymentManagement.war` to Tomcat.

Database defaults can be overridden with `LW_DB_URL`, `LW_DB_USER`, `LW_DB_PASSWORD` or JVM properties `lw.db.url`, `lw.db.user`, `lw.db.password`.

## Demo logins
- Passenger: `passenger` / `Passenger123`
- Admin: `admin@lankawings.com` / `Lankawings@admin`

The passenger has one seeded unpaid booking (`LW-PAY-001`) and one paid booking (`LW-PAY-002`).

## Demo cards
- Visa approved: `4242 4242 4242 4242`
- Mastercard approved: `5555 5555 5555 4444`
- Demo decline: `4000 0000 0000 0002`
- Use any valid future `MM/YY` and any 3-digit CVV.

## Member 1 viva scope
Be ready to explain payment ownership checks, why the amount is read from the database instead of the browser, card validation, Luhn checking, why only last-four digits are persisted, failed-payment auditing, transaction references, database transaction/rollback, CSRF, parameterized SQL and how successful payment updates booking payment status.
