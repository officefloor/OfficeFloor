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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.classpath.ClasspathHttpFile;
import net.officefloor.web.resource.classpath.ClasspathHttpResourceFactory;
import net.officefloor.web.resource.impl.AbstractHttpFileDescription;
import net.officefloor.web.resource.impl.AbstractHttpFileTestCase;

/**
 * Classpath {@link HttpFile} test.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileTest extends AbstractHttpFileTestCase {

	/**
	 * Ensure obtain details from <code>toString</code> method.
	 */
	public void testToString() {
		final String RESOURCE_PATH = "/resource.html";
		final Charset charset = Charset.defaultCharset();
		final String classPath = this.getClassPath(RESOURCE_PATH);
		assertEquals(
				"Incorrect toString with full details",
				"ClasspathHttpFile: /resource.html (Class path: "
						+ classPath
						+ ", Content-Encoding: encoding, Content-Type: type; charset="
						+ charset.name() + ")",
				this.createHttpFile(RESOURCE_PATH, "encoding", "type", charset)
						.toString());
		assertEquals("Incorrect toString with no details",
				"ClasspathHttpFile: /resource.html (Class path: " + classPath
						+ ")",
				this.createHttpFile(RESOURCE_PATH, null, null, null).toString());
	}

	/**
	 * Obtains the class path for the resource path.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @return Class path.
	 */
	private String getClassPath(String resourcePath) {
		return AbstractHttpFileTestCase.class.getPackage().getName()
				.replace('.', '/')
				+ resourcePath;
	}

	/*
	 * ======================= AbstractHttpFileTest ====================
	 */

	@Override
	protected HttpFile createHttpFile(String resourcePath,
			String contentEncoding, String contentType, Charset charset) {

		final String CLASS_PATH_PREFIX = "PREFIX";

		// Obtain class path for resource
		String classPath = this.getClassPath(resourcePath);

		// Provide description of file
		AbstractHttpFileDescription description = new AbstractHttpFileDescription(
				resourcePath) {
			@Override
			public ByteBuffer getContents() {
				fail("Should not be invoked");
				return null;
			}
		};
		description.setContentEncoding(contentEncoding);
		description.setContentType(contentType, charset);

		// Ensure the resource factory is available
		ClasspathHttpResourceFactory.clearHttpResourceFactories();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		ClasspathHttpResourceFactory.getHttpResourceFactory(CLASS_PATH_PREFIX,
				classLoader);

		// Create HTTP File
		HttpFile httpFile = new ClasspathHttpFile(resourcePath, classPath,
				CLASS_PATH_PREFIX, description);

		// Return the HTTP File
		return httpFile;
	}

}