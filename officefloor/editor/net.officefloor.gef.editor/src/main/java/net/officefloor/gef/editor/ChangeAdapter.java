package net.officefloor.gef.editor;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.model.change.Change;

/**
 * Provides default implementation of methods for {@link ChangeListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeAdapter implements ChangeListener {

	@Override
	public void beforeTransactionOperation(ITransactionalOperation operation) {
	}

	@Override
	public void afterTransactionOperation(ITransactionalOperation operation) {
	}

	@Override
	public void preApply(Change<?> change) {
	}

	@Override
	public void postApply(Change<?> change) {
	}

	@Override
	public void preRevert(Change<?> change) {
	}

	@Override
	public void postRevert(Change<?> change) {
	}

}