# Lanka Wings Airlines — Feedback Management

**Function:** Feedback Management / Passenger Reviews

This package is isolated to the Feedback Management function. It contains the feedback business logic plus only the minimum authentication/security dependencies needed to demonstrate passenger and administrator behaviour. **Notification Management is not included and Feedback Management does not call any notification code.**

## Implemented workflow

### Passenger
1. Sign in with a passenger account.
2. Open Feedback.
3. Submit a 1–5 star rating, subject and message.
4. View only your own feedback records.
5. Edit your own feedback while its status is `OPEN`.
6. Delete your own feedback while its status is `OPEN`.
7. Read the administrator's official response after it is added.

### Administrator
1. Sign in with the administrator account.
2. Open Passenger Care (the same `/feedback` function with admin permissions).
3. View all passenger feedback.
4. Search/filter by `OPEN`, `RESPONDED` or `CLOSED`.
5. Write or edit an official response.
6. Close a feedback case.
7. Delete feedback when required.
8. View the average rating, review total and 1–5 star distribution.

## Validation and rules
- Rating: 1 to 5.
- Subject: 3 to 120 characters.
- Message: 10 to 1000 characters.
- Admin response: 5 to 1000 characters.
- Maximum 5 new feedback submissions per passenger in 24 hours.
- Passenger updates are allowed only for their own `OPEN` feedback.
- Admin response changes status to `RESPONDED`.
- Closing changes status to `CLOSED`.
- All data-changing actions use POST + CSRF protection.
- SQL is parameterized.
- Output is HTML-escaped.

## Main business files
- `model/Feedback.java`
- `dao/FeedbackDAO.java`
- `servlet/FeedbackServlet.java`
- `WEB-INF/views/feedback.jsp`
- SQL table: `Feedback`

## Minimum shared technical files
- `User.java` and the trimmed `UserDAO.java` are used only for sign-in, role and active-status checks.
- `LoginServlet`, `LogoutServlet`, `SecurityFilter`.
- `DBConnection`, CSRF, validation, password hashing and HTML escaping utilities.

There is **no** `Notification.java`, `NotificationDAO.java`, `NotificationServlet.java`, Notifications table, notification page, notification bell, or feedback-to-notification integration in this ZIP.

## Database
Run:

`sql/FeedbackManagementDB.sql`

If your SQL Server does not already have the `Lankawings` SQL login, also run:

`sql/CREATE_SQL_LOGIN_Lankawings.sql`

Default connection:
- Database: `LankaWingsFeedbackDB`
- SQL user: `Lankawings`
- SQL password: `Lankawings123`

The connection can be overridden using `LW_DB_URL`, `LW_DB_USER`, `LW_DB_PASSWORD`, or JVM properties `lw.db.url`, `lw.db.user`, `lw.db.password`.

## Demo accounts

**Administrator**
- Username: `admin`
- Password: `Lankawings@admin`

**Passenger**
- Username: `passenger`
- Password: `Passenger123`

## Build / run
1. Install Java 17+, Maven and Tomcat 10.1+.
2. Run the SQL database script.
3. In this project folder run `mvn clean package`.
4. Deploy `target/LankaWings-FeedbackManagement.war` to Tomcat.
5. Open the application and sign in using one of the demo accounts.

## Suggested demonstration
- Sign in as `passenger` and submit a new 5-star review.
- Edit the review while it is `OPEN`.
- Sign out and sign in as `admin`.
- Find the review, respond to it and observe its status become `RESPONDED`.
- Close another feedback record and use search/status filtering.
- Sign back in as the passenger to see the administrator response inside Feedback Management itself.

## Viva scope
Be ready to explain the `Passenger 1 -> 0..* Feedback` relationship, role-based behaviour, `OPEN/RESPONDED/CLOSED` states, input validation, the 24-hour submission limit, ownership checks, administrator responses, rating statistics, parameterized SQL, CSRF, sessions and why authentication exists only as a technical dependency rather than as a separate User Management function.
