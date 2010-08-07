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
package net.officefloor.plugin.servlet.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link StdoutLogger}.
 * 
 * @author Daniel Sagenschneider
 */
public class StdoutLoggerTest extends OfficeFrameTestCase {

	/**
	 * Standard out.
	 */
	private PrintStream stdout;

	/**
	 * Output for testing.
	 */
	private ByteArrayOutputStream testout = new ByteArrayOutputStream();

	/**
	 * {@link Logger}.
	 */
	private Logger logger = new StdoutLogger();

	@Override
	protected void setUp() throws Exception {
		this.stdout = System.out;
		System.setOut(new PrintStream(this.testout));
	}

	@Override
	protected void tearDown() throws Exception {
		System.setOut(this.stdout);
	}

	/**
	 * Ensure logs message.
	 */
	public void testLogMessage() {
		this.logger.log("test");
		assertTextEquals("Incorrect message", "test\n", this.testout.toString());
	}

	/**
	 * Ensure logs {@link Exception}.
	 */
	public void testLogException() {
		final Exception exception = new Exception("test");

		// Create expected content
		StringWriter expected = new StringWriter();
		expected.write("test\n");
		exception.printStackTrace(new PrintWriter(expected));
		expected.write('\n');

		// Test
		this.logger.log("test", exception);
		assertTextEquals("Incorrect exception", expected.toString(),
				this.testout.toString());
	}

}