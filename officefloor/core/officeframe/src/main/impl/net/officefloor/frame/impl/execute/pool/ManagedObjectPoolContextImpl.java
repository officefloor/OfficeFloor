package net.officefloor.frame.impl.execute.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;

/**
 * {@link ManagedObjectPoolContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolContextImpl implements ManagedObjectPoolContext {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectPoolContextImpl(ManagedObjectSource<?, ?> managedObjectSource) {
		this.managedObjectSource = managedObjectSource;
	}

	/*
	 * ===================== ManagedObjectPoolContext ========================
	 */

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public boolean isCurrentThreadManaged() {
		return ManagedExecutionFactoryImpl.isCurrentThreadManaged();
	}

}