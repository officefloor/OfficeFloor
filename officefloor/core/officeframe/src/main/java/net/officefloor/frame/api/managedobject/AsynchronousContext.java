package net.officefloor.frame.api.managedobject;

/**
 * Context to be notified about asynchronous operations by the
 * {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousContext {

	/**
	 * Undertakes an {@link AsynchronousOperation}.
	 *
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is registered as started an
	 *            asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void start(AsynchronousOperation<T> operation);

	/**
	 * Indicates that the {@link AsynchronousManagedObject} has completed and is
	 * ready for another operation.
	 * 
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is unregistered from undertaking
	 *            an asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void complete(AsynchronousOperation<T> operation);

}