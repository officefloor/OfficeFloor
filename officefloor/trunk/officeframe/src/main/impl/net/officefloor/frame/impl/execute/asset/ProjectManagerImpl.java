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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ProjectManager;

/**
 * Implementation of the {@link ProjectManager}.
 * 
 * @author Daniel
 */
public class ProjectManagerImpl implements ProjectManager {

	/**
	 * Interval in milliseconds between each monitor run.
	 */
	protected final long MONITOR_INTERVAL;

	/**
	 * List of {@link AssetManager} instances to be managed.
	 */
	protected final AssetManager[] assetGroups;

	/**
	 * Initiate.
	 * 
	 * @param monitorInterval
	 *            Interval in milliseconds between each monitor run.
	 * @param assetGroups
	 *            List of {@link AssetManager} instances to be managed.
	 */
	public ProjectManagerImpl(long monitorInterval, AssetManager[] assetGroups) {
		this.MONITOR_INTERVAL = monitorInterval;
		this.assetGroups = assetGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ProjectManager#startManaging
	 * (long)
	 */
	public void startManaging(long interval) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProjectManager#manage()
	 */
	public void manage() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ProjectManager#stopManaging()
	 */
	public void stopManaging() {
		// TODO Auto-generated method stub

	}

}
