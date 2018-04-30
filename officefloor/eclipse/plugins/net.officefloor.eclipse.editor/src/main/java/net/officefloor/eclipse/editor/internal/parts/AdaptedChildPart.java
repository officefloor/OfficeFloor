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
package net.officefloor.eclipse.editor.internal.parts;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.IGeometry;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.geometry.planar.Polygon;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedConnectorRole;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ChildrenGroup;
import net.officefloor.eclipse.editor.DefaultImages;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.eclipse.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

public class AdaptedChildPart<M extends Model, A extends AdaptedChild<M>> extends AbstractAdaptedPart<M, A, Pane>
		implements AdaptedModelVisualFactoryContext<M>, AdaptedActionVisualFactoryContext {

	/**
	 * Indicates whether a Palette prototype.
	 */
	protected boolean isPalettePrototype = false;

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

	/**
	 * Obtains the {@link AdaptedErrorHandler}.
	 * 
	 * @return {@link AdaptedErrorHandler}.
	 */
	public AdaptedErrorHandler getErrorHandler() {
		return this.getContent().getErrorHandler();
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

		// Load the adapted connector visuals
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

		// Determine if specific styling
		ReadOnlyProperty<URL> stylesheetUrl = this.getContent().getStylesheetUrl();
		if (stylesheetUrl != null) {

			// Load initial styling
			URL initialUrl = stylesheetUrl.getValue();
			if (initialUrl != null) {
				pane.getStylesheets().add(initialUrl.toExternalForm());
			}

			// Bind potential changes to the styling
			stylesheetUrl.addListener((event, oldValue, newValue) -> {
				if (oldValue != null) {
					pane.getStylesheets().remove(oldValue.toExternalForm());
				}
				if (newValue != null) {
					pane.getStylesheets().add(newValue.toExternalForm());
				}
			});
		}

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
		// Ensure label is configured
		ReadOnlyProperty<String> labelProperty = this.getContent().getLabel();
		if (labelProperty == null) {
			throw new IllegalStateException(
					"No label configured for visual for model " + this.getContent().getModel().getClass().getName());
		}

		// Configure the label
		Label label = this.addNode(parent, new Label());
		label.textProperty().bind(labelProperty);
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
	public final <G extends IGeometry, N extends GeometryNode<G>> Connector connector(N geometryNode,
			Class... connectionClasses) {
		return new Connector() {

			// Indicates if initialised
			private boolean isInitialised = false;

			// Role of connector (when connecting to self)
			private AdaptedConnectorRole role = null;

			// Role specific connection classes
			private Class<?>[] roleConnectionClasses = null;

			@Override
			public Connector source(Class... sourceConnectionClass) {
				if (this.isInitialised) {
					throw new IllegalStateException("Connector already initialised for model "
							+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
				}
				if (this.role != null) {
					throw new IllegalStateException("Connector already initialised to target for model "
							+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
				}
				this.role = AdaptedConnectorRole.SOURCE;
				this.roleConnectionClasses = sourceConnectionClass;
				return this;
			}

			@Override
			public Connector target(Class... targetConnectionClass) {
				if (this.isInitialised) {
					throw new IllegalStateException("Connector already initialised for model "
							+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
				}
				if (this.role != null) {
					throw new IllegalStateException("Connector already initialised to source for model "
							+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
				}
				this.role = AdaptedConnectorRole.TARGET;
				this.roleConnectionClasses = targetConnectionClass;
				return this;
			}

			@Override
			public Node getNode() {

				// Determine if already initialised
				if (this.isInitialised) {
					return geometryNode;
				}

				// Create the assocation listing
				List<AdaptedConnector<M>> assocations = new ArrayList<>(connectionClasses.length);

				// Register the non-typed connections
				this.loadConnectors(connectionClasses, null, assocations);

				// Provide the type specific connections
				if (this.roleConnectionClasses != null) {
					this.loadConnectors(this.roleConnectionClasses, this.role, assocations);
				}

				// Provide blank node if palette prototype
				if (AdaptedChildPart.this.isPalettePrototype) {
					return new Pane();
				}

				// Return geometry node
				return geometryNode;
			}

			/**
			 * Loads the connector.
			 * 
			 * @param connectionClasses
			 *            {@link ConnectionModel} {@link Class} instances.
			 * @param role
			 *            {@link AdaptedConnectorRole}.
			 * @param assocations
			 *            Associations list for {@link AdaptedConnector}.
			 */
			private void loadConnectors(Class<?>[] connectionClasses, AdaptedConnectorRole role,
					List<AdaptedConnector<M>> assocations) {
				for (Class<?> connectionClass : connectionClasses) {

					// Obtain the adapted connector
					AdaptedConnector<M> connector = AdaptedChildPart.this.getContent()
							.getAdaptedConnector((Class<? extends ConnectionModel>) connectionClass, role);
					if (connector == null) {
						throw new IllegalStateException(
								"Connection " + connectionClass.getName() + " not configured to connect to model "
										+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
					}

					// Obtain the visual
					AdaptedConnectorVisual visual = AdaptedChildPart.this.adaptedConnectorVisuals.get(connector);
					if (visual.node != null) {
						throw new IllegalStateException(
								"Connection " + connectionClass.getName() + " configured more than once for model "
										+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
					}

					// Load the connector visual
					visual.node = geometryNode;

					// Associate the connectors
					assocations.add(connector);
					connector.setAssociation(assocations, this.role);
				}
			}
		};
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Connector connector(Class... connectionClasses) {

		// Create the geometry node for the anchor
		final double X_LEFT = 0;
		final double X_STEM = 5;
		final double X_TIP = 9;
		final double Y_TOP = 0;
		final double Y_BOTTOM = 10;
		final double Y_STEM_INSET = 2;
		final double Y_STEM_TOP = Y_TOP + Y_STEM_INSET;
		final double Y_STEM_BOTTOM = Y_BOTTOM - Y_STEM_INSET;
		final double Y_TIP = (Y_BOTTOM - Y_TOP) / 2;
		GeometryNode<Polygon> node = new GeometryNode<>(new Polygon(new Point(X_LEFT, Y_STEM_TOP),
				new Point(X_STEM, Y_STEM_TOP), new Point(X_STEM, Y_TOP), new Point(X_TIP, Y_TIP),
				new Point(X_STEM, Y_BOTTOM), new Point(X_STEM, Y_STEM_BOTTOM), new Point(X_LEFT, Y_STEM_BOTTOM)));
		node.setFill(Color.BLACK);

		// Return the connector
		return this.connector(node, connectionClasses);
	}

	@Override
	public Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath) {
		return DefaultImages.createImageWithHover(resourceClass, imageFilePath, hoverImageFilePath);
	}

	@Override
	public <R extends Model, O> void action(ModelAction<R, O, M> action) {
		this.getContent().action(action);
	}

	@Override
	public <R extends Model, O> Node action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {

		// Provide blank node if palette prototype
		if (AdaptedChildPart.this.isPalettePrototype) {
			return new Pane();
		}

		// Provide action
		Node node = visualFactory.createVisual(this);
		node.setOnMouseClicked((event) -> this.action(action));
		node.getStyleClass().add("action");
		return node;
	}

	@Override
	public boolean isPalettePrototype() {
		return this.isPalettePrototype;
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
	 * {@link AdaptedConnectorImpl} visual.
	 */
	private static class AdaptedConnectorVisual {

		/**
		 * {@link GeometryNode} for the {@link AdaptedConnectorImpl}.
		 */
		private GeometryNode<?> node = null;
	}

}