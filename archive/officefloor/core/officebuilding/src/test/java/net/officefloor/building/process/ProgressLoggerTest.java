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
package net.officefloor.building.process;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProgressLogger}.
 *
 * @author Daniel Sagenschneider
 */
public class ProgressLoggerTest extends OfficeFrameTestCase {

	/**
	 * Buffer containing the logged content.
	 */
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	/**
	 * Logger.
	 */
	private final PrintStream logger = new PrintStream(buffer);

	/**
	 * Ensure can use manual initial method to log.
	 */
	public void testManualInitialLogging() {
		ProgressLogger.logInitialMessage("INITIAL", this.logger);
		assertText("INITIAL ...");
	}

	/**
	 * Ensure can use manual complete method to log.
	 */
	public void testManualCompleteLogging() {
		ProgressLogger.logCompleteMessage("COMPLETE", this.logger);
		assertText(" COMPLETE\n");
	}

	/**
	 * Ensure can provide progress wrapping logging.
	 */
	public void testProgressWrappingLogging() {
		try (ProgressLogger progress = new ProgressLogger("INITIAL",
				"COMPLETE", this.logger)) {
			this.logger.print("BODY");
		}
		assertText("INITIAL ...BODY COMPLETE\n");
	}

	/**
	 * Asserts the logged content.
	 * 
	 * @param expectedText
	 *            Expected logged text.
	 * @param buffer
	 *            Buffer containing the logged content.
	 */
	private final void assertText(String expectedText) {

		// Obtain the logged content
		String loggedContent = new String(this.buffer.toByteArray());

		// Ensure correct logged content
		assertEquals("Incorrect logged content", expectedText, loggedContent);
	}

}