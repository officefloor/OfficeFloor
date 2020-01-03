package net.officefloor.gef.editor;

import net.officefloor.model.ConnectionModel;

/**
 * Adapted {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnection<C extends ConnectionModel> extends AdaptedModel<C> {

	/**
	 * Obtains the source {@link AdaptedConnectable}.
	 * 
	 * @return Source {@link AdaptedConnectable}.
	 */
	AdaptedConnectable<?> getSource();

	/**
	 * Obtains the target {@link AdaptedConnectable}.
	 * 
	 * @return Target {@link AdaptedConnectable}.
	 */
	AdaptedConnectable<?> getTarget();

	/**
	 * Indicates whether able to remove the {@link ConnectionModel}.
	 * 
	 * @return <code>true</code> if able to remove the {@link ConnectionModel}.
	 */
	boolean canRemove();

	/**
	 * Removes the {@link ConnectionModel}.
	 */
	void remove();

}