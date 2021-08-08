/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
