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

import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.issue.OfficeIssuesListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.OfficeFrameImpl;

/**
 * Singleton Office Frame to register the
 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public abstract class OfficeFrame {

	/**
	 * Singleton.
	 */
	private static OfficeFrame INSTANCE = null;

	/**
	 * Object to synchronise on.
	 */
	protected static final Object LOCK = new Object();

	/**
	 * {@link OfficeIssuesListener} to listen to issues in constructing an
	 * Office.
	 */
	protected static OfficeIssuesListener OFFICE_ISSUE_LISENER = new OfficeIssuesListener() {
	};

	/**
	 * Specifies the {@link OfficeFrame} implementation.
	 * 
	 * @param singleton
	 *            {@link OfficeFrame} implementation.
	 */
	public static void setInstance(OfficeFrame singleton) {
		synchronized (LOCK) {

			// Ensure not already specified
			if (INSTANCE != null) {
				throw new IllegalStateException(
						"OfficeFloor instance has already been specified");
			}

			// Specify OfficeFloor implementation
			INSTANCE = singleton;
		}
	}

	/**
	 * Obtains the the Singleton instance of the {@link OfficeFrame}.
	 * 
	 * @return Singleton {@link OfficeFrame}.
	 */
	public static OfficeFrame getInstance() {
		synchronized (LOCK) {
			// Lazy load
			if (INSTANCE == null) {
				INSTANCE = new OfficeFrameImpl();
			}
		}

		// Return the singleton instance
		return INSTANCE;
	}

	/**
	 * Obtains the {@link OfficeIssuesListener}.
	 * 
	 * @return {@link OfficeIssuesListener} for constructing Offices.
	 */
	public static OfficeIssuesListener getOfficeIssuesListener() {
		return OFFICE_ISSUE_LISENER;
	}

	/**
	 * Obtains the {@link BuilderFactory}.
	 * 
	 * @return {@link BuilderFactory}.
	 */
	public abstract BuilderFactory getMetaDataFactory();

	/**
	 * Registers the {@link OfficeFloorBuilder} on this {@link OfficeFrame}.
	 * 
	 * @param name
	 *            Name of the office.
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder} of the {@link OfficeFloor} to be
	 *            registered.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If invalid configuration.
	 */
	public synchronized final OfficeFloor registerOfficeFloor(String name,
			OfficeFloorBuilder officeFloorBuilder) throws Exception {
		return this.registerOfficeFloor(name, officeFloorBuilder,
				getOfficeIssuesListener());
	}

	/**
	 * Registers the {@link OfficeBuilder} on this {@link OfficeFrame}.
	 * 
	 * @param name
	 *            Name of the office.
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder} of the {@link OfficeFloor} to be
	 *            registered.
	 * @param issuesListener
	 *            {@link OfficeIssuesListener}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If invalid configuration.
	 */
	protected abstract OfficeFloor registerOfficeFloor(String name,
			OfficeFloorBuilder officeFloorBuilder,
			OfficeIssuesListener issuesListener) throws Exception;

}
