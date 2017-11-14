/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.test;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;

/**
 * Context for the {@link CompileWebExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWebContext extends CompileOfficeContext {

	/**
	 * Obtains the {@link WebArchitect}.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Convenience method to link a URI to the <code>service</code> method of
	 * the {@link Class}.
	 * 
	 * @param applicationPath
	 *            Application path.
	 * @param isSecure
	 *            Indicates if secure.
	 * @param sectionClass
	 *            {@link Class} containing a <code>service</code> method.
	 * @return {@link HttpUrlContinuation}.
	 */
	HttpUrlContinuation link(boolean isSecure, String applicationPath, Class<?> sectionClass);

	/**
	 * Convenience method to link a URL to the <code>service</code> method of
	 * the {@link Class}.
	 * 
	 * @param isSecure
	 *            Indicates if secure.
	 * @param httpMethod
	 *            {@link HttpMethod}.
	 * @param applicationPath
	 *            Application path.
	 * @param sectionClass
	 *            {@link Class} containing a <code>service</code> method.
	 * @return {@link HttpInput}.
	 */
	HttpInput link(boolean isSecure, HttpMethod httpMethod, String applicationPath, Class<?> sectionClass);

}