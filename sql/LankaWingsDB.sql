IF DB_ID('LankaWingsDB') IS NULL
BEGIN
    CREATE DATABASE LankaWingsDB;
END;
GO

USE LankaWingsDB;
GO

IF OBJECT_ID('dbo.Notifications', 'U') IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.Feedback', 'U') IS NOT NULL DROP TABLE dbo.Feedback;
IF OBJECT_ID('dbo.Payments', 'U') IS NOT NULL DROP TABLE dbo.Payments;
IF OBJECT_ID('dbo.Tickets', 'U') IS NOT NULL DROP TABLE dbo.Tickets;
IF OBJECT_ID('dbo.Bookings', 'U') IS NOT NULL DROP TABLE dbo.Bookings;
IF OBJECT_ID('dbo.Flights', 'U') IS NOT NULL DROP TABLE dbo.Flights;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(120) NOT NULL,
    Username VARCHAR(60) NOT NULL UNIQUE,
    Email VARCHAR(120) NOT NULL UNIQUE,
    Phone VARCHAR(25),
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) NOT NULL DEFAULT 'PASSENGER' CHECK (Role IN ('PASSENGER','ADMIN','FINANCE')),
    Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (Status IN ('ACTIVE','INACTIVE')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE Flights (
    FlightID INT IDENTITY(1,1) PRIMARY KEY,
    FlightNo VARCHAR(15) NOT NULL UNIQUE,
    Origin VARCHAR(80) NOT NULL,
    Destination VARCHAR(80) NOT NULL,
    DepartureTime DATETIME2 NOT NULL,
    ArrivalTime DATETIME2 NOT NULL,
    Fare DECIMAL(10,2) NOT NULL CHECK (Fare >= 0),
    TotalSeats INT NOT NULL DEFAULT 72 CHECK (TotalSeats > 0),
    Status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (Status IN ('SCHEDULED','BOARDING','DELAYED','CANCELLED','COMPLETED')),
    Aircraft VARCHAR(80) NOT NULL DEFAULT 'Airbus A320'
);

CREATE TABLE Bookings (
    BookingID INT IDENTITY(1,1) PRIMARY KEY,
    PNR VARCHAR(20) NOT NULL UNIQUE,
    UserID INT NOT NULL,
    FlightID INT NOT NULL,
    PassengerName VARCHAR(120) NOT NULL,
    PassportNo VARCHAR(40) NOT NULL,
    SeatNumber VARCHAR(8) NOT NULL,
    BookingStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (BookingStatus IN ('PENDING','CONFIRMED','CANCELLED')),
    PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (PaymentStatus IN ('UNPAID','PAID','REFUNDED')),
    BookedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Bookings_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Bookings_Flights FOREIGN KEY (FlightID) REFERENCES Flights(FlightID)
);
GO
CREATE UNIQUE INDEX UX_Bookings_ActiveSeat
ON Bookings(FlightID, SeatNumber)
WHERE BookingStatus <> 'CANCELLED';
GO

CREATE TABLE Tickets (
    TicketID INT IDENTITY(1,1) PRIMARY KEY,
    TicketNumber VARCHAR(30) NOT NULL UNIQUE,
    BookingID INT NOT NULL UNIQUE,
    TicketStatus VARCHAR(20) NOT NULL DEFAULT 'ISSUED' CHECK (TicketStatus IN ('ISSUED','CANCELLED')),
    IssuedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    LastUpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Tickets_Bookings FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID)
);
GO

CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    BookingID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    Method VARCHAR(30) NOT NULL,
    CardLast4 CHAR(4),
    TransactionRef VARCHAR(40) NOT NULL UNIQUE,
    PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' CHECK (PaymentStatus IN ('SUCCESS','FAILED','REFUNDED')),
    PaidAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Payments_Bookings FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID)
);

CREATE TABLE Feedback (
    FeedbackID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    Rating INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
    Subject VARCHAR(120) NOT NULL,
    Message VARCHAR(1000) NOT NULL,
    AdminResponse VARCHAR(1000),
    Status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (Status IN ('OPEN','RESPONDED','CLOSED')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Feedback_Users FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

CREATE TABLE Notifications (
    NotificationID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    Title VARCHAR(150) NOT NULL,
    Message VARCHAR(800) NOT NULL,
    Type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    IsRead BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Notifications_Users FOREIGN KEY (UserID) REFERENCES Users(UserID)
);



INSERT INTO Users (FullName, Username, Email, Phone, PasswordHash, Role)
VALUES
('Nizla - System Administrator', 'admin@lankawings.com', 'admin@lankawings.lk', '0770000000', 'pbkdf2_sha256$600000$ogYFZUjli+ekkiC8quP/VQ==$cFb5oUo9cDOznIz2kF1Sy5PUCuevDxmEvRScaHXdMd8=', 'ADMIN'),
('Demo Passenger', 'passenger', 'passenger@example.com', '0711112233', 'pbkdf2_sha256$600000$+vgadORXnkSC8p5ub9Wx1Q==$NjqrpZDXXWAfVwabhWYkzr4mUY+Q73edwupWsBwdqWg=', 'PASSENGER');

INSERT INTO Flights (FlightNo, Origin, Destination, DepartureTime, ArrivalTime, Fare, TotalSeats, Status, Aircraft)
VALUES
('LW101', 'Colombo (CMB)', 'Dubai (DXB)', DATEADD(DAY, 3, SYSDATETIME()), DATEADD(HOUR, 8, DATEADD(DAY, 3, SYSDATETIME())), 78500.00, 72, 'SCHEDULED', 'Airbus A320neo'),
('LW204', 'Colombo (CMB)', 'Doha (DOH)', DATEADD(DAY, 5, SYSDATETIME()), DATEADD(HOUR, 7, DATEADD(DAY, 5, SYSDATETIME())), 69900.00, 72, 'SCHEDULED', 'Airbus A320'),
('LW330', 'Colombo (CMB)', 'Singapore (SIN)', DATEADD(DAY, 8, SYSDATETIME()), DATEADD(HOUR, 5, DATEADD(DAY, 8, SYSDATETIME())), 92500.00, 84, 'SCHEDULED', 'Airbus A321'),
('LW415', 'Colombo (CMB)', 'Kuala Lumpur (KUL)', DATEADD(DAY, 10, SYSDATETIME()), DATEADD(HOUR, 4, DATEADD(DAY, 10, SYSDATETIME())), 64800.00, 72, 'SCHEDULED', 'Airbus A320neo'),
('LW520', 'Colombo (CMB)', 'Malé (MLE)', DATEADD(DAY, 2, SYSDATETIME()), DATEADD(HOUR, 2, DATEADD(DAY, 2, SYSDATETIME())), 45500.00, 60, 'SCHEDULED', 'ATR 72-600');

INSERT INTO Notifications (UserID, Title, Message, Type)
SELECT UserID, 'Welcome to Lanka Wings', 'Your passenger account is ready. Search flights and start your next journey.', 'WELCOME'
FROM Users WHERE Username = 'passenger';
GO





CREATE INDEX IX_Bookings_User ON Bookings(UserID);
CREATE INDEX IX_Bookings_Flight ON Bookings(FlightID);
CREATE INDEX IX_Payments_Booking ON Payments(BookingID);
CREATE INDEX IX_Feedback_User ON Feedback(UserID);
CREATE INDEX IX_Notifications_User ON Notifications(UserID, IsRead);
GO
