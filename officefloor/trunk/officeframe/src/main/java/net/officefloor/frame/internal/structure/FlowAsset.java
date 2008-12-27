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
 * {@link Flow} Asset.
 * 
 * @author Daniel
 */
public interface FlowAsset {

	/**
	 * <p>
	 * Flags for the input {@link JobNode} to wait on this {@link Flow}.
	 * <p>
	 * Note that may not wait on a {@link Flow} bound to the same
	 * {@link ThreadState} as that of the {@link JobNode}. This would result on
	 * the {@link ThreadState} waiting on itself and subsequently no progression
	 * of the {@link ThreadState}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to wait on this {@link Flow}.
	 * @param notifySet
	 *            {@link JobActivateSet} should {@link Flow} be completed.
	 * @return <code>true</code> if waiting on this {@link Flow}, otherwise
	 *         <code>false</code> if not to wait.
	 */
	boolean waitOnFlow(JobNode jobNode, JobActivateSet notifySet);

}
