package net.officefloor.gef.editor;

import javafx.collections.ObservableList;
import net.officefloor.gef.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.Model;

/**
 * Child group.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChildrenGroup<M extends Model, E extends Enum<E>> {

	/**
	 * Obtains the {@link ChildrenGroupImpl} name.
	 * 
	 * @return {@link ChildrenGroupImpl} name.
	 */
	String getChildrenGroupName();

	/**
	 * Obtains the parent {@link AdaptedChild}.
	 * 
	 * @return Parent {@link AdaptedChild}.
	 */
	AdaptedChild<M> getParent();

	/**
	 * Obtains the {@link AdaptedChild} instances.
	 * 
	 * @return {@link AdaptedChild} instances.
	 */
	ObservableList<AdaptedChild<?>> getChildren();

	/**
	 * Obtains the events.
	 * 
	 * @return Events.
	 */
	E[] getEvents();

}
