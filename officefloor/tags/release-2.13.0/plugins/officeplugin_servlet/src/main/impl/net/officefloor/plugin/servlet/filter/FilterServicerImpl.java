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
import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * {@link FilterServicer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterServicerImpl implements FilterServicer {

	/**
	 * {@link Filter} mapping.
	 */
	private final String filterMapping;

	/**
	 * {@link Servlet} name.
	 */
	private final String servletName;

	/**
	 * {@link MappingType} instances.
	 */
	private final MappingType[] mappingTypes;

	/**
	 * {@link FilterContainerFactory}.
	 */
	private final FilterContainerFactory factory;

	/**
	 * Initiate.
	 * 
	 * @param filterMapping
	 *            {@link Filter} mapping.
	 * @param servletName
	 *            {@link Servlet} name.
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 * @param factory
	 *            {@link FilterContainerFactory}.
	 */
	public FilterServicerImpl(String filterMapping, String servletName,
			MappingType[] mappingTypes, FilterContainerFactory factory) {
		this.filterMapping = filterMapping;
		this.servletName = servletName;
		this.mappingTypes = mappingTypes;
		this.factory = factory;
	}

	/*
	 * =================== FilterServicer =========================
	 */

	@Override
	public String getFilterMapping() {
		return this.filterMapping;
	}

	@Override
	public String getServletName() {
		return this.servletName;
	}

	@Override
	public MappingType[] getMappingTypes() {
		return this.mappingTypes;
	}

	@Override
	public FilterContainerFactory getFilterContainerFactory() {
		return this.factory;
	}

}