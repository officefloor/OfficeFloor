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

import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise.Translate;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
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
	 * {@link ProcessState} that is managing this {@link AssetManager}.
	 */
	private final ProcessState managingProcess;

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
	 * @param managingProcess {@link ProcessState} that is managing this
	 *                        {@link AssetManager}.
	 * @param clock           {@link MonitorClock}.
	 * @param loop            {@link FunctionLoop}.
	 */
	public AssetManagerImpl(ProcessState managingProcess, MonitorClock clock, FunctionLoop loop) {
		this.managingProcess = managingProcess;
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
	 * @param latch {@link AssetLatch} to register.
	 */
	void registerAssetLatch(AssetLatchImpl latch) {
		this.getThreadState().runThreadSafeOperation(() -> {
			this.latches.addEntry(latch);
			return null;
		});
	}

	/**
	 * Unregisters the {@link AssetLatch}.
	 * 
	 * @param latch {@link AssetLatch} to unregister.
	 */
	void unregisterAssetLatch(AssetLatchImpl latch) {
		this.getThreadState().runThreadSafeOperation(() -> {
			this.latches.removeEntry(latch);
			return null;
		});
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
		return this.managingProcess.getMainThreadState();
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
