package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * {@link ProcessMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMetaDataImpl implements ProcessMetaData {

	/**
	 * {@link Executive} to provide the process identifiers.
	 */
	private final Executive executive;

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Initiate.
	 * 
	 * @param executive             {@link Executive}.
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} instances.
	 * @param threadMetaData        {@link ThreadMetaData}.
	 */
	public ProcessMetaDataImpl(Executive executive, ManagedObjectMetaData<?>[] managedObjectMetaData,
			ThreadMetaData threadMetaData) {
		this.executive = executive;
		this.managedObjectMetaData = managedObjectMetaData;
		this.threadMetaData = threadMetaData;
	}

	/*
	 * ============== ProcessMetaData =================================
	 */

	@Override
	public Object createProcessIdentifier() {
		return this.executive.createProcessIdentifier();
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

}