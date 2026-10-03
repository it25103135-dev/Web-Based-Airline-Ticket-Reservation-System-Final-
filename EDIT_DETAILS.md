# Lanka Wings — complete edit details

## Delivered files

- `LankaWingsAirlineReservation_Electric_Lemonade.zip`: full Maven source project, original SQL/setup resources, new tests and this guide.
- `LankaWings.war`: compiled application for Tomcat 10.1.
- `EDIT_DETAILS.md`: standalone copy of this guide.

## 1. Ticket management: full CRUD

**Create**

The Tickets page now includes a Create a ticket section. Its booking selector lists only eligible bookings with no existing ticket. A booking must be paid, confirmed, not cancelled, and on a flight that has not departed or been cancelled. Automatic ticket issuance after successful payment remains in place; manual creation also allows a deleted ticket to be recreated before departure. A booking cannot receive duplicate tickets.

**Read**

The searchable list still shows ticket number, booking reference (PNR), passenger, flight, seat, status and issue date. View / print opens the printable ticket from this page.

**Update**

Each editable issued ticket has an Edit ticket panel with passenger name, passport/ID and seat. Saving updates the linked booking and ticket update timestamp in the same transaction. This keeps the booking record and printable ticket consistent. Server validation checks the name, passport and seat format, aircraft seat layout, duplicate passenger identification and occupied seats. Cancelled and departed bookings cannot be edited. The ticket must still exist and be issued when the update runs.

**Delete**

Delete ticket asks for confirmation, then permanently removes the ticket document record. This does not cancel travel, release the seat, change payment, or issue a refund. The page explicitly explains that distinction. Use Bookings → Cancel for cancellation and the existing simulated refund process. An eligible paid booking can recreate its ticket. Deleted tickets for past or cancelled travel cannot be recreated with the Create action.

**Permissions and transaction handling**

Passengers manage only tickets linked to their own bookings. Administrators can manage tickets across users. Ownership is checked on the server, so changing a submitted booking ID does not bypass access controls. Existing login, CSRF protection, escaped output and parameterized SQL remain in use. Create/delete operations lock the booking first; updates reuse the booking transaction. Failed changes roll back.

Ticket viewing no longer creates database records. A deleted ticket therefore stays deleted when an old ticket link is reopened.

## 2. Animated booking confirmation

The checkout sequence is now:

Reserve a seat → pending booking → successful payment → Booking confirmed → optional navigation to Tickets.

Only paid, confirmed bookings show the success page. A reservation that has not been paid remains pending; it is not presented as confirmed. Declined payments keep the existing error flow.

The new page includes:

- Large “Booking confirmed!” heading and welcome message.
- Animated checkmark, entrance animation, confetti and a small flight animation.
- Booking reference, without displaying the e-ticket or its barcode.
- Go to Tickets and My bookings buttons, with no automatic ticket opening.
- Reduced-motion support that presents the completed checkmark without decorative motion.

The confirmation route verifies ownership and current booking/payment status. Reopening checkout for an already-paid booking also leads to confirmation. The Bookings page's old direct E-Ticket link now leads to Ticket management. The printable `/ticket` detail route remains available when opened from Tickets; it retains ownership checks.

## 3. Admin feedback response redesign

The admin Feedback page is presented as “Passenger care”. Existing review search, status filtering, star ratings and review management remain available.

The response area now has:

- A rounded expandable response panel with a Lemonade reply icon.
- Clear Write a thoughtful reply / Edit your response labels.
- Short guidance to help an administrator write a helpful response.
- A larger labeled textarea with the existing 1,000-character counter and validation.
- A prominent Publish response button and a note identifying who can see the reply.
- A styled official-response card with a Lemonade border and preserved line breaks.
- Responsive spacing, keyboard focus styles and stacked controls on narrow screens.

Publishing still uses the existing admin-only response endpoint and passenger notification behavior. Closing and deleting reviews remain separate controls.

## 4. Electric and Lemonade theme

| Color | Value | Main usage |
|---|---|---|
| Electric | `#09080E` | Headings, primary text accents, dark panels, footer, ticket header |
| Lemonade | `#FE6807` | Primary calls to action, brand badge, highlights, selected seats, confirmation artwork |

Shared styles apply the theme across the application, including home, authentication, dashboard, bookings, tickets and feedback. Neutral backgrounds and white cards provide readable contrast. Lemonade buttons use Electric text. Error, warning and status indicators retain distinct semantic colors.

The added `electric.css` loads after the original styles and contains the component styling, responsive rules and reduced-motion support. Shared JSP fragments now explicitly declare UTF-8 so icons and footer punctuation render correctly.

## 5. Build and deployment

1. Extract the source ZIP and open its `LankaWingsAirlineReservation` folder as a Maven project.
2. Select JDK 17 for the project and Maven runner.
3. Keep the existing SQL Server connection settings. No schema change is required for this edit.
4. Run `mvn clean package` to run tests and build the application.
5. Deploy `target/LankaWings.war`, or the supplied standalone `LankaWings.war`, to Tomcat 10.1.
6. Open the deployed `/LankaWings/` application. Refresh the browser to load the new styles.

For a brand-new database, follow the original README and SQL setup files. Do not recreate an existing database just to apply this update. Existing bookings, payments and feedback do not need to be migrated for these changes.

## 6. Verification completed

- Clean Maven build on Java 17: passed; deployable WAR generated.
- 21 automated regression tests: passed, zero failures/errors/skipped tests.
- All JSP templates compiled using Tomcat 10.1 Jasper: zero errors.
- Browser inspection of the actual JSP confirmation, ticket management and expanded admin reply layouts using isolated sample fixtures: passed. Ticket editing fields, character counter and corrected shared icons were also checked.

The automated tests cover ownership, eligibility, duplicate creation, deletion without payment/cancellation updates, rollback, invalid ticket edits, synchronized booking/ticket updates, confirmation access, paid-checkout redirect and read-only ticket viewing.

**Verification limit:** the tests mock JDBC access. A complete payment/CRUD cycle against a live SQL Server database has not been run. Browser fixtures were for layout review only; they are not included in the source ZIP or WAR. Payment remains the original application's simulation, not a live payment gateway.

## 7. Acceptance checks on your database

1. Make a future reservation and pay successfully. Confirm that Booking confirmed appears and no e-ticket opens automatically.
2. Open Tickets and view/print the automatically issued ticket.
3. Edit its passenger details and choose an available seat. Check that both Bookings and the printed ticket reflect the update.
4. Try an occupied seat or invalid passenger information; the application should reject it without a partial update.
5. Delete the ticket. Verify that the booking and payment remain unchanged and the old ticket URL does not recreate it.
6. Recreate the ticket from the eligible paid-booking selector. Verify that duplicate creation is rejected.
7. Confirm that a second passenger cannot modify or view the first passenger's ticket by changing its booking ID.
8. Sign in as admin, publish/edit a feedback response, and check the passenger's Feedback and notification pages.
9. Cancel a paid booking through Bookings and check the existing cancellation/refund behavior.

## 8. Source files changed

### Added

- `src/main/java/com/lankawings/servlet/BookingConfirmationServlet.java`
- `src/main/webapp/WEB-INF/views/booking-confirmed.jsp`
- `src/main/webapp/assets/css/electric.css`
- `src/test/java/com/lankawings/dao/TicketManagementTest.java`
- `src/test/java/com/lankawings/servlet/BookingConfirmationTest.java`

### Modified

- `README.md`
- `pom.xml`
- `src/main/java/com/lankawings/dao/BookingDAO.java`
- `src/main/java/com/lankawings/dao/TicketDAO.java`
- `src/main/java/com/lankawings/servlet/PaymentServlet.java`
- `src/main/java/com/lankawings/servlet/TicketManagementServlet.java`
- `src/main/java/com/lankawings/servlet/TicketServlet.java`
- `src/main/webapp/WEB-INF/jspf/csrf.jspf`
- `src/main/webapp/WEB-INF/jspf/flash.jspf`
- `src/main/webapp/WEB-INF/jspf/footer.jspf`
- `src/main/webapp/WEB-INF/jspf/head.jspf`
- `src/main/webapp/WEB-INF/jspf/header.jspf`
- `src/main/webapp/WEB-INF/jspf/sky.jspf`
- `src/main/webapp/WEB-INF/views/bookings.jsp`
- `src/main/webapp/WEB-INF/views/feedback.jsp`
- `src/main/webapp/WEB-INF/views/tickets.jsp`
- `src/main/webapp/assets/css/enhance.css`
- `src/main/webapp/assets/css/style.css`

`EDIT_DETAILS.md` is also included as a new project document. Original SQL scripts and image assets are retained.
