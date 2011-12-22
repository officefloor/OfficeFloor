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
package net.officefloor.tutorial.databasehttpserver;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.sql.DataSource;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.plugin.jdbc.datasource.DataSourceManagedObjectSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.hsqldb.jdbcDriver;

/**
 * Example {@link HttpServerAutoWireOfficeFloorSource} with database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class DatabaseHttpServer {

	public static void main(String[] args) throws Exception {

		// Create the database
		createDatabase();

		// Configure the HTTP server
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();
		HttpParametersObjectManagedObjectSource.autoWire(source, Row.class);
		source.addHttpTemplate("Template.ofp", Template.class, "example");

		// Provide DataSource to database for dependency injection
		AutoWireObject object = source.addManagedObject(
				DataSourceManagedObjectSource.class.getName(), null,
				new AutoWire(DataSource.class));
		object.loadProperties("datasource.properties");

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
						"CREATE TABLE EXAMPLE ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20), DESCRIPTION VARCHAR(256) )");
		connection
				.createStatement()
				.execute(
						"INSERT INTO EXAMPLE ( NAME, DESCRIPTION ) VALUES ( 'TEST', 'TEST' )");
	}
	// END SNIPPET: createDatabase

}