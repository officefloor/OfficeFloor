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

/**
 * <p>
 * Monitor for to allow {@link ThreadState} instances to wait until an
 * {@link Asset} is completed processing.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link AssetMonitor}
 * instances for an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetMonitor extends
		LinkedListSetEntry<AssetMonitor, AssetManager> {

	/**
	 * Obtains the {@link Asset} for this {@link AssetMonitor}.
	 * 
	 * @return {@link Asset} for this {@link AssetMonitor}.
	 */
	Asset getAsset();

	/**
	 * <p>
	 * Flags for the {@link JobNode} (and more specifically the
	 * {@link ThreadState} of the {@link JobNode}) to wait until the
	 * {@link Asset} activates it.
	 * <p>
	 * This is typically because the {@link Asset} is doing some processing that
	 * the {@link JobNode} requires completed before proceeding.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to be activated when {@link Asset} processing
	 *            is complete.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} that the {@link JobNode} is added
	 *            to if the {@link Asset} has permanently activated this
	 *            {@link AssetMonitor}. This may be <code>null</code> to
	 *            activate via the {@link OfficeManager} however it is much more
	 *            efficient to provide a {@link JobNodeActivateSet}.
	 * @return <code>true</code> if the {@link JobNode} is waiting on the
	 *         {@link Asset}.
	 */
	boolean waitOnAsset(JobNode jobNode, JobNodeActivateSet activateSet);

	/**
	 * Adds all the {@link JobNode} instances waiting on the {@link Asset} to
	 * the input {@link JobNodeActivateSet} to be activated.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} that will later activate the
	 *            {@link JobNode} instances waiting on the {@link Asset}. This
	 *            may be <code>null</code> to activate via the
	 *            {@link OfficeManager} however it is much more efficient to
	 *            provide a {@link JobNodeActivateSet}.
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetMonitor} from now on are activated
	 *            immediately. It is useful to flag an {@link AssetMonitor} in
	 *            this state when the {@link Asset} is no longer being used to
	 *            stop a {@link JobNode} from waiting forever.
	 */
	void activateJobNodes(JobNodeActivateSet activateSet, boolean isPermanent);

	/**
	 * Adds all the {@link JobNode} instances waiting on this {@link Asset} to
	 * the input {@link JobNodeActivateSet} to be activated with the
	 * {@link ThreadState} receiving the input failure.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} that will later activate the
	 *            {@link JobNode} instances waiting on the {@link Asset}. This
	 *            may be <code>null</code> to activate via the
	 *            {@link OfficeManager} however it is much more efficient to
	 *            provide a {@link JobNodeActivateSet}.
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} of the
	 *            {@link JobNode} instances waiting on the {@link Asset}.
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetMonitor} from now on are activated
	 *            immediately with the input failure. It is useful to flag an
	 *            {@link AssetMonitor} in this state when the {@link Asset} is
	 *            in a failed state that can not be recovered from.
	 */
	void failJobNodes(JobNodeActivateSet activateSet, Throwable failure,
			boolean isPermanent);

}