/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.test.LogTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.test.logger.LoggerUtil.LoggerReset;

/**
 * Tests the {@link LoggerUtil}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class LoggerUtilTest {

	private final LogTestSupport log = new LogTestSupport();

	/**
	 * Ensure can disable logging.
	 */
	@Test
	public void disableLogging() {

		// Logger names
		final String alreadyCreatedLoggerName = "ALREADY_CREATED_" + UUID.randomUUID().toString();
		final String createdLoggerName = "CREATED_" + UUID.randomUUID().toString();

		// Obtain the logger
		Logger logger = Logger.getLogger(alreadyCreatedLoggerName);
		logger.setLevel(Level.INFO);

		// Ensure disable logging
		AtomicBoolean isLogged = new AtomicBoolean(false);
		String stdOutErr = this.log.captureStdOutErr(() -> {
			String logs = this.log.captureLoggerOutput(() -> {

				// Disable to ensure
				LoggerReset reset = LoggerUtil.disableLogging();
				try {

					// Attempt to log
					logger.info("DISABLED");
					assertEquals(Level.OFF, logger.getLevel(), "Logging should be off");

					// Create logger to ensure also disabled
					Logger created = Logger.getLogger(createdLoggerName);
					created.info("ALSO DISABLED");
					assertNull(created.getLevel(), "Logging should also be off (as inherits)");

					// Indicate logged
					isLogged.set(true);

				} finally {
					reset.reset();
				}
			});
			assertEquals("", logs, "Should disable logging");
		});
		assertEquals("", stdOutErr, "Should disable log output");

		// Ensure can log again
		assertTrue(isLogged.get(), "Should have logged");
		assertEquals(Level.INFO, logger.getLevel(), "Logging should be info");
		assertNull(Logger.getLogger(createdLoggerName).getLevel(), "Logging should also be info (as inherits)");
	}

}
