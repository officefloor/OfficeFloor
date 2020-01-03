package net.officefloor.gef.editor;

import net.officefloor.model.Model;

/**
 * Builds the child group.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChildrenGroupBuilder<R extends Model, O> {

	/**
	 * Obtains the configuration path.
	 * 
	 * @return Configuration path.
	 */
	String getConfigurationPath();

	/**
	 * Adds a child {@link Model}.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param <E>
	 *            {@link Model} event type.
	 * @param modelPrototype
	 *            {@link Model} prototype to determine {@link Class} of the
	 *            {@link Model} and used in visual validation.
	 * @param viewFactory
	 *            {@link AdaptedChildVisualFactory} to create the view for the
	 *            {@link AdaptedChild}.
	 * @return {@link AdaptedParentBuilder} to build the adapter over the
	 *         {@link Model}.
	 */
	<M extends Model, E extends Enum<E>> AdaptedChildBuilder<R, O, M, E> addChild(M modelPrototype,
			AdaptedChildVisualFactory<M> viewFactory);

}