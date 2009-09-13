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
package net.officefloor.plugin.socket.server.http.file;

import java.util.Properties;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link FileExtensionHttpFileDescriber}.
 *
 * @author Daniel Sagenschneider
 */
public class FileExtensionHttpFileDescriberTest extends OfficeFrameTestCase {

	/**
	 * {@link FileExtensionHttpFileDescriber} to test.
	 */
	private final FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();

	/**
	 * Mock {@link HttpFileDescription}.
	 */
	private final HttpFileDescription description = this
			.createMock(HttpFileDescription.class);

	/**
	 * Ensure does nothing if <code>null</code> file extension.
	 */
	public void testNoDetailsForNullExtension() {
		this.doTest(null, null, null);
	}

	/**
	 * Ensure does nothing if no details.
	 */
	public void testNoDetailsForUnknownExtension() {
		this.doTest("unknown", null, null);
	}

	/**
	 * Ensures provides defaults for <code>html</code>.
	 */
	public void testHtmlDefault() {
		this.describer.loadDefaultDescriptions();
		this.doTest("html", null, "text/html");
	}

	/**
	 * Ensure handles case insensitive matching of extensions.
	 */
	public void testCaseInsenstive() {
		this.describer.loadDefaultDescriptions();
		this.doTest("HtMl", null, "text/html");
	}

	/**
	 * Ensure able to customise details for an extension.
	 */
	public void testCustomiseDetails() {
		this.describer.mapContentEncoding("zip", "gzip");
		this.describer.mapContentType("zip", "application/octet-stream");
		this.doTest("zip", "gzip", "application/octet-stream");
	}

	/**
	 * Ensure able to customise details by overriding defaults.
	 */
	public void testOverrideDefaults() {
		this.describer.loadDefaultDescriptions();
		this.describer.mapContentEncoding("html", "deflate");
		this.describer.mapContentType("html", "text/plain; charset=UTF-8");
		this.doTest("html", "deflate", "text/plain; charset=UTF-8");
	}

	/**
	 * Ensure able to load descriptions from properties.
	 */
	public void testLoadFromProperties() {

		// Create properties
		Properties properties = new Properties();
		properties.put(FileExtensionHttpFileDescriber.CONTENT_ENCODING_PREFIX
				+ "html", "compress");
		properties.put(FileExtensionHttpFileDescriber.CONTENT_TYPE_PREFIX
				+ "html", "text/html; charset=UTF-8");

		// Load properties and test
		this.describer.loadDescriptions(properties);
		this.doTest("html", "compress", "text/html; charset=UTF-8");
	}

	/**
	 * Does the testing.
	 *
	 * @param extension
	 *            File extension to describe.
	 * @param contentEncoding
	 *            Expected <code>Content-Encoding</code> for file extension.
	 * @param contentType
	 *            Expected <code>Content-Type</code> for file extension.
	 */
	private void doTest(String extension, String contentEncoding,
			String contentType) {

		// Record obtaining file extension
		this.recordReturn(this.description, this.description.getExtension(),
				extension);

		// Record loading description (if expected)
		if (contentEncoding != null) {
			this.description.setContentEncoding(contentEncoding);
		}
		if (contentType != null) {
			this.description.setContentType(contentType);
		}

		// Test
		this.replayMockObjects();
		this.describer.describe(this.description);
		this.verifyMockObjects();
	}

}