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

import net.officefloor.frame.spi.team.Team;

/**
 * <p>
 * Node within the graph of {@link JobNode} instances to execute.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link JobNode}
 * instances for a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobNode {

	/**
	 * Enable {@link Promise} like functionality.
	 * 
	 * @param thenJobNode
	 *            {@link JobNode} to execute after this {@link JobNode} chain is
	 *            complete.
	 * @return {@link JobNode} to execute the current {@link JobNode} then the
	 *         input {@link JobNode}.
	 */
	default JobNode then(JobNode thenJobNode) {
		return Promise.then(this, thenJobNode);
	}

	/**
	 * <p>
	 * Obtains the {@link TeamManagement} responsible for this {@link JobNode}.
	 * <p>
	 * By default, {@link JobNode} may be executed by any
	 * {@link TeamManagement}.
	 * 
	 * @return {@link TeamManagement} responsible for this {@link JobNode}. May
	 *         be <code>null</code> to indicate any {@link Team} may execute the
	 *         {@link JobNode}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null;
	}

	/**
	 * Obtains the {@link ThreadState} for this {@link JobNode}.
	 * 
	 * @return {@link ThreadState} for this {@link JobNode}.
	 */
	ThreadState getThreadState();

	/**
	 * Indicates if the {@link JobNode} requires {@link ThreadState} safety.
	 * 
	 * @return <code>true</code> should {@link JobNode} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Undertakes the {@link JobNode}.
	 * 
	 * @return Next {@link JobNode} to be executed. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances to execute.
	 */
	JobNode doJob();

}