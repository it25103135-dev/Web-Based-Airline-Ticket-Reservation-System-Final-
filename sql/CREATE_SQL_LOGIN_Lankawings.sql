USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name='Lankawings')
BEGIN CREATE LOGIN [Lankawings] WITH PASSWORD='Lankawings123', CHECK_POLICY=OFF; END;
GO
USE LankaWingsPaymentDB;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name='Lankawings') CREATE USER [Lankawings] FOR LOGIN [Lankawings];
GO
ALTER ROLE db_datareader ADD MEMBER [Lankawings];
ALTER ROLE db_datawriter ADD MEMBER [Lankawings];
GO
