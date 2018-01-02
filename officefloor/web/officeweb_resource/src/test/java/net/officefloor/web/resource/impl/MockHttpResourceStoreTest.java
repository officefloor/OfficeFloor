/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.resource.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;

/**
 * Tests the {@link HttpResourceStore} with mock {@link ResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpResourceStoreTest extends AbstractHttpResourceStoreTestCase implements ResourceSystemFactory {

	/**
	 * {@link ResourceSystemContext}.
	 */
	private ResourceSystemContext context;

	/**
	 * Mock {@link ResourceSystem}.
	 */
	private final ResourceSystem system = (path) -> {
		String directory = this.getLocation();
		return Paths.get(directory, path);
	};

	/**
	 * Ensure the system is accessing the test files.
	 */
	public void testAbleToGetFile() throws IOException {
		Path path = system.getResource("index.html");
		assertTrue("Should find index.html", Files.isReadable(path));
	}

	/**
	 * Ensure can trigger resource changed.
	 */
	public void testTriggerResourceChanged() throws IOException {
		HttpResource file = this.getHttpResourceStore().getHttpResource("/index.html");
		HttpResource directory = this.getHttpResourceStore().getHttpResource("/directory");
		this.context.notifyResourceChanged("/index.html");
		assertNull("File should no longer be cached", this.getHttpResourceCache().getHttpResource("/index.html"));
		assertSame("Directory should still be cached", directory,
				this.getHttpResourceCache().getHttpResource("/directory"));
		HttpResource reloaded = this.getHttpResourceStore().getHttpResource("/index.html");
		assertNotSame("Should be different loaded HTTP file", file, reloaded);
	}

	/**
	 * Ensure can trigger all resources changed.
	 */
	public void testTriggerAllResourcesChanged() throws IOException {
		HttpResource file = this.getHttpResourceStore().getHttpResource("/index.html");
		HttpResource directory = this.getHttpResourceStore().getHttpResource("/directory");
		this.context.notifyResourceChanged(null);
		assertNull("File should no longer be cached", this.getHttpResourceCache().getHttpResource("/index.html"));
		assertNull("Directory should no longer be cached", this.getHttpResourceCache().getHttpResource("/directory"));
		assertNotSame("Should be different loaded HTTP file", file,
				this.getHttpResourceStore().getHttpResource("/index.html"));
		assertNotSame("Should be different loaded HTTP directory", directory,
				this.getHttpResourceStore().getHttpResource("/directory"));
	}

	/*
	 * =========== AbstractHttpResourceStoreTestCase ============
	 */

	@Override
	protected String getLocation() {
		return this.getPackageRelativePath(AbstractHttpResourceStoreTestCase.class);
	}

	@Override
	protected ResourceSystemFactory getResourceSystemFactory() {
		return this;
	}

	/*
	 * ================= ResourceSystemFactory ==================
	 */

	@Override
	public String getProtocolName() {
		return "mock";
	}

	@Override
	public ResourceSystem createResourceSystem(ResourceSystemContext context) {
		this.context = context;
		assertEquals("Incorrect location", this.getLocation(), context.getLocation());
		return this.system;
	}

}