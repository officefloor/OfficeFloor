package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Registered {@link ManagedObject} with the {@link Governance}.
 * <p>
 * Must be executed as a {@link FunctionState} to register the
 * {@link ManagedObject} with the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RegisteredGovernance extends FunctionState {
}