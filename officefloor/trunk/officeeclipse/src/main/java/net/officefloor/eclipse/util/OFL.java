/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Office Floor Logger (OFL), used mainly for debugging.
 * 
 * @author Daniel
 */
public class OFL {

	/**
	 * Log levels.
	 */
	private enum LogLevel {
		NONE, DEBUG, ERROR
	}

	/**
	 * Log level.
	 */
	private static final LogLevel logLevel;

	/**
	 * Initiates the log level from system properties.
	 */
	static {
		String levelName = System.getProperty("office.floor.eclipse.log.level");
		if ("debug".equalsIgnoreCase(levelName)) {
			logLevel = LogLevel.DEBUG;
		} else {
			logLevel = LogLevel.NONE;
		}
	}

	/**
	 * Debug methods and returns/exceptions of methods on the input object.
	 * 
	 * @param object
	 *            Object to proxy and provide debug information.
	 */
	public static <T> T proxyDebug(T object) {
		switch (logLevel) {
		case DEBUG:
			return createLoggingProxy(object, LogLevel.DEBUG);
		default:
			// Do not proxy
			return object;
		}
	}

	/**
	 * Debug level log.
	 * 
	 * @param messageParts
	 *            Message parts to be concatenated to construct the log message.
	 */
	public static void debug(Object... messageParts) {
		switch (logLevel) {
		case DEBUG:
			logMessage(LogLevel.DEBUG, messageParts);
		}
	}

	/**
	 * Logs the message.
	 * 
	 * @param level
	 *            {@link LogLevel}.
	 * @param messageParts
	 *            Parts of the message.
	 */
	private static void logMessage(LogLevel level, Object... messageParts) {

		// Create the message
		String message = constructLogMessage(messageParts);

		// Obtain location called from
		Throwable stackDetail = new Throwable();
		StackTraceElement[] stackTrace = stackDetail.getStackTrace();
		String location = "";
		if (stackTrace.length > 2) {
			// This method, debug method, caller (2)
			StackTraceElement caller = stackTrace[2];
			location = "[" + caller.getClassName() + "#"
					+ caller.getMethodName() + " (" + caller.getFileName()
					+ ":" + caller.getLineNumber() + ")]";
		}

		// Log the message
		logMessage(level, message + "       " + location);
	}

	/**
	 * Logs the message.
	 * 
	 * @param message
	 *            Message.
	 */
	private static void logMessage(LogLevel level, String message) {
		// TODO consider better way to log message
		switch (level) {
		case DEBUG:
			System.out.println(message);
			break;
		case ERROR:
			System.err.print(message);
			break;
		}
	}

	/**
	 * Creates the logging proxy.
	 * 
	 * @param object
	 *            Object to proxy.
	 * @param level
	 *            {@link LogLevel}.
	 * @return Logging proxy to the object.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T createLoggingProxy(final T object, final LogLevel level) {

		// Obtain the class of object to proxied
		final Class type = object.getClass();

		// Log creating proxy
		logMessage(level, "New Proxy: " + type.getName());

		// Create and initiate the CG Enhancer
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args,
					MethodProxy proxy) throws Throwable {

				// Determine the method signature
				String signature = type.getSimpleName() + "."
						+ method.getName() + "(...)";

				try {
					// Invoke the method
					Object returnValue = proxy.invoke(object, args);

					// Log method successful
					logMessage(level, "Method: " + signature);

					// Return the value
					return returnValue;

				} catch (Throwable ex) {
					// Log method failing
					StackTraceElement element = ex.getStackTrace()[0];
					logMessage(LogLevel.ERROR, "Failed invoking method: "
							+ signature + "  [" + ex.getClass().getSimpleName()
							+ ": " + ex.getMessage() + " ("
							+ element.getFileName() + ":"
							+ element.getLineNumber() + ")]");

					// Propagate failure
					throw ex;
				}
			}
		});

		// Try for default constructor
		T proxy;
		try {
			boolean hasDefaultConstructor = false;
			for (Constructor<T> constructor : type.getConstructors()) {
				if ((constructor.getParameterTypes() == null)
						|| (constructor.getParameterTypes().length == 0)) {
					hasDefaultConstructor = true;
				}
			}
			if (hasDefaultConstructor) {
				// Create via default constructor
				proxy = (T) enhancer.create();
			} else {
				// Use any constructor (providing values when possible)
				Constructor constructor = type.getConstructors()[0];
				Class[] parameterTypes = constructor.getParameterTypes();
				Object[] parameters = new Object[parameterTypes.length];
				for (int i = 0; i < parameters.length; i++) {
					Class parameterType = parameterTypes[i];
					if (parameterType.isAssignableFrom(String.class)) {
						parameters[i] = "Mock constructor string";
					}
				}
				proxy = (T) enhancer.create(parameterTypes, parameters);
			}
		} catch (Throwable ex) {
			logMessage(LogLevel.ERROR, "Failed creating proxy for type: ", type
					.getName(), "\n", ex);
			throw new OfficeFloorPluginFailure(ex);
		}

		// Return the proxy for the object
		return proxy;
	}

	/**
	 * Constructs the message from the input parts.
	 * 
	 * @param messageParts
	 *            Message parts.
	 * @return Message.
	 */
	private static String constructLogMessage(Object... messageParts) {
		StringBuilder message = new StringBuilder();
		for (Object part : messageParts) {
			message.append(part);
		}
		return message.toString();
	}

	/**
	 * All access via static methods.
	 */
	private OFL() {
	}

}
