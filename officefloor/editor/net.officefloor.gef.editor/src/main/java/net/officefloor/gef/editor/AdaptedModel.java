package net.officefloor.gef.editor;

import net.officefloor.model.Model;

/**
 * Adapted {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedModel<M extends Model> {

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @return {@link Model}.
	 */
	M getModel();

	/**
	 * Obtains the parent {@link AdaptedModel}.
	 * 
	 * @return Parent {@link AdaptedModel} or <code>null</code> if
	 *         {@link AdaptedParent}.
	 */
	AdaptedModel<?> getParent();

	/**
	 * Obtains the {@link AdaptedErrorHandler}.
	 * 
	 * @return {@link AdaptedErrorHandler}.
	 */
	AdaptedErrorHandler getErrorHandler();

}