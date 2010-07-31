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
package net.officefloor.plugin.servlet.dispatch;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;

/**
 * Factory for the creation of a {@link RequestDispatcher}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestDispatcherFactory {

	/**
	 * Creates the {@link RequestDispatcher} for the path.
	 * 
	 * @param path
	 *            Path.
	 * @return {@link RequestDispatcher} or <code>null</code> if can not create
	 *         {@link RequestDispatcher} for the path.
	 */
	RequestDispatcher createRequestDispatcher(String path);

	/**
	 * Creates the {@link RequestDispatcher} for the named {@link Servlet}.
	 * 
	 * @param name
	 *            Name of {@link Servlet}.
	 * @return {@link RequestDispatcher} for named {@link Servlet} or
	 *         <code>null</code> if no {@link Servlet} by name.
	 */
	RequestDispatcher createNamedDispatcher(String name);

}