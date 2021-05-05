/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.editor.internal.parts;

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

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.gef.editor.internal.parts.AdaptedModelVisualFactoryContextImpl.ConnectorLoader;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the {@link AdaptedConnectable}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedConnectablePart<M extends Model, A extends AdaptedConnectable<M>>
		extends AbstractAdaptedPart<M, A, Node> implements AdaptedModelStyler {

	/**
	 * Loads the styling for the child {@link Pane}.
	 * 
	 * @param visualNode    Child visual {@link Node}.
	 * @param modelClass    {@link Class} of the {@link Model}.
	 * @param stylesheetUrl {@link ReadOnlyProperty} to specific styling
	 *                      {@link URL}.
	 */
	public static void loadStyling(Node visualNode, Class<? extends Model> modelClass,
			ReadOnlyProperty<URL> stylesheetUrl) {

		// Determine if can style node
		if (!(visualNode instanceof Parent)) {
			return;
		}
		Parent childVisual = (Parent) visualNode;

		// Provide model as class for CSS
		childVisual.getStyleClass().add("child");
		childVisual.getStyleClass().add(modelClass.getSimpleName());

		// Determine if specific styling
		if (stylesheetUrl != null) {

			// Load initial styling
			URL initialUrl = stylesheetUrl.getValue();
			if (initialUrl != null) {
				childVisual.getStylesheets().add(initialUrl.toExternalForm());
			}

			// Bind potential changes to the styling
			stylesheetUrl.addListener((event, oldValue, newValue) -> {
				if (oldValue != null) {
					childVisual.getStylesheets().remove(oldValue.toExternalForm());
				}
				if (newValue != null) {
					childVisual.getStylesheets().add(newValue.toExternalForm());
				}
			});
		}
	}

	/**
	 * Indicates whether a Palette prototype.
	 */
	protected boolean isPalettePrototype = false;

	/**
	 * {@link AdaptedConnectorVisual} instances for the {@link AdaptedConnector}
	 * instances.
	 */
	private Map<AdaptedConnector<M>, AdaptedConnectorVisual> adaptedConnectorVisuals;

	/**
	 * Obtains the {@link GeometryNode} for the {@link AdaptedConnector}.
	 * 
	 * @param connector {@link AdaptedConnector}.
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
	 * ================== AdaptedModelStyler ========================
	 */

	@Override
	public Model getModel() {
		return this.getContent().getModel();
	}

	@Override
	public Property<String> style() {
		return this.getContent().getStylesheet();
	}

	/*
	 * ================== IContentPart =========================
	 */

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected void doAttachToAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		// already attached
	}

	@Override
	protected void doDetachFromAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		// nothing to detach
	}

	@Override
	protected List<Object> doGetContentChildren() {
		List<Object> children = new ArrayList<>();
		children.addAll(this.getContent().getAdaptedConnectors());
		return children;
	}

	@Override
	protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
		// Should only be static connectors
	}

	@Override
	protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
		// Should only be static connectors
	}

	/**
	 * Creates the visual {@link Node}.
	 * 
	 * @return Visual {@link Node}.
	 */
	protected abstract Node createVisualNode();

	@Override
	public Node doCreateVisual() {

		// Create the visual node
		Node visualNode = this.createVisualNode();

		// Ensure connectors for all configured connections
		for (AdaptedConnector<M> connector : this.getContent().getAdaptedConnectors()) {
			AdaptedConnectorVisual visual = this.adaptedConnectorVisuals.get(connector);
			if (visual.node == null) {
				throw new IllegalStateException("Connector to " + connector.getConnectionModelClass().getName()
						+ " not configured in view of model " + this.getContent().getModel().getClass().getName());
			}
		}

		// Provide styling
		loadStyling(visualNode, this.getContent().getModel().getClass(), this.getContent().getStylesheetUrl());

		// Provide select only
		SelectOnly selectOnly = this.getContent().getSelectOnly();
		if (selectOnly != null) {
			visualNode.setOnMouseClicked((event) -> {
				this.getContent().getErrorHandler().isError(() -> {
					selectOnly.model(this);
				});
				event.consume();
			});
		}

		// Return the visual
		return visualNode;
	}

	@Override
	protected void doRefreshVisual(Node visual) {
		// nothing to refresh
	}

	/**
	 * Obtains the {@link ConnectorLoader}.
	 * 
	 * @return {@link ConnectorLoader}.
	 */
	@SuppressWarnings("unchecked")
	protected ConnectorLoader<M> getConnectorLoader() {

		// Load the adapted connector visuals
		this.adaptedConnectorVisuals = new HashMap<>();
		for (AdaptedConnector<M> adaptedConnector : this.getContent().getAdaptedConnectors()) {
			this.adaptedConnectorVisuals.put(adaptedConnector, new AdaptedConnectorVisual());
		}

		// Return the connector loader
		return (connectionClasses, role, assocations, node) -> {

			// Load the connectors for the connection classes
			for (Class<?> connectionClass : connectionClasses) {

				// Obtain the adapted connector
				AdaptedConnector<M> connector = AbstractAdaptedConnectablePart.this.getContent()
						.getAdaptedConnector((Class<? extends ConnectionModel>) connectionClass, role);
				if (connector == null) {
					throw new IllegalStateException(
							"Connection " + connectionClass.getName() + " not configured to connect to model "
									+ AbstractAdaptedConnectablePart.this.getContent().getModel().getClass().getName());
				}

				// Obtain the visual
				AdaptedConnectorVisual visual = AbstractAdaptedConnectablePart.this.adaptedConnectorVisuals
						.get(connector);
				if (visual.node != null) {
					throw new IllegalStateException(
							"Connection " + connectionClass.getName() + " configured more than once for model "
									+ AbstractAdaptedConnectablePart.this.getContent().getModel().getClass().getName());
				}

				// Load the connector visual
				visual.node = node;

				// Associate the connectors
				assocations.add(connector);
				connector.setAssociation(assocations, role);
			}
		};
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
