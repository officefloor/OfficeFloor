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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Ensure constructs appropriate {@link FilterChain} with a combination of
 * {@link Filter} mapping and {@link Servlet} name matching.
 * 
 * @author Daniel Sagenschneider
 */
public class CombinationChainTest extends AbstractFilterChainFactoryTestCase {

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * Ensure appropriate ordering for combination (filter mapping before
	 * {@link Servlet} name).
	 */
	public void testOrder() {
		final String PATH = "/path";
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("FilterServlet", null, SERVLET_NAME);
		this.addServicer("FilterPath", PATH, null);
		this.record_init(this.office, "FilterServlet", "FilterPath");
		this.record_doFilter(this.office, "FilterPath", "FilterServlet");
		this.doFilter(this.office, PATH, null, MappingType.REQUEST,
				SERVLET_NAME);
	}

	/**
	 * Ensure {@link Filter} used only once in {@link FilterChain}.
	 */
	public void testUseFilterOnlyOnce() {
		final String PATH = "/path";
		final String SERVLET_NAME = "Servlet Name";
		final FilterContainerFactory factory = this.createFactory("Filter");
		this.addServicer(factory, null, SERVLET_NAME);
		this.addServicer(factory, PATH, null);
		this.record_init(this.office, "Filter");
		this.record_doFilter(this.office, "Filter");
		this.doFilter(this.office, PATH, null, MappingType.REQUEST,
				SERVLET_NAME);
	}

}