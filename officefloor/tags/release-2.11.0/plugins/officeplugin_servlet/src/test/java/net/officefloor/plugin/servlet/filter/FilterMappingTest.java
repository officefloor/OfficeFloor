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
import javax.servlet.FilterChain;

import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Tests {@link Filter} mapping for {@link FilterChainFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterMappingTest extends AbstractFilterChainFactoryTestCase {

	/**
	 * Ensure not filter if no filters.
	 */
	public void test_none() {
		this.record_init();
		this.record_doFilter();
		this.doFilter("/path", null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure not filter if not exact match on path.
	 */
	public void test_exact_NoMatch() {
		this.addServicer("Filter", "/exact/path", null);
		this.record_init("Filter");
		this.record_doFilter();
		this.doFilter("/not/match", null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure exact match on path.
	 */
	public void test_exact_Match() {
		this.doSingleFilterTest("Servlet", "/exact/path", null,
				MappingType.REQUEST, "/exact/path", null);
	}

	/**
	 * Ensure exact have multiple match on exact path.
	 */
	public void test_exact_MultipleMatch() {
		final String PATH = "/exact/path";
		this.addServicer("FilterOne", PATH, null);
		this.addServicer("FilterTwo", PATH, null);
		this.record_init("FilterOne", "FilterTwo");
		this.record_doFilter("FilterOne", "FilterTwo");
		this.doFilter(PATH, null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure exact have selective match on exact path.
	 */
	public void test_exact_SelectiveMatch() {
		final String PATH = "/exact/path";
		this.addServicer("FilterOne", PATH, null);
		this.addServicer("FilterTwo", "/different/path", null);
		this.addServicer("FilterThree", PATH, null);
		this.record_init("FilterOne", "FilterTwo", "FilterThree");
		this.record_doFilter("FilterOne", "FilterThree");
		this.doFilter(PATH, null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure that matches on {@link MappingType}.
	 */
	public void test_exact_MappingType() {
		final String PATH = "/exact/path";
		this.addServicer("RequestFilter", PATH, null, MappingType.REQUEST);
		this.addServicer("ForwardFilter", PATH, null, MappingType.FORWARD);
		this.addServicer("IncludeFilter", PATH, null, MappingType.INCLUDE);
		this.record_init("RequestFilter", "ForwardFilter", "IncludeFilter");
		this.record_doFilter("IncludeFilter");
		this.record_doFilter("ForwardFilter");
		this.record_doFilter("RequestFilter");
		this.doFilter(PATH, null, MappingType.INCLUDE, "Include");
		this.doFilter(PATH, null, MappingType.FORWARD, "Forward");
		this.doFilter(PATH, null, MappingType.REQUEST, "Request");
	}

	/**
	 * Ensure {@link Filter} used only once in {@link FilterChain} on exact
	 * path.
	 */
	public void test_exact_UseFilterOnlyOnce() {
		final String PATH = "/exact/path";
		final FilterContainerFactory factory = this.createFactory("Filter");
		this.addServicer(factory, PATH, null);
		this.addServicer(factory, PATH, null);
		this.record_init("Filter");
		this.record_doFilter("Filter");
		this.doFilter(PATH, null, MappingType.REQUEST, "FilterOnlyOnce");
	}

	/**
	 * Ensure can match with <code>null</code> path info.
	 */
	public void test_path_NullPathInfo() {
		this.doSingleFilterTest("Servlet", "/servlet/path", null,
				MappingType.REQUEST, "/servlet/path/*", null);
	}

	/**
	 * Ensure can match with path info.
	 */
	public void test_path_WithPathInfo() {
		this.doSingleFilterTest("Servlet", "/servlet", "/path",
				MappingType.REQUEST, "/servlet/path/*", null);
	}

	/**
	 * Ensure not match if path too short.
	 */
	public void test_path_PathTooShort() {
		this.addServicer("Filter", "/path/that/is/longer/*", null);
		this.record_init("Filter");
		this.record_doFilter();
		this.doFilter("/path", null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure can match if path is exact.
	 */
	public void test_path_PathExact() {
		this.doSingleFilterTest("Servlet", "/exact", "/path/",
				MappingType.REQUEST, "/exact/path/*", null);
	}

	/**
	 * Ensure can match if path is longer.
	 */
	public void test_path_PathLonger() {
		this.doSingleFilterTest("Servlet", "/path/longer/than/mapping", null,
				MappingType.REQUEST, "/path/*", null);
	}

	/**
	 * Ensure exact have multiple match on path.
	 */
	public void test_path_MultipleMatch() {
		final String PATH = "/path";
		this.addServicer("FilterOne", PATH + "/*", null);
		this.addServicer("FilterTwo", PATH + "/*", null);
		this.record_init("FilterOne", "FilterTwo");
		this.record_doFilter("FilterOne", "FilterTwo");
		this.doFilter(PATH, null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure exact have selective match on path.
	 */
	public void test_path_SelectiveMatch() {
		final String PATH = "/path";
		this.addServicer("FilterOne", PATH + "/*", null);
		this.addServicer("FilterTwo", "/different/path/*", null);
		this.addServicer("FilterThree", PATH + "/*", null);
		this.record_init("FilterOne", "FilterTwo", "FilterThree");
		this.record_doFilter("FilterOne", "FilterThree");
		this.doFilter(PATH, null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure that matches on {@link MappingType}.
	 */
	public void test_path_MappingType() {
		final String PATH = "/path";
		this.addServicer("RequestFilter", PATH + "/*", null,
				MappingType.REQUEST);
		this.addServicer("ForwardFilter", PATH + "/*", null,
				MappingType.FORWARD);
		this.addServicer("IncludeFilter", PATH + "/*", null,
				MappingType.INCLUDE);
		this.record_init("RequestFilter", "ForwardFilter", "IncludeFilter");
		this.record_doFilter("IncludeFilter");
		this.record_doFilter("ForwardFilter");
		this.record_doFilter("RequestFilter");
		this.doFilter(PATH, null, MappingType.INCLUDE, "Include");
		this.doFilter(PATH, null, MappingType.FORWARD, "Forward");
		this.doFilter(PATH, null, MappingType.REQUEST, "Request");
	}

	/**
	 * Ensure {@link Filter} used only once in {@link FilterChain} on path.
	 */
	public void test_path_UseFilterOnlyOnce() {
		final String PATH = "/exact/path";
		final FilterContainerFactory factory = this.createFactory("Filter");
		this.addServicer(factory, PATH + "/*", null);
		this.addServicer(factory, PATH + "/*", null);
		this.record_init("Filter");
		this.record_doFilter("Filter");
		this.doFilter(PATH, null, MappingType.REQUEST, "FilterOnlyOnce");
	}

	/**
	 * Ensure no filtering if not match on extension.
	 */
	public void test_extension_NoMatch() {
		this.addServicer("Filter", "*.extension", null);
		this.record_init("Filter");
		this.record_doFilter();
		this.doFilter("/no.match", null, MappingType.REQUEST, "Servlet");
	}

	/**
	 * Ensure match on extension.
	 */
	public void test_extenstion_Match() {
		this.doSingleFilterTest("Servlet", "/path", "/resource.extension",
				MappingType.REQUEST, "*.extension", null);
	}

	/**
	 * Ensure exact have multiple match on extension.
	 */
	public void test_extension_MultipleMatch() {
		this.addServicer("FilterOne", "*.extension", null);
		this.addServicer("FilterTwo", "*.extension", null);
		this.record_init("FilterOne", "FilterTwo");
		this.record_doFilter("FilterOne", "FilterTwo");
		this.doFilter("/resource.extension", null, MappingType.REQUEST,
				"Servlet");
	}

	/**
	 * Ensure exact have selective match on extension.
	 */
	public void test_extension_SelectiveMatch() {
		this.addServicer("FilterOne", "*.extension", null);
		this.addServicer("FilterTwo", "*.different", null);
		this.addServicer("FilterThree", "*.extension", null);
		this.record_init("FilterOne", "FilterTwo", "FilterThree");
		this.record_doFilter("FilterOne", "FilterThree");
		this.doFilter("/resource.extension", null, MappingType.REQUEST,
				"Servlet");
	}

	/**
	 * Ensure that matches on {@link MappingType}.
	 */
	public void test_extension_MappingType() {
		this.addServicer("RequestFilter", "*.extension", null,
				MappingType.REQUEST);
		this.addServicer("ForwardFilter", "*.extension", null,
				MappingType.FORWARD);
		this.addServicer("IncludeFilter", "*.extension", null,
				MappingType.INCLUDE);
		this.record_init("RequestFilter", "ForwardFilter", "IncludeFilter");
		this.record_doFilter("IncludeFilter");
		this.record_doFilter("ForwardFilter");
		this.record_doFilter("RequestFilter");
		this.doFilter("/resource.extension", null, MappingType.INCLUDE,
				"Include");
		this.doFilter("/resource.extension", null, MappingType.FORWARD,
				"Forward");
		this.doFilter("/resource.extension", null, MappingType.REQUEST,
				"Request");
	}

	/**
	 * Ensure {@link Filter} used only once in {@link FilterChain} on extension.
	 */
	public void test_extension_UseFilterOnlyOnce() {
		final FilterContainerFactory factory = this.createFactory("Filter");
		this.addServicer(factory, "*.extension", null);
		this.addServicer(factory, "*.extension", null);
		this.record_init("Filter");
		this.record_doFilter("Filter");
		this.doFilter("/resource.extension", null, MappingType.REQUEST,
				"FilterOnlyOnce");
	}

	/**
	 * Ensure {@link Filter} used only once in {@link FilterChain} on path.
	 */
	public void test_all_UseFilterOnlyOnce() {
		final FilterContainerFactory factory = this.createFactory("Filter");
		this.addServicer(factory, "/path/resource.extension", null);
		this.addServicer(factory, "/path*", null);
		this.addServicer(factory, "*.extension", null);
		this.record_init("Filter");
		this.record_doFilter("Filter");
		this.doFilter("/path/resource.extension", null, MappingType.REQUEST,
				"FilterOnlyOnce");
	}

}