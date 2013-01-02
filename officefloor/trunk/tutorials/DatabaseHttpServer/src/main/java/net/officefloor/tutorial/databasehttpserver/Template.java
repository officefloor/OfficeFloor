/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.databasehttpserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Provides logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: getRows
public class Template {

	public Row[] getRows(DataSource dataSource) throws SQLException {

		Connection connection = dataSource.getConnection();
		try {
			
			// Obtain the row instances
			ResultSet resultSet = connection.createStatement().executeQuery(
					"SELECT * FROM EXAMPLE ORDER BY ID");
			List<Row> rows = new LinkedList<Row>();
			while (resultSet.next()) {
				rows.add(new Row(resultSet.getInt("ID"), resultSet
						.getString("NAME"), resultSet.getString("DESCRIPTION")));
			}
			resultSet.close();

			// Return the row instances
			return rows.toArray(new Row[rows.size()]);
			
		} finally {
			connection.close();
		}
	}
	// END SNIPPET: getRows

	// START SNIPPET: addRow
	public void addRow(Row row, DataSource dataSource) throws SQLException {
		
		Connection connection = dataSource.getConnection();
		try {

			// Add the row
			PreparedStatement statement = connection
					.prepareStatement("INSERT INTO EXAMPLE (NAME, DESCRIPTION) VALUES ( ?, ? )");
			statement.setString(1, row.getName());
			statement.setString(2, row.getDescription());
			statement.executeUpdate();

		} finally {
			connection.close();
		}
	}
	// END SNIPPET: addRow

	// START SNIPPET: deleteRow
	public void deleteRow(Row row, DataSource dataSource) throws SQLException {

		Connection connection = dataSource.getConnection();
		try {

			// Delete the row
			PreparedStatement statement = connection
					.prepareStatement("DELETE FROM EXAMPLE WHERE ID = ?");
			statement.setInt(1, row.getId());
			statement.executeUpdate();

		} finally {
			connection.close();
		}
	}
	// END SNIPPET: deleteRow

}