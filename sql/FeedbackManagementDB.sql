IF DB_ID('LankaWingsFeedbackDB') IS NULL CREATE DATABASE LankaWingsFeedbackDB;
GO
USE LankaWingsFeedbackDB;
GO
IF OBJECT_ID('dbo.Feedback','U') IS NOT NULL DROP TABLE dbo.Feedback;
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
GO
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(120) NOT NULL,
    Username VARCHAR(60) NOT NULL UNIQUE,
    Email VARCHAR(120) NOT NULL UNIQUE,
    Phone VARCHAR(20) NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) NOT NULL CHECK (Role IN ('PASSENGER','ADMIN')),
    Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (Status IN ('ACTIVE','INACTIVE')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE Feedback (
    FeedbackID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    Rating INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
    Subject VARCHAR(120) NOT NULL,
    Message VARCHAR(1000) NOT NULL,
    AdminResponse VARCHAR(1000) NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (Status IN ('OPEN','RESPONDED','CLOSED')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Feedback_User FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO
CREATE INDEX IX_Feedback_UserCreated ON Feedback(UserID,CreatedAt DESC);
CREATE INDEX IX_Feedback_StatusCreated ON Feedback(Status,CreatedAt DESC);
GO

-- Demo passwords:
-- admin     -> Lankawings@admin
-- passenger -> Passenger123
INSERT INTO Users(FullName,Username,Email,Phone,PasswordHash,Role,Status) VALUES
('System Administrator','admin','admin@lankawings.lk','0710000000','pbkdf2_sha256$600000$ogYFZUjli+ekkiC8quP/VQ==$cFb5oUo9cDOznIz2kF1Sy5PUCuevDxmEvRScaHXdMd8=','ADMIN','ACTIVE'),
('Demo Passenger','passenger','passenger@example.com','0711112233','pbkdf2_sha256$600000$+vgadORXnkSC8p5ub9Wx1Q==$NjqrpZDXXWAfVwabhWYkzr4mUY+Q73edwupWsBwdqWg=','PASSENGER','ACTIVE');
GO
DECLARE @Passenger INT=(SELECT UserID FROM Users WHERE Username='passenger');
INSERT INTO Feedback(UserID,Rating,Subject,Message,AdminResponse,Status) VALUES
(@Passenger,5,'Excellent support','The staff were helpful and the service experience was very smooth.',NULL,'OPEN'),
(@Passenger,4,'Good overall experience','The airline service was good and I appreciate the friendly assistance.','Thank you for sharing your experience. We are glad our team could assist you.','RESPONDED');
GO
