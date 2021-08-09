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

package net.officefloor.frame.api;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.OfficeFrameImpl;

/**
 * <p>
 * Office Frame to create the {@link OfficeFloor}.
 * <p>
 * This is the starting point to use the framework.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFrame {

	/**
	 * <p>
	 * {@link System#getProperty(String)} that allows specifying the
	 * {@link OfficeFrame} implementation {@link Class}.
	 * <p>
	 * Should this not be specified the default {@link OfficeFrameImpl} will be
	 * used.
	 * <p>
	 * Note: it is anticipated that {@link OfficeFrameImpl} will always be used.
	 */
	public static final String IMPLEMENTATION_CLASS_PROPERTY_NAME = "net.officefloor.frame.implementation";

	/**
	 * <p>
	 * {@link System#getProperty(String)} that flags to infer the source
	 * {@link Class} undertaking the logging.
	 * <p>
	 * By default the {@link Logger} instances will specify a <code>null</code>
	 * source {@link Class} name so that the {@link Logger} name is used by the
	 * {@link SimpleFormatter}. This will then result in {@link LogRecord} instances
	 * formatted with the {@link Logger} name.
	 * <p>
	 * Flagging this <code>true</code> will allow the source {@link Class} name to
	 * be inferred. However, a {@link Formatter} will likely need to be configured
	 * to indicate the specific {@link Logger} name to distinguish the configured
	 * instance.
	 */
	public static final String LOG_SOURCE_CLASS_PROPERTY_NAME = "net.officefloor.log.source.class";

	/**
	 * Singleton {@link OfficeFrame}.
	 */
	private static OfficeFrame INSTANCE = null;

	/**
	 * Specifies the {@link OfficeFrame} implementation. Allows for overriding the
	 * default implementation.
	 * 
	 * @param singleton {@link OfficeFrame} implementation.
	 */
	public synchronized static final void setInstance(OfficeFrame singleton) {
		// Ensure not already specified
		if (INSTANCE != null) {
			throw new IllegalStateException("OfficeFloor instance has already been specified");
		}

		// Specify OfficeFloor implementation
		INSTANCE = singleton;
	}

	/**
	 * Obtains the the Singleton instance of the {@link OfficeFrame}.
	 * 
	 * @return Singleton {@link OfficeFrame}.
	 */
	public synchronized static final OfficeFrame getInstance() {

		// Lazy load
		if (INSTANCE == null) {

			// Determine if overriding the implementation
			String implementationClassName = System.getProperty(IMPLEMENTATION_CLASS_PROPERTY_NAME);
			if ((implementationClassName != null) && (implementationClassName.trim().length() > 0)) {
				// Have override implementation, so use
				try {
					INSTANCE = (OfficeFrame) Class.forName(implementationClassName).getDeclaredConstructor()
							.newInstance();
				} catch (Throwable ex) {
					throw new IllegalArgumentException(
							"Can not create instance of " + implementationClassName + " from default constructor", ex);
				}

			} else {
				// No override, so use default implementation
				INSTANCE = new OfficeFrameImpl();
			}
		}

		// Return the singleton instance
		return INSTANCE;
	}

	/**
	 * <p>
	 * Convenience method to create a single {@link OfficeFloorBuilder}, as there is
	 * typically only one {@link OfficeFloor} per JVM.
	 * <p>
	 * If more than one {@link OfficeFloor} is required, use the {@link OfficeFrame}
	 * returned from {@link #getInstance()}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	public static final OfficeFloorBuilder createOfficeFloorBuilder() {

		// Use default name for the OfficeFloor
		String officeFloorName = OfficeFloor.class.getSimpleName();

		// Create the OfficeFloor builder by the default name
		return OfficeFrame.getInstance().createOfficeFloorBuilder(officeFloorName);
	}

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @param loggerName Name of {@link Logger}.
	 * @return {@link Logger}.
	 */
	public static Logger getLogger(String loggerName) {

		// Create the logger
		Logger logger = Logger.getLogger(loggerName);

		// Determine if allow inferring source class name
		if (!Boolean.getBoolean(LOG_SOURCE_CLASS_PROPERTY_NAME)) {
			logger.setFilter((record) -> {
				record.setSourceClassName(null); // avoid inferring
				return true; // always log
			});
		}

		// Return the logger
		return logger;
	}

	/*
	 * ========== Methods to be implemented by the OfficeFrame ==============
	 */

	/**
	 * Obtains the {@link OfficeFloorBuilder}.
	 * 
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @return {@link OfficeFloorBuilder}.
	 */
	public abstract OfficeFloorBuilder createOfficeFloorBuilder(String officeFloorName);

}
