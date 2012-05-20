/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import javax.sql.DataSource;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class Setup {

	public void setupDatabase(DataSource dataSource) throws Exception {

		Connection connection = dataSource.getConnection();
		try {
			connection
					.createStatement()
					.execute(
							"CREATE TABLE EXAMPLE ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20), DESCRIPTION VARCHAR(256) )");
			connection
					.createStatement()
					.execute(
							"INSERT INTO EXAMPLE ( NAME, DESCRIPTION ) VALUES ( 'WoOF', 'Web on OfficeFloor' )");
		} finally {
			connection.close();
		}
	}

}
// END SNIPPET: example