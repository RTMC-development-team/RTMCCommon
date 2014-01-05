package com.rushteamc.plugin.common.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager
{
	private static Connection connection;
	private static DatabaseQueryHandler databaseQueryHandler;
	private static String tablePrefix = "";

	public static <returnType> returnType addDatabaseRunnable(DatabaseRunnable<returnType> databaseRunnable)
	{
		databaseQueryHandler.addDatabaseRunnable(databaseRunnable);
		while (databaseRunnable.isBlocking()) {
			try
			{
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return databaseRunnable.getReturnValue();
	}

	public static void setup(String host, String database, String username, String password)
	{
		String url = "jdbc:mysql://" + host + "/" + database + "?characterEncoding=UTF-8&autoReconnect=true";
		try
		{
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		databaseQueryHandler = new DatabaseQueryHandler();

		addDatabaseRunnable(new DatabaseRunnable<Void>(DatabaseRunnable.Priority.HIGHEST)
		{
			public Void run()
			{
				try {
					Statement statement = DatabaseManager.connection.createStatement();

					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "Users` (ID int UNSIGNED NOT NULL AUTO_INCREMENT, Username varchar(127) NOT NULL, Password varchar(128) NOT NULL DEFAULT '00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000', Prefix Text, Suffix Text, PRIMARY KEY (ID) );");
					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "UserGroups` (UserID int UNSIGNED NOT NULL, GroupID int UNSIGNED NOT NULL, PRIMARY KEY (UserID, GroupID) );");
					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "UserPermissions` (UserID int UNSIGNED NOT NULL, Permission varchar(255) NOT NULL, Granded boolean NOT NULL, PRIMARY KEY (UserID, Permission) );");

					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "Groups` (ID int UNSIGNED NOT NULL AUTO_INCREMENT, Groupname varchar(127) NOT NULL, Prefix Text, Suffix Text, `Default` boolean DEFAULT 0 NOT NULL, PRIMARY KEY (ID) );");
					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "GroupParents` (GroupID int UNSIGNED NOT NULL, ParentGroupID int UNSIGNED NOT NULL, PRIMARY KEY (GroupID, ParentGroupID) );");
					statement.addBatch("CREATE TABLE IF NOT EXISTS `" + DatabaseManager.getTablePrefix() + "GroupPermissions` (GroupID int UNSIGNED NOT NULL, Permission varchar(255) NOT NULL, Granded boolean NOT NULL, PRIMARY KEY (GroupID, Permission) );");

					statement.executeBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	public static void close()
	{
		if (connection != null)
		{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public static PreparedStatement createPreparedStatement(String SQLQuery) throws SQLException
	{
		if (connection == null) {
			throw new ConnectionNotInitializedException();
		}
		return connection.prepareStatement(SQLQuery);
	}

	public static PreparedStatement createPreparedInsertStatement(String SQLQuery) throws SQLException
	{
		if (connection == null) {
			throw new ConnectionNotInitializedException();
		}
		return connection.prepareStatement(SQLQuery, 1);
	}

	public static PreparedStatement createPreparedInsertStatement(String SQLQuery, String id) throws SQLException
	{
		if (connection == null) {
			throw new ConnectionNotInitializedException();
		}
		return connection.prepareStatement(SQLQuery, new String[] { id });
	}

	public static String getTablePrefix()
	{
		return tablePrefix;
	}

	public static void setTablePrefix(String tablePrefix)
	{
		DatabaseManager.tablePrefix = tablePrefix;
	}

	public static class ConnectionNotInitializedException extends SQLException
	{
		private static final long serialVersionUID = -6848497979400328468L;
	}
}

