package net.officefloor.gef.editor;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.model.change.Change;

/**
 * Listens to {@link Change} instances being executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChangeListener {

	/**
	 * Notified before the {@link ITransactionalOperation} is registered for
	 * execution.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void beforeTransactionOperation(ITransactionalOperation operation);

	/**
	 * Notified after the {@link ITransactionalOperation} is registered for
	 * execution.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void afterTransactionOperation(ITransactionalOperation operation);

	/**
	 * Notified pre-applying the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void preApply(Change<?> change);

	/**
	 * Notified post-applying the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void postApply(Change<?> change);

	/**
	 * Notified pre-reverting the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void preRevert(Change<?> change);

	/**
	 * Notified post-reverting the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void postRevert(Change<?> change);

}