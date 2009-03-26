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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;

/**
 * Provides package access to {@link JobNodeActivatableSetImpl}.
 * 
 * @author Daniel
 */
public class AssetNotifySetImplAccess {

	/**
	 * Obtain access to {@link JobNodeActivatableSetImpl#tasks}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivatableSetImpl}.
	 * @return {@link LinkedList} for the {@link JobNode} instances of the input
	 *         {@link JobNodeActivatableSetImpl}.
	 */
	public static LinkedList<?, Object> tasks(JobNodeActivatableSetImpl activateSet) {
		return activateSet.jobNodes;
	}
}
