/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.stream.inputstream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link BrowsableInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class BrowsableInputStreamTest extends OfficeFrameTestCase {

	/**
	 * Ensure can read contents
	 */
	public void testReadContents() {
		BrowsableInputStream stream = createBrowsableInputStream("TEST");
		assertContent("TEST", stream);
	}

	/**
	 * Ensure can browse contents.
	 */
	public void testBrowseContents() {
		BrowsableInputStream stream = createBrowsableInputStream("TEST");

		// Ensure can browse
		InputStream browse = stream.createBrowser();
		assertContent("TEST", browse);

		// Ensure can still input remaining
		assertContent("TEST", stream);
	}

	/**
	 * Ensure can browse mid stream.
	 */
	public void testBrowseMidStream() {
		BrowsableInputStream stream = createBrowsableInputStream("TEST");

		// Consume some content
		assertContent("TE", stream);

		// Ensure browse only the remaining
		InputStream browse = stream.createBrowser();
		assertContent("ST", browse);

		// Ensure consume remaining
		assertContent("ST", stream);
	}

	/**
	 * Ensure able to consume then still able to browse.
	 */
	public void testConsumeThenBrowse() {
		BrowsableInputStream stream = createBrowsableInputStream("TEST");

		// Obtain browse
		InputStream browse = stream.createBrowser();

		// Consume content
		assertContent("TEST", stream);

		// Ensure able to still browse content
		assertContent("TEST", browse);
	}

	/**
	 * Assert content.
	 * 
	 * @param expectedContent
	 *            Expected content.
	 * @param stream
	 *            {@link InputStream} to validate.
	 */
	private static void assertContent(String expectedContent, InputStream stream) {
		try {
			// Ensure available content
			int byteSize = expectedContent.getBytes().length;
			assertTrue("Incorrect available content",
					byteSize <= stream.available());

			// Ensure content is correct
			byte[] actual = new byte[byteSize];
			for (int i = 0; i < byteSize; i++) {
				actual[i] = (byte) stream.read();
			}
			assertEquals("Incorrect content", expectedContent, new String(
					actual));

		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * Creates the {@link BrowsableInputStream}.
	 * 
	 * @param contents
	 *            Contents.
	 * @return {@link BrowsableInputStream}.
	 */
	private static BrowsableInputStream createBrowsableInputStream(
			String contents) {

		// Create input stream to content
		ByteArrayInputStream input = new ByteArrayInputStream(
				contents.getBytes());

		// Create and return the browsable input stream
		return new BrowsableInputStream(input, 10, new Object());
	}

}