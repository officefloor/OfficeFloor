package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Explorer of execution tree from {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionObjectExplorer {

	/**
	 * Explores the execution tree for the {@link ManagedObject}.
	 * 
	 * @param context {@link ExecutionObjectExplorerContext}.
	 * @throws Exception If failure in exploring the {@link ManagedObject}.
	 */
	void explore(ExecutionObjectExplorerContext context) throws Exception;

}