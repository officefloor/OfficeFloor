package net.officefloor.compile.impl.pool;

import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * {@link ManagedObjectPoolType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolTypeImpl implements ManagedObjectPoolType {

	/**
	 * Pooled object type.
	 */
	private final Class<?> pooledObjectType;

	/**
	 * {@link ManagedObjectPoolFactory}.
	 */
	private final ManagedObjectPoolFactory managedObjectPoolFactory;

	/**
	 * {@link ThreadCompletionListenerFactory} instances.
	 */
	private final ThreadCompletionListenerFactory[] threadCompletionListenerFactories;

	/**
	 * Instantiate.
	 * 
	 * @param pooledObjectType
	 *            Pooled object type.
	 * @param managedObjectPoolFactory
	 *            {@link ManagedObjectPoolFactory}.
	 * @param threadCompletionListenerFactories
	 *            {@link ThreadCompletionListenerFactory} instances.
	 */
	public ManagedObjectPoolTypeImpl(Class<?> pooledObjectType, ManagedObjectPoolFactory managedObjectPoolFactory,
			ThreadCompletionListenerFactory[] threadCompletionListenerFactories) {
		this.pooledObjectType = pooledObjectType;
		this.managedObjectPoolFactory = managedObjectPoolFactory;
		this.threadCompletionListenerFactories = threadCompletionListenerFactories;
	}

	/*
	 * ==================== ManagedObjectPoolType =======================
	 */

	@Override
	public Class<?> getPooledObjectType() {
		return this.pooledObjectType;
	}

	@Override
	public ManagedObjectPoolFactory getManagedObjectPoolFactory() {
		return this.managedObjectPoolFactory;
	}

	@Override
	public ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories() {
		return this.threadCompletionListenerFactories;
	}

}