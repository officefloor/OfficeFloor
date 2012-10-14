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

package net.officefloor.plugin.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

/**
 * <p>
 * {@link HttpServlet} to service {@link HttpServletRequest} instances via
 * {@link WebAutoWireApplication}.
 * <p>
 * To use this class:
 * <ol>
 * <li>Create an implementation</li>
 * <li>Configure the implementation into the {@link Servlet} application as a
 * {@link ServletContextListener}.</li>
 * </ol>
 * <p>
 * Note that the implementation requires to be configured as a
 * {@link ServletContextListener} (rather than a {@link Servlet}) as it
 * configures itself with its appropriate request handling mappings.
 * <p>
 * Please also note that the instance used for the
 * {@link ServletContextListener} is not the instance used as the
 * {@link HttpServlet}. This is necessary as a new instance of the
 * {@link OfficeFloorServlet} implementation is created by the {@link Servlet}
 * container to allow it to manage dependency injection of resources.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFloorServlet extends HttpServlet implements
		ServletContextListener {

	/**
	 * Obtains the name to register this {@link Servlet}.
	 * 
	 * @return Name to register this {@link Servlet}.
	 */
	public abstract String getServletName();

	/**
	 * Provides configuration of the {@link WebAutoWireApplication} for this
	 * {@link OfficeFloorServlet}.
	 * 
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @param servletContext
	 *            {@link ServletContext}.
	 * @return <code>true</code> to load the {@link OfficeFloorServlet}.
	 *         <code>false</code> does not configure it.
	 * @throws Exception
	 *             If fails to configure.
	 */
	public abstract boolean configure(WebAutoWireApplication application,
			ServletContext servletContext) throws Exception;

	/**
	 * {@link ServletWebAutoWireApplication} for this {@link OfficeFloorServlet}
	 * instance.
	 */
	@SuppressWarnings("rawtypes")
	private ServletWebAutoWireApplication application;

	/*
	 * ================== ServletContextListener =====================
	 */

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Configure this Servlet
		ServletWebAutoWireApplication
				.configure(this, event.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Do nothing as should just be garbage collected
	}

	/*
	 * ======================= HttpServlet ===========================
	 */

	@Override
	public String getServletInfo() {
		return OfficeFloorServlet.class.getSimpleName()
				+ " registered under name " + this.getServletName()
				+ " and implemented by " + this.getClass().getName();
	}

	@Override
	public void init() throws ServletException {
		// Initialise and obtain the source for this Servlet
		this.application = ServletWebAutoWireApplication.initiate(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Service the request
		boolean isServiced = this.application.service(this, request, response);

		// Fail if not handled
		if (!isServiced) {
			throw new ServletException("HTTP request "
					+ request.getRequestURI() + " is not handled by "
					+ this.getServletName() + " "
					+ Servlet.class.getSimpleName());
		}
	}

	@Override
	public void destroy() {
		// Destroy the source
		if (this.application != null) {
			this.application.destroy();
		}
	}

}