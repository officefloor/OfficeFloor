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
package net.officefloor.tutorial.resthttpserver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Setup database.
 * 
 * @author Daniel Sagenschneider
 */
public class Setup {

	public void setup(Connection connection) throws SQLException {
		try {
			connection.createStatement().executeQuery("SELECT * FROM VEHICLE");
			return; // table exists
		} catch (SQLException exCreate) {
			try {
				connection.createStatement()
						.executeUpdate("CREATE TABLE VEHICLE ( ID IDENTITY, VEHICLE_TYPE VARCHAR(10), WHEELS INT)");
			} catch (SQLException exConcurrentSetup) {
				if (!exConcurrentSetup.getMessage().contains("Table \"VEHICLE\" already exists")) {
					throw exConcurrentSetup; // failure creating table
				}
			}
		}
	}

}