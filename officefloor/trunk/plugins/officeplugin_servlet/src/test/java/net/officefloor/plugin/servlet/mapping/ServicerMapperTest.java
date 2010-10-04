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
package net.officefloor.plugin.servlet.mapping;

import java.util.List;

/**
 * Tests the {@link ServicerMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicerMapperTest extends AbstractServicerMapperTestCase {

	/**
	 * {@link Servicer}.
	 */
	private final Servicer exactPath = new MockServicer("exactPath",
			"/exact/path");

	/**
	 * {@link Servicer}.
	 */
	private final Servicer exactResource = new MockServicer("exactResource",
			"/exact/resource.extension");

	/**
	 * {@link Servicer}.
	 */
	private final Servicer path = new MockServicer("path", "/path/*");

	/**
	 * {@link Servicer}.
	 */
	private final Servicer pathLonger = new MockServicer("pathLonger",
			"/path/longer/*");

	/**
	 * {@link Servicer}.
	 */
	private final Servicer extension = new MockServicer("extension",
			"*.extension");

	/**
	 * {@link ServicerMapper} to test.
	 */
	private final ServicerMapper mapper = new ServicerMapperImpl(
			this.exactPath, this.exactResource, this.path, this.pathLonger,
			this.extension);

	/**
	 * Ensure exact path map.
	 */
	public void test_exact_Path() {
		ServicerMapping mapping = this.mapper.mapPath("/exact/path");
		assertMapping(mapping, this.exactPath, "/exact/path", null, null);
	}

	/**
	 * Ensure exact resource map.
	 */
	public void test_exact_Resource() {
		ServicerMapping mapping = this.mapper
				.mapPath("/exact/resource.extension");
		assertMapping(mapping, this.exactResource, "/exact/resource.extension",
				null, null);
	}

	/**
	 * Ensure exact map with a query string.
	 */
	public void test_exact_QueryString() {
		ServicerMapping mapping = this.mapper
				.mapPath("/exact/resource.extension?name=value");
		assertMapping(mapping, this.exactResource, "/exact/resource.extension",
				null, "name=value", "name", "value");
	}

	/**
	 * Ensure path map with a query string.
	 */
	public void test_path_QueryString() {
		ServicerMapping mapping = this.mapper.mapPath("/path?name=value");
		assertMapping(mapping, this.path, "/path", null, "name=value", "name",
				"value");
	}

	/**
	 * Ensure match on the longer path.
	 */
	public void test_path_LongerIsBetterMatch() {
		ServicerMapping mapping = this.mapper.mapPath("/path/longer/resource");
		assertMapping(mapping, this.pathLonger, "/path/longer", "/resource",
				null);
	}

	/**
	 * Ensure can ignore blank segment.
	 */
	public void test_path_IgnoreBlankSegment() {
		ServicerMapping mapping = this.mapper.mapPath("//path");
		assertMapping(mapping, this.path, "/path", null, null);
	}

	/**
	 * Ensure can match with a single trailing separator.
	 */
	public void test_path_IgnoreTrailingSeparator() {
		ServicerMapping mapping = this.mapper.mapPath("/path/");
		assertMapping(mapping, this.path, "/path", null, null);
	}

	/**
	 * Ensure can match with many trailing separators.
	 */
	public void test_path_TrailingSeparators() {
		ServicerMapping mapping = this.mapper.mapPath("/path//");
		assertMapping(mapping, this.path, "/path", "//", null);
	}

	/**
	 * Ensure can match extension ignoring case.
	 */
	public void test_extension_IgnoreCase() {
		ServicerMapping mapping = this.mapper
				.mapPath("/extension/test.EXTENSION");
		assertMapping(mapping, this.extension, "/extension/test.EXTENSION",
				null, null);
	}

	/**
	 * Ensure path map with a query string.
	 */
	public void test_extension_QueryString() {
		ServicerMapping mapping = this.mapper
				.mapPath("/extension/test.extension?name=value");
		assertMapping(mapping, this.extension, "/extension/test.extension",
				null, "name=value", "name", "value");
	}

	/**
	 * Ensure returns no mapping if not match and no default.
	 */
	public void test_none_NullIfNoMatchAndNoDefault() {
		ServicerMapping mapping = this.mapper.mapPath("/unknown");
		assertNull("Should not have mapping if no default", mapping);
	}

	/**
	 * Ensure able to obtain named {@link ServicerMapping}.
	 */
	public void test_name_path() {
		Servicer servicer = this.mapper.mapName("path");
		assertEquals("Incorrect named mapping", this.path, servicer);
	}

	/**
	 * Ensure returns <code>null</code> if unknown name.
	 */
	public void test_name_unknown() {
		Servicer servicer = this.mapper.mapName("unknown");
		assertNull("Should not map servicer for unknown name", servicer);
	}

	/**
	 * Ensure can map to all {@link Servicer} instances.
	 */
	public void test_all_path() {
		List<Servicer> servicers = this.mapper.mapAll("/path");
		assertList(servicers, this.path);
	}

	/**
	 * Ensure can map to all {@link Servicer} instances.
	 */
	public void test_all_path_longer() {
		List<Servicer> servicers = this.mapper.mapAll("/path/longer");
		assertList(servicers, this.path, this.pathLonger);
	}

	/**
	 * Ensure can map to all {@link Servicer} instances.
	 */
	public void test_all_path_extension() {
		List<Servicer> servicers = this.mapper
				.mapAll("/path/longer/resource.extension");
		assertList(servicers, this.path, this.pathLonger, this.extension);
	}

	/**
	 * Ensure can map to all {@link Servicer} instances.
	 */
	public void test_all_exact_extension() {
		List<Servicer> servicers = this.mapper
				.mapAll("/exact/resource.extension");
		assertList(servicers, this.exactResource, this.extension);
	}

	/**
	 * Ensure get empty {@link List} for unknown path.
	 */
	public void test_all_unknown() {
		List<Servicer> servicers = this.mapper.mapAll("/unknown");
		assertList(servicers);
	}

}