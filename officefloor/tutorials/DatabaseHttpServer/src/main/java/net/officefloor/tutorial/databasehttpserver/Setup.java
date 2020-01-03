package net.officefloor.tutorial.databasehttpserver;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class Setup {

	public void setupDatabase(Connection connection) throws Exception {
		try (Statement statement = connection.createStatement()) {
			try {
				// Determine if table exists
				statement.executeQuery("SELECT * FROM EXAMPLE");

			} catch (SQLException ex) {
				// Create the table and row
				statement.execute(
						"CREATE TABLE EXAMPLE ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20), DESCRIPTION VARCHAR(256) )");
				statement.execute("INSERT INTO EXAMPLE ( NAME, DESCRIPTION ) VALUES ( 'WoOF', 'Web on OfficeFloor' )");
			}
		}
	}

}
// END SNIPPET: example