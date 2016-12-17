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

import java.util.function.Function;

import net.officefloor.frame.impl.execute.job.SafeJobImpl;
import net.officefloor.frame.impl.execute.jobnode.FailThreadStateJobNode;
import net.officefloor.frame.impl.execute.jobnode.LinkedListSetJobNode;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Implementation of the {@link AssetLatch}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetLatchImpl extends AbstractLinkedListSetEntry<AssetLatchImpl, AssetManager>
		implements AssetLatch, JobNode {

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
	public JobNode waitOnAsset(JobNode jobNode) {

		// Undertake permanent release of latch
		if (this.isPermanentlyActivate) {

			// Fail immediately if failure of asset
			Throwable failure = this.failure;
			if (failure != null) {
				return new FailThreadStateJobNode(failure, jobNode);
			}

			// Activate immediately if permanently active
			return jobNode;
		}

		// Return job to wait on the asset
		return new AwaitingJobNode(jobNode);
	}

	@Override
	public void proceedWithJobNodes(boolean isPermanent) {

		// Latch released, so proceed with job nodes
		ReleaseJobNode proceed = new ReleaseJobNode(isPermanent);
		TeamManagement responsibleTeam = proceed.getResponsibleTeam();
		responsibleTeam.getTeam().assignJob(new SafeJobImpl(proceed), responsibleTeam.getIdentifier());
	}

	@Override
	public void failJobNodes(Throwable failure, boolean isPermanent) {
		// TODO implement AssetLatch.failJobNodes
		throw new UnsupportedOperationException("TODO implement AssetLatch.failJobNodes");

	}

	/*
	 * ================= JobNode ==========================================
	 */

	@Override
	public JobNode doJob(JobContext context) {
		this.asset.checkOnAsset(new CheckAssetContext() {

			@Override
			public long getTime() {
				return OfficeManagerImpl.currentTimeMillis();
			}

			@Override
			public void proceedWithJobNodes(boolean isPermanent) {
				// TODO implement Type1481993905345.proceedWithJobNodes
				throw new UnsupportedOperationException("TODO implement Type1481993905345.proceedWithJobNodes");

			}

			@Override
			public void failJobNodes(Throwable failure, boolean isPermanent) {
				// TODO implement Type1481993905345.failJobNodes
				throw new UnsupportedOperationException("TODO implement Type1481993905345.failJobNodes");

			}
		});
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		// Any team is fine to check on asset
		return null;
	}

	@Override
	public ThreadState getThreadState() {
		return this.asset.getOwningThreadState();
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
		public JobNode doJob(JobContext context) {

			// Determine if proceeding is permanent
			if (AssetLatchImpl.this.isPermanentlyActivate) {

				// Proceed immediately with job nodes
				return new ReleaseJobNode(true);
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

		@Override
		public TeamManagement getResponsibleTeam() {
			return null; // any team is fine
		}

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}
	}

	/**
	 * Transforms the {@link AwaitingJobNode} to a {@link JobNode} to release
	 * the {@link JobNode}.
	 */
	private static final Function<LinkedListSetItem<AwaitingJobNode>, JobNode> RELEASE_JOB_NODE_FACTORY = new Function<LinkedListSetItem<AwaitingJobNode>, JobNode>() {
		@Override
		public JobNode apply(LinkedListSetItem<AwaitingJobNode> awaitingJobNode) {
			return awaitingJobNode.getEntry().jobNode;
		}
	};

	/**
	 * {@link JobNode} to release the {@link AssetLatch} and proceed with
	 * awaiting {@link JobNode} instances.
	 */
	private class ReleaseJobNode implements JobNode {

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
		public ReleaseJobNode(boolean isPermanent) {
			this.isPermanent = isPermanent;
		}

		/*
		 * ======================== JobNode ============================
		 */

		@Override
		public JobNode doJob(JobContext context) {

			// Flag if permanent proceeding
			if (this.isPermanent) {
				AssetLatchImpl.this.isPermanentlyActivate = true;
			}

			// Obtain the job nodes to release
			LinkedListSetItem<AwaitingJobNode> releasedJobNodesHead = AssetLatchImpl.this.awaitingJobNodes
					.copyEntries();
			AssetLatchImpl.this.awaitingJobNodes.purgeEntries();

			// Release the job nodes
			return new LinkedListSetJobNode<AwaitingJobNode>(releasedJobNodesHead, RELEASE_JOB_NODE_FACTORY);
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return AssetLatchImpl.this.assetManager.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}
	}

}