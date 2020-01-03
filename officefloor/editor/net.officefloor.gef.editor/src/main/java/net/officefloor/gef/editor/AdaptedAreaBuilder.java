package net.officefloor.gef.editor;

import org.eclipse.gef.geometry.planar.Dimension;

import net.officefloor.model.Model;

/**
 * Builds an {@link AdaptedArea}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedAreaBuilder<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedConnectableBuilder<R, O, M, E> {

	/**
	 * Specifies the minimum {@link Dimension}.
	 * 
	 * @param width  Minimum width.
	 * @param height Minimum height.
	 */
	void setMinimumDimension(double width, double height);

	/**
	 * Configures an {@link ModelAction} for the area {@link Model}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}