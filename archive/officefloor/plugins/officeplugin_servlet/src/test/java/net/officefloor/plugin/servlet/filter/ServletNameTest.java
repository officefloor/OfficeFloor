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
 * Tests {@link Servlet} name for {@link FilterChainFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletNameTest extends AbstractFilterChainFactoryTestCase {

	/**
	 * Ensure no filtering on no {@link Filter} instances.
	 */
	public void testNoFiltering() {
		this.record_doFilter();
		this.doFilter("/not/matched", null, MappingType.REQUEST, "NotMatched");
	}

	/**
	 * Ensure appropriately not matches on {@link Servlet} name.
	 */
	public void testNoMatch() {
		this.addServicer("Filter", null, "Different Servlet Name");
		this.record_init("Filter");
		this.record_doFilter();
		this.doFilter("/not/matched", null, MappingType.REQUEST,
				"No Servlet Name Match");
	}

	/**
	 * Ensure can match on {@link Servlet} name.
	 */
	public void testSingleServletNameMatch() {
		this.doSingleFilterTest("Servlet", "/ignored", null,
				MappingType.REQUEST, null, "Servlet");
	}

	/**
	 * Ensure can match on multiple {@link Filter} instances.
	 */
	public void testMultipleServletNameMatch() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("FilterOne", null, SERVLET_NAME);
		this.addServicer("FilterTwo", null, SERVLET_NAME);
		this.record_init("FilterOne", "FilterTwo");
		this.record_doFilter("FilterOne", "FilterTwo");
		this.doFilter("/not/matched", null, MappingType.REQUEST, SERVLET_NAME);
	}

	/**
	 * Ensure can match selectively {@link Filter} instances.
	 */
	public void testSelectiveServletNameMatch() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("FilterOne", null, SERVLET_NAME);
		this.addServicer("FilterTwo", null, "Another Servlet");
		this.addServicer("FilterThree", null, SERVLET_NAME);
		this.record_init("FilterOne", "FilterTwo", "FilterThree");
		this.record_doFilter("FilterOne", "FilterThree");
		this.doFilter("/not/matched", null, MappingType.REQUEST, SERVLET_NAME);
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
		this.record_init("RequestFilter", "ForwardFilter", "IncludeFilter");
		this.record_doFilter("IncludeFilter");
		this.record_doFilter("ForwardFilter");
		this.record_doFilter("RequestFilter");
		this.doFilter("/include", null, MappingType.INCLUDE, SERVLET_NAME);
		this.doFilter("/forward", null, MappingType.FORWARD, SERVLET_NAME);
		this.doFilter("/request", null, MappingType.REQUEST, SERVLET_NAME);
	}

	/**
	 * Ensure that matches on multiple {@link MappingType} instances for the
	 * {@link Filter}.
	 */
	public void testMultipleMappingTypesForFilter() {
		final String SERVLET_NAME = "Servlet Name";
		this.addServicer("Filter", null, SERVLET_NAME, MappingType.REQUEST,
				MappingType.FORWARD, MappingType.INCLUDE);
		this.record_init("Filter");
		this.record_doFilter("Filter");
		this.record_doFilter("Filter");
		this.record_doFilter("Filter");
		this.doFilter("/include", null, MappingType.INCLUDE, SERVLET_NAME);
		this.doFilter("/forward", null, MappingType.FORWARD, SERVLET_NAME);
		this.doFilter("/request", null, MappingType.REQUEST, SERVLET_NAME);
	}

}