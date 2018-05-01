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
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.ChildrenGroup;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.eclipse.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the {@link AdaptedChild}.
 *
 * @author Daniel Sagenschneider
 */
public class AdaptedChildPart<M extends Model, A extends AdaptedChild<M>> extends AbstractAdaptedPart<M, A, Pane> {

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
	public Region getAdaptedConnectorNode(AdaptedConnector<?> connector) {
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
	@SuppressWarnings("unchecked")
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
		Pane pane = this.getContent().createVisual(new AdaptedModelVisualFactoryContextImpl<M>(
				(Class<M>) this.getContent().getModel().getClass(), this.isPalettePrototype, () -> {

					// Return the label
					return this.getContent().getLabel();

				}, (childGroupName, parent) -> {

					// Load the child group pane
					for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
						if (childGroupName.equals(childrenGroup.getChildrenGroupName())) {

							// Found the child group, so load the pane
							ChildrenGroupVisual visual = this.childrenGroupVisuals.get(childrenGroup);
							visual.pane = parent;

							// Child group registered
							return true;
						}
					}

					// Child group not registered
					return false;

				}, (connectionClasses, role, assocations, node) -> {

					// Load the connectors for the connection classes
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
						visual.node = node;

						// Associate the connectors
						assocations.add(connector);
						connector.setAssociation(assocations, role);
					}

				}, (action) -> {

					// Undertake the action
					this.getContent().action(action);
				}));

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
		private Region node = null;
	}

}