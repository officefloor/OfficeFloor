package net.officefloor.model;

/**
 * Interface to aid manipulation of items.
 * 
 * @author Daniel Sagenschneider
 */
public interface ItemModel<M extends Model> extends Model {

	/**
	 * Removes the {@link ConnectionModel} instances connected to this
	 * {@link ItemModel}.
	 * 
	 * @return {@link RemoveConnectionsAction} containing the
	 *         {@link ConnectionModel} instances removed.
	 */
	RemoveConnectionsAction<M> removeConnections();
}
