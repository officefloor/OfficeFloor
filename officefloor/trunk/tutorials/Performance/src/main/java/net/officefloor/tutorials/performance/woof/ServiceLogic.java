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
package net.officefloor.tutorials.performance.woof;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.tutorials.performance.pool.PooledDataSource;
import net.officefloor.tutorials.performance.pool.PooledDataSource.Connection;

/**
 * Logic to service the database request.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceLogic {

	@FlowInterface
	private static interface Flows {
		void insert(char value);
	}

	public void validate(ServerHttpConnection connection, Flows flows)
			throws IOException {

		// Check if valid request
		String requestUri = connection.getHttpRequest().getRequestURI();
		char value = requestUri.charAt(requestUri.length() - 1);
		if (value == 'N') {
			// Send response of invalid request
			connection.getHttpResponse().getBody().getOutputStream()
					.write((byte) 'n');
			return;
		}

		// Valid request so insert
		flows.insert(value);
	}

	public void insert(@Parameter char value, ServerHttpConnection conn,
			PooledDataSource dataSource) throws InterruptedException,
			SQLException, IOException {

		// Obtain connection
		Connection connection = dataSource.getConnection();
		try {

			// Simulate database interaction
			Thread.sleep(1);

		} finally {
			connection.close();
		}

		// Send response of valid request
		conn.getHttpResponse().getBody().getOutputStream().write((byte) 'y');
	}

}