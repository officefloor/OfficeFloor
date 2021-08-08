/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.gef.editor.internal.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.gef.fx.nodes.GeometryNode;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.gef.editor.AdaptedActionVisualFactory;
import net.officefloor.gef.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;
import net.officefloor.gef.editor.AdaptedConnectorVisualFactory;
import net.officefloor.gef.editor.AdaptedConnectorVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link AdaptedChildVisualFactoryContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedModelVisualFactoryContextImpl<M extends Model>
		implements AdaptedModelVisualFactoryContext<M>, AdaptedActionVisualFactoryContext {

	/**
	 * {@link Function} interface to load connectors.
	 */
	public static interface ConnectorLoader<M extends Model> {

		/**
		 * Loads the connector.
		 * 
		 * @param connectionClasses {@link ConnectionModel} {@link Class} instances.
		 * @param role              {@link AdaptedConnectorRole}.
		 * @param assocations       Associations list for {@link AdaptedConnector}.
		 * @param node              Connector {@link GeometryNode}.
		 */
		void loadConnectors(Class<?>[] connectionClasses, AdaptedConnectorRole role,
				List<AdaptedConnector<M>> assocations, Region node);
	}

	/**
	 * {@link Function} interface to action a {@link ModelAction}.
	 */
	public static interface Actioner<M extends Model> {

		/**
		 * Undertakes the {@link ModelAction}.
		 * 
		 * @param action {@link ModelAction}.
		 */
		void action(ModelAction<?, ?, M> action);
	}

	/**
	 * {@link Class} of the {@link Model}.
	 */
	protected final Class<M> modelClass;

	/**
	 * Indicates if rendering the palette prototype.
	 */
	protected final boolean isPalettePrototype;

	/**
	 * {@link ConnectorLoader}.
	 */
	private final ConnectorLoader<M> connectorLoader;

	/**
	 * {@link Actioner}.
	 */
	private final Actioner<M> actioner;

	/**
	 * Instantiate.
	 * 
	 * @param modelClass         {@link Class} of the {@link Model}.
	 * @param isPalettePrototype Indicates if rendering the palette prototype.
	 * @param connectorLoader    {@link ConnectorLoader}.
	 * @param actioner           {@link Actioner}.
	 */
	public AdaptedModelVisualFactoryContextImpl(Class<M> modelClass, boolean isPalettePrototype,
			ConnectorLoader<M> connectorLoader, Actioner<M> actioner) {
		this.modelClass = modelClass;
		this.isPalettePrototype = isPalettePrototype;
		this.connectorLoader = connectorLoader;
		this.actioner = actioner;
	}

	@Override
	public <N extends Node> N addNode(Pane parent, N node) {
		parent.getChildren().add(node);
		return node;
	}

	@Override
	public Pane addIndent(HBox parent) {
		Pane indent = new Pane();
		indent.getStyleClass().add("indent");
		return this.addNode(parent, indent);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public <N extends Region> Connector connector(AdaptedConnectorVisualFactory<N> visualFactory,
			Class... connectionModelClasses) {
		return new Connector() {

			// Indicates if initialised
			private boolean isInitialised = false;

			// Role of connector (when connecting to self)
			private AdaptedConnectorRole role = null;

			// Role specific connection classes
			private Class<?>[] roleConnectionClasses = null;

			// Geometry Node
			private Region geometryNode = null;

			@Override
			public Connector source(Class... sourceConnectionClass) {
				if (this.isInitialised) {
					throw new IllegalStateException("Connector already initialised for model "
							+ AdaptedModelVisualFactoryContextImpl.this.modelClass.getName());
				}
				if (this.role != null) {
					throw new IllegalStateException("Connector already initialised to target for model "
							+ AdaptedModelVisualFactoryContextImpl.this.modelClass.getName());
				}
				this.role = AdaptedConnectorRole.SOURCE;
				this.roleConnectionClasses = sourceConnectionClass;
				return this;
			}

			@Override
			public Connector target(Class... targetConnectionClass) {
				if (this.isInitialised) {
					throw new IllegalStateException("Connector already initialised for model "
							+ AdaptedModelVisualFactoryContextImpl.this.modelClass.getName());
				}
				if (this.role != null) {
					throw new IllegalStateException("Connector already initialised to source for model "
							+ AdaptedModelVisualFactoryContextImpl.this.modelClass.getName());
				}
				this.role = AdaptedConnectorRole.TARGET;
				this.roleConnectionClasses = targetConnectionClass;
				return this;
			}

			@Override
			public Node getNode() {

				// Determine if already initialised
				if (this.isInitialised) {
					return this.geometryNode;
				}

				// Create the geometry node
				Region node = visualFactory.createGeometryNode(new AdaptedConnectorVisualFactoryContext() {
				});
				if (!(node instanceof GeometryNode)) {
					throw new IllegalStateException("Connector visual must implement " + GeometryNode.class.getName()
							+ " for model " + AdaptedModelVisualFactoryContextImpl.this.modelClass.getName());
				}
				this.geometryNode = node;

				// Create the assocation listing
				List<AdaptedConnector<M>> assocations = new ArrayList<>(connectionModelClasses.length);

				// Register the non-typed connections
				AdaptedModelVisualFactoryContextImpl.this.connectorLoader.loadConnectors(connectionModelClasses, null,
						assocations, this.geometryNode);

				// Provide the type specific connections
				if (this.roleConnectionClasses != null) {
					AdaptedModelVisualFactoryContextImpl.this.connectorLoader.loadConnectors(this.roleConnectionClasses,
							this.role, assocations, this.geometryNode);
				}

				// Provide blank node if palette prototype
				if (AdaptedModelVisualFactoryContextImpl.this.isPalettePrototype) {
					return new Pane();
				}

				// Return geometry node
				return this.geometryNode;
			}
		};
	}

	@Override
	public Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath) {
		return DefaultImages.createImageWithHover(resourceClass, imageFilePath, hoverImageFilePath);
	}

	@Override
	public <R extends Model, O> void action(ModelAction<R, O, M> action) {
		this.actioner.action(action);
	}

	@Override
	public <R extends Model, O> Node action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {

		// Provide blank node if palette prototype
		if (this.isPalettePrototype) {
			return new Pane();
		}

		// Provide action
		Node node = visualFactory.createVisual(this);
		node.setOnMouseClicked((event) -> this.action(action));
		node.getStyleClass().add("action");
		return node;
	}

}
