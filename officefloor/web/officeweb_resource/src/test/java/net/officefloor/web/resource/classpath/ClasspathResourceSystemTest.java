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
package net.officefloor.web.resource.classpath;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.impl.AbstractHttpResourceStoreTestCase;
import net.officefloor.web.resource.spi.ResourceSystemFactory;

/**
 * Tests the {@link ClasspathResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystemTest extends AbstractHttpResourceStoreTestCase {

	/**
	 * Tests retrieving directory from a JAR (as most tests are using
	 * directories).
	 * 
	 * Note test expects JUnit on class path.
	 */
	public void testObtainDirectoryFromJar() throws Exception {
		this.setupNewHttpResourceStore("", null, "Assert.class");
		HttpResource resource = this.getHttpResourceStore().getHttpResource("/org/junit");
		assertTrue("Should be directory", resource instanceof HttpDirectory);
		HttpDirectory directory = (HttpDirectory) resource;
		HttpFile defaultFile = directory.getDefaultHttpFile();
		assertEquals("Should have default file", "/org/junit/Asset.class", defaultFile.getPath());
	}

	/**
	 * Tests retrieving file from a JAR (as most tests are using directories).
	 * 
	 * Note test expects JUnit 4.8.2 on class path.
	 */
	public void testObtainFileFromJar() throws Exception {
		this.setupNewHttpResourceStore("", null, "Assert.class");
		HttpResource resource = this.getHttpResourceStore().getHttpResource("/org/junit/Asset.class");
		assertTrue("Should be file", resource instanceof HttpFile);
		HttpFile file = (HttpFile) resource;
		assertEquals("Incorrect file", "/org/junit/Asset.class", file.getPath());
	}

	/*
	 * ============== AbstractHttpResourceStoreTestCase ==================
	 */

	@Override
	protected String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ResourceSystemFactory getResourceSystemFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}