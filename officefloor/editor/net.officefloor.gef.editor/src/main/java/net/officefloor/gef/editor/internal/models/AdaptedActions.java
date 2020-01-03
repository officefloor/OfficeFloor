package net.officefloor.gef.editor.internal.models;

import java.util.List;

import net.officefloor.gef.editor.ModelAction;
import net.officefloor.model.Model;

/**
 * Adapted {@link ModelAction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedActions<R extends Model, O, M extends Model> {

	/**
	 * {@link AdaptedAction} instances.
	 */
	private final List<AdaptedAction<R, O, M>> actions;

	/**
	 * Instantiate.
	 * 
	 * @param actions
	 *            {@link AdaptedAction} instances.
	 */
	public AdaptedActions(List<AdaptedAction<R, O, M>> actions) {
		this.actions = actions;
	}

	/**
	 * Obtains the {@link AdaptedAction} instances.
	 * 
	 * @return {@link AdaptedAction} instances.
	 */
	public List<AdaptedAction<R, O, M>> getAdaptedActions() {
		return this.actions;
	}

}