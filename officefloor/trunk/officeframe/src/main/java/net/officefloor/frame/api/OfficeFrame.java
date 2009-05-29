/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.api;

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
	 * Singleton {@link OfficeFrame}.
	 */
	private static OfficeFrame INSTANCE = null;

	/**
	 * Specifies the {@link OfficeFrame} implementation. Allows for overriding
	 * the default implementation.
	 * 
	 * @param singleton
	 *            {@link OfficeFrame} implementation.
	 */
	public synchronized static final void setInstance(OfficeFrame singleton) {
		// Ensure not already specified
		if (INSTANCE != null) {
			throw new IllegalStateException(
					"OfficeFloor instance has already been specified");
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
			String implementationClassName = System
					.getProperty(IMPLEMENTATION_CLASS_PROPERTY_NAME);
			if ((implementationClassName != null)
					&& (implementationClassName.trim().length() > 0)) {
				// Have override implementation, so use
				try {
					INSTANCE = (OfficeFrame) Class.forName(
							implementationClassName).newInstance();
				} catch (Throwable ex) {
					throw new IllegalArgumentException(
							"Can not create instance of "
									+ implementationClassName
									+ " from default constructor", ex);
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
	 * Convenience method to create a single {@link OfficeFloorBuilder}, as
	 * there is typically only one {@link OfficeFloor} per JVM.
	 * <p>
	 * If more than one {@link OfficeFloor} is required, use the
	 * {@link OfficeFrame} returned from {@link #getInstance()}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	public static final OfficeFloorBuilder createOfficeFloorBuilder() {

		// Use default name for the office floor
		String officeFloorName = OfficeFloor.class.getSimpleName();

		// Create the office floor builder by the default name
		return OfficeFrame.getInstance().createOfficeFloorBuilder(
				officeFloorName);
	}

	/*
	 * ========== Methods to be implemented by the OfficeFrame ==============
	 */

	/**
	 * Obtains the {@link BuilderFactory}.
	 * 
	 * @param officeFloorName
	 *            Name of the {@link OfficeFloor}.
	 * @return {@link BuilderFactory}.
	 */
	public abstract OfficeFloorBuilder createOfficeFloorBuilder(
			String officeFloorName);

}
