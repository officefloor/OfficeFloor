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
	 * {@link ServicerMapper} to test.
	 */
	private final ServicerMapper mapper = new ServicerMapperImpl(
			this.exactPath, this.exactResource);

	/**
	 * Ensure exact path map.
	 */
	public void testExactPath() {
		ServicerMapping mapping = this.mapper.mapPath("/exact/path");
		assertMapping(mapping, this.exactPath, "/exact/path", null, null);
	}

	/**
	 * Ensure exact resource map.
	 */
	public void testExactResource() {
		ServicerMapping mapping = this.mapper
				.mapPath("/exact/resource.extension");
		assertMapping(mapping, this.exactResource, "/exact/resource.extension",
				null, null);
	}

	// TODO continue further tests (especially with blank segments)

}