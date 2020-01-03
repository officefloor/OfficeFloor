package net.officefloor.frame.api.managedobject.recycle;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Parameter to the recycle {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RecycleManagedObjectParameter<MO extends ManagedObject> {

	/**
	 * Convenience method to obtain the {@link RecycleManagedObjectParameter} from
	 * the {@link ManagedFunctionContext}.
	 * 
	 * @param <M>
	 *            {@link ManagedObject} type.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @return {@link RecycleManagedObjectParameter}.
	 */
	@SuppressWarnings("unchecked")
	static <M extends ManagedObject> RecycleManagedObjectParameter<M> getRecycleManagedObjectParameter(
			ManagedFunctionContext<?, ?> context) {
		return (RecycleManagedObjectParameter<M>) context.getObject(0);
	}

	/**
	 * Obtains the {@link ManagedObject} being recycled.
	 * 
	 * @return {@link ManagedObject} being recycled.
	 */
	MO getManagedObject();

	/**
	 * <p>
	 * Invoked at the end of recycling to re-use the {@link ManagedObject}.
	 * </p>
	 * Should this method not be invoked, the {@link ManagedObject} will be
	 * destroyed.
	 */
	void reuseManagedObject();

	/**
	 * Obtains possible {@link CleanupEscalation} instances that occurred in
	 * cleaning up previous {@link ManagedObject} instances.
	 * 
	 * @return Possible {@link CleanupEscalation} instances.
	 */
	CleanupEscalation[] getCleanupEscalations();

}