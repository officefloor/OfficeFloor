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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * Constructs a {@link FilterChain}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterChainFactory {

	/**
	 * Constructs a {@link FilterChain} to a target {@link FilterChain} to allow
	 * create chains of chains.
	 * 
	 * @param mapping
	 *            {@link ServicerMapping}.
	 * @param mappingType
	 *            {@link MappingType}.
	 * @param target
	 *            Target as last node in constructed the {@link FilterChain}.
	 * @return {@link FilterChain}.
	 * @throws ServletException
	 *             If fails to create {@link FilterChain}.
	 */
	FilterChain createFilterChain(ServicerMapping mapping,
			MappingType mappingType, FilterChain target)
			throws ServletException;
}