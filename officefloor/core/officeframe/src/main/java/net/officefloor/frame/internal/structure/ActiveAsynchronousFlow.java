package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.AsynchronousFlow;

/**
 * Actively executing {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveAsynchronousFlow extends LinkedListSetEntry<ActiveAsynchronousFlow, ManagedFunctionContainer> {

	/**
	 * Indicates if already waiting on completion.
	 * 
	 * @return <code>true</code> if already waiting on completion.
	 */
	boolean isWaiting();

	/**
	 * Ensure wait on completion.
	 * 
	 * @return {@link FunctionState} to wait on completion.
	 */
	FunctionState waitOnCompletion();

}