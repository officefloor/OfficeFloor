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