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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;

/**
 * {@link Flow} {@link Asset}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowAsset {

	/**
	 * <p>
	 * Flags for the input {@link JobNode} to wait on this {@link Flow}.
	 * <p>
	 * Note that the {@link JobNode} may not wait on a {@link Flow} contained in
	 * the same {@link ThreadState} as that of the {@link JobNode}. This would
	 * result on the {@link ThreadState} waiting on itself and subsequently no
	 * progression of the {@link ThreadState}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to wait on this {@link Flow}.
	 * @param timeout
	 *            The maximum time to wait in milliseconds for the {@link Flow}
	 *            to complete.
	 * @param token
	 *            A token added to the {@link FlowJoinTimedOutEscalation} to aid
	 *            in identifying which {@link Flow} join timed out. May be
	 *            <code>null</code>.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to activate the {@link JobNode}
	 *            instances should {@link Flow} be completed.
	 * @return <code>true</code> if waiting on this {@link Flow}, otherwise
	 *         <code>false</code> if {@link Flow} has already completed and not
	 *         waiting.
	 */
	boolean waitOnFlow(JobNode jobNode, long timeout, Object token,
			JobNodeActivateSet activateSet);

}