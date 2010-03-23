/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.example.ejborchestration;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Start up {@link Servlet} to initiate the state of the example application.
 * 
 * @author Daniel Sagenschneider
 */
public class StartupServlet extends HttpServlet {

	/**
	 * {@link Customer}.
	 */
	public static Customer customer;

	/**
	 * {@link Product} instances.
	 */
	public static List<Product> products = new LinkedList<Product>();

	/*
	 * ======================= HttpServlet =========================
	 */

	@Override
	public String getServletInfo() {
		return "Startup Servlet to initialise the state of the example application";
	}

	@Override
	public void init() throws ServletException {

		// Create customer
		try {
			SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
			customer = sales.createCustomer("daniel@officefloor.net", "Daniel");
		} catch (CustomerExistsException ex) {
			throw new ServletException(ex);
		}

		// Create the products
		ProductCatalogLocal catalog = ActionUtil
				.lookupService(ProductCatalogLocal.class);
		products.add(catalog.createProduct("Shirt", 19.00));
		products.add(catalog.createProduct("Trousers", 25.00));
		products.add(catalog.createProduct("Hat", 7.00));
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.write("<HTML><BODY>Startup Servlet</BODY></HTML>");
	}

}