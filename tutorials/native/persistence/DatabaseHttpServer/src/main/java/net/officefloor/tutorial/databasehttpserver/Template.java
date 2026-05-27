package net.officefloor.tutorial.databasehttpserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpQueryParameter;

/**
 * Provides logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
public class Template {

	// START SNIPPET: getRows
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
	public void addRow(Row row, Connection connection, ServerHttpConnection http) throws SQLException, IOException {

		// Add the row
		try (PreparedStatement statement = connection
				.prepareStatement("INSERT INTO EXAMPLE (NAME, DESCRIPTION) VALUES ( ?, ? )")) {
			statement.setString(1, row.getName());
			statement.setString(2, row.getDescription());
			statement.executeUpdate();
		}

		// Redirect back to page (POST/Redirect/GET pattern)
		HttpResponse response = http.getResponse();
		response.setStatus(HttpStatus.SEE_OTHER);
		response.getHeaders().addHeader("location", "/example");
	}
	// END SNIPPET: addRow

	// START SNIPPET: deleteRow
	public void deleteRow(@HttpQueryParameter("id") String id, Connection connection, ServerHttpConnection http)
			throws SQLException, IOException {

		// Obtain the identifier
		int rowId = Integer.parseInt(id);

		// Delete the row
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM EXAMPLE WHERE ID = ?")) {
			statement.setInt(1, rowId);
			statement.executeUpdate();
		}

		// Redirect back to page
		HttpResponse response = http.getResponse();
		response.setStatus(HttpStatus.SEE_OTHER);
		response.getHeaders().addHeader("location", "/example");
	}
	// END SNIPPET: deleteRow

}