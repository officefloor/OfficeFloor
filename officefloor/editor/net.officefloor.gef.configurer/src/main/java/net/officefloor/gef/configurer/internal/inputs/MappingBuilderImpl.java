/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal.inputs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.configurer.MappingBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;
import net.officefloor.gef.editor.AdaptedEditorModule;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link MappingBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MappingBuilderImpl<M> extends AbstractBuilder<M, Map<String, String>, ValueInput, MappingBuilder<M>>
		implements MappingBuilder<M> {

	/**
	 * {@link Function} to extract the sources from the model.
	 */
	private final Function<M, ObservableList<String>> getSources;

	/**
	 * {@link Function} to extract the targets from the model.
	 */
	private final Function<M, ObservableList<String>> getTargets;

	/**
	 * Instantiate.
	 * 
	 * @param label      Label.
	 * @param getSources {@link Function} to extract the sources from the model.
	 * @param getTargets {@link Function} to extract the targets from the model.
	 */
	public MappingBuilderImpl(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets) {
		super(label);
		this.getSources = getSources;
		this.getTargets = getTargets;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected ValueInput createInput(ValueInputContext<M, Map<String, String>> context) {

		// Update value with input
		Runnable[] updater = new Runnable[1];

		// Create editor configuration
		AdaptedEditorModule module = new AdaptedEditorModule();

		// Create the parent
		Pane inputNode = module.createParent((builder) -> {
			AdaptedRootBuilder<MappingModel, Object> root = builder.root(MappingModel.class, (model) -> this);

			// Source
			AdaptedParentBuilder<MappingModel, Object, SourceModel, ChangeEvent> source = root
					.parent(new SourceModel("Source"), (mapping) -> mapping.sourceModels, (mapping, ctx) -> {
						HBox pane = new HBox();
						ctx.label(pane);
						ctx.addNode(pane, ctx.connector(DefaultConnectors.FLOW, MappingConnection.class).getNode());
						return pane;
					}, ChangeEvent.CHANGED);
			source.label((s) -> s.label);

			// Target
			root.parent(new TargetModel("Target"), (mapping) -> mapping.targetModels, (mapping, ctx) -> {
				HBox pane = new HBox();
				ctx.addNode(pane, ctx.connector(DefaultConnectors.FLOW, MappingConnection.class).getNode());
				ctx.label(pane);
				return pane;
			}, ChangeEvent.CHANGED).label((t) -> t.label);

			// Connection
			source.connectOne(MappingConnection.class, (s) -> s.connection, (c) -> c.source, ChangeEvent.CHANGED)
					.toOne(TargetModel.class, (t) -> t.connection, (c) -> c.target, ChangeEvent.CHANGED)
					.create((s, t, ctx) -> {
						// Remove existing connections
						if (s.connection != null) {
							s.connection.remove();
						}
						if (t.connection != null) {
							t.connection.remove();
						}

						// Add new connection
						new MappingConnection(s, t).connect();

						// Update the mapping
						updater[0].run();

					}).delete((ctx) -> {
						// Remove the connection
						ctx.getModel().remove();

						// Update the mapping
						updater[0].run();
					});
		});

		// Property
		Property<Map<String, String>> mapProperty = context.getInputValue();

		// Create the model
		ObservableList<String> sources = this.getSources.apply(context.getModel());
		ObservableList<String> targets = this.getTargets.apply(context.getModel());
		MappingModel mapping = new MappingModel(inputNode, mapProperty, sources, targets);

		// Create the updater
		updater[0] = () -> {

			// Obtain the mappings
			Map<String, String> updated = new HashMap<>();
			for (SourceModel source : mapping.sourceModels) {
				if (source.connection != null) {
					TargetModel target = source.connection.target;
					if (target != null) {
						updated.put(source.label, target.label);
					}
				}
			}

			// Update the mappings
			mapProperty.setValue(updated);
		};

		// Return the input (allowing activation)
		return new ValueInput() {

			@Override
			public Node getNode() {
				return inputNode;
			}

			@Override
			public void activate() {
				module.activateDomain(mapping);
			}
		};
	}

	/**
	 * Changed event.
	 */
	private static enum ChangeEvent {
		CHANGED
	}

	/**
	 * Root mapping {@link Model}.
	 */
	private static class MappingModel extends AbstractModel {

		/**
		 * Parent {@link Pane}.
		 */
		private final Pane parent;

		/**
		 * {@link Property} of the {@link Map}.
		 */
		private final Property<Map<String, String>> mapping;

		/**
		 * {@link ObservableList} instances.
		 */
		private final ObservableList<String> sources;

		/**
		 * Current loaded {@link SourceModel} instances.
		 */
		private final List<SourceModel> sourceModels = new ArrayList<>();

		/**
		 * {@link ObservableList} instances.
		 */
		private final ObservableList<String> targets;

		/**
		 * Current loaded {@link TargetModel} instances.
		 */
		private final List<TargetModel> targetModels = new ArrayList<>();

		/**
		 * Instantiate.
		 * 
		 * @param parent  Parent {@link Pane}.
		 * @param mapping {@link Map} of source to target.
		 * @param sources Sources.
		 * @param targets Targets.
		 */
		public MappingModel(Pane parent, Property<Map<String, String>> mapping, ObservableList<String> sources,
				ObservableList<String> targets) {
			this.parent = parent;
			this.mapping = mapping;
			this.sources = sources;
			this.targets = targets;

			// Trigger change event on changes
			this.mapping.addListener((observable, oldValue, newValue) -> this.reloadModels(true));
			this.sources.addListener((Change<? extends String> event) -> this.reloadModels(false));
			this.targets.addListener((Change<? extends String> event) -> this.reloadModels(false));

			// Load models
			this.reloadModels(true);

			// Update position of targets on resize
			this.parent.widthProperty().addListener((observable, oldValue, newValue) -> this.reloadModels(false));
		}

		/**
		 * Reloads the models.
		 * 
		 * @param isMappingChange Indicates if external mapping change.
		 */
		private void reloadModels(boolean isMappingChange) {

			// Create clone of input mapping
			Map<String, String> inputMappings = this.mapping.getValue();
			Map<String, String> clonedInputMappings = new HashMap<>(
					inputMappings == null ? new HashMap<>() : inputMappings);

			// Obtain the configured mapping
			Map<String, String> configuredMapping = new HashMap<>();
			for (SourceModel source : this.sourceModels) {
				if ((source.connection != null) && (source.connection.target != null)) {
					TargetModel target = source.connection.target;

					// Load the mapping
					configuredMapping.put(source.label, target.label);
				}
			}

			// Obtain the mappings to use to configure the connections
			Map<String, String> connectionMappings;
			if (isMappingChange) {
				// Input mappings changed, so override configured mappings
				connectionMappings = configuredMapping;
				connectionMappings.putAll(clonedInputMappings);
			} else {
				// No input mapping change, so configured mappings override
				connectionMappings = clonedInputMappings;
				connectionMappings.putAll(configuredMapping);
			}

			// Position details
			final int ySeparation = 40;

			// Create map of existing sources
			Map<String, SourceModel> existingSources = new HashMap<>();
			for (SourceModel source : this.sourceModels) {
				existingSources.put(source.label, source);
			}

			// Create the new listing of sources
			this.sourceModels.clear();
			int nextY = 10;
			for (String sourceLabel : this.sources) {
				SourceModel source = existingSources.get(sourceLabel);
				if (source != null) {
					source.connection = null;
				} else {
					source = new SourceModel(sourceLabel);
				}
				this.sourceModels.add(source);

				// Position the source
				source.setX(10);
				source.setY(nextY);
				nextY += ySeparation;
			}
			int maxHeight = nextY;

			// Create the map of existing targets
			Map<String, TargetModel> existingTargets = new HashMap<>();
			for (TargetModel target : this.targetModels) {
				existingTargets.put(target.label, target);
			}

			// Determine where to align targets
			int widthX = (int) (this.parent.widthProperty().get() - 100);
			widthX = Math.min(widthX, 500);
			widthX = Math.max(widthX, 200);

			// Create the new listing of targets
			this.targetModels.clear();
			nextY = 10;
			for (String targetLabel : this.targets) {
				TargetModel target = existingTargets.get(targetLabel);
				if (target != null) {
					target.connection = null;
				} else {
					target = new TargetModel(targetLabel);
				}
				this.targetModels.add(target);

				// Position the target
				target.setX(widthX);
				target.setY(nextY);
				nextY += ySeparation;

				// Trigger relocate of model
				target.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
			}
			maxHeight = Math.max(maxHeight, nextY);

			// Load the connections
			Map<String, SourceModel> connectingSources = new HashMap<>();
			for (SourceModel source : this.sourceModels) {
				connectingSources.put(source.label, source);
			}
			Map<String, TargetModel> connectingTargets = new HashMap<>();
			for (TargetModel target : this.targetModels) {
				connectingTargets.put(target.label, target);
			}
			for (String sourceLabel : connectionMappings.keySet()) {
				SourceModel source = connectingSources.get(sourceLabel);
				if (source != null) {
					String targetLabel = connectionMappings.get(sourceLabel);
					TargetModel target = connectingTargets.get(targetLabel);
					if (target != null) {
						// Create connection between source and target
						MappingConnection connection = new MappingConnection(source, target);
						connection.connect();
					}
				}
			}

			// Specify the height to display mappings
			this.parent.setMinHeight(maxHeight);
			this.parent.setMaxHeight(maxHeight);

			// Notify editor of change in model
			this.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
		}
	}

	/**
	 * Source {@link Model}.
	 */
	private static class SourceModel extends AbstractModel {

		/**
		 * Label.
		 */
		private final String label;

		/**
		 * {@link MappingConnection}.
		 */
		private MappingConnection connection = null;

		/**
		 * Instantiate.
		 * 
		 * @param label Label.
		 */
		private SourceModel(String label) {
			this.label = label;
		}
	}

	/**
	 * Target {@link Model}.
	 */
	private static class TargetModel extends AbstractModel {

		/**
		 * Label.
		 */
		public final String label;

		/**
		 * {@link MappingConnection}.
		 */
		private MappingConnection connection = null;

		/**
		 * Instantiate.
		 * 
		 * @param label Label.
		 * @param x     X location.
		 * @param y     Y location.
		 */
		private TargetModel(String label) {
			this.label = label;
		}
	}

	/**
	 * Mapping {@link ConnectionModel}.
	 */
	private static class MappingConnection extends AbstractModel implements ConnectionModel {

		/**
		 * {@link SourceModel}.
		 */
		private final SourceModel source;

		/**
		 * {@link TargetModel}.
		 */
		private final TargetModel target;

		/**
		 * Instantiate.
		 * 
		 * @param source {@link SourceModel}.
		 * @param target {@link TargetModel}.
		 */
		private MappingConnection(SourceModel source, TargetModel target) {
			this.source = source;
			this.target = target;
		}

		/*
		 * ============== ConnectionModel =================
		 */

		@Override
		public boolean isRemovable() {
			return true;
		}

		@Override
		public void connect() {
			this.source.connection = this;
			this.target.connection = this;

			// Fire change to refresh view of model changes
			this.source.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
			this.target.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
		}

		@Override
		public void remove() {
			this.source.connection = null;
			this.target.connection = null;

			// Fire change to refresh view of model changes
			this.source.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
			this.target.firePropertyChange(ChangeEvent.CHANGED.name(), null, null);
		}
	}

}
