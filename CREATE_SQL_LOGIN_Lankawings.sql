/* Run in SQL Server as an administrator if the SQL login does not already exist. */
USE [master];
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'Lankawings')
    CREATE LOGIN [Lankawings] WITH PASSWORD = N'Lankawings123', CHECK_POLICY = OFF;
GO
