package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;

/**
 * {@link ManagedObjectFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowNode extends LinkFlowNode, AugmentedManagedObjectFlow, SectionManagedObjectFlow,
		OfficeManagedObjectFlow, OfficeFloorManagedObjectFlow {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Flow";

	/**
	 * Initialises the {@link ManagedObjectFlowNode}.
	 */
	void initialise();

}