package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ExternalServiceInput} {@link Node}.
 *
 * @param <O> Object type.
 * @param <M> {@link ManagedObject} type.
 */
public interface ExternalServiceInputNode<O, M extends ManagedObject> {

    /**
     * Obtains the {@link OfficeFloorManagedObjectFlow} to link for servicing the {@link ExternalServiceInput}.
     *
     * @return {@link OfficeFloorManagedObjectFlow} to link for servicing the {@link ExternalServiceInput}.
     */
    OfficeFloorManagedObjectFlow getOfficeFloorManagedObjectFlow();

    /**
     * Obtains the {@link ExternalServiceInput}.
     *
     * @return {@link ExternalServiceInput}.
     */
    ExternalServiceInput<O, M> getExternalServiceInput();

}
