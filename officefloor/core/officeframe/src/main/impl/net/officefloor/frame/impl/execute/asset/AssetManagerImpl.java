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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.function.LinkedListSetFunctionLogic;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.OfficeClock;
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
	 * {@link OfficeClock}.
	 */
	private final OfficeClock clock;

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
	 *            {@link OfficeClock}.
	 * @param loop
	 *            {@link FunctionLoop}.
	 */
	public AssetManagerImpl(ProcessState officeManagerProcess, OfficeClock clock, FunctionLoop loop) {
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
	public FunctionState execute() throws Throwable {

		// Copy the list of latches
		LinkedListSetItem<AssetLatchImpl> head = this.latches.copyEntries();
		if (head == null) {
			return null; // no latches to check
		}

		// Undertake checks for each of the latches
		@SuppressWarnings({ "unchecked", "rawtypes" })
		FunctionLogic functionLogic = new LinkedListSetFunctionLogic(head, LATCH_TO_CHECK);
		return functionLogic.execute(new FlowImpl(null, this.officeManagerProcess.getMainThreadState()));
	}

	/**
	 * Obtains the check {@link FunctionState} for the {@link AssetLatch}.
	 */
	private static final Function<LinkedListSetItem<AssetLatchImpl>, FunctionState> LATCH_TO_CHECK = new Function<LinkedListSetItem<AssetLatchImpl>, FunctionState>() {
		@Override
		public FunctionState apply(LinkedListSetItem<AssetLatchImpl> latch) {
			return latch.getEntry().check();
		}
	};

}