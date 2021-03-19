/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
	private boolean isPermanentlyActivate = false;

	/**
	 * Permanent failure of the {@link Asset}.
	 */
	private Throwable failure = null;

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
	 * @param asset        {@link Asset} to be managed.
	 * @param assetManager {@link AssetManager} for managing this.
	 * @param clock        {@link MonitorClock}.
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
		this.releaseFunctions(isPermanent, null);
	}

	@Override
	public void releaseFunctions(boolean isPermanent, FunctionState functionState) {

		// Latch released, so proceed with functions
		FunctionState release = Promise.then(functionState, new ReleaseOperation(isPermanent));
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
	 * Abstract operation.
	 */
	private abstract class AbstractOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		/*
		 * =============== FunctionState ====================
		 */

		@Override
		public ThreadState getThreadState() {
			return AssetLatchImpl.this.asset.getOwningThreadState();
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return true; // async typically involves threading, so need to synchronise memories
		}
	}

	/**
	 * {@link FunctionLogic} to check on the {@link Asset}.
	 */
	private class CheckOperation extends AbstractOperation {

		/*
		 * =============== FunctionState ====================
		 */

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
		 * @param function {@link FunctionState} awaiting on this {@link AssetLatch}.
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
	private class AwaitingOperation extends AbstractOperation {

		/**
		 * {@link AwaitingEntry} to be registered.
		 */
		public final AwaitingEntry entry;

		/**
		 * Initiate.
		 * 
		 * @param entry {@link AwaitingEntry} to be registered.
		 */
		public AwaitingOperation(AwaitingEntry entry) {
			this.entry = entry;
		}

		/*
		 * =============== FunctionState ====================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to latch
			AssetLatchImpl latch = AssetLatchImpl.this;

			// Determine if release is permanent
			if (latch.isPermanentlyActivate) {

				// Fail immediately if failure of asset
				final Throwable failure = latch.failure;
				if (failure != null) {
					return new AbstractDelegateFunctionState(this.entry.function) {
						@Override
						public FunctionState execute(FunctionStateContext context) throws Throwable {
							throw failure;
						}
					};
				}

				// Proceed immediately with function
				return this.entry.function;
			}

			// Determine if first function waiting
			if (latch.awaiting.getHead() == null) {
				latch.assetManager.registerAssetLatch(latch);
			}

			// Register this entry with the latch
			latch.awaiting.addEntry(this.entry);

			// Nothing further, as function now waiting on latch
			return null;
		}
	}

	/**
	 * {@link FunctionState} to release the {@link AssetLatch} and proceed with
	 * awaiting {@link FunctionState} instances.
	 */
	private class ReleaseOperation extends AbstractOperation {

		/**
		 * Indicates if proceeding is permanent.
		 */
		private final boolean isPermanent;

		/**
		 * Instantiate.
		 * 
		 * @param isPermanent Indicates if proceeding is permanent.
		 */
		public ReleaseOperation(boolean isPermanent) {
			this.isPermanent = isPermanent;
		}

		/*
		 * =============== FunctionState ====================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to latch
			AssetLatchImpl latch = AssetLatchImpl.this;

			// Flag if permanent proceeding
			if (this.isPermanent) {
				latch.isPermanentlyActivate = true;
			}

			// Obtain the awaiting functions
			AwaitingEntry entry = latch.awaiting.purgeEntries();
			if (entry != null) {

				// Unregister from asset manager
				latch.assetManager.unregisterAssetLatch(latch);

				// Release the functions in their own threads
				do {
					// Release the function and continue its flow independently
					FunctionLoop functionLoop = latch.assetManager.getFunctionLoop();
					functionLoop.delegateFunction(entry.function);

					// Release the next waiting function
					entry = entry.getNext();
				} while (entry != null);
			}

			// Nothing further, as functions delegated
			return null;
		}
	}

	/**
	 * {@link FunctionState} to release the {@link AssetLatch} and fail the
	 * {@link FunctionState} instances.
	 */
	private class FailOperation extends AbstractOperation {

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
		 * @param failure     Failure.
		 * @param isPermanent Indicates if proceeding is permanent.
		 */
		public FailOperation(Throwable failure, boolean isPermanent) {
			this.failure = failure;
			this.isPermanent = isPermanent;
		}

		/*
		 * =============== FunctionState ====================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to latch
			AssetLatchImpl latch = AssetLatchImpl.this;

			// Flag if permanent failure
			if (this.isPermanent) {
				latch.isPermanentlyActivate = true;

				// Capture only the first permanent failure
				if (latch.failure == null) {
					latch.failure = this.failure;
				}
			}

			// Obtain the awaiting functions
			AwaitingEntry entry = latch.awaiting.purgeEntries();
			if (entry != null) {

				// Unregister from asset manager
				latch.assetManager.unregisterAssetLatch(latch);

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
