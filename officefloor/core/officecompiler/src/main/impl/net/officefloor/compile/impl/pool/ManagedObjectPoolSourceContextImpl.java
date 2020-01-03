package net.officefloor.compile.impl.pool;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ManagedObjectPoolSource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolSourceContextImpl extends SourceContextImpl implements ManagedObjectPoolSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param managedObjectPoolName Name of the {@link ManagedObjectPool}.
	 * @param isLoadingType         Indicates if loading type.
	 * @param properties            Properties.
	 * @param sourceContext         Delegate {@link SourceContext}.
	 */
	public ManagedObjectPoolSourceContextImpl(String managedObjectPoolName, boolean isLoadingType,
			SourceProperties properties, SourceContext sourceContext) {
		super(managedObjectPoolName, isLoadingType, sourceContext, properties);
	}

}