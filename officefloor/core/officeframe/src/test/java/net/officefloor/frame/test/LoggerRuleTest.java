/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.test;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Tests the {@link LoggerRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerRuleTest extends OfficeFrameTestCase {

	/**
	 * Default {@link Logger} name.
	 */
	private static final String LOGGER_NAME = "TEST";

	/**
	 * Ensure can assert specific {@link LogRecord} instances.
	 */
	public void testSpecificEvent() throws Throwable {
		this.doLoggerRuleTest("specific", Level.INFO, "test message", (logger) -> logger.info("test message"),
				e("specific", Level.INFO, "test message"));
	}

	/**
	 * Ensure can match on multiple {@link LogRecord} instances.
	 */
	public void testMultipleEvents() throws Throwable {
		this.doLoggerRuleTest("multiple", Level.INFO, "multiple messages", (logger) -> {
			logger.info("multiple messages");
			logger.info("multiple messages");
		}, e("multiple", Level.INFO, "multiple messages"), e("multiple", Level.INFO, "multiple messages"));
	}

	/**
	 * Ensure can match by regular expression.
	 */
	public void testRegularExpression() throws Throwable {
		final String message = "Regex on number 1 to find record";
		this.doLoggerRuleTest("regex", Level.INFO, "\\d", (logger) -> logger.info(message),
				e("regex", Level.INFO, message));
	}

	/**
	 * Ensure can find messages from specific {@link Logger}.
	 */
	public void testRecordsFromLogger() throws Throwable {
		final String[] loggerNames = new String[] { "ONE", "TWO", "THREE" };
		for (String loggerName : loggerNames) {
			this.doLoggerRuleTest(loggerName, null, null, (logger) -> {
				for (String logName : loggerNames) {
					Logger.getLogger(logName).info("Message " + logName);
				}
			}, e(loggerName, Level.INFO, "Message " + loggerName));
		}
	}

	/**
	 * Ensure can find messages from {@link Level}.
	 */
	public void testLevels() throws Throwable {
		Logger.getLogger(LOGGER_NAME).setLevel(Level.ALL);
		final Level[] levels = new Level[] { Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE,
				Level.FINER, Level.FINEST };
		for (Level level : levels) {
			this.doLoggerRuleTest(null, level, null, (logger) -> {
				for (Level logLevel : levels) {
					logger.log(logLevel, "Message " + logLevel.getName());
				}
			}, e(LOGGER_NAME, level, "Message " + level.getName()));
		}
	}

	/**
	 * Evaluates the {@link LoggerRule} indicating whether test was run.
	 * 
	 * @param rule        {@link LoggerRule} to use.
	 * @param description {@link Description}.
	 * @param logic       Logic to run within the {@link LoggerRule}.
	 * @return <code>true</code> if test run.
	 */
	private void evaluateLoggerRule(LoggerRule rule, Runnable logic) throws Throwable {
		Description description = Description.createTestDescription(LoggerRuleTest.class, "test");
		Closure<Boolean> isRun = new Closure<>(false);
		rule.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {
				logic.run();
				isRun.value = true;
			}
		}, description).evaluate();
		assertTrue("Should be successful", isRun.value);
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
		Logger logger = Logger.getLogger(name != null ? name : LOGGER_NAME);

		// Ensure run logging
		LoggerRule rule = new LoggerRule();
		this.evaluateLoggerRule(rule, () -> logic.accept(logger));

		// Assert the log records
		LogRecord[] records = rule.assertLog(name, level, message);
		assertEquals("Incorrect number of log records", expectedLogRecords.length, records.length);
		for (int i = 0; i < expectedLogRecords.length; i++) {
			ExpectedLogRecord expected = expectedLogRecords[i];
			LogRecord actual = records[i];
			assertEquals("Incorrect name for record " + i, expected.name, actual.getLoggerName());
			assertEquals("Incorrect level for record " + i, expected.level, actual.getLevel());
			assertEquals("incorrect message for record " + i, expected.message, actual.getMessage());
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