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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.Promise;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data of the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectMetaDataImpl<D extends Enum<D>> implements ManagedObjectMetaData<D> {

	/**
	 * Name of the {@link ManagedObject} bound within the
	 * {@link ManagedObjectScope}.
	 */
	private final String boundManagedObjectName;

	/**
	 * Type of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Instance index.
	 */
	private final int instanceIndex;

	/**
	 * {@link ManagedObjectSource} of the {@link ManagedObject}.
	 */
	private final ManagedObjectSource<?, ?> source;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link NameAwareManagedObject}.
	 */
	private final boolean isNameAwareManagedObject;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link AsynchronousManagedObject}.
	 */
	private final boolean isManagedObjectAsynchronous;

	/**
	 * Timeout of an asynchronous operation.
	 */
	private final long timeout;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link CoordinatingManagedObject}.
	 */
	private final boolean isCoordinatingManagedObject;

	/**
	 * {@link ManagedObjectIndex} for the dependencies in the index order
	 * required.
	 */
	private final ManagedObjectIndex[] dependencyMapping;

	/**
	 * {@link ManagedObjectPool} of the {@link ManagedObject}.
	 */
	private final ManagedObjectPool pool;

	/**
	 * {@link AssetManager} to manage the sourcing of the {@link ManagedObject}
	 * instances.
	 */
	private final AssetManager sourcingManager;

	/**
	 * <p>
	 * {@link AssetManager} to manage the asynchronous operations on the
	 * {@link ManagedObject} instances.
	 * <p>
	 * Should be <code>null</code> if {@link ManagedObject} is not
	 * {@link AsynchronousManagedObject}.
	 */
	private final AssetManager operationsManager;

	/**
	 * {@link ManagedObjectGovernanceMetaData} instances applicable to this
	 * {@link ManagedObject}.
	 */
	private final ManagedObjectGovernanceMetaData<?>[] governanceMetaData;

	/**
	 * {@link OfficeMetaData} containing this {@link ManagedObjectMetaData} to
	 * create the {@link Job} instances.
	 */
	private OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} for the recycling of this {@link ManagedObject}.
	 */
	private FlowMetaData<?> recycleFlowMetaData;

	/**
	 * Initiate with meta-data of the {@link ManagedObject} to source specific
	 * to the {@link Work}.
	 * 
	 * @param boundManagedObjectName
	 *            Name of the {@link ManagedObject} bound within the
	 *            {@link ManagedObjectScope}.
	 * @param objectType
	 *            Type of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param instanceIndex
	 *            Instance index.
	 * @param source
	 *            {@link ManagedObjectSource} of the {@link ManagedObject}.
	 * @param isNameAwareManagedObject
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link NameAwareManagedObject}.
	 * @param pool
	 *            {@link ManagedObjectPool} of the {@link ManagedObject}.
	 * @param sourcingManager
	 *            {@link AssetManager} to manage the sourcing of the
	 *            {@link ManagedObject} instances.
	 * @param isManagedObjectAsynchronous
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link AsynchronousManagedObject}.
	 * @param operationsManager
	 *            {@link AssetManager} to manage the asynchronous operations on
	 *            the {@link ManagedObject} instances.
	 * @param isCoordinatingManagedObject
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link CoordinatingManagedObject}.
	 * @param dependencyMapping
	 *            {@link ManagedObjectIndex} for the dependencies in the index
	 *            order required.
	 * @param timeout
	 *            Timeout of an asynchronous operation by the
	 *            {@link ManagedObject} being managed.
	 * @param governanceMetaData
	 *            {@link ManagedObjectGovernanceMetaData} instances applicable
	 *            to this {@link ManagedObject}.
	 */
	public ManagedObjectMetaDataImpl(String boundManagedObjectName, Class<?> objectType, int instanceIndex,
			ManagedObjectSource<?, ?> source, ManagedObjectPool pool, boolean isNameAwareManagedObject,
			AssetManager sourcingManager, boolean isManagedObjectAsynchronous, AssetManager operationsManager,
			boolean isCoordinatingManagedObject, ManagedObjectIndex[] dependencyMapping, long timeout,
			ManagedObjectGovernanceMetaData<?>[] governanceMetaData) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.objectType = objectType;
		this.instanceIndex = instanceIndex;
		this.source = source;
		this.timeout = timeout;
		this.isNameAwareManagedObject = isNameAwareManagedObject;
		this.isCoordinatingManagedObject = isCoordinatingManagedObject;
		this.dependencyMapping = dependencyMapping;
		this.pool = pool;
		this.isManagedObjectAsynchronous = isManagedObjectAsynchronous;
		this.sourcingManager = sourcingManager;
		this.operationsManager = operationsManager;
		this.governanceMetaData = governanceMetaData;
	}

	/**
	 * Loads the remaining state of this {@link ManagedObjectMetaData}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} of the {@link Office} containing this
	 *            {@link ManagedObjectMetaData}.
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} for the recycling of this
	 *            {@link ManagedObject}.
	 */
	public void loadRemainingState(OfficeMetaData officeMetaData, FlowMetaData<?> recycleFlowMetaData) {
		this.officeMetaData = officeMetaData;
		this.recycleFlowMetaData = recycleFlowMetaData;
	}

	/*
	 * ================= ManagedObjectMetaData ============================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public int getInstanceIndex() {
		return this.instanceIndex;
	}

	@Override
	public ManagedObjectContainer createManagedObjectContainer(ThreadState threadState) {
		return new ManagedObjectContainerImpl(this, threadState);
	}

	@Override
	public AssetManager getSourcingManager() {
		return this.sourcingManager;
	}

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.source;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public boolean isNameAwareManagedObject() {
		return this.isNameAwareManagedObject;
	}

	@Override
	public boolean isManagedObjectAsynchronous() {
		return this.isManagedObjectAsynchronous;
	}

	@Override
	public AssetManager getOperationsManager() {
		return this.operationsManager;
	}

	@Override
	public boolean isCoordinatingManagedObject() {
		return this.isCoordinatingManagedObject;
	}

	@Override
	public FunctionLoop getJobNodeLoop() {
		return this.officeMetaData.getFunctionLoop();
	}

	@Override
	public OfficeClock getOfficeClock() {
		return this.officeMetaData.getOfficeClock();
	}

	@Override
	public FunctionState createReadyCheckJobNode(ManagedObjectReadyCheck check, WorkContainer<?> workContainer,
			ManagedObjectContainer currentContainer) {

		// Ensure there are dependencies
		if ((this.dependencyMapping.length == 0) && (currentContainer == null)) {
			return null; // nothing to check
		}

		// Create the managed object ready check wrapper
		ManagedObjectReadyCheckWrapper wrapper = new ManagedObjectReadyCheckWrapper(check);

		// Create the array of check job nodes
		FunctionState[] checkJobNodes = new FunctionState[this.dependencyMapping.length
				+ ((currentContainer != null) ? 1 : 0)];
		for (int i = 0; i < this.dependencyMapping.length; i++) {
			checkJobNodes[i] = workContainer.getManagedObjectContainer(this.dependencyMapping[i])
					.createCheckReadyJobNode(wrapper);
		}
		if (currentContainer != null) {
			checkJobNodes[checkJobNodes.length - 1] = currentContainer.createCheckReadyJobNode(wrapper);
		}

		// Return job to check all managed objects are ready
		return new CheckReadyJobNode(wrapper, checkJobNodes, 0);
	}

	/**
	 * Wrapper around {@link ManagedObjectReadyCheck} to determine if
	 * {@link ManagedObject} was not ready.
	 */
	private static class ManagedObjectReadyCheckWrapper implements ManagedObjectReadyCheck {

		/**
		 * Delegate {@link ManagedObjectReadyCheck}.
		 */
		private final ManagedObjectReadyCheck delegate;

		/**
		 * Indicates if ready.
		 */
		private boolean isReady = true;

		/**
		 * Instantiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedObjectReadyCheck}.
		 */
		public ManagedObjectReadyCheckWrapper(ManagedObjectReadyCheck delegate) {
			this.delegate = delegate;
		}

		/*
		 * ====================== ManagedObjectReadyCheck ====================
		 */

		@Override
		public ManagedFunctionContainer getManagedJobNode() {
			return this.delegate.getManagedJobNode();
		}

		@Override
		public FunctionState setNotReady() {

			// Flag not ready
			this.isReady = false;

			// Return not ready job node
			return this.delegate.setNotReady();
		}
	}

	/**
	 * {@link FunctionState} to check {@link ManagedObject} is ready.
	 */
	private static class CheckReadyJobNode implements FunctionState {

		/**
		 * {@link ManagedObjectReadyCheckWrapper}.
		 */
		private final ManagedObjectReadyCheckWrapper readyCheck;

		/**
		 * {@link FunctionState} instances to check the necessary
		 * {@link ManagedObject} instances are ready.
		 */
		private final FunctionState[] checkJobNodes;

		/**
		 * Index of the {@link FunctionState} to use for checking.
		 */
		private final int currentJobNodeIndex;

		/**
		 * Instantiate.
		 * 
		 * @param readyCheck
		 *            {@link ManagedObjectReadyCheckWrapper}.
		 * @param checkJobNodes
		 *            {@link FunctionState} instances to check the necessary
		 *            {@link ManagedObject} instances are ready.
		 * @param currentJobNodeIndex
		 *            Index of the {@link FunctionState} to use for checking.
		 */
		public CheckReadyJobNode(ManagedObjectReadyCheckWrapper readyCheck, FunctionState[] checkJobNodes,
				int currentJobNodeIndex) {
			this.readyCheck = readyCheck;
			this.checkJobNodes = checkJobNodes;
			this.currentJobNodeIndex = currentJobNodeIndex;
		}

		/*
		 * ====================== JobNode ====================
		 */

		@Override
		public ThreadState getThreadState() {
			return this.checkJobNodes[this.currentJobNodeIndex].getThreadState();
		}

		@Override
		public FunctionState execute() {

			// Undertake check for current job node
			FunctionState currentJobNode = this.checkJobNodes[this.currentJobNodeIndex];
			FunctionState nextJobNode = currentJobNode.execute();

			// Determine if not ready (only single job checking)
			if (!this.readyCheck.isReady) {
				// Not ready, so no further checking necessary
				return nextJobNode;
			}

			// Determine if last check
			int nextJobNodeIndex = this.currentJobNodeIndex + 1;
			if (nextJobNodeIndex >= this.checkJobNodes.length) {
				// No further checks
				return nextJobNode;
			}

			// Continue with check of next managed object
			return Promise.then(nextJobNode,
					new CheckReadyJobNode(this.readyCheck, this.checkJobNodes, nextJobNodeIndex));
		}
	}

	@Override
	public <W extends Work> ObjectRegistry<D> createObjectRegistry(WorkContainer<W> workContainer,
			ThreadState threadState) {
		return new ObjectRegistryImpl<D>(workContainer, this.dependencyMapping, threadState);
	}

	@Override
	public ManagedObjectGovernanceMetaData<?>[] getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public FunctionState createRecycleJobNode(ManagedObject managedObject, ManagedObjectCleanup cleanup) {
		return cleanup.createCleanUpJobNode(this.recycleFlowMetaData, this.objectType, managedObject, this.pool);
	}

}