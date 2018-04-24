/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor;

import org.eclipse.gef.fx.anchors.IAnchor;
import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.IGeometry;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnector;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Context for the {@link AdaptedModelVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedModelVisualFactoryContext<M extends Model> {

	/**
	 * Convenience method to add the {@link AdaptedModel} {@link Label} to the
	 * {@link Pane}.
	 * 
	 * @param parent
	 *            {@link Pane}.
	 * @return Added {@link Label}.
	 */
	Label label(Pane parent);

	/**
	 * <p>
	 * Add the {@link Node} to the parent {@link Pane} returning it.
	 * <p>
	 * This allows for convenient adding new {@link Node} instances to {@link Pane}.
	 * 
	 * @param pane
	 *            {@link Pane}.
	 * @param node
	 *            {@link Node}.
	 * @return Input {@link Node}
	 */
	<N extends Node> N addNode(Pane parent, N node);

	/**
	 * Specifies the {@link Pane} for the child group.
	 * 
	 * @param childGroupName
	 *            Name of the child group.
	 * @param parent
	 *            {@link Pane} to add the child group visuals.
	 * @return Input {@link Pane}.
	 */
	<P extends Pane> P childGroup(String childGroupName, P parent);

	/**
	 * Specifies the {@link GeometryNode} as {@link AdaptedConnector}
	 * {@link IAnchor}.
	 * 
	 * @param geometryNode
	 *            {@link GeometryNode} to be used as the {@link IAnchor}.
	 * @param connectionModelClasses
	 *            {@link ConnectionModel} {@link Class} instances that this
	 *            connector satisfies.
	 * @return Input {@link Node}.
	 */
	@SuppressWarnings("rawtypes")
	<G extends IGeometry, N extends GeometryNode<G>> N connector(N geometryNode, Class... connectionModelClasses);

	/**
	 * Configures the default {@link GeometryNode} as {@link AdaptedConnector}
	 * {@link IAnchor}.
	 * 
	 * @param connectionClasses
	 *            {@link ConnectionModel} {@link Class} instances that this
	 *            connector satisfies.
	 * @return Input {@link Node}.
	 */
	@SuppressWarnings("rawtypes")
	Node connector(Class... connectionClasses);

	/**
	 * Convenience method to create a {@link Node} with {@link Image} and hover
	 * {@link Image}. Typically this is to create button for the action.
	 * 
	 * @param resourceClass
	 *            {@link Class} within the class path containing the images.
	 * @param imageFilePath
	 *            Path to the {@link Image}.
	 * @param hoverImageFilePath
	 *            Path to the hover {@link Image}.
	 * @return {@link Node} for the {@link Image} with hover.
	 */
	Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath);

	/**
	 * <p>
	 * Allows {@link ModelAction} instances to be actioned via visual.
	 * <p>
	 * This allows custom visuals (e.g. button) tor trigger an action.
	 * 
	 * @param action
	 *            {@link ModelAction} to be actioned.
	 */
	<R extends Model, O> void action(ModelAction<R, O, M> action);

	/**
	 * Convenience method to add a {@link ModelAction} to be actioned via
	 * {@link AdaptedActionVisualFactory}.
	 * 
	 * @param action
	 *            {@link ModelAction} to be actioned.
	 * @param visualFactory
	 *            {@link AdaptedActionVisualFactory}.
	 * @return {@link Node} to trigger the {@link ModelAction}.
	 */
	<R extends Model, O> Node action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}