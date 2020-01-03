package net.officefloor.model.impl.change;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;

/**
 * {@link Change} to simplify removing {@link ConnectionModel} instances.
 *
 * @author Daniel Sagenschneider
 */
public abstract class DisconnectChange<T> extends AbstractChange<T> {

	/**
	 * {@link ConnectionModel} instances removed.
	 */
	private ConnectionModel[] connections;

	/**
	 * Initiate.
	 *
	 * @param target
	 *            Target to remove {@link ConnectionModel} instances.
	 */
	public DisconnectChange(T target) {
		super(target, "Remove connections for "
				+ target.getClass().getSimpleName());
	}

	/*
	 * ======================== Change =================================
	 */

	@Override
	public void apply() {
		// Populate the removed connections
		List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
		this.populateRemovedConnections(connList);
		this.connections = connList.toArray(new ConnectionModel[0]);
	}

	/**
	 * Populates the removed {@link ConnectionModel}.
	 *
	 * @param connList
	 *            List to populate with the removed {@link ConnectionModel}
	 *            instances.
	 */
	protected abstract void populateRemovedConnections(
			List<ConnectionModel> connList);

	@Override
	public void revert() {
		// Re-connect connections in reverse order
		for (int i = (this.connections.length - 1); i >= 0; i--) {
			this.connections[i].connect();
		}
	}
}