package net.officefloor.tutorial.teamhttpserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Setup {

	public void setupDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			try {

				// Determine if table exists
				statement.executeQuery("SELECT * FROM LETTER_CODE");

			} catch (SQLException ex) {

				// Create the table
				statement.execute("CREATE TABLE LETTER_CODE ( LETTER CHAR(1) PRIMARY KEY, CODE CHAR(1) )");

				// Load the data
				try (PreparedStatement insert = connection
						.prepareStatement("INSERT INTO LETTER_CODE ( LETTER, CODE ) VALUES ( ?, ? )")) {
					for (char letter = ' '; letter <= 'z'; letter++) {
						char code = (char) ('z' - letter + ' '); // simple reverse order
						insert.setString(1, String.valueOf(letter));
						insert.setString(2, String.valueOf(code));
						insert.execute();
					}
				}
			}
		}
	}

}
// END SNIPPET: tutorial