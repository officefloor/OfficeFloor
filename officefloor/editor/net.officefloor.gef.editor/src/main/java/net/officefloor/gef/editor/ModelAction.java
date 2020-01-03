package net.officefloor.gef.editor;

import net.officefloor.model.Model;

/**
 * Action on a particular {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelAction<R extends Model, O, M extends Model> {

	/**
	 * Executes the action.
	 * 
	 * @param context
	 *            {@link ModelActionContext}.
	 * @throws Throwable
	 *             Possible {@link Exception} in executing {@link ModelAction}.
	 */
	void execute(ModelActionContext<R, O, M> context) throws Throwable;

}