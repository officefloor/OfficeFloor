package net.officefloor.compile.spi.section;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} from a section {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionManagedObjectFlow extends ManagedObjectFlow, SectionFlowSourceNode {
}