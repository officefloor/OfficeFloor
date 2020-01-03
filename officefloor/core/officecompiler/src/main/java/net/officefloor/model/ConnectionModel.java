package net.officefloor.model;

/**
 * Interface to aid manipulation of connections/associations.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionModel extends Model {

	/**
	 * Indicates if this connection is removable.
	 * 
	 * @return True if may remove the connection.
	 */
	boolean isRemovable();

	/**
	 * Connects the source and target.
	 */
	void connect();

	/**
	 * Removes the connection.
	 */
	void remove();
}
