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
package net.officefloor.plugin.servlet.filter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * {@link FilterContainerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterContainerFactoryImpl implements FilterContainerFactory {

	/**
	 * <p>
	 * {@link FilterContainer} instances by their {@link Office}.
	 * <p>
	 * Typically expecting only a single {@link Office}.
	 */
	private final Map<Office, FilterContainer> containers = new HashMap<Office, FilterContainer>(
			1);

	/**
	 * {@link Filter} name.
	 */
	private final String filterName;

	/**
	 * {@link Class} of the {@link Filter}.
	 */
	private final Class<? extends Filter> filterClass;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext;

	/**
	 * Initiate.
	 * 
	 * @param filterName
	 *            {@link Filter} name.
	 * @param filterClass
	 *            {@link Class} of the {@link Filter}.
	 * @param initParameters
	 *            Init parameters.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 */
	public FilterContainerFactoryImpl(String filterName,
			Class<? extends Filter> filterClass,
			Map<String, String> initParameters,
			OfficeServletContext officeServletContext) {
		this.filterName = filterName;
		this.filterClass = filterClass;
		this.initParameters = initParameters;
		this.officeServletContext = officeServletContext;
	}

	/*
	 * ================= FilterContainerFactory ========================
	 */

	@Override
	public synchronized FilterContainer createFilterContainer(Office office)
			throws ServletException {

		// Lazy create container for office
		FilterContainer container = this.containers.get(office);
		if (container == null) {

			// Instantiate the filter
			Filter filter;
			try {
				filter = this.filterClass.newInstance();
			} catch (Exception ex) {
				throw new ServletException(ex);
			}

			// Create the container
			container = new FilterContainerImpl(this.filterName, filter,
					this.initParameters, this.officeServletContext, office);

			// Register the container for the office
			this.containers.put(office, container);
		}

		// Return the container
		return container;
	}

}