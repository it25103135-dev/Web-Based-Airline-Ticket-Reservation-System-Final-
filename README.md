# Lanka Wings Airline Reservation — Electric & Lemonade edition

JSP + Servlet + JDBC on SQL Server, Tomcat 10, IntelliJ IDEA.

## Setup (IntelliJ + Tomcat)
1. **New database:** run `sql/LankaWingsDB.sql`, then `sql/CREATE_SQL_LOGIN_Lankawings.sql` in SSMS.
   **Existing database (keep your data):** run `sql/UPGRADE_SECURITY.sql` once instead.
2. Enable TCP/IP on port 1433 in SQL Server Configuration Manager.
3. Open the folder in IntelliJ (Maven project), run with your Tomcat 10 configuration, open `http://localhost:8080/LankaWings/`.
4. Different DB password or server? No code edit needed. Set env vars `LW_DB_URL`, `LW_DB_USER`, `LW_DB_PASSWORD`
   or JVM options `-Dlw.db.url=... -Dlw.db.user=... -Dlw.db.password=...`.

## Demo accounts
| Role | Username | Password |
|---|---|---|
| Admin | `admin@lankawings.com` | `Lankawings@admin` |
| Passenger | `passenger` | `Passenger123` |

Demo payment cards: `4242 4242 4242 4242` (Visa, approved), `5555 5555 5555 4444` (Mastercard, approved),
`4000 0000 0000 0002` (declined). Any future expiry and any 3-digit CVV.

## Modules (all with full CRUD)
| Module | Create | Read | Update | Delete |
|---|---|---|---|---|
| Users | Register | Profile | Edit profile, change password | Close (deactivate) account |
| Admin users | | User list + search | Role, active/inactive | Deactivate |
| Notifications | System events, admin announcements | Inbox + live bell | Mark read / all read | Delete / clear read |
| Flights (admin) | Add flight | List, search, passenger search | Edit (passengers notified of delay/cancel/reschedule) | Delete (blocked if booked) |
| Reservation | Seat map + passenger details | Bookings list + search | Edit name/passport/seat | Cancel (auto-refund if paid) |
| Payment | Card checkout | History | | Refund on cancel |
| Tickets | Auto-issued on payment; manually create/recreate for eligible paid bookings | Ticket list, printable e-ticket | Passenger name, passport/ID, seat (synced with booking) | Delete ticket document; booking cancellation remains separate |
| Feedback | Star rating + review | Reviews + rating summary | Edit own (until answered), admin response | Delete own / admin any |

## Validation (browser AND server; the server is the one that counts)
* **Mobile:** exactly 10 digits, `070`-`078` (Sri Lanka). `+94` / `94` prefixes are converted. Too short / too long / letters are rejected.
* **Card:** exactly 16 digits, Luhn checksum, must match chosen Visa / Mastercard, expiry `MM/YY` not expired, CVV exactly 3 digits, holder name letters only. Only the last 4 digits are stored.
* **Password:** 8-64 chars with upper, lower, number, special; no spaces; not common; not containing the username. Live strength meter.
* Names (Sinhala/Tamil/English letters), username, email, passport/ID, flight number `LW101`, seat `12A` (must exist on the aircraft), fares, dates (arrival after departure, 20 min - 24 h), seat counts, review length, and more.

## Security
* PBKDF2-HMAC-SHA256 (600k iterations, per-user salt); old hashes upgrade automatically at next login.
* CSRF token on every POST; logout is POST.
* Login lockout (5 wrong attempts / 15 min) + per-IP limit; card-attempt and password-check limits; sign-up rate limit + honeypot.
* New session id at login (no fixation), HttpOnly cookie, 30 min timeout, no session id in URLs.
* Content-Security-Policy (no inline scripts), clickjacking, MIME-sniffing and referrer headers, no-cache on private pages.
* Every JSP lives under `WEB-INF/views` (cannot be opened directly); admin URLs are role-checked in a filter **and** the servlet.
* Deactivated users / demoted admins lose access on their next click (account re-checked each request).
* Ownership checks on bookings, tickets, payments, feedback; amounts always come from the database.
* Database errors are logged, never shown to users. All output is HTML-escaped. SQL is parameterised.
* Least-privilege DB login (read/write only, no schema changes).

## Before going live
Serve over HTTPS and enable `<secure>true</secure>` in `web.xml`; change the DB password; rotate the demo passwords.

## Electric & Lemonade update
See `EDIT_DETAILS.md` for the complete change guide, file list, validation results and acceptance checklist.

- Brand colors: Electric `#09080E` and Lemonade `#FE6807`.
- Ticket management includes Create, Read, Update and Delete for passengers' own tickets and administrators' tickets across users.
- Successful payment redirects to an animated Booking confirmed page. Tickets are opened through Ticket management.
- Admin feedback has an expanded reply composer and redesigned response cards.
- Use **JDK 17**, Maven and **Tomcat 10.1**. Run `mvn clean package`; deploy `target/LankaWings.war`.
- This edit requires no new database migration. Existing installations keep their current upgraded schema and data.
- Run `mvn test` on JDK 17 for the 21 regression tests; these tests mock database access.
