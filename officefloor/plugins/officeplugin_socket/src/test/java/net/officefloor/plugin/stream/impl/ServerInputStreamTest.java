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
package net.officefloor.plugin.stream.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.stream.BrowseInputStream;
import net.officefloor.plugin.stream.NoAvailableInputException;
import net.officefloor.plugin.stream.ServerInputStream;

/**
 * Tests the {@link ServerInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerInputStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link ServerInputStream}.
	 */
	private final ServerInputStreamImpl stream = new ServerInputStreamImpl(this);

	/**
	 * {@link BrowseInputStream}.
	 */
	private final BrowseInputStream browse = this.stream.createBrowseInputStream();

	/**
	 * Ensure EOF.
	 */
	public void testEof() throws IOException {

		// Provide data
		this.stream.inputData(null, 0, 0, false);

		// Validate EOF
		assertEquals("Should be EOF", -1, this.stream.read());
		assertEquals("No available data", -1, this.stream.available());

		// Validate browsing
		assertEquals("Browse should be EOF", -1, this.browse.read());
		assertEquals("No available browse data", -1, this.browse.available());
	}

	/**
	 * Ensure {@link NoAvailableInputException} on reading past available.
	 */
	public void testNoAvailableInput() throws IOException {

		// Should be no available data
		assertEquals("No available data", 0, this.stream.available());
		assertEquals("No browse data", 0, this.browse.available());

		// Attempting to read should cause failure
		try {
			this.stream.read();
			fail("Should not be successful");
		} catch (NoAvailableInputException ex) {
			// Correct exception
		}

		// Attempting to browse should cause failure
		try {
			this.browse.read();
			fail("Browse should not be successful");
		} catch (NoAvailableInputException ex) {
			// Correct exception
		}
	}

	/**
	 * Ensure can read data.
	 */
	public void testReadData() throws IOException {

		// Obtain another browse input stream
		BrowseInputStream another = this.stream.createBrowseInputStream();

		// Input the data indicating not further data
		this.stream.inputData(new byte[] { 10 }, 0, 0, false);
		assertEquals("Data available", 1, this.stream.available());
		assertEquals("Browse data available", 1, this.browse.available());

		// Obtain another browse
		assertEquals("Incorrect browse read value", 10, another.read());
		assertEquals("Should be browse EOF", -1, another.read());
		assertEquals("No further browse data available", -1, another.available());

		// Read the only data
		assertEquals("Incorrect read value", 10, this.stream.read());
		assertEquals("Should be EOF", -1, this.stream.read());
		assertEquals("No further data available", -1, this.stream.available());

		// Browse data should be available after read
		assertEquals("Read value still available for browse", 10, this.browse.read());
		assertEquals("Should now be EOF for browse", -1, this.browse.read());
		assertEquals("No further data available for browse", -1, this.browse.available());
	}

	/**
	 * Ensure can read bounded data.
	 */
	public void testReadBoundedData() throws IOException {

		// Input the data indicating not further data
		this.stream.inputData(new byte[] { 1, 2, 3 }, 1, 1, false);
		assertEquals("Data available", 1, this.stream.available());
		assertEquals("Browse data available", 1, this.browse.available());

		// Read the only bounded data
		assertEquals("Incorrect read value", 2, this.stream.read());
		assertEquals("Should be EOF", -1, this.stream.read());
		assertEquals("No further data available", -1, this.stream.available());

		// Browse the only bounded data
		assertEquals("Incorrect browse read value", 2, this.browse.read());
		assertEquals("Should be browse EOF", -1, this.browse.read());
		assertEquals("No further browse data available", -1, this.browse.available());
	}

	/**
	 * Ensure can read data from multiple backing arrays.
	 */
	public void testReadAcrossMultipleData() throws IOException {

		// Input two arrays of data
		this.stream.inputData(new byte[] { 11 }, 0, 0, true);
		this.stream.inputData(new byte[] { 22 }, 0, 0, false);
		assertEquals("Data should be available", 2, this.stream.available());
		assertEquals("Browse data should be available", 2, this.browse.available());

		// Read the first datum
		assertEquals("Incorrect first read", 11, this.stream.read());
		assertEquals("Data still available", 1, this.stream.available());

		// Browse the first datum
		assertEquals("Incorrect first browse", 11, this.browse.read());
		assertEquals("Browse data still available", 1, this.browse.available());

		// Read the second datum
		assertEquals("Incorrect second read", 22, this.stream.read());
		assertEquals("No available data", -1, this.stream.available());
		assertEquals("Should be EOF", -1, this.stream.read());

		// Browse the second datum
		assertEquals("Incorrect second browse", 22, this.browse.read());
		assertEquals("No available browse data", -1, this.browse.available());
		assertEquals("Should be browse EOF", -1, this.browse.read());
	}

	/**
	 * Ensure that can not input data once identified no further data.
	 */
	public void testNoFurtherInputData() {
		this.stream.inputData(null, 0, 0, false);
		try {
			this.stream.inputData(null, 0, 0, false);
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", "May not input further data as flagged previously that no further data",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can not obtain the state momento if the input data is not yet
	 * finished.
	 */
	public void testNoStateMomentoAsInputNotFinished() {
		try {
			this.stream.exportState();
			fail("Should not be successful");
		} catch (NotAllDataAvailableException ex) {
			assertEquals("Incorrect cause",
					"ServerInputStream has not finished receiving data.  Can not obtain complete state momento.",
					ex.getMessage());
		}
	}

	/**
	 * Ensure only allow appropriate momento.
	 */
	public void testInvalidMomento() throws Exception {

		// Ensure fails if invalid momento
		try (Closeable ignore = new ServerInputStreamImpl(this, this.createMock(Serializable.class))) {
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause", "Invalid momento for ServerInputStream", ex.getMessage());
		}
	}

	/**
	 * Ensure can have no data for momento state.
	 */
	public void testNoDataStateMomento() {

		// No further data
		this.stream.inputData(null, 0, 0, false);

		// Ensure no content
		this.assertMomentoClonedStream(null);
	}

	/**
	 * Ensure can have data for momento state.
	 */
	public void testSingleInputDataStateMomento() {

		// Load the single input data
		final String CONTENT = "TEST";
		final byte[] CONTENT_BYTES = CONTENT.getBytes();
		this.stream.inputData(CONTENT_BYTES, 0, (CONTENT_BYTES.length - 1), false);

		// Ensure correct content
		this.assertMomentoClonedStream(CONTENT);
	}

	/**
	 * Ensure can have data for momento state.
	 */
	public void testMultipleInputDataStateMomento() {

		// Load multiple input data
		for (String content : new String[] { "ONE_", "TWO_", "THREE_", "last" }) {
			byte[] contentBytes = content.getBytes();
			this.stream.inputData(contentBytes, 0, (contentBytes.length - 1), (!("last".equals(content))));
		}

		// Ensure correct content
		this.assertMomentoClonedStream("ONE_TWO_THREE_last");
	}

	/**
	 * Ensure input data can be offset.
	 */
	public void testOffsetInputDataStateMomento() {

		for (String content : new String[] { "ONE_", "TWO_", "THREE_", "last" }) {
			byte[] contentBytes = content.getBytes();
			byte[] inputBytes = new byte[contentBytes.length + 4];
			for (int i = 0; i < contentBytes.length; i++) {
				inputBytes[i + 2] = contentBytes[i];
			}
			this.stream.inputData(inputBytes, 2, (inputBytes.length - 3), (!("last".equals(content))));
		}

		// Ensure correct content
		this.assertMomentoClonedStream("ONE_TWO_THREE_last");
	}

	/**
	 * Asserts the cloning with momento.
	 * 
	 * @param expectedContent
	 *            Expected content of the cloned {@link ServerInputStream}.
	 */
	private void assertMomentoClonedStream(String expectedContent) {
		try {

			// Export the state (ensuring serialises)
			Serializable momento = this.stream.exportState();

			// Serialise the momento
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
			output.writeObject(momento);
			output.flush();

			// Unserialise the momento
			ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
			ObjectInputStream input = new ObjectInputStream(inputBuffer);
			Serializable unserialisedMomento = (Serializable) input.readObject();

			// Create new input stream from momento
			ServerInputStream clonedStream = new ServerInputStreamImpl(this, unserialisedMomento);

			// Determine stream cloned correctly
			if (expectedContent == null) {
				// Should have no content (end of stream)
				assertEquals("Should have no content", -1, clonedStream.available());

			} else {
				// Ensure appropriate content is available
				byte[] expectedBytes = expectedContent.getBytes();
				assertEquals("Incorrect number of bytes available", expectedBytes.length, clonedStream.available());
				byte[] actualBytes = new byte[expectedBytes.length];
				clonedStream.read(actualBytes);
				assertEquals("Incorrect content", expectedContent, new String(actualBytes));
				assertEquals("Should now be end of stream", -1, clonedStream.available());
			}

			// Close the cloned stream
			clonedStream.close();

		} catch (Exception ex) {
			// Should not occur so do not impose exception on tests
			throw fail(ex);
		}
	}

}