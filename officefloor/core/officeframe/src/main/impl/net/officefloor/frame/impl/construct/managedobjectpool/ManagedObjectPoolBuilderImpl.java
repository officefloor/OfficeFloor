package net.officefloor.frame.impl.construct.managedobjectpool;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;

/**
 * Implements the {@link ManagedObjectPoolBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolBuilderImpl implements ManagedObjectPoolBuilder, ManagedObjectPoolConfiguration {

	/**
	 * {@link ManagedObjectPoolFactory}.
	 */
	private final ManagedObjectPoolFactory managedObjectPoolFactory;

	/**
	 * {@link ThreadCompletionListenerFactory} instances.
	 */
	private final List<ThreadCompletionListenerFactory> threadCompletionListenerFactories = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectPoolFactory
	 *            {@link ManagedObjectPoolFactory}.
	 */
	public ManagedObjectPoolBuilderImpl(ManagedObjectPoolFactory managedObjectPoolFactory) {
		this.managedObjectPoolFactory = managedObjectPoolFactory;
	}

	/*
	 * ======================= ManagedObjectPoolBuilder =======================
	 */

	@Override
	public void addThreadCompletionListener(ThreadCompletionListenerFactory threadCompletionListenerFactory) {
		this.threadCompletionListenerFactories.add(threadCompletionListenerFactory);
	}

	/*
	 * =================== ManagedObjectPoolConfiguration =====================
	 */

	@Override
	public ManagedObjectPoolFactory getManagedObjectPoolFactory() {
		return this.managedObjectPoolFactory;
	}

	@Override
	public ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories() {
		return this.threadCompletionListenerFactories.toArray(new ThreadCompletionListenerFactory[0]);
	}

}