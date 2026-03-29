package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;

/**
 * {@link ManagedObject} passed in externally.
 */
public interface InputManagedObject extends ManagedObject {

    /**
     * Invoked to clean the {@link ManagedObject} at end of servicing.
     *
     * @param cleanupEscalations {@link CleanupEscalation} instances on failure of servicing.
     * @throws Throwable If fails to handle the {@link CleanupEscalation} instances.
     */
    void clean(CleanupEscalation[] cleanupEscalations) throws Throwable;

}
