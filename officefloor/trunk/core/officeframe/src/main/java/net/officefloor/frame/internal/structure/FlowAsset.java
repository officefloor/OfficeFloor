/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;

/**
 * {@link JobSequence} {@link Asset}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowAsset {

	/**
	 * <p>
	 * Flags for the input {@link JobNode} to wait on this {@link JobSequence}.
	 * <p>
	 * Note that the {@link JobNode} may not wait on a {@link JobSequence} contained in
	 * the same {@link ThreadState} as that of the {@link JobNode}. This would
	 * result on the {@link ThreadState} waiting on itself and subsequently no
	 * progression of the {@link ThreadState}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to wait on this {@link JobSequence}.
	 * @param timeout
	 *            The maximum time to wait in milliseconds for the {@link JobSequence}
	 *            to complete.
	 * @param token
	 *            A token added to the {@link FlowJoinTimedOutEscalation} to aid
	 *            in identifying which {@link JobSequence} join timed out. May be
	 *            <code>null</code>.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to activate the {@link JobNode}
	 *            instances should {@link JobSequence} be completed.
	 * @return <code>true</code> if waiting on this {@link JobSequence}, otherwise
	 *         <code>false</code> if {@link JobSequence} has already completed and not
	 *         waiting.
	 */
	boolean waitOnFlow(JobNode jobNode, long timeout, Object token,
			JobNodeActivateSet activateSet);

}