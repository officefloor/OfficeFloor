package net.officefloor.gef.editor;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.model.change.Change;

/**
 * Executes {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChangeExecutor {

	/**
	 * Executes the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void execute(Change<?> change);

	/**
	 * Executes the {@link ITransactionalOperation}.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void execute(ITransactionalOperation operation);

	/**
	 * Adds a {@link ChangeListener}.
	 * 
	 * @param changeListener
	 *            {@link ChangeListener}.
	 */
	void addChangeListener(ChangeListener changeListener);

	/**
	 * Removes the {@link ChangeListener}.
	 * 
	 * @param changeListener
	 *            {@link ChangeListener}.
	 */
	void removeChangeListener(ChangeListener changeListener);

}