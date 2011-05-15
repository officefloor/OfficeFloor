/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.tutorial.teamhttpserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.plugin.autowire.AutoWireTeam;
import net.officefloor.plugin.jdbc.datasource.DataSourceManagedObjectSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.hsqldb.jdbcDriver;

/**
 * Example {@link HttpServerAutoWireOfficeFloorSource} with specific
 * {@link Team} instances for {@link Task} instances.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TeamHttpServer {

	public static void main(String[] args) throws Exception {

		// Create the database
		createDatabase();

		// Configure the HTTP server
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();
		source.addHttpTemplate("Template.ofp", Template.class, "example");
		HttpParametersObjectManagedObjectSource.autoWire(source,
				EncryptLetter.class);
		source.addManagedObject(DataSourceManagedObjectSource.class, null,
				DataSource.class).loadProperties("datasource.properties");

		// Configure team for all database tasks
		AutoWireTeam team = source.assignTeam(LeaderFollowerTeamSource.class,
				DataSource.class);
		team.addProperty("size", "10");

		// Start the HTTP server
		source.openOfficeFloor();
	}

	// END SNIPPET: example

	// START SNIPPET: createDatabase
	private static void createDatabase() throws Exception {
		Class.forName(jdbcDriver.class.getName());
		Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:mem:exampleDb", "sa", "");
		connection
				.createStatement()
				.execute(
						"CREATE TABLE LETTER_CODE ( LETTER CHAR(1) PRIMARY KEY, CODE CHAR(1) )");
		PreparedStatement statement = connection
				.prepareStatement("INSERT INTO LETTER_CODE ( LETTER, CODE ) VALUES ( ?, ? )");
		for (char letter = ' '; letter <= 'z'; letter++) {
			char code = (char) ('z' - letter + ' '); // simple reverse order
			statement.setString(1, String.valueOf(letter));
			statement.setString(2, String.valueOf(code));
			statement.execute();
		}
	}
	// END SNIPPET: createDatabase
}