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

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Provides promise like functionality for {@link JobNode} instances.
 *
 * @author Daniel Sagenschneider
 */
public class Promise {

	/**
	 * Continue with an array of {@link JobNode} instances.
	 * 
	 * @param jobNodes
	 *            Listing of {@link JobNode} instances to continue with.
	 * @return Next {@link JobNode} to undertake all the {@link JobNode}
	 *         instances.
	 */
	public static JobNode all(JobNode... jobNodes) {

		// Ensure have job nodes
		if ((jobNodes == null) || (jobNodes.length == 0)) {
			return null;
		}

		// Create the listing of continue job nodes
		// (load in reverse order to execute in order)
		JobNode returnJobNode = null;
		for (int i = jobNodes.length - 1; i >= 0; i--) {
			returnJobNode = then(jobNodes[i], null);
		}

		// Return the head job node
		return returnJobNode;
	}

	/**
	 * <p>
	 * Execute the {@link JobNode} then the {@link JobNode}.
	 * <p>
	 * State is passed between {@link JobNode} instances via
	 * {@link ManagedObject} instances, so no parameter is provided.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to execute it and its sequence of
	 *            {@link JobNode} instances.
	 * @param thenJobNode
	 *            {@link JobNode} to then continue after the first input
	 *            {@link JobNode} sequence completes.
	 * @return Next {@link JobNode} to undertake the {@link JobNode} sequence
	 *         and then continue {@link JobNode} sequence.
	 */
	public static JobNode then(JobNode jobNode, JobNode thenJobNode) {
		if (jobNode == null) {
			// No initial job node, so just continue
			return thenJobNode;

		} else if (thenJobNode != null) {
			// Create continue link
			return new ThenJobNode(jobNode, thenJobNode);
		}

		// Only the initial job node
		return jobNode;
	}

	/**
	 * All access via static methods.
	 */
	private Promise() {
	}

	/**
	 * Then {@link JobNode}.
	 */
	private static class ThenJobNode implements JobNode {

		/**
		 * Delegate {@link JobNode}.
		 */
		private final JobNode delegate;

		/**
		 * Continue {@link JobNode}.
		 */
		private final JobNode continueJobNode;

		/**
		 * Creation by static methods.
		 * 
		 * @param delegate
		 *            Delegate {@link JobNode} to complete it and all produced
		 *            {@link JobNode} instances before continuing.
		 * @param continueJobNode
		 *            Continue {@link JobNode}.
		 */
		private ThenJobNode(JobNode delegate, JobNode continueJobNode) {
			this.delegate = delegate;
			this.continueJobNode = continueJobNode;
		}

		/*
		 * =================== JobNode ==============================
		 */

		@Override
		public JobNode doJob(JobContext context) {
			return Promise.then(this.delegate.doJob(context), this.continueJobNode);
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return this.delegate.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return this.delegate.getThreadState();
		}
	}

}
