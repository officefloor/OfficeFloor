/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.databasehttpserver;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import net.officefloor.web.HttpParameters;

/**
 * Provides logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: getRows
public class Template {

	public Row[] getRows(Connection connection) throws SQLException {

		// Obtain the row instances
		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM EXAMPLE ORDER BY ID")) {

			ResultSet resultSet = statement.executeQuery();
			List<Row> rows = new LinkedList<Row>();
			while (resultSet.next()) {
				rows.add(new Row(resultSet.getInt("ID"), resultSet.getString("NAME"),
						resultSet.getString("DESCRIPTION")));
			}

			// Return the row instances
			return rows.toArray(new Row[rows.size()]);
		}
	}
	// END SNIPPET: getRows

	// START SNIPPET: addRow
	public void addRow(Row row, Connection connection) throws SQLException {

		// Add the row
		try (PreparedStatement statement = connection
				.prepareStatement("INSERT INTO EXAMPLE (NAME, DESCRIPTION) VALUES ( ?, ? )")) {
			statement.setString(1, row.getName());
			statement.setString(2, row.getDescription());
			statement.executeUpdate();

		}
	}
	// END SNIPPET: addRow

	// START SNIPPET: deleteRow
	@Data
	@HttpParameters
	public static class DeleteRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private String id;
	}

	public void deleteRow(DeleteRow delete, Connection connection) throws SQLException {

		// Obtain the identifier
		int id = Integer.parseInt(delete.id);

		// Delete the row
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM EXAMPLE WHERE ID = ?")) {
			statement.setInt(1, id);
			statement.executeUpdate();
		}
	}
	// END SNIPPET: deleteRow

}