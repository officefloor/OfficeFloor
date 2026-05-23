package net.officefloor.tutorial.threadaffinityhttpserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Setup database.
 * 
 * @author Daniel Sagenschneider
 */
public class Setup {

	public void setup(Connection connection) throws SQLException {
		try {
			connection.createStatement().executeQuery("SELECT COUNT(*) AS CPU_COUNT FROM CPU");
			return; // table exists with the rows
		} catch (SQLException ex) {
			connection.createStatement().executeUpdate("CREATE TABLE CPU ( ID IDENTITY, CPU_NUMBER INT)");
			PreparedStatement insert = connection.prepareStatement("INSERT INTO CPU ( CPU_NUMBER ) VALUES ( ? )");
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				insert.setInt(1, i);
				insert.executeUpdate();
			}
		}
	}

}