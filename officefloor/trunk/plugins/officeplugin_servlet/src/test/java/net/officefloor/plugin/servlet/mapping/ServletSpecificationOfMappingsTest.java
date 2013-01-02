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
package net.officefloor.plugin.servlet.mapping;

import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.container.HttpServletServicer;

/**
 * Tests the {@link ServicerMapper} as per &quot;Specification Of Mappings&quot;
 * of the {@link Servlet} specification.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletSpecificationOfMappingsTest extends
		AbstractServicerMapperTestCase {

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer one = new MockServicer("servlet1", "/foo/bar/*");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer two = new MockServicer("servlet2", "/baz/*");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer three = new MockServicer("servlet3", "/catalog");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer four = new MockServicer("servlet4", "*.bop");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer any = new MockServicer("default", "/");

	/**
	 * {@link ServicerMapper} to test.
	 */
	private final ServicerMapper mapper = new ServicerMapperImpl(this.one,
			this.two, this.three, this.four, this.any);

	/**
	 * Ensure maps /foo/bar/index.html .
	 */
	public void test_foo_bar_index_html() {
		ServicerMapping mapping = this.mapper.mapPath("/foo/bar/index.html");
		assertMapping(mapping, this.one, "/foo/bar", "/index.html", null);
	}

	/**
	 * Ensure maps /foo/bar/index.bop .
	 */
	public void test_foo_bar_index_bop() {
		ServicerMapping mapping = this.mapper.mapPath("/foo/bar/index.bop");
		assertMapping(mapping, this.one, "/foo/bar", "/index.bop", null);
	}

	/**
	 * Ensure maps /baz .
	 */
	public void test_baz() {
		ServicerMapping mapping = this.mapper.mapPath("/baz");
		assertMapping(mapping, this.two, "/baz", null, null);
	}

	/**
	 * Ensure maps /baz/index.html .
	 */
	public void test_baz_index_html() {
		ServicerMapping mapping = this.mapper.mapPath("/baz/index.html");
		assertMapping(mapping, this.two, "/baz", "/index.html", null);
	}

	/**
	 * Ensure maps /catalog .
	 */
	public void test_catalog() {
		ServicerMapping mapping = this.mapper.mapPath("/catalog");
		assertMapping(mapping, this.three, "/catalog", null, null);
	}

	/**
	 * Ensure maps /catalog/index.html .
	 */
	public void test_catalog_index_html() {
		ServicerMapping mapping = this.mapper.mapPath("/catalog/index.html");
		assertMapping(mapping, this.any, "", "/catalog/index.html", null);
	}

	/**
	 * Ensure maps /catalog/index.bop .
	 */
	public void test_catalog_index_bop() {
		ServicerMapping mapping = this.mapper.mapPath("/catalog/index.bop");
		assertMapping(mapping, this.four, "/catalog/index.bop", null, null);
	}

	/**
	 * Ensure maps /index.bop .
	 */
	public void test_index_bop() {
		ServicerMapping mapping = this.mapper.mapPath("/index.bop");
		assertMapping(mapping, this.four, "/index.bop", null, null);
	}

}