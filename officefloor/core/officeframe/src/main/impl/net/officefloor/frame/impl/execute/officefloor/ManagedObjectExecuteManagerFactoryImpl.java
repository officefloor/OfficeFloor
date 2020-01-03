package net.officefloor.frame.impl.execute.officefloor;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManagerFactory;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ManagedObjectExecuteManagerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteManagerFactoryImpl<F extends Enum<F>>
		implements ManagedObjectExecuteManagerFactory<F> {

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * Index of the {@link ManagedObject} within the {@link ProcessState}.
	 */
	private final int processMoIndex;

	/**
	 * {@link FlowMetaData} in index order for the {@link ManagedObjectSource}.
	 */
	private final FlowMetaData[] processLinks;

	/**
	 * {@link ExecutionStrategy} instances in index order for the
	 * {@link ManagedObjectSource}.
	 */
	private final ThreadFactory[][] executionStrategies;

	/**
	 * {@link Logger} for {@link ManagedObjectExecuteContext}.
	 */
	private final Logger executeLogger;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Instantiate for {@link ManagedObjectExecuteContext} that has no
	 * {@link FlowMetaData}.
	 * 
	 * @param executionStrategies {@link ExecutionStrategy} instances in index order
	 *                            for the {@link ManagedObjectSource}.
	 * @param executeLogger       {@link Logger} for the
	 *                            {@link ManagedObjectExecuteContext}.
	 */
	public ManagedObjectExecuteManagerFactoryImpl(ThreadFactory[][] executionStrategies, Logger executeLogger) {
		this.managedObjectMetaData = null;
		this.processMoIndex = -1;
		this.processLinks = new FlowMetaData[0];
		this.executionStrategies = executionStrategies;
		this.executeLogger = executeLogger;
		this.officeMetaData = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} of the
	 *                              {@link ManagedObject}.
	 * @param processMoIndex        Index of the {@link ManagedObject} within the
	 *                              {@link ProcessState}.
	 * @param processLinks          {@link FlowMetaData} in index order for the
	 *                              {@link ManagedObjectSource}.
	 * @param executionStrategies   {@link ExecutionStrategy} instances in index
	 *                              order for the {@link ManagedObjectSource}.
	 * @param executeLogger         {@link Logger} for the
	 *                              {@link ManagedObjectExecuteContext}.
	 * @param officeMetaData        {@link OfficeMetaData} to create
	 *                              {@link ProcessState} instances.
	 */
	public ManagedObjectExecuteManagerFactoryImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, ThreadFactory[][] executionStrategies, Logger executeLogger,
			OfficeMetaData officeMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.executionStrategies = executionStrategies;
		this.executeLogger = executeLogger;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ================ ManagedObjectExecuteManagerFactory ==================
	 */

	@Override
	public ManagedObjectExecuteManager<F> createManagedObjectExecuteManager() {
		return new ManagedObjectExecuteManagerImpl<>(this.managedObjectMetaData, this.processMoIndex, this.processLinks,
				this.executionStrategies, this.executeLogger, this.officeMetaData);
	}

}