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
package net.officefloor.tutorials.performance.logic;

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

	/**
	 * Allow hook for profiling.
	 */
	public static volatile Runnable runnable = null;

	@FlowInterface
	private static interface Flows {
		void database(char value);
	}

	public void news(ServerHttpConnection connection, Flows flows)
			throws IOException {

		// Indicate servicing
		if (runnable != null) {
			runnable.run();
		}

		String requestUri = connection.getHttpRequest().getRequestURI();
		char value = requestUri.charAt(requestUri.length() - 1);
		if (value == 'N') {
			// News feed
			connection.getHttpResponse().getEntity().write((byte) 'n');
			return;
		}

		// Database
		flows.database(value);
	}

	public void database(@Parameter char value, ServerHttpConnection conn,
			PooledDataSource dataSource) throws InterruptedException,
			SQLException, IOException {

		// Simulate database interaction
		Connection connection = dataSource.getConnection();
		try {
			Thread.sleep(100);
		} finally {
			connection.close();
		}
		conn.getHttpResponse().getEntity().write((byte) 'd');
	}

}