IF DB_ID('LankaWingsPaymentDB') IS NULL CREATE DATABASE LankaWingsPaymentDB;
GO
USE LankaWingsPaymentDB;
GO
IF OBJECT_ID('dbo.Payments','U') IS NOT NULL DROP TABLE dbo.Payments;
IF OBJECT_ID('dbo.Bookings','U') IS NOT NULL DROP TABLE dbo.Bookings;
IF OBJECT_ID('dbo.Flights','U') IS NOT NULL DROP TABLE dbo.Flights;
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
GO
CREATE TABLE Users(
 UserID INT IDENTITY(1,1) PRIMARY KEY, FullName VARCHAR(120) NOT NULL, Username VARCHAR(60) NOT NULL UNIQUE,
 Email VARCHAR(120) NOT NULL UNIQUE, PasswordHash VARCHAR(255) NOT NULL,
 Role VARCHAR(20) NOT NULL DEFAULT 'PASSENGER' CHECK(Role IN('PASSENGER','ADMIN','FINANCE')),
 Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK(Status IN('ACTIVE','INACTIVE'))
);
CREATE TABLE Flights(
 FlightID INT IDENTITY(1,1) PRIMARY KEY, FlightNo VARCHAR(15) NOT NULL UNIQUE, Origin VARCHAR(80) NOT NULL,
 Destination VARCHAR(80) NOT NULL, DepartureTime DATETIME2 NOT NULL, Fare DECIMAL(10,2) NOT NULL CHECK(Fare>=0),
 Status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK(Status IN('SCHEDULED','BOARDING','DELAYED','CANCELLED','COMPLETED'))
);
CREATE TABLE Bookings(
 BookingID INT IDENTITY(1,1) PRIMARY KEY, PNR VARCHAR(20) NOT NULL UNIQUE, UserID INT NOT NULL, FlightID INT NOT NULL,
 PassengerName VARCHAR(120) NOT NULL, SeatNumber VARCHAR(8) NOT NULL,
 BookingStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(BookingStatus IN('PENDING','CONFIRMED','CANCELLED')),
 PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK(PaymentStatus IN('UNPAID','PAID','REFUNDED')),
 CONSTRAINT FK_Pay_User FOREIGN KEY(UserID) REFERENCES Users(UserID), CONSTRAINT FK_Pay_Flight FOREIGN KEY(FlightID) REFERENCES Flights(FlightID)
);
CREATE TABLE Payments(
 PaymentID INT IDENTITY(1,1) PRIMARY KEY, BookingID INT NOT NULL, Amount DECIMAL(10,2) NOT NULL,
 Method VARCHAR(30) NOT NULL, CardLast4 CHAR(4), TransactionRef VARCHAR(40) NOT NULL UNIQUE,
 PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' CHECK(PaymentStatus IN('SUCCESS','FAILED','REFUNDED')),
 PaidAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(), CONSTRAINT FK_Payments_Booking FOREIGN KEY(BookingID) REFERENCES Bookings(BookingID)
);
GO
INSERT INTO Users(FullName,Username,Email,PasswordHash,Role) VALUES
('Payment Administrator','admin@lankawings.com','admin@lankawings.lk','pbkdf2_sha256$600000$ogYFZUjli+ekkiC8quP/VQ==$cFb5oUo9cDOznIz2kF1Sy5PUCuevDxmEvRScaHXdMd8=','ADMIN'),
('Demo Passenger','passenger','passenger@example.com','pbkdf2_sha256$600000$+vgadORXnkSC8p5ub9Wx1Q==$NjqrpZDXXWAfVwabhWYkzr4mUY+Q73edwupWsBwdqWg=','PASSENGER');
INSERT INTO Flights(FlightNo,Origin,Destination,DepartureTime,Fare,Status) VALUES
('LW101','Colombo (CMB)','Dubai (DXB)',DATEADD(DAY,3,SYSDATETIME()),78500.00,'SCHEDULED'),
('LW204','Colombo (CMB)','Doha (DOH)',DATEADD(DAY,5,SYSDATETIME()),69900.00,'SCHEDULED'),
('LW330','Colombo (CMB)','Singapore (SIN)',DATEADD(DAY,8,SYSDATETIME()),92500.00,'SCHEDULED');
DECLARE @Passenger INT=(SELECT UserID FROM Users WHERE Username='passenger');
INSERT INTO Bookings(PNR,UserID,FlightID,PassengerName,SeatNumber,BookingStatus,PaymentStatus)
SELECT 'LW-PAY-001',@Passenger,FlightID,'Demo Passenger','12A','PENDING','UNPAID' FROM Flights WHERE FlightNo='LW101';
INSERT INTO Bookings(PNR,UserID,FlightID,PassengerName,SeatNumber,BookingStatus,PaymentStatus)
SELECT 'LW-PAY-002',@Passenger,FlightID,'Demo Passenger','8C','CONFIRMED','PAID' FROM Flights WHERE FlightNo='LW204';
INSERT INTO Payments(BookingID,Amount,Method,CardLast4,TransactionRef,PaymentStatus)
SELECT b.BookingID,f.Fare,'Visa','4242','TXN-DEMO000001','SUCCESS' FROM Bookings b JOIN Flights f ON b.FlightID=f.FlightID WHERE b.PNR='LW-PAY-002';
CREATE INDEX IX_Payments_Booking ON Payments(BookingID);
CREATE INDEX IX_Bookings_User ON Bookings(UserID);
GO
