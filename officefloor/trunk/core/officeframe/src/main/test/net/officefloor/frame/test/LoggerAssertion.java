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
package net.officefloor.frame.test;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Asserts {@link Logger} messages.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerAssertion {

	/**
	 * Setups the {@link LoggerAssertion} for the {@link Logger}.
	 * 
	 * @param loggerName
	 *            Name of the {@link Logger}.
	 * @return {@link LoggerAssertion}.
	 */
	public static LoggerAssertion setupLoggerAssertion(String loggerName) {

		// Create the logger
		Logger logger = Logger.getLogger(loggerName);

		// Create the logger assertion
		final LoggerAssertion assertion = new LoggerAssertion(logger);

		// Set up to intercept all logging
		logger.setUseParentHandlers(false);

		// Remove the existing handlers
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
			assertion.logHandlers.add(handler);
		}

		// Add handler to intercept message
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				synchronized (assertion.logRecords) {
					assertion.logRecords.add(record);
				}
			}

			@Override
			public void flush() {
				// Do nothing
			}

			@Override
			public void close() throws SecurityException {
				// Do nothing
			}
		});

		// Return the logger assertion
		return assertion;
	}

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * {@link LogRecord} instances.
	 */
	private final List<LogRecord> logRecords = new LinkedList<LogRecord>();

	/**
	 * {@link Handler} instances.
	 */
	private final List<Handler> logHandlers = new LinkedList<Handler>();

	/**
	 * Initiate.
	 * 
	 * @param logger
	 *            {@link Logger}.
	 */
	private LoggerAssertion(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Obtains the {@link LogRecord} instances from the {@link Logger}.
	 * 
	 * @return {@link LogRecord} instances from the {@link Logger}.
	 */
	public LogRecord[] getLogRecords() {
		synchronized (this.logRecords) {
			return this.logRecords
					.toArray(new LogRecord[this.logRecords.size()]);
		}
	}

	/**
	 * Disconnects from the {@link Logger}.
	 * 
	 * @return {@link LogRecord} instances provided to the {@link Logger}.
	 */
	public LogRecord[] disconnectFromLogger() {

		// Reinstate handlers and logger state
		this.logger.setUseParentHandlers(true);
		for (Handler handler : this.logger.getHandlers()) {
			this.logger.removeHandler(handler);
		}
		for (Handler handler : this.logHandlers) {
			this.logger.addHandler(handler);
		}

		// Return the log records
		return this.getLogRecords();
	}

}