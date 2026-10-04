IF DB_ID('LankaWingsUserNotificationDB') IS NULL CREATE DATABASE LankaWingsUserNotificationDB;
GO
USE LankaWingsUserNotificationDB;
GO

IF OBJECT_ID('dbo.Notifications','U') IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.UserAudit','U') IS NOT NULL DROP TABLE dbo.UserAudit;
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(120) NOT NULL,
    Username VARCHAR(60) NOT NULL UNIQUE,
    Email VARCHAR(120) NOT NULL UNIQUE,
    Phone VARCHAR(20) NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(30) NOT NULL DEFAULT 'PASSENGER'
        CHECK (Role IN ('PASSENGER','TRAVEL_AGENT','CHECKIN_STAFF','RESERVATION_MANAGER','CUSTOMER_SUPPORT','FINANCE','ADMIN')),
    Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (Status IN ('ACTIVE','INACTIVE')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE UserAudit (
    AuditID INT IDENTITY(1,1) PRIMARY KEY,
    ActorUserID INT NOT NULL,
    TargetUserID INT NOT NULL,
    Action VARCHAR(40) NOT NULL,
    Details VARCHAR(500) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_UserAudit_Actor FOREIGN KEY (ActorUserID) REFERENCES Users(UserID),
    CONSTRAINT FK_UserAudit_Target FOREIGN KEY (TargetUserID) REFERENCES Users(UserID)
);

CREATE TABLE Notifications (
    NotificationID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    Title VARCHAR(150) NOT NULL,
    Message VARCHAR(800) NOT NULL,
    Type VARCHAR(30) NOT NULL DEFAULT 'ANNOUNCEMENT',
    IsRead BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Notifications_User FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

CREATE INDEX IX_Users_RoleStatus ON Users(Role,Status);
CREATE INDEX IX_UserAudit_Time ON UserAudit(CreatedAt DESC);
CREATE INDEX IX_Notifications_UserReadTime ON Notifications(UserID,IsRead,CreatedAt DESC);
GO

-- Demo passwords:
-- admin     -> Lankawings@admin
-- passenger -> Passenger123
INSERT INTO Users(FullName,Username,Email,Phone,PasswordHash,Role,Status) VALUES
('System Administrator','admin','admin@lankawings.lk','0710000000',
 'pbkdf2_sha256$600000$ogYFZUjli+ekkiC8quP/VQ==$cFb5oUo9cDOznIz2kF1Sy5PUCuevDxmEvRScaHXdMd8=','ADMIN','ACTIVE'),
('Demo Passenger','passenger','passenger@example.com','0711112233',
 'pbkdf2_sha256$600000$+vgadORXnkSC8p5ub9Wx1Q==$NjqrpZDXXWAfVwabhWYkzr4mUY+Q73edwupWsBwdqWg=','PASSENGER','ACTIVE');
GO

DECLARE @Admin INT=(SELECT UserID FROM Users WHERE Username='admin');
DECLARE @Passenger INT=(SELECT UserID FROM Users WHERE Username='passenger');

INSERT INTO UserAudit(ActorUserID,TargetUserID,Action,Details)
VALUES(@Admin,@Passenger,'ACCOUNT_CREATE','Demo passenger account prepared for User + Notification Management demonstration');

INSERT INTO Notifications(UserID,Title,Message,Type,IsRead) VALUES
(@Passenger,'Welcome to Lanka Wings','Your passenger account is ready. Account and security alerts will appear here.','WELCOME',0),
(@Passenger,'Security reminder','Keep your password private and update it from Profile whenever necessary.','SECURITY',0),
(@Admin,'Notification Management ready','Use the Notifications page to send an announcement to all active users or one active username.','ANNOUNCEMENT',0);
GO
