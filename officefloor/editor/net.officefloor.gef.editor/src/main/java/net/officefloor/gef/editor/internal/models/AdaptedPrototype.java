package net.officefloor.gef.editor.internal.models;

import org.eclipse.gef.geometry.planar.Point;

import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.model.Model;

/**
 * Adapted prototype for creating a new {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedPrototype<M extends Model> {

	/**
	 * Triggers creating a new {@link AdaptedParent} at the location.
	 * 
	 * @param location
	 *            Location for the new {@link AdaptedParent}.
	 */
	void newAdaptedParent(Point location);

}