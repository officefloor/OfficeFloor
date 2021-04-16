/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.test.logger;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Utility functionality for {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerUtil {

	/**
	 * Resets the {@link Logger} state.
	 */
	public static class LoggerReset {

		/**
		 * Reset {@link Level} values by {@link Logger} name.
		 */
		private final Map<String, Level> levels;

		/**
		 * Instantiate.
		 * 
		 * @param levels Reset {@link Level} values by {@link Logger} name.
		 */
		private LoggerReset(Map<String, Level> levels) {
			this.levels = levels;
		}

		/**
		 * Resets the {@link Logger} instances to log.
		 */
		public void reset() {

			// Reset the logger levels
			for (String loggerName : this.levels.keySet()) {
				Level level = this.levels.get(loggerName);
				Logger.getLogger(loggerName).setLevel(level);
			}
		}
	}

	/**
	 * Disables all {@link Logger} instances from logging.
	 * 
	 * @return {@link LoggerReset}.
	 */
	public static LoggerReset disableLogging() {

		// Disable all the loggers
		Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
		Map<String, Level> levels = new HashMap<>();
		while (loggerNames.hasMoreElements()) {
			String loggerName = loggerNames.nextElement();

			// Obtain the logger
			Logger logger = Logger.getLogger(loggerName);

			// Capture level
			Level originalLevel = logger.getLevel();
			levels.put(loggerName, originalLevel);

			// Disable the logging
			logger.setLevel(Level.OFF);
		}

		// Return the logger reset
		return new LoggerReset(levels);
	}

}
