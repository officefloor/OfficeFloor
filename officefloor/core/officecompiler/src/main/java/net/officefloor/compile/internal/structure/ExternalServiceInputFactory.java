package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Factory to create an {@link ExternalServiceInput} to {@link OfficeFloor}.
 *
 * @param <O> Type of object returned from the {@link ManagedObject}.
 * @param <M> Type of {@link ManagedObject}.
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceInputFactory<O, M extends ManagedObject> {

    /**
     * Creates the {@link ExternalServiceInput} for the {@link DeployedOfficeInput}.
     *
     * @param deployedOfficeInput {@link DeployedOfficeInput} to service.
     * @return {@link ExternalServiceInput}.
     */
    ExternalServiceInputNode<O, M> createExternalServiceInput(DeployedOfficeInput deployedOfficeInput);

}
