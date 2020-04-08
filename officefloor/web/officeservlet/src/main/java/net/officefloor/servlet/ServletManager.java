/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import javax.servlet.Filter;
import javax.servlet.Servlet;

/**
 * Manager of {@link Servlet} instances for {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletManager {

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param name         Name of {@link Servlet}.
	 * @param servletClass {@link Servlet} {@link Class}.
	 * @return {@link ServletServicer}.
	 */
	ServletServicer addServlet(String name, Class<? extends Servlet> servletClass);

	/**
	 * Adds a {@link Filter}.
	 * 
	 * @param name        Name of {@link Filter}.
	 * @param filterClass {@link Filter} {@link Class}.
	 * @return {@link FilterServicer}.
	 */
	FilterServicer addFilter(String name, Class<? extends Filter> filterClass);

}
