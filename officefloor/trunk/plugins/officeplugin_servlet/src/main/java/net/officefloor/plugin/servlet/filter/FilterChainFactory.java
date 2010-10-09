/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * Constructs a {@link FilterChain}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterChainFactory {

	/**
	 * <p>
	 * Constructs a {@link FilterChain} to a target {@link FilterChain} to allow
	 * create chains of chains.
	 * <p>
	 * The {@link FilterChain} is to be constructed with {@link Filter}
	 * instances specific to the {@link Office}.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param mapping
	 *            {@link ServicerMapping}.
	 * @param mappingType
	 *            {@link MappingType}.
	 * @param target
	 *            Target as last node in constructed the {@link FilterChain}.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}. Typical implementation will have
	 *            this provided from the {@link HttpServletContainer} so that
	 *            filtering will be using the same {@link OfficeServletContext}
	 *            as {@link Servlet} execution.
	 * @return {@link FilterChain}.
	 * @throws ServletException
	 *             If fails to create {@link FilterChain}.
	 */
	FilterChain createFilterChain(Office office, ServicerMapping mapping,
			MappingType mappingType, FilterChain target,
			OfficeServletContext officeServletContext) throws ServletException;

}