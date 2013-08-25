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
import javax.servlet.http.HttpServlet;

import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Servicer for a {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterServicer {

	/**
	 * Obtains the URL pattern matching path to the {@link Filter} Container.
	 * 
	 * @return URL pattern. May be <code>null</code> if being matched on
	 *         {@link #getServletName()}.
	 */
	String getFilterMapping();

	/**
	 * Obtains the name of {@link HttpServlet} to apply the {@link Filter}.
	 * 
	 * @return Name of {@link HttpServlet}. May be <code>null</code> if being
	 *         matched on {@link #getFilterMapping()}.
	 */
	String getServletName();

	/**
	 * Obtains the {@link MappingType} instances on which this
	 * {@link FilterServicer} is applicable.
	 * 
	 * @return {@link MappingType} instances.
	 */
	MappingType[] getMappingTypes();

	/**
	 * Obtains the {@link FilterContainerFactory}.
	 * 
	 * @return {@link FilterContainerFactory}.
	 */
	FilterContainerFactory getFilterContainerFactory();

}