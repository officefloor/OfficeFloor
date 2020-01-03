package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Container managing the {@link Governance}.
 * <p>
 * {@link Governance} may only reside on the single {@link ThreadState}
 * requiring the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceContainer<E> {

	/**
	 * Indicates if {@link Governance} within this {@link GovernanceContainer} is
	 * active.
	 * 
	 * @return <code>true</code> if the {@link Governance} is active.
	 */
	boolean isGovernanceActive();

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 *
	 * @param <O>                      Object type.
	 * @param managedObjectExtension   Extension of the {@link ManagedObject} to
	 *                                 enable {@link Governance}.
	 * @param managedObjectContainer   {@link ManagedObjectContainer} for the
	 *                                 {@link ManagedObject}.
	 * @param managedObjectMetaData    {@link ManagedObjectMetaData} for the
	 *                                 {@link ManagedObject}.
	 * @param managedFunctionContainer {@link ManagedFunctionContainer} to enable
	 *                                 access to {@link ManagedFunctionContainer}
	 *                                 bound dependencies.
	 * @return {@link RegisteredGovernance}.
	 */
	<O extends Enum<O>> RegisteredGovernance registerManagedObject(E managedObjectExtension,
			ManagedObjectContainer managedObjectContainer, ManagedObjectMetaData<O> managedObjectMetaData,
			ManagedFunctionContainer managedFunctionContainer);

	/**
	 * Activates the {@link Governance}. This will co-ordinate the
	 * {@link Governance} over the {@link ManagedObject} instances.
	 * 
	 * @return {@link BlockState} to activate the {@link Governance}.
	 */
	BlockState activateGovernance();

	/**
	 * Enforces the {@link Governance}.
	 * 
	 * @return {@link BlockState} to enforce the {@link Governance}.
	 */
	BlockState enforceGovernance();

	/**
	 * Disregards the {@link Governance}.
	 * 
	 * @return {@link BlockState} to disregard the {@link Governance}.
	 */
	BlockState disregardGovernance();

	/**
	 * Deactivates the {@link Governance}. This will release the
	 * {@link ManagedObject} instances from {@link Governance}.
	 * 
	 * @return Deactivate the {@link Governance}.
	 */
	FunctionState deactivateGovernance();

}