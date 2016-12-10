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

import java.io.PrintStream;

/**
 * Provides convenient means to log progress of the start up of the
 * {@link Process}.
 *
 * @author Daniel Sagenschneider
 */
public class ProgressLogger implements AutoCloseable {

	/**
	 * Logs the initial message.
	 * 
	 * @param initialMessage
	 *            Initial message.
	 * @param logger
	 *            Logger.
	 */
	public static void logInitialMessage(String initialMessage,
			PrintStream logger) {
		logger.print(initialMessage + " ...");
		logger.flush();
	}

	/**
	 * Logs the complete message.
	 * 
	 * @param completeMessage
	 *            Complete message.
	 * @param logger
	 *            Logger.
	 */
	public static void logCompleteMessage(String completeMessage,
			PrintStream logger) {
		logger.println(" " + completeMessage);
	}

	/**
	 * Complete message.
	 */
	private final String completeMessage;

	/**
	 * Logger.
	 */
	private final PrintStream logger;

	/**
	 * Initialise outputting the initial message to the logger.
	 * 
	 * @param initialMessage
	 *            Initial message.
	 * @param completeMessage
	 *            Complete message.
	 * @param logger
	 *            Logger.
	 */
	public ProgressLogger(String initialMessage, String completeMessage,
			PrintStream logger) {
		this.completeMessage = completeMessage;
		this.logger = logger;

		// Output the initial message
		logInitialMessage(initialMessage, logger);
	}

	/*
	 * ======================== AutoCloseable =============================
	 */

	@Override
	public void close() {
		// Output the completion message
		logCompleteMessage(this.completeMessage, this.logger);
	}

}