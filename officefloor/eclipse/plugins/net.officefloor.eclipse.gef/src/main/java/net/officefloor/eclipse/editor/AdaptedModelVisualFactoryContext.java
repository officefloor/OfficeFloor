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
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnector;
import net.officefloor.model.ConnectionModel;

/**
 * Context for the {@link AdaptedModelVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedModelVisualFactoryContext {

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
	<G extends IGeometry, N extends GeometryNode<G>, C extends ConnectionModel> N connector(N geometryNode,
			Class... connectionModelClasses);

}