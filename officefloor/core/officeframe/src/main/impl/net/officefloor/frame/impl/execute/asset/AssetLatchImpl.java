/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link AssetLatch}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetLatchImpl extends AbstractLinkedListSetEntry<AssetLatchImpl, AssetManager>
		implements AssetLatch, CheckAssetContext {

	/**
	 * {@link Asset} being monitored.
	 */
	private final Asset asset;

	/**
	 * {@link AssetManager} to manage this {@link AssetLatch}.
	 */
	private final AssetManagerImpl assetManager;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock clock;

	/**
	 * Flag indicating to permanently activate waiting {@link FunctionState}
	 * instances.
	 */
	private volatile boolean isPermanentlyActivate = false;

	/**
	 * Permanent failure of the {@link Asset}.
	 */
	private volatile Throwable failure = null;

	/**
	 * Set of {@link FunctionState} instances waiting on the {@link Asset}.
	 */
	private final LinkedListSet<AwaitingEntry, AssetLatch> awaiting = new ComparatorLinkedListSet<AwaitingEntry, AssetLatch>() {
		@Override
		protected AssetLatch getOwner() {
			return AssetLatchImpl.this;
		}

		@Override
		protected boolean isEqual(AwaitingEntry entryA, AwaitingEntry entryB) {
			return (entryA.function == entryB.function);
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param asset
	 *            {@link Asset} to be managed.
	 * @param assetManager
	 *            {@link AssetManager} for managing this.
	 * @param clock
	 *            {@link MonitorClock}.
	 */
	public AssetLatchImpl(Asset asset, AssetManagerImpl assetManager, MonitorClock clock) {
		this.asset = asset;
		this.assetManager = assetManager;
		this.clock = clock;
	}

	/**
	 * Creates {@link FunctionState} to check this {@link AssetLatch}.
	 * 
	 * @return {@link FunctionState} to check this {@link AssetLatch}.
	 */
	FunctionState check() {
		return new CheckOperation();
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
	public FunctionState awaitOnAsset(FunctionState function) {

		// Undertake permanent release of latch
		if (this.isPermanentlyActivate) {

			// Fail immediately if failure of asset
			final Throwable failure = this.failure;
			if (failure != null) {
				return new AbstractDelegateFunctionState(function) {
					@Override
					public FunctionState execute(FunctionStateContext context) throws Throwable {
						throw failure;
					}
				};
			}

			// Activate immediately if permanently active
			return function;
		}

		// Return operation to wait on the asset
		return new AwaitingOperation(new AwaitingEntry(function));
	}

	/*
	 * ================= CheckAssetContext ====================================
	 */

	@Override
	public long getTime() {
		return this.clock.currentTimeMillis();
	}

	@Override
	public void releaseFunctions(boolean isPermanent) {
		// Latch released, so proceed with functions
		ReleaseOperation release = new ReleaseOperation(isPermanent);
		FunctionLoop loop = this.assetManager.getFunctionLoop();
		loop.delegateFunction(release);
	}

	@Override
	public void failFunctions(Throwable failure, boolean isPermanent) {
		// Latch failed, so fail the functions
		FailOperation fail = new FailOperation(failure, isPermanent);
		FunctionLoop loop = this.assetManager.getFunctionLoop();
		loop.delegateFunction(fail);
	}

	/**
	 * {@link FunctionLogic} to check on the {@link Asset}.
	 */
	private class CheckOperation extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.asset.getOwningThreadState();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			try {
				// Check on the asset
				AssetLatchImpl.this.asset.checkOnAsset(AssetLatchImpl.this);

			} catch (Throwable ex) {
				// Fail the functions
				return new FailOperation(ex, false);
			}

			// Nothing further, as release/fail functions continue independently
			return null;
		}
	}

	/**
	 * Awaiting entry.
	 */
	private class AwaitingEntry extends AbstractLinkedListSetEntry<AwaitingEntry, AssetLatch> {

		/**
		 * {@link FunctionState} awaiting on this {@link AssetLatch}.
		 */
		private FunctionState function;

		/**
		 * Instantiate.
		 * 
		 * @param function
		 *            {@link FunctionState} awaiting on this {@link AssetLatch}.
		 */
		public AwaitingEntry(FunctionState function) {
			this.function = function;
		}

		@Override
		public AssetLatchImpl getLinkedListSetOwner() {
			return AssetLatchImpl.this;
		}
	}

	/**
	 * {@link FunctionState} waiting on the {@link AssetLatch}.
	 */
	private class AwaitingOperation extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		/**
		 * {@link AwaitingEntry} to be registered.
		 */
		public final AwaitingEntry entry;

		/**
		 * Initiate.
		 * 
		 * @param entry
		 *            {@link AwaitingEntry} to be registered.
		 */
		public AwaitingOperation(AwaitingEntry entry) {
			this.entry = entry;
		}

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Determine if release is permanent
			if (AssetLatchImpl.this.isPermanentlyActivate) {
				// Proceed immediately with functions
				return Promise.then(this.entry.function, new ReleaseOperation(true));
			}

			// Determine if first function waiting
			if (AssetLatchImpl.this.awaiting.getHead() == null) {
				AssetLatchImpl.this.assetManager.registerAssetLatch(AssetLatchImpl.this);
			}

			// Register this entry with the latch
			AssetLatchImpl.this.awaiting.addEntry(this.entry);

			// Nothing further, as function now waiting on latch
			return null;
		}
	}

	/**
	 * {@link FunctionState} to release the {@link AssetLatch} and proceed with
	 * awaiting {@link FunctionState} instances.
	 */
	private class ReleaseOperation extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

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
		public ReleaseOperation(boolean isPermanent) {
			this.isPermanent = isPermanent;
		}

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Flag if permanent proceeding
			if (this.isPermanent) {
				AssetLatchImpl.this.isPermanentlyActivate = true;
			}

			// Obtain the awaiting functions
			AwaitingEntry entry = AssetLatchImpl.this.awaiting.purgeEntries();
			if (entry != null) {

				// Unregister from asset manager
				AssetLatchImpl.this.assetManager.unregisterAssetLatch(AssetLatchImpl.this);

				// Release the functions in their own threads
				do {
					// Release the function and continue its flow independently
					FunctionLoop functionLoop = AssetLatchImpl.this.assetManager.getFunctionLoop();
					functionLoop.delegateFunction(entry.function);

					// Release the next waiting function
					entry = entry.getNext();
				} while (entry != null);
			}

			// No further job nodes to fail
			return null;
		}
	}

	/**
	 * {@link FunctionState} to release the {@link AssetLatch} and fail the
	 * {@link FunctionState} instances.
	 */
	private class FailOperation extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

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
		public FailOperation(Throwable failure, boolean isPermanent) {
			this.failure = failure;
			this.isPermanent = isPermanent;
		}

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.assetManager.getThreadState();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to latch
			AssetLatchImpl latch = AssetLatchImpl.this;

			// Flag if permanent failure
			if (this.isPermanent) {
				latch.failure = this.failure;
				latch.isPermanentlyActivate = true;
			}

			// Obtain the awaiting functions
			AwaitingEntry entry = latch.awaiting.purgeEntries();
			if (entry != null) {

				// Unregister from asset manager
				latch.assetManager.unregisterAssetLatch(AssetLatchImpl.this);

				// Fail the functions in their own threads
				do {
					// Fail the function and continue its flow independently
					latch.assetManager.getFunctionLoop()
							.delegateFunction(new AbstractDelegateFunctionState(entry.function) {
								@Override
								public FunctionState execute(FunctionStateContext context) throws Throwable {
									throw FailOperation.this.failure;
								}
							});

					// Fail the next waiting function
					entry = entry.getNext();
				} while (entry != null);
			}

			// No further functions to fail
			return null;
		}
	}

}