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
package net.officefloor.eclipse.util;

import net.officefloor.eclipse.OfficeFloorPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utility class to allow logging.
 * 
 * @author Daniel Sagenschneider
 */
public class LogUtil {

	/**
	 * Log levels.
	 */
	private static enum LogLevel {
		DEBUG, ERROR
	}

	/**
	 * <p>
	 * Flag indicating if to log {@link LogLevel#DEBUG} messages.
	 * <p>
	 * {@link LogLevel#DEBUG} messages are to be only shown during development
	 * and not part of actual running.
	 */
	private static final boolean isLogDebug;

	/**
	 * Initiates the whether logging {@link LogLevel#DEBUG} by system
	 * properties.
	 */
	static {
		// Determine whether to log debug
		String logDebug = System
				.getProperty("net.officefloor.eclipse.log.debug");
		isLogDebug = (logDebug == null ? false : Boolean.parseBoolean(logDebug));
	}

	/**
	 * Allows to determine if logging {@link LogLevel#DEBUG}.
	 * 
	 * @return <code>true</code> if logging {@link LogLevel#DEBUG}.
	 */
	public static boolean isDebugLevel() {
		return isLogDebug;
	}

	/**
	 * Log {@link LogLevel#DEBUG}.
	 * 
	 * @param message
	 *            Message.
	 */
	public static void logDebug(String message) {
		log(LogLevel.DEBUG, message, null);
	}

	/**
	 * Log {@link LogLevel#DEBUG}.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public static void logDebug(String message, Throwable cause) {
		log(LogLevel.DEBUG, message, cause);
	}

	/**
	 * Log {@link LogLevel#ERROR}.
	 * 
	 * @param message
	 *            Message.
	 */
	public static void logError(String message) {
		log(LogLevel.ERROR, message, null);
	}

	/**
	 * Log {@link LogLevel#ERROR}.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public static void logError(String message, Throwable cause) {
		log(LogLevel.ERROR, message, cause);
	}

	/**
	 * Logs the message.
	 * 
	 * @param level
	 *            {@link LogLevel}.
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause. May be <code>null</code>.
	 */
	private static void log(LogLevel level, String message, Throwable cause) {

		// Determine if log debug messages
		if ((!isLogDebug) && (LogLevel.DEBUG.equals(level))) {
			return; // do not log debug
		}

		// Obtain the message
		if (EclipseUtil.isBlank(message)) {
			if (cause != null) {
				message = cause.getClass().getSimpleName() + ": "
						+ cause.getMessage();
			}
		}

		// Obtain location called from
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		String location = "";
		if (stackTrace.length >= 3) {
			// This method, logXXX method, caller (makes 2)
			StackTraceElement caller = stackTrace[2];
			location = "       [" + caller.getClassName() + "#"
					+ caller.getMethodName() + " (" + caller.getFileName()
					+ ":" + caller.getLineNumber() + ")]";
		}

		// Append location to the message
		message = message + location;

		// Obtain the severity
		int severity;
		switch (level) {
		case DEBUG:
			severity = IStatus.INFO;
			break;
		case ERROR:
			severity = IStatus.ERROR;
			break;
		default:
			// Unknown log level
			logStatus(new Status(IStatus.ERROR, OfficeFloorPlugin.PLUGIN_ID,
					"Unknown log level " + level + " (message: " + message
							+ ")"));
			return;
		}

		// Determine if core exception, and if so obtain the status
		IStatus childStatus = null;
		if (cause instanceof CoreException) {
			CoreException coreEx = (CoreException) cause;
			childStatus = coreEx.getStatus();
		}

		// Create the status to log
		IStatus status;
		if (childStatus == null) {
			// Create status for logging
			status = new Status(severity, OfficeFloorPlugin.PLUGIN_ID, message,
					cause);
		} else {
			// Create status to contain log details
			IStatus logStatus = new Status(severity,
					OfficeFloorPlugin.PLUGIN_ID, message, cause);

			// Create multiple status containing both log and child status
			status = new MultiStatus(OfficeFloorPlugin.PLUGIN_ID, 0,
					new IStatus[] { logStatus, childStatus }, message, cause);
		}

		// Log the status
		logStatus(status);
	}

	/**
	 * Logs the {@link IStatus}.
	 * 
	 * @param status
	 *            {@link IStatus} to log.
	 */
	private static void logStatus(IStatus status) {

		// Obtain the log
		ILog log = OfficeFloorPlugin.getDefault().getLog();

		// Log the status
		log.log(status);
	}

	/**
	 * All access via static methods.
	 */
	private LogUtil() {
	}

}