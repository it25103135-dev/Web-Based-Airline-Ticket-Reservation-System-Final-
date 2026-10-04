IF DB_ID('LankaWingsFlightDB') IS NULL CREATE DATABASE LankaWingsFlightDB;
GO
USE LankaWingsFlightDB;
GO

IF OBJECT_ID('dbo.Notifications','U') IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.Bookings','U') IS NOT NULL DROP TABLE dbo.Bookings;
IF OBJECT_ID('dbo.Flights','U') IS NOT NULL DROP TABLE dbo.Flights;
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
GO

-- Shared minimum dependency: only authentication/authorization for the Reservation Manager.
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(120) NOT NULL,
    Username VARCHAR(60) NOT NULL UNIQUE,
    Email VARCHAR(120) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(30) NOT NULL CHECK (Role IN ('RESERVATION_MANAGER','ADMIN','PASSENGER')),
    Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (Status IN ('ACTIVE','INACTIVE'))
);

-- OWNED TABLE: flight schedules.
CREATE TABLE Flights (
    FlightID INT IDENTITY(1,1) PRIMARY KEY,
    FlightNo VARCHAR(15) NOT NULL UNIQUE,
    Origin VARCHAR(80) NOT NULL,
    Destination VARCHAR(80) NOT NULL,
    DepartureTime DATETIME2 NOT NULL,
    ArrivalTime DATETIME2 NOT NULL,
    Fare DECIMAL(10,2) NOT NULL CHECK (Fare >= 0),
    TotalSeats INT NOT NULL CHECK (TotalSeats BETWEEN 1 AND 500),
    Status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (Status IN ('SCHEDULED','BOARDING','DELAYED','CANCELLED','COMPLETED')),
    Aircraft VARCHAR(80) NOT NULL,
    CONSTRAINT CK_Flights_Time CHECK (ArrivalTime > DepartureTime)
);

-- Dependency only: enough booking data to calculate occupied capacity, block unsafe delete/seat reduction,
-- and identify affected passengers after schedule changes. There is no booking-management code in this ZIP.
CREATE TABLE Bookings (
    BookingID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    FlightID INT NOT NULL,
    BookingStatus VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (BookingStatus IN ('PENDING','CONFIRMED','CANCELLED')),
    CONSTRAINT FK_FM_Booking_User FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_FM_Booking_Flight FOREIGN KEY (FlightID) REFERENCES Flights(FlightID)
);

-- Integration-only output created when a managed flight is delayed, cancelled or rescheduled.
CREATE TABLE Notifications (
    NotificationID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    Title VARCHAR(150) NOT NULL,
    Message VARCHAR(800) NOT NULL,
    Type VARCHAR(30) NOT NULL DEFAULT 'FLIGHT_UPDATE',
    IsRead BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_FM_Notification_User FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- Password: Lankawings@admin
INSERT INTO Users(FullName,Username,Email,PasswordHash,Role,Status) VALUES
('Reservation Manager','flightmanager','flightmanager@lankawings.lk','pbkdf2_sha256$600000$ogYFZUjli+ekkiC8quP/VQ==$cFb5oUo9cDOznIz2kF1Sy5PUCuevDxmEvRScaHXdMd8=','RESERVATION_MANAGER','ACTIVE'),
('Demo Passenger','passenger','passenger@example.com','pbkdf2_sha256$600000$+vgadORXnkSC8p5ub9Wx1Q==$NjqrpZDXXWAfVwabhWYkzr4mUY+Q73edwupWsBwdqWg=','PASSENGER','ACTIVE');

INSERT INTO Flights(FlightNo,Origin,Destination,DepartureTime,ArrivalTime,Fare,TotalSeats,Status,Aircraft) VALUES
('LW101','Colombo (CMB)','Dubai (DXB)',DATEADD(DAY,3,SYSDATETIME()),DATEADD(HOUR,5,DATEADD(DAY,3,SYSDATETIME())),78500.00,72,'SCHEDULED','Airbus A320neo'),
('LW204','Colombo (CMB)','Doha (DOH)',DATEADD(DAY,5,SYSDATETIME()),DATEADD(HOUR,5,DATEADD(DAY,5,SYSDATETIME())),69900.00,72,'SCHEDULED','Airbus A320'),
('LW330','Colombo (CMB)','Singapore (SIN)',DATEADD(DAY,8,SYSDATETIME()),DATEADD(HOUR,4,DATEADD(DAY,8,SYSDATETIME())),92500.00,84,'SCHEDULED','Airbus A321'),
('LW415','Colombo (CMB)','Kuala Lumpur (KUL)',DATEADD(DAY,10,SYSDATETIME()),DATEADD(HOUR,4,DATEADD(DAY,10,SYSDATETIME())),64800.00,72,'SCHEDULED','Airbus A320neo'),
('LW520','Colombo (CMB)','Malé (MLE)',DATEADD(DAY,2,SYSDATETIME()),DATEADD(HOUR,2,DATEADD(DAY,2,SYSDATETIME())),45500.00,60,'DELAYED','ATR 72-600');

-- One dependency booking so booked-seat protection and flight-update notification can be demonstrated.
INSERT INTO Bookings(UserID,FlightID,BookingStatus)
SELECT u.UserID,f.FlightID,'CONFIRMED' FROM Users u CROSS JOIN Flights f WHERE u.Username='passenger' AND f.FlightNo='LW101';

CREATE INDEX IX_FM_Flights_Departure ON Flights(DepartureTime);
CREATE INDEX IX_FM_Flights_AircraftTime ON Flights(Aircraft,DepartureTime,ArrivalTime);
CREATE INDEX IX_FM_Bookings_Flight ON Bookings(FlightID,BookingStatus);
CREATE INDEX IX_FM_Notifications_User ON Notifications(UserID,IsRead);
GO
