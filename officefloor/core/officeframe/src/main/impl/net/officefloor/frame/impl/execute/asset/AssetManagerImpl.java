package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise.Translate;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link AssetManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerImpl extends AbstractLinkedListSetEntry<FunctionState, Flow> implements AssetManager {

	/**
	 * {@link ProcessState} that is managing the {@link Office}.
	 */
	private final ProcessState officeManagerProcess;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock clock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop loop;

	/**
	 * {@link LinkedListSet} of {@link AssetLatch} instances requiring managing.
	 */
	private final LinkedListSet<AssetLatchImpl, AssetManager> latches = new StrictLinkedListSet<AssetLatchImpl, AssetManager>() {
		@Override
		protected AssetManager getOwner() {
			return AssetManagerImpl.this;
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param officeManagerProcess
	 *            {@link ProcessState} that is managing the {@link Office}.
	 * @param clock
	 *            {@link MonitorClock}.
	 * @param loop
	 *            {@link FunctionLoop}.
	 */
	public AssetManagerImpl(ProcessState officeManagerProcess, MonitorClock clock, FunctionLoop loop) {
		this.officeManagerProcess = officeManagerProcess;
		this.clock = clock;
		this.loop = loop;
	}

	/**
	 * Obtains the {@link FunctionLoop}.
	 * 
	 * @return {@link FunctionLoop}.
	 */
	FunctionLoop getFunctionLoop() {
		return this.loop;
	}

	/**
	 * Registers the {@link AssetLatch}.
	 * 
	 * @param latch
	 *            {@link AssetLatch} to register.
	 */
	void registerAssetLatch(AssetLatchImpl latch) {
		this.latches.addEntry(latch);
	}

	/**
	 * Unregisters the {@link AssetLatch}.
	 * 
	 * @param latch
	 *            {@link AssetLatch} to unregister.
	 */
	void unregisterAssetLatch(AssetLatchImpl latch) {
		this.latches.removeEntry(latch);
	}

	/*
	 * ================ AssetManager ======================================
	 */

	@Override
	public AssetLatch createAssetLatch(Asset asset) {
		return new AssetLatchImpl(asset, this, this.clock);
	}

	/*
	 * ================ FunctionState =====================================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.officeManagerProcess.getMainThreadState();
	}

	@Override
	public FunctionState execute(FunctionStateContext context) throws Throwable {
		// Undertake checks for each of the latches
		return LinkedListSetPromise.all(this.latches, LATCH_TO_CHECK);
	}

	/**
	 * Obtains the check {@link FunctionState} for the {@link AssetLatch}.
	 */
	private static final Translate<AssetLatchImpl> LATCH_TO_CHECK = new Translate<AssetLatchImpl>() {
		@Override
		public FunctionState translate(AssetLatchImpl latch) {
			return latch.check();
		}
	};

}