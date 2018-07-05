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
package net.officefloor.tutorial.transactionhttpserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Setup {

	public void setupDatabase(DataSource dataSource) throws SQLException {

		Connection connection = dataSource.getConnection();
		try {

			// Create the user name table
			connection
					.createStatement()
					.execute(
							"CREATE TABLE USER ( ID IDENTITY PRIMARY KEY, USERNAME VARCHAR(20) NOT NULL )");

			// Add user
			connection.createStatement().execute(
					"INSERT INTO USER ( USERNAME) VALUES ( 'daniel' )");

			// Create the person details table
			connection
					.createStatement()
					.execute(
							"CREATE TABLE PERSON ( ID IDENTITY PRIMARY KEY, USER_ID INTEGER NOT NULL, FULLNAME VARCHAR(50) NOT NULL, "
									+ " CONSTRAINT USER_FK FOREIGN KEY ( USER_ID ) REFERENCES USER ( ID ) )");

			// Add person
			ResultSet results = connection.createStatement().executeQuery(
					"SELECT ID FROM USER WHERE USERNAME = 'daniel'");
			results.next();
			long userId = results.getLong("ID");
			connection.createStatement().execute(
					"INSERT INTO PERSON ( USER_ID, FULLNAME ) VALUES ( " + userId
							+ ", 'Daniel Sagenschneider' )");

		} finally {
			connection.close();
		}
	}

}
// END SNIPPET: tutorial