package net.officefloor.frame.internal.structure;

/**
 * Context for executing a {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionStateContext {

	/**
	 * Wraps executing the delegate {@link FunctionState} to enable breaking the
	 * delegate chain. This avoids {@link StackOverflowError} issues.
	 * 
	 * @param delegate
	 *            Delegate {@link FunctionState}.
	 * @return Next {@link FunctionState} to execute.
	 * @throws Throwable
	 *             If failure in executing the delegate {@link FunctionState}.
	 */
	FunctionState executeDelegate(FunctionState delegate) throws Throwable;

}