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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.jobnode.LinkedListSetJobNode;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link AssetManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerImpl implements AssetManager {

	/**
	 * {@link ProcessState} that is managing the {@link Office}.
	 */
	private final ProcessState officeManagerProcess;

	/**
	 * {@link TeamManagement} responsible for the {@link Asset} instances.
	 */
	private final TeamManagement responsibleTeam;

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
	 * @param responsibleTeam
	 *            {@link TeamManagement} responsible for the {@link Asset}
	 *            instances.
	 */
	public AssetManagerImpl(ProcessState officeManagerProcess, TeamManagement responsibleTeam) {
		this.officeManagerProcess = officeManagerProcess;
		this.responsibleTeam = responsibleTeam;
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
		return new AssetLatchImpl(asset, this);
	}

	/*
	 * =================== JobNode ======================================
	 */

	@Override
	public JobNode doJob(JobContext context) {

		// Copy the list of latches
		LinkedListSetItem<AssetLatchImpl> head = this.latches.copyEntries();
		if (head == null) {
			return null; // no latches to check
		}

		// Undertake checks for each of the latches
		return new LinkedListSetJobNode<>(head);
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public ThreadState getThreadState() {
		return this.officeManagerProcess.getMainThreadState();
	}

}