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
package net.officefloor.web.resource.war;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.web.resource.AbstractHttpFileTestCase;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.impl.AbstractHttpFileDescription;
import net.officefloor.web.resource.war.WarHttpFile;

/**
 * Tests the {@link WarHttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpFileTest extends AbstractHttpFileTestCase {

	/**
	 * Ensure obtain details from <code>toString</code> method.
	 */
	public void testToString() throws IOException {
		final String RESOURCE_PATH = "/index.html";
		final File file = this.getFile(RESOURCE_PATH);
		final Charset charset = Charset.defaultCharset();
		assertEquals(
				"Incorrect toString with full details",
				"WarHttpFile: /index.html (file: "
						+ file.getAbsolutePath()
						+ ", Content-Encoding: encoding, Content-Type: type; charset="
						+ charset.name() + ")",
				this.createHttpFile(RESOURCE_PATH, "encoding", "type", charset)
						.toString());
		assertEquals("Incorrect toString with no details",
				"WarHttpFile: /index.html (file: " + file.getAbsolutePath()
						+ ")",
				this.createHttpFile(RESOURCE_PATH, null, null, null).toString());
	}

	/**
	 * Obtains the {@link File}.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @return {@link File}.
	 * @throws IOException
	 *             {@link IOException}.
	 */
	private File getFile(String resourcePath) throws IOException {
		return this.findFile(AbstractHttpFileTestCase.class, resourcePath);
	}

	/*
	 * =================== AbstractHttpFileTestCase ================
	 */

	@Override
	protected HttpFile createHttpFile(String resourcePath,
			String contentEncoding, String contentType, Charset charset)
			throws IOException {

		// Obtain the file
		File file = this.getFile(resourcePath);

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

		// Create HTTP File
		HttpFile httpFile = new WarHttpFile(resourcePath, file, description);

		// Return the HTTP File
		return httpFile;

	}

}