USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'Lankawings')
BEGIN
    -- CHANGE THIS PASSWORD before real use (and set it in DBConnection / env var LW_DB_PASSWORD).
    CREATE LOGIN [Lankawings] WITH PASSWORD = 'Lankawings123', CHECK_POLICY = OFF;
END;
GO
USE LankaWingsTicketReservationDB;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'Lankawings')
BEGIN
    CREATE USER [Lankawings] FOR LOGIN [Lankawings];
END;
GO
-- Least privilege: read + write data only. (The app never creates or drops tables.)
ALTER ROLE db_datareader ADD MEMBER [Lankawings];
ALTER ROLE db_datawriter ADD MEMBER [Lankawings];
GO
