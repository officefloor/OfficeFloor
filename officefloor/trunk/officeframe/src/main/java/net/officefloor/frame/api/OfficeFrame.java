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
package net.officefloor.frame.api;

import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.OfficeFrameImpl;

/**
 * Singleton Office Frame to create the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public abstract class OfficeFrame {

	/**
	 * Singleton {@link OfficeFloor}.
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
			// Default implementation
			INSTANCE = new OfficeFrameImpl();
		}

		// Return the singleton instance
		return INSTANCE;
	}

	/**
	 * <p>
	 * Convenience method to register a single {@link OfficeFloor}, as there is
	 * typically only one {@link OfficeFloor} per JVM.
	 * <p>
	 * If more than one {@link OfficeFloor} is required, use the
	 * {@link OfficeFrame} returned from {@link #getInstance()}.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link OfficeFloor} if successfully registered, or
	 *         <code>null</code> if could not construct {@link OfficeFloor} with
	 *         reasons passed to the {@link OfficeFloorIssues}.
	 */
	public static final OfficeFloor registerOfficeFloor(
			OfficeFloorBuilder officeFloorBuilder, OfficeFloorIssues issues) {

		// Use default name for the office floor
		String officeFloorName = OfficeFloor.class.getSimpleName();

		// Register the office floor
		return OfficeFrame.getInstance().registerOfficeFloor(officeFloorName,
				officeFloorBuilder, issues);
	}

	/**
	 * <p>
	 * Convenience method to register a single {@link OfficeFloor}, as there is
	 * typically only one {@link OfficeFloor} per JVM. Also throws
	 * {@link OfficeFloorConstructException} on first issue that arises in
	 * constructing the {@link OfficeFloor}.
	 * <p>
	 * If more than one {@link OfficeFloor} is required, use the
	 * {@link OfficeFrame} returned from {@link #getInstance()}.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @return {@link OfficeFloor}.
	 * @throws OfficeFloorConstructException
	 *             If fails to construct the {@link OfficeFloor}.
	 */
	public static final OfficeFloor registerOfficeFloor(
			OfficeFloorBuilder officeFloorBuilder)
			throws OfficeFloorConstructException {
		return OfficeFloorConstructException
				.registerOfficeFloor(officeFloorBuilder);
	}

	/*
	 * ========== Methods to be implemented by the OfficeFrame ==============
	 */

	/**
	 * Obtains the {@link BuilderFactory}.
	 * 
	 * @return {@link BuilderFactory}.
	 */
	public abstract OfficeFloorBuilder createOfficeFloorBuilder();

	/**
	 * Registers the {@link OfficeBuilder} on this {@link OfficeFrame}.
	 * 
	 * @param officeFloorName
	 *            Name of the {@link OfficeFloor}.
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder} of the {@link OfficeFloor} to be
	 *            registered.
	 * @param issuesListener
	 *            {@link OfficeFloorIssues} to listen for issues in constructing
	 *            the {@link OfficeFloor}.
	 * @return {@link OfficeFloor} if successfully registered, or
	 *         <code>null</code> if could not construct {@link OfficeFloor} with
	 *         reasons passed to the {@link OfficeFloorIssues}.
	 */
	public abstract OfficeFloor registerOfficeFloor(String officeFloorName,
			OfficeFloorBuilder officeFloorBuilder,
			OfficeFloorIssues issuesListener);

}
