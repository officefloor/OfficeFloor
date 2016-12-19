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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.jobnode.FailThreadStateJobNode;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.Promise;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Implementation of the {@link AssetLatch}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetLatchImpl extends AbstractLinkedListSetEntry<AssetLatchImpl, AssetManager>
		implements AssetLatch, JobNode, CheckAssetContext {

	/**
	 * {@link Asset} being monitored.
	 */
	private final Asset asset;

	/**
	 * {@link AssetManager} to manage this {@link AssetLatch}.
	 */
	private final AssetManagerImpl assetManager;

	/**
	 * Flag indicating to permanently activate waiting {@link JobNode}
	 * instances.
	 */
	private volatile boolean isPermanentlyActivate = false;

	/**
	 * Permanent failure of the {@link Asset}.
	 */
	private volatile Throwable failure = null;

	/**
	 * Set of {@link JobNode} instances waiting on the {@link Asset}.
	 */
	private final LinkedListSet<AwaitingJobNode, AssetLatch> awaitingJobNodes = new ComparatorLinkedListSet<AwaitingJobNode, AssetLatch>() {
		@Override
		protected AssetLatch getOwner() {
			return AssetLatchImpl.this;
		}

		@Override
		protected boolean isEqual(AwaitingJobNode entryA, AwaitingJobNode entryB) {
			return (entryA.jobNode == entryB.jobNode);
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param asset
	 *            {@link Asset} to be managed.
	 * @param assetManager
	 *            {@link AssetManager} for managing this.
	 */
	public AssetLatchImpl(Asset asset, AssetManagerImpl assetManager) {
		this.asset = asset;
		this.assetManager = assetManager;
	}

	/*
	 * ======================= LinkedListSetEntry ==============================
	 */

	@Override
	public AssetManager getLinkedListSetOwner() {
		return this.assetManager;
	}

	/*
	 * ================= AssetLatch ==========================================
	 */

	@Override
	public Asset getAsset() {
		return this.asset;
	}

	@Override
	public JobNode awaitOnAsset(JobNode jobNode) {

		// Undertake permanent release of latch
		if (this.isPermanentlyActivate) {

			// Fail immediately if failure of asset
			Throwable failure = this.failure;
			if (failure != null) {
				return new FailThreadStateJobNode(failure, jobNode.getThreadState()).then(jobNode);
			}

			// Activate immediately if permanently active
			return jobNode;
		}

		// Return job to wait on the asset
		return new AwaitingJobNode(jobNode);
	}

	/*
	 * ================= CheckAssetContext ====================================
	 */

	@Override
	public long getTime() {
		return OfficeManagerImpl.currentTimeMillis();
	}

	@Override
	public void releaseJobNodes(boolean isPermanent) {
		// Latch released, so proceed with job nodes
		this.assetManager.getJobNodeDelegator().delegateJobNode(new ReleaseJobNodes(isPermanent), true);
	}

	@Override
	public void failJobNodes(Throwable failure, boolean isPermanent) {
		// Latch failed, so fail the job nodes
		this.assetManager.getJobNodeDelegator().delegateJobNode(new FailJobNodes(failure, isPermanent), true);
	}

	/*
	 * ================= JobNode ==========================================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.asset.getOwningThreadState();
	}

	@Override
	public JobNode doJob(JobContext context) {

		// Check on the asset
		this.asset.checkOnAsset(this);

		// Nothing further, as release/fail job nodes continue independently
		return null;
	}

	/**
	 * {@link JobNode} waiting on the {@link AssetLatch}.
	 */
	private class AwaitingJobNode extends AbstractLinkedListSetEntry<AwaitingJobNode, AssetLatch> implements JobNode {

		/**
		 * {@link JobNode} being monitored.
		 */
		public final JobNode jobNode;

		/**
		 * Initiate.
		 * 
		 * @param jobNode
		 *            {@link JobNode} waiting on the {@link AssetLatch}.
		 */
		public AwaitingJobNode(JobNode jobNode) {
			this.jobNode = jobNode;
		}

		/*
		 * ================= LinkedListSetEntry =========================
		 */

		@Override
		public AssetLatch getLinkedListSetOwner() {
			return AssetLatchImpl.this;
		}

		/*
		 * ======================== JobNode ============================
		 */

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public JobNode doJob(JobContext context) {

			// Determine if release is permanent
			if (AssetLatchImpl.this.isPermanentlyActivate) {
				// Proceed immediately with job nodes
				return Promise.then(this.jobNode, new ReleaseJobNodes(true));
			}

			// Determine if first job waiting
			if (AssetLatchImpl.this.awaitingJobNodes.getHead() == null) {
				AssetLatchImpl.this.assetManager.registerAssetLatch(AssetLatchImpl.this);
			}

			// Register this latch with the asset manager
			AssetLatchImpl.this.awaitingJobNodes.addEntry(this);

			// Nothing further, as job node now waiting on latch
			return null;
		}
	}

	/**
	 * {@link JobNode} to release the {@link AssetLatch} and proceed with
	 * awaiting {@link JobNode} instances.
	 */
	private class ReleaseJobNodes implements JobNode {

		/**
		 * Indicates if proceeding is permanent.
		 */
		private final boolean isPermanent;

		/**
		 * Instantiate.
		 * 
		 * @param isPermanent
		 *            Indicates if proceeding is permanent.
		 */
		public ReleaseJobNodes(boolean isPermanent) {
			this.isPermanent = isPermanent;
		}

		/*
		 * ======================== JobNode ============================
		 */

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public JobNode doJob(JobContext context) {

			// Flag if permanent proceeding
			if (this.isPermanent) {
				AssetLatchImpl.this.isPermanentlyActivate = true;
			}

			// Release the job nodes in their own threads
			AwaitingJobNode awaitingJobNode = AssetLatchImpl.this.awaitingJobNodes.purgeEntries();
			while (awaitingJobNode != null) {

				// Release the job and continue its flow independently
				AssetLatchImpl.this.assetManager.getJobNodeDelegator().delegateJobNode(awaitingJobNode.jobNode, false);

				// Fail the next waiting job node
				awaitingJobNode = awaitingJobNode.getNext();
			}

			// No further job nodes to fail
			return null;
		}
	}

	/**
	 * {@link JobNode} to release the {@link AssetLatch} and fail the
	 * {@link JobNode} instances.
	 */
	private class FailJobNodes implements JobNode {

		/**
		 * Failure.
		 */
		private final Throwable failure;

		/**
		 * Indicates if proceeding is permanent.
		 */
		private final boolean isPermanent;

		/**
		 * Instantiate.
		 * 
		 * @param failure
		 *            Failure.
		 * @param isPermanent
		 *            Indicates if proceeding is permanent.
		 */
		public FailJobNodes(Throwable failure, boolean isPermanent) {
			this.failure = failure;
			this.isPermanent = isPermanent;
		}

		/*
		 * ======================== JobNode ============================
		 */

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public JobNode doJob(JobContext context) {

			// Flag if permanent failure
			if (this.isPermanent) {
				AssetLatchImpl.this.failure = this.failure;
				AssetLatchImpl.this.isPermanentlyActivate = true;
			}

			// Fail the job nodes in their own threads
			AwaitingJobNode awaitingJobNode = AssetLatchImpl.this.awaitingJobNodes.purgeEntries();
			while (awaitingJobNode != null) {

				// Fail the job and continue its flow independently
				AssetLatchImpl.this.assetManager.getJobNodeDelegator().delegateJobNode(
						new FailThreadStateJobNode(this.failure, awaitingJobNode.jobNode.getThreadState())
								.then(awaitingJobNode.jobNode),
						false);

				// Fail the next waiting job node
				awaitingJobNode = awaitingJobNode.getNext();
			}

			// No further job nodes to fail
			return null;
		}
	}

}