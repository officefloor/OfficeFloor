package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManagerFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;

/**
 * {@link ManagedObjectSourceInstance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstanceImpl<F extends Enum<F>> implements ManagedObjectSourceInstance<F> {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, F> managedObjectSource;

	/**
	 * {@link ManagedObjectExecuteManagerFactory} for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectExecuteManagerFactory<F> managedObjectExecuteContextFactory;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectExecuteContextFactory
	 *            {@link ManagedObjectExecuteManagerFactory} for the
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 */
	public ManagedObjectSourceInstanceImpl(ManagedObjectSource<?, F> managedObjectSource,
			ManagedObjectExecuteManagerFactory<F> managedObjectExecuteContextFactory,
			ManagedObjectPool managedObjectPool) {
		this.managedObjectSource = managedObjectSource;
		this.managedObjectExecuteContextFactory = managedObjectExecuteContextFactory;
		this.managedObjectPool = managedObjectPool;
	}

	/*
	 * ==================== ManagedObjectSourceInstance ==================
	 */

	@Override
	public ManagedObjectSource<?, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectExecuteManagerFactory<F> getManagedObjectExecuteManagerFactory() {
		return this.managedObjectExecuteContextFactory;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

}