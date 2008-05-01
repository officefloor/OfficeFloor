/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Deque;
import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * Test {@link NonblockingSocketChannel} that provides mock data.
 * 
 * @author Daniel
 */
public class TestSocketChannel implements NonblockingSocketChannel {

	/**
	 * {@link SelectionKey}.
	 */
	private final SelectionKey selectionKey;

	/**
	 * Data.
	 */
	private final Deque<Byte> data = new LinkedList<Byte>();

	/**
	 * Flag indicating if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey}.
	 */
	public TestSocketChannel(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	/**
	 * Indicates if closed.
	 * 
	 * @return <code>true</code> if closed.
	 */
	public boolean isClosed() {
		return this.isClosed;
	}

	/**
	 * Mocks inputting the data from the client.
	 * 
	 * @param data
	 *            Mock data input from client.
	 */
	public void input(byte[] data) {
		for (byte b : data) {
			this.data.addFirst(new Byte(b));
		}
	}

	/**
	 * Mocks inputting the text from the client.
	 * 
	 * @param text
	 *            Mock text input from client.
	 */
	public void input(String text) {
		this.input(text.getBytes());
	}

	/**
	 * Validates the output of data to the client.
	 * 
	 * @param data
	 *            Data that the client is expected to receive.
	 */
	public void validateOutput(byte[] data) {

		final char SEPARATOR = ',';

		// Create the String version of expected text
		StringBuilder expectedText = new StringBuilder();
		for (byte b : data) {
			expectedText.append((int) b);
			expectedText.append(SEPARATOR);
		}

		// Determine if have all data available
		if (this.data.size() < data.length) {
			// Not enough data
			StringBuilder actualText = new StringBuilder();
			while (!this.data.isEmpty()) {
				byte b = this.data.removeLast().byteValue();
				actualText.append((int) b);
				actualText.append(SEPARATOR);
			}

			// Fail as not enough data
			TestCase.assertEquals("More output data expected", expectedText
					.toString(), actualText.toString());
		}

		// Validate match output data
		StringBuilder actualText = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			byte b = this.data.removeLast().byteValue();
			actualText.append((int) b);
			actualText.append(SEPARATOR);
		}
		TestCase.assertEquals("Incorrect output data expected", expectedText
				.toString(), actualText.toString());
	}

	/**
	 * Validates the output of text to the client.
	 * 
	 * @param text
	 *            Text that the client is expected to receive.
	 */
	public void validateOutput(String text) {

		// Obtain number of bytes expected
		int byteCountExpected = text.getBytes().length;

		// Determine if have all text available
		if (this.data.size() < byteCountExpected) {
			// Not enough data
			byte[] actualData = new byte[this.data.size()];
			for (int i = 0; i < actualData.length; i++) {
				actualData[i] = this.data.removeLast().byteValue();
			}
			String actualText = new String(actualData);

			// Fail as not enough text
			TestCase
					.assertEquals("More output text expected", text, actualText);
		}

		// Validate match output text
		byte[] actualData = new byte[byteCountExpected];
		for (int i = 0; i < actualData.length; i++) {
			actualData[i] = this.data.removeLast().byteValue();
		}
		String actualText = new String(actualData);
		TestCase.assertEquals("Inccorect output text expected", text,
				actualText);
	}

	/**
	 * Validate that no further output data.
	 */
	public void validateNoOutput() {

		// Valid if no further data
		if (this.data.size() == 0) {
			// Correct as no further data
			return;
		}

		// Obtain the remaining data
		StringBuilder actualBytes = new StringBuilder();
		byte[] actualData = new byte[this.data.size()];
		for (int i = 0; i < actualData.length; i++) {
			byte b = this.data.removeLast().byteValue();
			actualData[i] = b;
			actualBytes.append((int) b);
			actualBytes.append(",");
		}
		String actualText = new String(actualData);

		// Fail as further data available
		TestCase.fail("Further data available when none expected\n  Text: '"
				+ actualText + "'\n  Data: " + actualBytes.toString());
	}

	/*
	 * =========================================================================
	 * NonblockingSocketChannel
	 * =========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#register(java.nio.channels.Selector,
	 *      int, java.lang.Object)
	 */
	@Override
	public SelectionKey register(Selector selector, int ops, Object attachment)
			throws IOException {
		// Attach attachment to selection
		this.selectionKey.attach(attachment);

		// Return the selection key
		return this.selectionKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#read(java.nio.ByteBuffer)
	 */
	@Override
	public int read(ByteBuffer buffer) throws IOException {
		// Write data into buffer
		int readSize = 0;
		for (;;) {

			// Determine if buffer is full
			if (buffer.remaining() == 0) {
				return readSize;
			}

			// Determine if further data
			if (this.data.size() == 0) {
				return readSize;
			}

			// Obtain next byte of data
			byte b = this.data.removeLast().byteValue();
			buffer.put(b);
			readSize++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#write(java.nio.ByteBuffer)
	 */
	@Override
	public int write(ByteBuffer data) throws IOException {
		// Write data out to mock client
		int writeSize = 0;
		while (data.remaining() > 0) {
			byte b = data.get();
			this.data.addFirst(new Byte(b));
			writeSize++;
		}
		return writeSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#close()
	 */
	@Override
	public void close() throws IOException {
		this.isClosed = true;
	}

}
