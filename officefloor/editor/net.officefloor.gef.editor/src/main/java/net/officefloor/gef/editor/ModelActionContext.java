package net.officefloor.gef.editor;

import com.google.inject.Injector;

import net.officefloor.model.Model;

/**
 * Context for the {@link ModelAction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelActionContext<R extends Model, O, M extends Model> {

	/**
	 * Obtains the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	R getRootModel();

	/**
	 * Obtains the operations.
	 * 
	 * @return Operations.
	 */
	O getOperations();

	/**
	 * <p>
	 * Obtains the {@link Model}.
	 * <p>
	 * Will only be <code>null</code> if action is to create a {@link Model}.
	 * 
	 * @return {@link Model} or <code>null</code>.
	 */
	M getModel();

	/**
	 * <p>
	 * Obtains the {@link AdaptedModel}.
	 * <p>
	 * Will only be <code>null</code> if action is to create a {@link Model}.
	 * 
	 * @return {@link AdaptedModel} or <code>null</code>.
	 */
	AdaptedModel<M> getAdaptedModel();

	/**
	 * Shows an overlay at the position of the action.
	 * 
	 * @param overlayVisualFactory
	 *            {@link OverlayVisualFactory}.
	 */
	void overlay(OverlayVisualFactory overlayVisualFactory);

	/**
	 * Obtains the {@link ChangeExecutor}.
	 * 
	 * @return {@link ChangeExecutor}.
	 */
	ChangeExecutor getChangeExecutor();

	/**
	 * Obtains the {@link Injector}.
	 * 
	 * @return {@link Injector}.
	 */
	Injector getInjector();

	/**
	 * Convenience method to position the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model} to be positioned.
	 * @return Input {@link Model}.
	 */
	M position(M model);

}