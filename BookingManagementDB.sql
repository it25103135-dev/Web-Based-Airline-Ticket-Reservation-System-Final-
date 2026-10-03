/*
  Lanka Wings - Booking Management / Book Flight on Behalf of Client
  SQL Server setup. This database contains only the tables required by this function:
  Users (authentication + client lookup), Flights (read-only booking dependency),
  Bookings (owned business data) and BookingAudit (traceability).
*/
USE [master];
GO
IF DB_ID(N'LankaWingsBookingDB') IS NOT NULL
BEGIN
    ALTER DATABASE [LankaWingsBookingDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [LankaWingsBookingDB];
END
GO
CREATE DATABASE [LankaWingsBookingDB];
GO
USE [LankaWingsBookingDB];
GO

CREATE TABLE dbo.Users(
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(80) NOT NULL,
    Username VARCHAR(30) NOT NULL UNIQUE,
    Email VARCHAR(120) NOT NULL UNIQUE,
    Phone VARCHAR(20) NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) NOT NULL CONSTRAINT CK_Users_Role CHECK(Role IN ('PASSENGER','TRAVEL_AGENT','ADMIN')),
    Status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CONSTRAINT CK_Users_Status CHECK(Status IN ('ACTIVE','INACTIVE')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE dbo.Flights(
    FlightID INT IDENTITY(1,1) PRIMARY KEY,
    FlightNo VARCHAR(12) NOT NULL UNIQUE,
    Origin NVARCHAR(80) NOT NULL,
    Destination NVARCHAR(80) NOT NULL,
    DepartureTime DATETIME2 NOT NULL,
    ArrivalTime DATETIME2 NOT NULL,
    Fare DECIMAL(12,2) NOT NULL CHECK(Fare >= 0),
    TotalSeats INT NOT NULL CHECK(TotalSeats BETWEEN 1 AND 600),
    Status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK(Status IN ('SCHEDULED','DELAYED','CANCELLED','COMPLETED')),
    Aircraft NVARCHAR(80) NOT NULL
);
GO

CREATE TABLE dbo.Bookings(
    BookingID INT IDENTITY(1,1) PRIMARY KEY,
    PNR VARCHAR(12) NOT NULL UNIQUE,
    UserID INT NOT NULL,              -- client/passenger account
    AgentUserID INT NOT NULL,         -- Travel Agent who created the booking
    FlightID INT NOT NULL,
    PassengerName NVARCHAR(80) NOT NULL,
    PassportNo VARCHAR(20) NOT NULL,
    SeatNumber VARCHAR(4) NOT NULL,
    BookingStatus VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK(BookingStatus IN ('PENDING','CONFIRMED','CANCELLED')),
    PaymentStatus VARCHAR(15) NOT NULL DEFAULT 'UNPAID' CHECK(PaymentStatus IN ('UNPAID','PAID','REFUNDED')),
    CommissionAmount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK(CommissionAmount >= 0),
    BookedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Bookings_Client FOREIGN KEY(UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Bookings_Agent FOREIGN KEY(AgentUserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Bookings_Flight FOREIGN KEY(FlightID) REFERENCES dbo.Flights(FlightID)
);
GO
CREATE UNIQUE INDEX UX_Bookings_ActiveSeat ON dbo.Bookings(FlightID,SeatNumber) WHERE BookingStatus <> 'CANCELLED';
CREATE UNIQUE INDEX UX_Bookings_ActivePassengerDocument ON dbo.Bookings(FlightID,PassportNo) WHERE BookingStatus <> 'CANCELLED';
GO

CREATE TABLE dbo.BookingAudit(
    AuditID BIGINT IDENTITY(1,1) PRIMARY KEY,
    PNR VARCHAR(12) NOT NULL,
    ActorUserID INT NOT NULL,
    ActionType VARCHAR(20) NOT NULL,
    Details NVARCHAR(400) NULL,
    ActionAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_BookingAudit_Actor FOREIGN KEY(ActorUserID) REFERENCES dbo.Users(UserID)
);
GO

-- Demo accounts. Passwords are PBKDF2-HMAC-SHA256 hashes used by PasswordUtil.
INSERT dbo.Users(FullName,Username,Email,Phone,PasswordHash,Role,Status) VALUES
(N'Anas Travel Agent','agent1','agent1@lankawings.demo','0771111111','pbkdf2_sha256$600000$OSmNmR54sPplA9/eHHHhNQ==$4fZxva6JHc0VnNxr1XzKXBgXbCSxZ6CKcpG5TlT5y1E=','TRAVEL_AGENT','ACTIVE'),
(N'Nimal Perera','passenger1','passenger1@example.com','0772222222','pbkdf2_sha256$600000$oUY+LWPh6A1Y+IVo+UY1ZA==$jsYG0bOr2KeGpLETJyWDLG0BP8riEsfg9VzStMATbQ4=','PASSENGER','ACTIVE'),
(N'Sara Fernando','passenger2','passenger2@example.com','0773333333','pbkdf2_sha256$600000$cg1rtFFqkWJj5REXC4eAJw==$Y0dUw7WWbxD5/77NGtDfX3gxb37pk1w+XfprIGTqQJ8=','PASSENGER','ACTIVE'),
(N'System Administrator','admin','admin@lankawings.demo','0774444444','pbkdf2_sha256$600000$UrjHBcu4HEVM/JEBxSZdAA==$hI2wILkU5U1bKtj4Y2U4GL8gxWNMy3Wl7hfDXNIlBuc=','ADMIN','ACTIVE');
GO

-- Future flights so the package works even when run later.
INSERT dbo.Flights(FlightNo,Origin,Destination,DepartureTime,ArrivalTime,Fare,TotalSeats,Status,Aircraft) VALUES
('LW601',N'Colombo',N'Dubai',DATEADD(DAY,10,SYSDATETIME()),DATEADD(MINUTE,280,DATEADD(DAY,10,SYSDATETIME())),85000.00,60,'SCHEDULED',N'Airbus A320'),
('LW602',N'Colombo',N'Singapore',DATEADD(DAY,14,SYSDATETIME()),DATEADD(MINUTE,250,DATEADD(DAY,14,SYSDATETIME())),92000.00,72,'SCHEDULED',N'Airbus A321'),
('LW603',N'Colombo',N'Malé',DATEADD(DAY,18,SYSDATETIME()),DATEADD(MINUTE,95,DATEADD(DAY,18,SYSDATETIME())),42000.00,48,'DELAYED',N'ATR 72');
GO

-- Demo agent-linked bookings. UserID=client, AgentUserID=agent1.
INSERT dbo.Bookings(PNR,UserID,AgentUserID,FlightID,PassengerName,PassportNo,SeatNumber,BookingStatus,PaymentStatus,CommissionAmount)
VALUES
('LWAGT001',2,1,1,N'Nimal Perera','N1234567','2A','PENDING','UNPAID',4250.00),
('LWAGT002',3,1,2,N'Sara Fernando','P7654321','3C','CONFIRMED','PAID',4600.00);
GO
INSERT dbo.BookingAudit(PNR,ActorUserID,ActionType,Details) VALUES
('LWAGT001',1,'CREATE',N'Demo booking created by Travel Agent for Nimal Perera.'),
('LWAGT002',1,'CREATE',N'Demo booking created by Travel Agent for Sara Fernando.');
GO

-- Map the SQL login to this database and grant only the permissions needed by the app.
IF EXISTS (SELECT 1 FROM sys.server_principals WHERE name=N'Lankawings')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name=N'Lankawings')
        CREATE USER [Lankawings] FOR LOGIN [Lankawings];
    ALTER ROLE db_datareader ADD MEMBER [Lankawings];
    ALTER ROLE db_datawriter ADD MEMBER [Lankawings];
END
GO

PRINT 'LankaWingsBookingDB ready.';
PRINT 'Travel Agent: agent1 / Agent@123';
PRINT 'Passenger: passenger1 / Passenger@123';
PRINT 'Admin: admin / Admin@123';
GO
