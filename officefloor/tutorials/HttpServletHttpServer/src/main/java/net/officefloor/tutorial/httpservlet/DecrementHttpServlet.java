/*-
 * #%L
 * Embedding WoOF within HttpServlet container
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.httpservlet;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HttpServlet} for testing.
 * 
 * @author Daniel Sagenschneider
 */
@WebServlet("/servlet")
public class DecrementHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * =============== HttpServlet ========================
	 */

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Read in content
		StringWriter buffer = new StringWriter();
		Reader entity = req.getReader();
		for (int character = entity.read(); character != -1; character = entity.read()) {
			buffer.write(character);
		}

		// Obtain value
		int value = Integer.parseInt(buffer.toString());

		// Return decremented value
		resp.getWriter().write(String.valueOf(value - 1));
	}

}
