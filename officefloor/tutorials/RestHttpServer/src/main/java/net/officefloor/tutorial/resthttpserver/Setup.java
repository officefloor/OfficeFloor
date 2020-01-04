package net.officefloor.tutorial.resthttpserver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Setup database.
 * 
 * @author Daniel Sagenschneider
 */
public class Setup {

	public void setup(Connection connection) throws SQLException {
		try {
			connection.createStatement().executeQuery("SELECT * FROM VEHICLE");
			return; // table exists
		} catch (SQLException exCreate) {
			try {
				connection.createStatement()
						.executeUpdate("CREATE TABLE VEHICLE ( ID IDENTITY, VEHICLE_TYPE VARCHAR(10), WHEELS INT)");
			} catch (SQLException exConcurrentSetup) {
				if (!exConcurrentSetup.getMessage().contains("Table \"VEHICLE\" already exists")) {
					throw exConcurrentSetup; // failure creating table
				}
			}
		}
	}

}