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
 * Set of {@link JobNode} instances that are to be activated.
 * 
 * @author Daniel
 */
public interface JobActivateSet {

	/**
	 * <p>
	 * Adds a {@link JobNode} being notified.
	 * <p>
	 * The {@link JobNode#activateJob()} method will be invoked at a later time
	 * of the input {@link JobNode} when less likely to have dead-lock occur.
	 * 
	 * @param notifiedJobNode
	 *            Notified {@link JobNode}.
	 */
	void addNotifiedJobNode(JobNode notifiedJobNode);

	/**
	 * Adds an {@link JobNode} being notified of a failure.
	 * 
	 * @param notifiedJobNode
	 *            Notified {@link JobNode}.
	 * @param failure
	 *            {@link Throwable} indicating the failure.
	 */
	void addFailedJobNode(JobNode notifiedJobNode, Throwable failure);

}
