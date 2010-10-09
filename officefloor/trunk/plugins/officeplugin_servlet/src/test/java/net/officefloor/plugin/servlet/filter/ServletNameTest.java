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
import javax.servlet.Servlet;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Tests {@link Servlet} name for {@link FilterChainFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletNameTest extends AbstractFilterChainFactoryTestCase {

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * Ensure no filtering on no {@link Filter} instances.
	 */
	public void testNoFiltering() {
		this.record_doFilter(this.office);
		this.doFilter(this.office, "/not/matched", null, MappingType.REQUEST,
				"NotMatched");
	}

	/**
	 * Ensure appropriately not matches on {@link Servlet} name.
	 */
	public void testNoMatch() {
		this.addServicer("Filter", null, "Different Servlet Name");
		this.record_init(this.office, "Filter");
		this.record_doFilter(this.office);
		this.doFilter(this.office, "/not/matched", null, MappingType.REQUEST,
				"No Servlet Name Match");
	}

	/**
	 * Ensure can match on {@link Servlet} name.
	 */
	public void testSingleServletNameMatch() {
		this.doSingleFilterTest("Servlet", "/ignored", null,
				MappingType.REQUEST, this.office, null, "Servlet");
	}

	/**
	 * Ensure can match on multiple {@link Filter} instances.
	 */
	public void testMultipleServletNameMatch() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("FilterOne", null, SERVLET_NAME);
		this.addServicer("FilterTwo", null, SERVLET_NAME);
		this.record_init(this.office, "FilterOne", "FilterTwo");
		this.record_doFilter(this.office, "FilterOne", "FilterTwo");
		this.doFilter(this.office, "/not/matched", null, MappingType.REQUEST,
				SERVLET_NAME);
	}

	/**
	 * Ensure can match selectively {@link Filter} instances.
	 */
	public void testSelectiveServletNameMatch() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("FilterOne", null, SERVLET_NAME);
		this.addServicer("FilterTwo", null, "Another Servlet");
		this.addServicer("FilterThree", null, SERVLET_NAME);
		this.record_init(this.office, "FilterOne", "FilterTwo", "FilterThree");
		this.record_doFilter(this.office, "FilterOne", "FilterThree");
		this.doFilter(this.office, "/not/matched", null, MappingType.REQUEST,
				SERVLET_NAME);
	}

	/**
	 * Ensure that matches on {@link MappingType}.
	 */
	public void testMappingType() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("RequestFilter", null, SERVLET_NAME,
				MappingType.REQUEST);
		this.addServicer("ForwardFilter", null, SERVLET_NAME,
				MappingType.FORWARD);
		this.addServicer("IncludeFilter", null, SERVLET_NAME,
				MappingType.INCLUDE);
		this.record_init(this.office, "RequestFilter", "ForwardFilter",
				"IncludeFilter");
		this.record_doFilter(this.office, "IncludeFilter");
		this.record_doFilter(this.office, "ForwardFilter");
		this.record_doFilter(this.office, "RequestFilter");
		this.doFilter(this.office, "/include", null, MappingType.INCLUDE,
				SERVLET_NAME);
		this.doFilter(this.office, "/forward", null, MappingType.FORWARD,
				SERVLET_NAME);
		this.doFilter(this.office, "/request", null, MappingType.REQUEST,
				SERVLET_NAME);
	}

	/**
	 * Ensure that matches on multiple {@link MappingType} instances for the
	 * {@link Filter}.
	 */
	public void testMultipleMappingTypesForFilter() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("Filter", null, SERVLET_NAME, MappingType.REQUEST,
				MappingType.FORWARD, MappingType.INCLUDE);
		this.record_init(this.office, "Filter");
		this.record_doFilter(this.office, "Filter");
		this.record_doFilter(this.office, "Filter");
		this.record_doFilter(this.office, "Filter");
		this.doFilter(this.office, "/include", null, MappingType.INCLUDE,
				SERVLET_NAME);
		this.doFilter(this.office, "/forward", null, MappingType.FORWARD,
				SERVLET_NAME);
		this.doFilter(this.office, "/request", null, MappingType.REQUEST,
				SERVLET_NAME);
	}

	/**
	 * Ensure {@link Filter} instances are isolated to an {@link Office}.
	 */
	public void testDifferentOffices() {
		final Office one = this.createMock(Office.class);
		final Office two = this.createMock(Office.class);
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("Filter", null, SERVLET_NAME);
		this.record_init(one, "Filter");
		this.record_doFilter(one, "Filter");
		this.record_init(two, "Filter");
		this.record_doFilter(two, "Filter");
		this.doFilter(one, "/office/one", null, MappingType.REQUEST,
				SERVLET_NAME);
		this.doFilter(two, "/office/two", null, MappingType.REQUEST,
				SERVLET_NAME);
	}

}