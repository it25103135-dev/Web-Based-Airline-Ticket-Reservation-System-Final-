# Lanka Wings Airlines — User Management + Notification Management

This package contains **only** the User Management and Notification Management functions, plus the minimum technical files required to run them.

## User Management included
- Passenger signup / registration
- Login with username or email
- Logout and secure session handling
- View and edit personal profile
- Change password
- Self account deactivation for non-admin users
- Administrator user creation
- Administrator edit of user details
- Role assignment
- Activate / deactivate accounts
- User audit history
- Server-side role-based authorization

## Notification Management included
- Notification bell with unread badge
- View latest notifications
- Unread notification count
- Mark one notification as read
- Mark all notifications as read
- Delete one notification
- Clear all read notifications
- Administrator announcement to all ACTIVE users
- Administrator announcement to one ACTIVE username
- Welcome notification after passenger signup
- Security notification after password/profile changes
- Account-created / role/status-update notifications from User Management

## Notification types used
- `WELCOME`
- `SECURITY`
- `ANNOUNCEMENT`

The schema also allows other text values in `Type`, but this isolated package intentionally does not include Payment, Flight, Booking, Ticket, Refund, or Feedback event sources.

## Main business files

### User Management
- `model/User.java`
- `model/UserAudit.java`
- `dao/UserDAO.java`
- `dao/UserAuditDAO.java`
- `servlet/RegisterServlet.java`
- `servlet/LoginServlet.java`
- `servlet/LogoutServlet.java`
- `servlet/ProfileServlet.java`
- `servlet/AdminUserServlet.java`

### Notification Management
- `model/Notification.java`
- `dao/NotificationDAO.java`
- `servlet/NotificationServlet.java`
- `WEB-INF/views/notifications.jsp`

### Shared/minimum infrastructure
- `DBConnection.java`
- `SecurityFilter.java`
- CSRF, validation, password hashing, request helpers, rate limiting and HTML escaping
- Common JSP fragments, CSS/JS and error pages

## Intentionally excluded
- Flight Management
- Ticket Reservation
- Booking Management / Travel Agent booking
- Payment Management
- Refund / Customer Support
- Feedback Management
- Loyalty / promotions
- Operational/financial reports

## Database
Run:

`sql/UserNotificationManagementDB.sql`

If your SQL Server login has not been created, also run:

`sql/CREATE_SQL_LOGIN_Lankawings.sql`

Default connection:
- Database: `LankaWingsUserNotificationDB`
- SQL user: `Lankawings`
- SQL password: `Lankawings123`

You can override the connection without editing source:
- `LW_DB_URL`
- `LW_DB_USER`
- `LW_DB_PASSWORD`

or JVM properties:
- `-Dlw.db.url=...`
- `-Dlw.db.user=...`
- `-Dlw.db.password=...`

## Build / run
1. Install Java 17+, Maven, SQL Server and Tomcat 10.1+.
2. Run the SQL script.
3. From the project folder run: `mvn clean package`
4. Deploy: `target/LankaWings-UserNotificationManagement.war`

## Demo accounts

### Administrator
- Username: `admin`
- Password: `Lankawings@admin`

### Passenger
- Username: `passenger`
- Password: `Passenger123`

## Suggested demo
1. Login as `passenger`.
2. Open Notifications and view the preloaded welcome/security messages.
3. Mark one notification read, then use **Mark all read** and **Clear read**.
4. Register a new passenger and confirm a welcome notification is created.
5. Change the passenger password and confirm a security notification is created.
6. Login as `admin`.
7. Open User Management, create or update an account and confirm the target user's notification is created.
8. Open Notifications as admin and send an announcement:
   - to everyone, or
   - to one active username.
9. Confirm a passenger cannot access `/admin/users`.
10. Confirm users can delete only their own notifications.

## Viva points
Be ready to explain:
- registration and duplicate username/email validation
- authentication vs authorization
- password hashing
- sessions and CSRF
- role/status administration
- audit logging
- one-to-many relationship: User -> Notifications
- unread/read state
- user ownership checks on mark-read/delete
- broadcast vs single-user announcement
- why Notification Management is integrated with User Management here but unrelated airline modules are excluded
