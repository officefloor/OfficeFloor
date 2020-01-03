package net.officefloor.compile.spi.managedobject;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} requird by a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlow {

	/**
	 * Obtains the name of this {@link ManagedObjectFlow}.
	 * 
	 * @return Name of this {@link ManagedObjectFlow}.
	 */
	String getManagedObjectFlowName();
}