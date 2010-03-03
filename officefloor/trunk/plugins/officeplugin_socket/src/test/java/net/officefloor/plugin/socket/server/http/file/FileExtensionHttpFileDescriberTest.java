/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.nio.charset.Charset;
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
	 * Default {@link Charset}.
	 */
	private final Charset charset = Charset.defaultCharset();

	/**
	 * Mock {@link HttpFileDescription}.
	 */
	private final HttpFileDescription description = this
			.createMock(HttpFileDescription.class);

	/**
	 * Ensure does nothing if <code>null</code> file extension.
	 */
	public void testNoDetailsForNullExtension() {
		this.doTest(null, null, null, null);
	}

	/**
	 * Ensure does nothing if no details.
	 */
	public void testNoDetailsForUnknownExtension() {
		this.doTest("unknown", null, null, null);
	}

	/**
	 * Ensures provides defaults for <code>html</code>.
	 */
	public void testHtmlDefault() {
		this.describer.loadDefaultDescriptions();
		this.doTest("html", null, "text/html", this.charset);
	}

	/**
	 * Ensure handles case insensitive matching of extensions.
	 */
	public void testCaseInsenstive() {
		this.describer.loadDefaultDescriptions();
		this.doTest("HtMl", null, "text/html", this.charset);
	}

	/**
	 * Ensure able to customise details for an extension.
	 */
	public void testCustomiseDetails() {
		this.describer.mapContentEncoding("zip", "gzip");
		this.describer.mapContentType("zip", "custom/type", this.charset);
		this.doTest("zip", "gzip", "custom/type", this.charset);
	}

	/**
	 * Ensure able to customise details by overriding defaults.
	 */
	public void testOverrideDefaults() {
		this.describer.loadDefaultDescriptions();
		this.describer.mapContentEncoding("html", "deflate");
		this.describer.mapContentType("html", "text/plain", null);
		this.doTest("html", "deflate", "text/plain", null);
	}

	/**
	 * Ensure able to load descriptions from properties.
	 */
	public void testLoadFromProperties() {

		// Create properties
		Properties properties = new Properties();
		properties
				.setProperty(
						FileExtensionHttpFileDescriber.CONTENT_ENCODING_PREFIX
								+ "html", "compress");
		properties.setProperty(
				FileExtensionHttpFileDescriber.CONTENT_TYPE_PREFIX + "html",
				"text/html");
		properties.setProperty(FileExtensionHttpFileDescriber.CHARSET_PREFIX
				+ "html", this.charset.name());

		// Load properties and test
		this.describer.loadDescriptions(properties);
		this.doTest("html", "compress", "text/html", this.charset);
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
	 * @param charset
	 *            Expected {@link Charset}.
	 */
	private void doTest(String extension, String contentEncoding,
			String contentType, Charset charset) {

		// Record obtaining file extension
		this.recordReturn(this.description, this.description.getExtension(),
				extension);

		// Record loading description (if expected)
		if (contentEncoding != null) {
			this.description.setContentEncoding(contentEncoding);
		}
		if (contentType != null) {
			this.description.setContentType(contentType, charset);
		}

		// Test
		this.replayMockObjects();
		this.describer.describe(this.description);
		this.verifyMockObjects();
	}

}