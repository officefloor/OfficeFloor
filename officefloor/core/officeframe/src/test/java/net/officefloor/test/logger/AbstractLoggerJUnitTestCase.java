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

package net.officefloor.test.logger;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Tests the {@link AbstractLoggerJUnit}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractLoggerJUnitTestCase {

	/**
	 * Default {@link Logger} name.
	 */
	private static final String LOGGER_NAME = "TEST";

	/**
	 * Initiated {@link Logger} before.
	 */
	private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

	/**
	 * Obtains the {@link AbstractLoggerJUnit} under test.
	 * 
	 * @return {@link AbstractLoggerJUnit} under test.
	 */
	protected abstract AbstractLoggerJUnit getLoggerJUnit();

	/**
	 * Ensure can assert specific {@link LogRecord} instances.
	 */
	public void specificEvent() throws Throwable {
		this.doLoggerRuleTest("specific", Level.INFO, "test message", (logger) -> logger.info("test message"),
				e("specific", Level.INFO, "test message"));
	}

	/**
	 * Ensure can match on multiple {@link LogRecord} instances.
	 */
	public void multipleEvents() throws Throwable {
		this.doLoggerRuleTest("multiple", Level.INFO, "multiple messages", (logger) -> {
			logger.info("multiple messages");
			logger.info("multiple messages");
		}, e("multiple", Level.INFO, "multiple messages"), e("multiple", Level.INFO, "multiple messages"));
	}

	/**
	 * Ensure can match by regular expression.
	 */
	public void regularExpression() throws Throwable {
		final String message = "Regex on number 1 to find record";
		this.doLoggerRuleTest("regex", Level.INFO, "\\d", (logger) -> logger.info(message),
				e("regex", Level.INFO, message));
	}

	/**
	 * Ensure can find messages from specific {@link Logger}.
	 */
	public void recordsFromLogger() throws Throwable {
		final String[] loggerNames = new String[] { "ONE", "TWO", "THREE" };
		for (String loggerName : loggerNames) {
			this.doLoggerRuleTest(loggerName, null, null, (logger) -> {
				for (String logName : loggerNames) {
					Logger.getLogger(logName).info("Message " + logName);
				}
			}, e(loggerName, Level.INFO, "Message " + loggerName));
			this.getLoggerJUnit().clear();
		}
	}

	/**
	 * Ensure can find messages from {@link Level}.
	 */
	public void levels() throws Throwable {
		Logger.getLogger(LOGGER_NAME).setLevel(Level.ALL);
		final Level[] levels = new Level[] { Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE,
				Level.FINER, Level.FINEST };
		for (Level level : levels) {
			this.doLoggerRuleTest(null, level, null, (logger) -> {
				for (Level logLevel : levels) {
					logger.log(logLevel, "Message " + logLevel.getName());
				}
			}, e(LOGGER_NAME, level, "Message " + level.getName()));
			this.getLoggerJUnit().clear();
		}
	}

	/**
	 * Convenience method to evaluate the {@link LoggerRule}.
	 * 
	 * @param name               Name of {@link Logger}.
	 * @param level              {@link Level}.
	 * @param message            Message.
	 * @param logic              Logic to run with {@link LoggerRule}.
	 * @param expectedLogRecords {@link ExpectedLogRecord} instances.
	 */
	private void doLoggerRuleTest(String name, Level level, String message, Consumer<Logger> logic,
			ExpectedLogRecord... expectedLogRecords) throws Throwable {

		// Create the logger (should be able to hook in after Logger creation)
		Logger logger = name != null ? Logger.getLogger(name) : LOGGER;

		// Undertake logic
		logic.accept(logger);

		// Assert the log records
		LogRecord[] records = this.getLoggerJUnit().assertLog(name, level, message);
		JUnitAgnosticAssert.assertEquals(expectedLogRecords.length, records.length, "Incorrect number of log records");
		for (int i = 0; i < expectedLogRecords.length; i++) {
			ExpectedLogRecord expected = expectedLogRecords[i];
			LogRecord actual = records[i];
			JUnitAgnosticAssert.assertEquals(expected.name, actual.getLoggerName(), "Incorrect name for record " + i);
			JUnitAgnosticAssert.assertEquals(expected.level, actual.getLevel(), "Incorrect level for record " + i);
			JUnitAgnosticAssert.assertEquals(expected.message, actual.getMessage(),
					"Incorrect message for record " + i);
		}
	}

	/**
	 * Convenience construction method.
	 * 
	 * @param name    Name of {@link Logger}.
	 * @param level   {@link Level}.
	 * @param message Message.
	 * @return {@link ExpectedLogRecord}.
	 */
	private static ExpectedLogRecord e(String name, Level level, String message) {
		return new ExpectedLogRecord(name, level, message);
	}

	/**
	 * Expected {@link LogRecord}.
	 */
	private static class ExpectedLogRecord {

		/**
		 * Name of {@link Logger}.
		 */
		private final String name;

		/**
		 * {@link Level}.
		 */
		private final Level level;

		/**
		 * Message.
		 */
		private final String message;

		/**
		 * Instantiate.
		 * 
		 * @param loggerName Name of {@link Logger}.
		 * @param level      {@link Level}.
		 * @param message    Message.
		 */
		private ExpectedLogRecord(String name, Level level, String message) {
			this.name = name;
			this.level = level;
			this.message = message;
		}
	}

}
