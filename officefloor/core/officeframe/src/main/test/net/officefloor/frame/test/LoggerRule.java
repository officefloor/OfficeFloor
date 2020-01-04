/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} to capture and assert {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerRule extends Handler implements TestRule {

	/**
	 * Captured {@link LogRecord} instances.
	 */
	private final Queue<LogRecord> records = new ConcurrentLinkedQueue<>();

	/**
	 * Asserts a {@link LogRecord} at any {@link Level} by any {@link Logger}.
	 * 
	 * @param message Message for {@link Logger}.
	 * @return Matching {@link LogRecord} instances.
	 */
	public LogRecord[] assertLog(String message) {
		return this.assertLog(null, message);
	}

	/**
	 * Asserts a {@link LogRecord} at particular {@link Level} by any
	 * {@link Logger}.
	 * 
	 * @param level   Expected {@link Level}.
	 * @param message Expected message.
	 * @return Matching {@link LogRecord} instances.
	 */
	public LogRecord[] assertLog(Level level, String message) {
		return this.assertLog(null, null, message);
	}

	/**
	 * Asserts a specific {@link LogRecord}.
	 * 
	 * @param loggerName Name of {@link Logger}.
	 * @param level      Expected {@link Level}.
	 * @param message    Expected message.
	 * @return Matching {@link LogRecord} instances.
	 */
	public LogRecord[] assertLog(String loggerName, Level level, String message) {

		// Create the predicates
		Predicate<LogRecord> namePredicate = loggerName != null ? (value) -> loggerName.equals(value.getLoggerName())
				: (value) -> true;
		Predicate<LogRecord> levelPredicate = level != null ? (value) -> level.equals(value.getLevel())
				: (value) -> true;
		Predicate<String> messagePredicate = message != null ? Pattern.compile(message).asPredicate() : (value) -> true;

		// Obtain the matching log records
		List<LogRecord> logs = this.records.stream().filter(namePredicate).filter(levelPredicate)
				.filter((log) -> messagePredicate.test(log.getMessage())).collect(Collectors.toList());

		// Ensure matched on at least one record
		assertTrue("Did not find log record (" + (loggerName != null ? " name=" + loggerName : "")
				+ (level != null ? " level=" + level.getName() : "") + (message != null ? " message=" + message : "")
				+ " )", logs.size() > 0);

		// Return the records
		return logs.toArray(new LogRecord[logs.size()]);
	}

	/*
	 * ================== Handler ====================
	 */

	@Override
	public void publish(LogRecord record) {
		this.records.add(record);
	}

	@Override
	public void flush() {
		// Nothing to flush
	}

	@Override
	public void close() throws SecurityException {
		// Nothing to tidy up (will just GC)
	}

	/*
	 * ================== TestRule ====================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Obtain the root logger
				Logger root = Logger.getLogger(LoggerRule.class.getName());
				while (root.getParent() != null) {
					root = root.getParent();
				}

				try {
					// Provide handler to capture the log records
					root.addHandler(LoggerRule.this);

					// Undertake test
					base.evaluate();

				} finally {
					// Ensure remove handling once done
					root.removeHandler(LoggerRule.this);
				}
			}
		};
	}

}
