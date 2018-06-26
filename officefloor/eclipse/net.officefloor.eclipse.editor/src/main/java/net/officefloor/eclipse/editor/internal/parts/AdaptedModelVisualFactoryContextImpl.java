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
package net.officefloor.eclipse.editor.internal.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.gef.fx.nodes.GeometryNode;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedConnectorRole;
import net.officefloor.eclipse.editor.AdaptedConnectorVisualFactory;
import net.officefloor.eclipse.editor.AdaptedConnectorVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.DefaultImages;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link AdaptedModelVisualFactoryContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedModelVisualFactoryContextImpl<M extends Model>
		implements AdaptedModelVisualFactoryContext<M>, AdaptedActionVisualFactoryContext {

	/**
	 * {@link Function} interface to register the child group.
	 */
	public static interface ChildGroupRegistrator {

		/**
		 * Registers the child group.
		 * 
		 * @param childGroupName
		 *            Name of the child group.
		 * @param parent
		 *            Parent {@link Pane} for the child group.
		 * @return <code>true</code> if registered.
		 */
		boolean registerChildGroup(String childGroupName, Pane parent);
	}

	/**
	 * {@link Function} interface to load connectors.
	 */
	public static interface ConnectorLoader<M extends Model> {

		/**
		 * Loads the connector.
		 * 
		 * @param connectionClasses
		 *            {@link ConnectionModel} {@link Class} instances.
		 * @param role
		 *            {@link AdaptedConnectorRole}.
		 * @param assocations
		 *            Associations list for {@link AdaptedConnector}.
		 * @param node
		 *            Connector {@link GeometryNode}.
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
		 * @param action
		 *            {@link ModelAction}.
		 */
		void action(ModelAction<?, ?, M> action);
	}

	/**
	 * {@link Class} of the {@link Model}.
	 */
	private final Class<M> modelClass;

	/**
	 * Indicates if rendering the palette prototype.
	 */
	private final boolean isPalettePrototype;

	/**
	 * {@link Supplier} of the label.
	 */
	private final Supplier<ReadOnlyProperty<String>> label;

	/**
	 * {@link ChildGroupRegistrator}.
	 */
	private final ChildGroupRegistrator childGroupRegistrator;

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
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param isPalettePrototype
	 *            Indicates if rendering the palette prototype.
	 * @param label
	 *            {@link Supplier} of the label.
	 * @param childGroupRegistrator
	 *            {@link ChildGroupRegistrator}.
	 * @param connectorLoader
	 *            {@link ConnectorLoader}.
	 * @param actioner
	 *            {@link Actioner}.
	 */
	public AdaptedModelVisualFactoryContextImpl(Class<M> modelClass, boolean isPalettePrototype,
			Supplier<ReadOnlyProperty<String>> label, ChildGroupRegistrator childGroupRegistrator,
			ConnectorLoader<M> connectorLoader, Actioner<M> actioner) {
		this.modelClass = modelClass;
		this.isPalettePrototype = isPalettePrototype;
		this.label = label;
		this.childGroupRegistrator = childGroupRegistrator;
		this.connectorLoader = connectorLoader;
		this.actioner = actioner;
	}

	@Override
	public Label label(Pane parent) {
		// Ensure label is configured
		ReadOnlyProperty<String> labelProperty = this.label.get();
		if (labelProperty == null) {
			throw new IllegalStateException("No label configured for visual for model " + this.modelClass.getName());
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
			throw new NullPointerException("No child group name provided for view of " + this.modelClass.getName());
		}

		// Register the child group
		if (this.childGroupRegistrator.registerChildGroup(childGroupName, parent)) {
			return parent;
		}

		// As here, no children group registered
		throw new IllegalStateException(
				"No children group '" + childGroupName + "' registered for view of model " + this.modelClass.getName());
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

	@Override
	public boolean isPalettePrototype() {
		return this.isPalettePrototype;
	}

}