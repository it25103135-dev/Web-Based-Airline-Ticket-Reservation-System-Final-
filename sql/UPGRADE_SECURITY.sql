/* Run this ONCE on an EXISTING LankaWingsDB (so you keep your data).
   New installs do not need it - LankaWingsDB.sql already contains everything.

   1) PasswordHash must be longer than 64 characters to hold PBKDF2 hashes.
      Existing SHA-256 passwords keep working and are upgraded automatically the next time each user signs in.
   2) Phone numbers: normalise "+94 77 123 4567" style values to the 10-digit format (0771234567).
   3) Helpful indexes. */
USE LankaWingsDB;
GO
ALTER TABLE Users ALTER COLUMN PasswordHash VARCHAR(255) NOT NULL;
GO
UPDATE Users
SET Phone = '0' + RIGHT(REPLACE(REPLACE(REPLACE(REPLACE(Phone,' ',''),'-',''),'+',''),'(',''), 9)
WHERE Phone IS NOT NULL
  AND REPLACE(REPLACE(REPLACE(Phone,' ',''),'-',''),'+','') LIKE '94[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]';
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Bookings_User')       CREATE INDEX IX_Bookings_User ON Bookings(UserID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Bookings_Flight')     CREATE INDEX IX_Bookings_Flight ON Bookings(FlightID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Payments_Booking')    CREATE INDEX IX_Payments_Booking ON Payments(BookingID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Feedback_User')       CREATE INDEX IX_Feedback_User ON Feedback(UserID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Notifications_User')  CREATE INDEX IX_Notifications_User ON Notifications(UserID, IsRead);
GO
/* Least privilege: the web app only needs to read and write rows, never to change the schema. */
IF IS_ROLEMEMBER('db_owner','Lankawings') = 1 ALTER ROLE db_owner DROP MEMBER [Lankawings];
ALTER ROLE db_datareader ADD MEMBER [Lankawings];
ALTER ROLE db_datawriter ADD MEMBER [Lankawings];
GO
