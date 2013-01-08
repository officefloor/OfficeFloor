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

import javax.servlet.Filter;
import javax.servlet.ServletException;

import net.officefloor.frame.api.manage.Office;

/**
 * Constructs the {@link FilterContainer} for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterContainerFactory {

	/**
	 * <p>
	 * Creates the {@link FilterContainer} for the {@link Office}.
	 * <p>
	 * Typically this will create a singleton {@link FilterContainer} for the
	 * {@link Office} so that there is only the single {@link Filter} instances
	 * per {@link Office}.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return {@link FilterContainer} for the {@link Office}.
	 * @throws ServletException
	 *             If fails to initialise the {@link Filter}.
	 */
	FilterContainer createFilterContainer(Office office)
			throws ServletException;

}