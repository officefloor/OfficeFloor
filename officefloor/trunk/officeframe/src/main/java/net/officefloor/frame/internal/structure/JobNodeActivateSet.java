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
package net.officefloor.frame.internal.structure;

/**
 * <p>
 * Set of {@link JobNode} instances that are to be activated.
 * <p>
 * The {@link JobNode} instances added will be activated at a later time when
 * locks are released to avoid dead-lock.
 * 
 * @author Daniel
 */
public interface JobNodeActivateSet {

	/**
	 * Adds a {@link JobNode} to be activated.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to be activated.
	 */
	void addJobNode(JobNode jobNode);

	/**
	 * Adds an {@link JobNode} to be activated with the failure set on the
	 * {@link ThreadState} of the {@link JobNode}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to be activated.
	 * @param failure
	 *            Failure for the {@link JobNode} to handle.
	 */
	void addJobNode(JobNode jobNode, Throwable failure);

}