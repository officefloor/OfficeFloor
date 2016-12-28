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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link OfficeManager} {@link ProcessState}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeManagerProcessState implements ProcessState {

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Main {@link ThreadState}.
	 */
	private final ThreadState mainThreadState;

	/**
	 * Instantiate.
	 * 
	 * @param officeClock
	 *            {@link OfficeClock}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public OfficeManagerProcessState(OfficeClock officeClock, FunctionLoop functionLoop) {

		// Create the main thread asset manager
		AssetManager mainThreadAssetManager = new AssetManagerImpl(this, officeClock, functionLoop);

		// Create the meta-data for the process and its main thread state
		this.threadMetaData = new ThreadMetaDataImpl(new ManagedObjectMetaData[0], new GovernanceMetaData[0],
				new AdministratorMetaData[0], new EscalationProcedureImpl(), null);

		// Create the main thread state
		// Note: purpose of this to enable synchronising changes to office
		this.mainThreadState = new ThreadStateImpl(this.threadMetaData, mainThreadAssetManager, (FlowCompletion) null,
				this, null);
	}

	/*
	 * ========================= ProcessState =========================
	 */

	@Override
	public Object getProcessIdentifier() {
		return this;
	}

	@Override
	public ThreadState getMainThreadState() {
		return this.mainThreadState;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getFunctionMetaData(String functionName) throws UnknownFunctionException {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should be be involved in specific tasks");
	}

	@Override
	public FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion, AssetManager flowAssetManager) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should be be spawning threads");
	}

	@Override
	public FunctionState threadComplete(ThreadState thread) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should be be completing threads");
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " does not have managed objects");
	}

	@Override
	public ManagedObjectCleanup getManagedObjectCleanup() {
		throw new IllegalStateException(
				this.getClass().getSimpleName() + " should not need clean up as does not have managed objects");
	}

}