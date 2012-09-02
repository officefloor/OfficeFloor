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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.tutorials.performance.pool.PoolSingleton;
import net.officefloor.tutorials.performance.pool.PooledDataSource.Connection;

/**
 * {@link HttpServlet} servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletServicer extends HttpServlet {

	/**
	 * Allow hook for profiling.
	 */
	public static volatile Runnable runnable = null;

	/*
	 * ===================== HttpServlet ==============================
	 */

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// Indicate servicing
		if (runnable != null) {
			runnable.run();
		}

		String queryString = req.getQueryString();
		char value = queryString.charAt(queryString.length() - 1);
		if (value == 'N') {
			// News feed
			resp.getWriter().write('n');

		} else {
			// Simulate database interaction
			try {
				Connection connection = null;
				try {
					connection = PoolSingleton.getPooledDataSource()
							.getConnection();
					Thread.sleep(100);
				} finally {
					if (connection != null) {
						connection.close();
					}
				}
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			resp.getWriter().write('d');
		}
	}

}