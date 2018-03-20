/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.IGeometry;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ChildrenGroup;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

public class AdaptedChildPart<M extends Model, A extends AdaptedChild<M>> extends AbstractAdaptedPart<M, A, Pane>
		implements AdaptedModelVisualFactoryContext {

	/**
	 * {@link ChildrenGroupVisual} instances for the {@link ChildrenGroupImpl}
	 * instances.
	 */
	private Map<ChildrenGroup<M, ?>, ChildrenGroupVisual> childrenGroupVisuals;

	/**
	 * {@link AdaptedConnectorVisual} instances for the {@link AdaptedConnector}
	 * instances.
	 */
	private Map<AdaptedConnector<M>, AdaptedConnectorVisual> adaptedConnectorVisuals;

	/**
	 * Obtains the {@link Pane} for the {@link ChildrenGroupImpl}.
	 * 
	 * @param childrenGroup
	 *            {@link ChildrenGroupImpl}.
	 * @return {@link Pane}.
	 */
	public Pane getChildrenGroupPane(ChildrenGroup<?, ?> childrenGroup) {
		return this.childrenGroupVisuals.get(childrenGroup).pane;
	}

	/**
	 * Obtains the {@link GeometryNode} for the {@link AdaptedConnector}.
	 * 
	 * @param connector
	 *            {@link AdaptedConnector}.
	 * @return {@link GeometryNode}.
	 */
	public GeometryNode<?> getAdaptedConnectorNode(AdaptedConnector<?> connector) {
		return this.adaptedConnectorVisuals.get(connector).node;
	}

	/*
	 * ================== IContentPart =========================
	 */

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<Object> doGetContentChildren() {
		List<Object> children = new ArrayList<>();
		children.addAll(this.getContent().getChildrenGroups());
		children.addAll(this.getContent().getAdaptedConnectors());
		return children;
	}

	@Override
	protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
		// Should only be children groups (already added)
	}

	@Override
	protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
		// Should only be children groups (never removed)
	}

	@Override
	public Pane doCreateVisual() {

		// Load the children group visuals
		this.childrenGroupVisuals = new HashMap<>();
		for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
			this.childrenGroupVisuals.put(childrenGroup, new ChildrenGroupVisual());
		}

		// Load the adapated connector visuals
		this.adaptedConnectorVisuals = new HashMap<>();
		for (AdaptedConnector<M> adaptedConnector : this.getContent().getAdaptedConnectors()) {
			this.adaptedConnectorVisuals.put(adaptedConnector, new AdaptedConnectorVisual());
		}

		// Create the visual pane
		Pane pane = this.getContent().createVisual(this);

		// Ensure all children groups are configured
		for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
			ChildrenGroupVisual visual = this.childrenGroupVisuals.get(childrenGroup);
			if (visual.pane == null) {
				throw new IllegalStateException("Children group Pane '" + childrenGroup.getChildrenGroupName()
						+ "' not configured in view of model " + this.getContent().getModel().getClass().getName());
			}
		}

		// Ensure connectors for all configured connections
		for (AdaptedConnector<M> connector : this.getContent().getAdaptedConnectors()) {
			AdaptedConnectorVisual visual = this.adaptedConnectorVisuals.get(connector);
			if (visual.node == null) {
				throw new IllegalStateException("Connector to " + connector.getConnectionModelClass().getName()
						+ " not configured in view of model " + this.getContent().getModel().getClass().getName());
			}
		}

		// Provide model as class for CSS
		pane.getStyleClass().add("child");
		pane.getStyleClass().add(this.getContent().getModel().getClass().getSimpleName());

		// Return the visual
		return pane;
	}

	@Override
	protected void doRefreshVisual(Pane visual) {
	}

	/*
	 * ========================== ViewFactoryContext ==========================
	 */

	@Override
	public Label label(Pane parent) {
		Label label = this.addNode(parent, new Label());
		label.textProperty().bind(this.getContent().getLabel());
		return label;
	}

	@Override
	public <N extends Node> N addNode(Pane parent, N node) {
		parent.getChildren().add(node);
		return node;
	}

	@Override
	public <P extends Pane> P childGroup(String childGroupName, P parent) {
		if (childGroupName == null) {
			throw new NullPointerException(
					"No child group name provided for view of " + this.getContent().getModel().getClass().getName());
		}

		// Load the child group pane
		for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
			if (childGroupName.equals(childrenGroup.getChildrenGroupName())) {

				// Found the child group, so load the pane
				ChildrenGroupVisual visual = this.childrenGroupVisuals.get(childrenGroup);
				visual.pane = parent;
				return parent;
			}
		}

		// As here, no children group registered
		throw new IllegalStateException("No children group '" + childGroupName + "' registered for view of model "
				+ this.getContent().getModel().getClass().getName());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final <G extends IGeometry, N extends GeometryNode<G>, C extends ConnectionModel> N connector(N geometryNode,
			Class... connectionClasses) {

		// Register the geometry node
		for (Class<? extends C> connectionClass : connectionClasses) {

			// Obtain the adapted connector
			AdaptedConnector<M> connector = (AdaptedConnector<M>) this.getContent()
					.getAdaptedConnector(connectionClass);
			if (connector == null) {
				throw new IllegalStateException("Connection " + connectionClass.getName()
						+ " not configured to connect to model " + this.getContent().getModel().getClass().getName());
			}

			// Obtain the visual
			AdaptedConnectorVisual visual = this.adaptedConnectorVisuals.get(connector);
			if (visual.node != null) {
				throw new IllegalStateException("Connection " + connectionClass.getName()
						+ " configured more than once for model " + this.getContent().getModel().getClass().getName());
			}

			// Load the connector
			visual.node = geometryNode;
		}

		// Return geometry node
		return geometryNode;
	}

	/**
	 * {@link ChildrenGroupImpl} visual.
	 */
	private static class ChildrenGroupVisual {

		/**
		 * {@link Pane} for the {@link ChildrenGroupImpl}.
		 */
		private Pane pane = null;
	}

	/**
	 * {@link AdaptedConnector} visual.
	 */
	private static class AdaptedConnectorVisual {

		/**
		 * {@link GeometryNode} for the {@link AdaptedConnector}.
		 */
		private GeometryNode<?> node = null;
	}

}