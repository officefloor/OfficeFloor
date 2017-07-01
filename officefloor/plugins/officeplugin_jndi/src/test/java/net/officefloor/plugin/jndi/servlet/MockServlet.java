/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.jndi.context.ValidateManagedFunction;

/**
 * {@link Servlet} to test running an {@link OfficeFloor} via JNDI.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServlet extends HttpServlet {

	/*
	 * ================= HttpServlet =========================
	 */

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean isTaskInvoked;
		try {
			// Create the initial context
			InitialContext initialContext = new InitialContext();

			// Obtain the OfficeFloor
			OfficeFloor officeFloor = (OfficeFloor) initialContext
					.lookup(ValidateManagedFunction.getOfficeFloorJndiName(false));

			// Invoke the task
			ValidateManagedFunction.reset();
			ValidateManagedFunction.invokeFunction(officeFloor, null);

			// Obtain whether the task was invoked
			isTaskInvoked = ValidateManagedFunction.isFunctionInvoked();

		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		// Provide response
		PrintWriter body = response.getWriter();
		body.print(String.valueOf(isTaskInvoked));
		body.close();
	}

}