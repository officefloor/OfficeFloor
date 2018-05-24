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
package net.officefloor.plugin.servlet.container;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <p>
 * Made available by the {@link HttpServletContainer} within the
 * {@link ServletRequest#getAttribute(String)}.
 * <p>
 * This allows the implementation of {@link RequestDispatcher} for the
 * {@link ServletContext}. It is also available to {@link HttpServlet}
 * implementations managed within the {@link HttpServletContainer} to utilise
 * {@link OfficeFloor} capabilities.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletRequestForwarder {

	/**
	 * Name of attribute in the {@link ServletRequest} containing this
	 * {@link ServletRequestForwarder}.
	 */
	String ATTRIBUTE_FORWARDER = "#net.officefloor.servlet.request.forwarder#";

	/**
	 * Forwards the {@link ServletRequest}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction} to forward the
	 *            {@link ServletRequest}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @throws ServletException
	 *             If fails to forward the {@link ServletRequest}.
	 */
	void forward(String functionName, Object parameter) throws ServletException;

}