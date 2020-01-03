package net.officefloor.frame.internal.structure;

/**
 * Managed {@link FunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogic {

	/**
	 * Indicates if {@link ThreadState} safety is required for this
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return <code>true</code> should {@link ThreadState} safety be required for
	 *         this {@link ManagedFunctionLogic}.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the {@link ManagedFunctionLogic}.
	 * 
	 * @param context {@link ManagedFunctionLogicContext}.
	 * @throws Throwable Failure of logic.
	 */
	void execute(ManagedFunctionLogicContext context) throws Throwable;

}