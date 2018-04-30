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
package net.officefloor.eclipse.editor.internal.models;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionBuilder;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedConnectorRole;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedPotentialConnection;
import net.officefloor.eclipse.editor.ChildrenGroup;
import net.officefloor.eclipse.editor.ChildrenGroupBuilder;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.eclipse.editor.internal.parts.AdaptedChildPart;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Factory for an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedChildFactory<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
		extends AbstractAdaptedFactory<R, O, M, E, A> implements AdaptedChildBuilder<R, O, M, E> {

	/**
	 * {@link Model} prototype.
	 */
	protected final M modelPrototype;

	/**
	 * {@link AdaptedModelVisualFactory}.
	 */
	private final AdaptedModelVisualFactory<M> viewFactory;

	/**
	 * Stylesheet content.
	 */
	private final Property<String> stylesheetContent = new SimpleStringProperty();

	/**
	 * {@link Function} to get the label from the {@link Model}.
	 */
	private Function<M, String> getLabel = null;

	/**
	 * {@link LabelChange}.
	 */
	private LabelChange<M> setLabel = null;

	/**
	 * {@link Enum} events fired by the {@link Model} for label changes.
	 */
	private E[] labelEvents = null;

	/**
	 * Static listing of {@link ChildrenGroupFactory} instances for the
	 * {@link Model}.
	 */
	private final List<ChildrenGroupFactory<R, O, M, E>> childrenGroups = new ArrayList<>();

	/**
	 * {@link ModelToConnection} instances.
	 */
	private final Map<Class<? extends ConnectionModel>, ModelToConnection<R, O, M, E, ? extends ConnectionModel>> connections = new HashMap<>();

	/**
	 * {@link Map} of {@link ConnectionKey} to {@link AdaptedConnectionFactory}.
	 */
	private final Map<ConnectionKey, AdaptedConnectionFactory<R, O, ?, ?, ?>> connectionFactories = new HashMap<>();

	/**
	 * {@link ReadOnlyProperty} to the stylesheet {@link URL}.
	 */
	private ReadOnlyProperty<URL> stylesheetUrl;

	/**
	 * Instantiate as {@link AdaptedChild}.
	 * 
	 * @param configurationPathPrefix
	 *            Prefix on the configuration path.
	 * @param modelPrototype
	 *            {@link Model} prototype.
	 * @param viewFactory
	 *            {@link AdaptedModelVisualFactory}.
	 * @param parentAdaptedModel
	 *            Parent {@link AbstractAdaptedFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedChildFactory(String configurationPathPrefix, M modelPrototype,
			AdaptedModelVisualFactory<M> viewFactory, AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel) {
		super(configurationPathPrefix, (Class<M>) modelPrototype.getClass(),
				() -> (A) new AdaptedChildImpl<R, O, M, E, AdaptedChild<M>>(), parentAdaptedModel);
		this.modelPrototype = modelPrototype;
		this.viewFactory = viewFactory;
	}

	/**
	 * Allow {@link AdaptedParentBuilder} inheritance.
	 * 
	 * @param configurationPathPrefix
	 *            Prefix on the configuration path.
	 * @param modelPrototype
	 *            {@link Model} prototype.
	 * @param newAdaptedModel
	 *            {@link Supplier} for the {@link AdaptedModel}.
	 * @param viewFactory
	 *            {@link AdaptedModelVisualFactory}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	@SuppressWarnings("unchecked")
	protected AdaptedChildFactory(String configurationPathPrefix, M modelPrototype, Supplier<A> newAdaptedModel,
			AdaptedModelVisualFactory<M> viewFactory, OfficeFloorContentPartFactory<R, O> contentPartFactory) {
		super(configurationPathPrefix, (Class<M>) modelPrototype.getClass(), newAdaptedModel, contentPartFactory);
		this.modelPrototype = modelPrototype;
		this.viewFactory = viewFactory;
	}

	/**
	 * Loads the {@link Model} to {@link ConnectionModel}.
	 * 
	 * @param connectionClass
	 *            {@link Class} of the {@link ConnectionModel}.
	 * @param modelToConnection
	 *            {@link ModelToConnection}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadModelToConnection(Class<? extends ConnectionModel> connectionClass,
			ModelToConnection<R, O, ?, ?, ?> modelToConnection) {

		// Obtain the possible existing connection
		ModelToConnection<R, O, M, E, ?> existing = this.connections.get(connectionClass);
		if (existing != null) {

			// Determine if linking to self
			AdaptedConnectionFactory<R, O, ?, ?, ?> connectionFactory = modelToConnection.getAdaptedConnectionFactory();
			if ((!(existing instanceof ModelToSelfConnection))
					&& (connectionFactory.getSourceModelClass() == connectionFactory.getTargetModelClass())) {

				// Overwrite with model to self connection
				this.connections.put(connectionClass,
						new ModelToSelfConnection<>(existing, (ModelToConnection) modelToConnection));
				return;
			}

			// Configured connection twice
			throw new IllegalStateException("Connection " + connectionClass.getName() + " already configured for model "
					+ this.getModelClass().getName());
		}
		this.connections.put(connectionClass,
				(ModelToConnection<R, O, M, E, ? extends ConnectionModel>) modelToConnection);
	}

	/*
	 * =================== AbstractAdaptedFactory =============
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void validate() throws IllegalStateException {

		// Register the styling
		this.stylesheetUrl = this.getContentPartFactory().getStyleRegistry().registerStyle(this.getConfigurationPath(),
				this.stylesheetContent);

		// Ensure all the connection events are same enum
		E firstEvent = null;
		for (Class<? extends ConnectionModel> connectionClass : this.connections.keySet()) {
			ModelToConnection<R, O, M, E, ? extends ConnectionModel> connector = this.connections.get(connectionClass);
			E[] events = connector.getConnectionChangeEvents();
			for (int i = 0; i < events.length; i++) {
				E checkEvent = events[i];
				if (firstEvent == null) {
					firstEvent = checkEvent;
				} else {
					if (firstEvent.getDeclaringClass() != checkEvent.getDeclaringClass()) {
						throw new IllegalStateException(
								"Differing connection event enums for model " + this.getModelClass().getName() + " ["
										+ firstEvent.name() + ", " + checkEvent.name() + "]");
					}
				}
			}
		}

		// Ensure no ambiguity in source to target connections
		for (Class<? extends ConnectionModel> connectionClass : this.connections.keySet()) {
			ModelToConnection<R, O, M, E, ? extends ConnectionModel> connector = this.connections.get(connectionClass);
			Class<?> sourceModelClass = connector.getAdaptedConnectionFactory().getSourceModelClass();
			Class<?> targetModelClass = connector.getAdaptedConnectionFactory().getTargetModelClass();

			// Determine if same models but multiple connections configured
			ConnectionKey connectionKey = new ConnectionKey(sourceModelClass, targetModelClass);
			AdaptedConnectionFactory<R, O, ?, ?, ?> connectionFactory = this.connectionFactories.get(connectionKey);
			if (connectionFactory != null) {
				throw new IllegalStateException("Ambiguous connection between " + sourceModelClass.getName() + " and "
						+ targetModelClass.getName() + " for connections " + connectionClass.getName() + " and "
						+ connectionFactory.getModelClass().getName());
			}

			// Load the connection factory
			connectionFactory = connector.getAdaptedConnectionFactory();
			this.connectionFactories.put(connectionKey, connectionFactory);
		}

		// Construct the view (ensures all children groups are registered)
		A adaptedModel = (A) this.getContentPartFactory().createAdaptedModel(this.modelPrototype, null);

		// Create the visual (will ensure valid)
		AdaptedChildPart<M, AdaptedChild<M>> childPart = this.getInjector().getInstance(AdaptedChildPart.class);
		childPart.setContent(adaptedModel);
		childPart.doCreateVisual();
	}

	/**
	 * Key to identify unique source/target relationships.
	 */
	private static class ConnectionKey {

		/**
		 * Source {@link Model} {@link Class}.
		 */
		private final Class<?> sourceModelClass;

		/**
		 * Target {@link Model} {@link Class}.
		 */
		private final Class<?> targetModelClass;

		/**
		 * Instantiate.
		 * 
		 * @param sourceModelClass
		 *            Source {@link Model} {@link Class}.
		 * @param targetModelClass
		 *            Target {@link Model} {@link Class}.
		 */
		public ConnectionKey(Class<?> sourceModelClass, Class<?> targetModelClass) {
			this.sourceModelClass = sourceModelClass;
			this.targetModelClass = targetModelClass;
		}

		/*
		 * ================= Object ===============================
		 */

		@Override
		public int hashCode() {
			// Hash key matches regardless of source/target order
			return this.sourceModelClass.hashCode() + this.targetModelClass.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ConnectionKey)) {
				return false;
			}
			ConnectionKey that = (ConnectionKey) obj;

			// Determine if same
			if (this.sourceModelClass.equals(that.sourceModelClass)
					&& (this.targetModelClass.equals(that.targetModelClass))) {
				return true;
			} else if (this.sourceModelClass.equals(that.targetModelClass)
					&& (this.targetModelClass.equals(that.sourceModelClass))) {
				return true;
			} else {
				// Not connecting the same model types
				return false;
			}
		}
	}

	/*
	 * ==================== AdaptedChildBuilder =====================
	 */

	@Override
	@SafeVarargs
	public final void label(Function<M, String> getLabel, E... labelChangeEvents) {
		this.label(getLabel, null, labelChangeEvents);
	}

	@Override
	@SafeVarargs
	public final void label(Function<M, String> getLabel, LabelChange<M> setLabel, E... labelChangeEvents) {
		this.getLabel = getLabel;
		this.setLabel = setLabel;
		this.labelEvents = labelChangeEvents;
	}

	@Override
	public Property<String> style() {
		return this.stylesheetContent;
	}

	@Override
	@SafeVarargs
	public final ChildrenGroupBuilder<R, O> children(String childGroupName,
			Function<M, List<? extends Model>> getChildren, E... childrenEvents) {
		ChildrenGroupFactory<R, O, M, E> factory = new ChildrenGroupFactory<>(this.getConfigurationPath(),
				childGroupName, getChildren, childrenEvents, this);
		this.childrenGroups.add(factory);
		return factory;
	}

	@Override
	@SafeVarargs
	public final <C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectOne(
			Class<C> connectionClass, Function<M, C> getConnection, Function<C, M> getSource,
			E... connectionChangeEvents) {
		return this.connectMany(connectionClass, (model) -> {
			C connection = getConnection.apply(model);
			if (connection == null) {
				return Collections.emptyList();
			} else {
				return Arrays.asList(connection);
			}
		}, getSource, connectionChangeEvents);
	}

	@Override
	@SafeVarargs
	public final <C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectMany(
			Class<C> connectionClass, Function<M, List<C>> getConnections, Function<C, M> getSource,
			E... connectionChangeEvents) {
		AdaptedConnectionFactory<R, O, M, C, E> adaptedConnectionFactory = new AdaptedConnectionFactory<>(
				this.getConfigurationPath(), connectionClass, this.getModelClass(), getSource, this);
		this.loadModelToConnection(connectionClass,
				new ModelToConnection<>(getConnections, connectionChangeEvents, adaptedConnectionFactory));
		return adaptedConnectionFactory;
	}

	/**
	 * {@link AdaptedChild} implementation.
	 */
	protected static class AdaptedChildImpl<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
			extends AbstractAdaptedModel<R, O, M, E, A, AdaptedChildFactory<R, O, M, E, A>>
			implements AdaptedChild<M>, ModelActionContext<R, O, M> {

		/**
		 * Label for the {@link Model}.
		 */
		private ReadOnlyStringWrapper label;

		/**
		 * Potential conflict in {@link LabelChange}.
		 */
		private ReadOnlyStringWrapper labelConflict;

		/**
		 * {@link ChildrenGroupImpl} instances.
		 */
		private List<ChildrenGroup<M, ?>> childrenGroups;

		/**
		 * {@link AdaptedConnector} instances by {@link ConnectorKey}.
		 */
		private Map<ConnectorKey, AdaptedConnector<M>> connectors;

		/**
		 * Key to identify an {@link AdapatedConnector}.
		 */
		private static class ConnectorKey {

			/**
			 * {@link ConnectionModel} {@link Class}.
			 */
			private final Class<? extends ConnectionModel> connectionClass;

			/**
			 * {@link AdaptedConnectorRole}. May be <code>null</code>.
			 */
			private final AdaptedConnectorRole type;

			/**
			 * Instantiate.
			 * 
			 * @param connectionClass
			 *            {@link ConnectionModel} {@link Class}.
			 * @param type
			 *            {@link AdaptedConnectorRole}. May be <code>null</code>.
			 */
			private ConnectorKey(Class<? extends ConnectionModel> connectionClass, AdaptedConnectorRole type) {
				this.connectionClass = connectionClass;
				this.type = type;
			}

			/*
			 * ============= Object ================
			 */

			@Override
			public int hashCode() {
				return this.connectionClass.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof ConnectorKey)) {
					return false;
				}
				ConnectorKey that = (ConnectorKey) obj;

				// Ensure same connector class
				if (!(this.connectionClass.equals(that.connectionClass))) {
					return false;
				}

				// Ensure match on type
				if ((this.type == null) || (that.type == null)) {
					return true;
				} else {
					return this.type.equals(that.type);
				}
			}
		}

		/*
		 * =================== AdaptedChild =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		protected void init() {

			// Load the children groups
			this.childrenGroups = new ArrayList<>(this.getFactory().childrenGroups.size());
			for (ChildrenGroupFactory<R, O, M, E> childrenGroupFactory : this.getFactory().childrenGroups) {
				this.childrenGroups.add(childrenGroupFactory.createChildrenGroup(this));
			}

			// Load the connectors and events for changes
			this.connectors = new HashMap<>(this.getFactory().connections.size());
			List<E> connectionChangeEvents = new ArrayList<>();
			for (Class<? extends ConnectionModel> connectionClass : this.getFactory().connections.keySet()) {
				// Obtain the model to connector
				ModelToConnection<R, O, M, E, ?> connector = this.getFactory().connections.get(connectionClass);

				// Determine if connecting to self
				if (connector instanceof ModelToSelfConnection) {
					ModelToSelfConnection<R, O, M, E, ?> selfConnector = (ModelToSelfConnection<R, O, M, E, ?>) connector;

					// Register the source and target connector
					this.connectors.put(new ConnectorKey(connectionClass, AdaptedConnectorRole.SOURCE),
							new AdaptedConnectorImpl<>(this, connectionClass, AdaptedConnectorRole.SOURCE,
									selfConnector.getSourceToConnection()));
					this.connectors.put(new ConnectorKey(connectionClass, AdaptedConnectorRole.TARGET),
							new AdaptedConnectorImpl<>(this, connectionClass, AdaptedConnectorRole.TARGET,
									selfConnector.getTargetToConnection()));

				} else {
					// Register the adapted connector (for source/target)
					this.connectors.put(new ConnectorKey(connectionClass, null),
							new AdaptedConnectorImpl<>(this, connectionClass, null, connector));
				}

				// Register the connection change events
				connectionChangeEvents.addAll(Arrays.asList(connector.getConnectionChangeEvents()));
			}

			// Handle change in connectors
			if (connectionChangeEvents.size() > 0) {
				E[] events = (E[]) Array.newInstance(connectionChangeEvents.get(0).getDeclaringClass(),
						connectionChangeEvents.size());
				for (int i = 0; i < events.length; i++) {
					events[i] = connectionChangeEvents.get(i);
				}
				this.registerEventListener(connectionChangeEvents.toArray(events), (event) -> {
					this.refreshContent();
				});
			}

			// Determine if label
			if (this.getFactory().getLabel == null) {
				// No label for model
				this.label = null;
				this.labelConflict = null;

			} else {
				// Create the label conflict property
				this.labelConflict = new ReadOnlyStringWrapper("");

				// Create the edit label property
				String initialLabel = this.getFactory().getLabel.apply(this.getModel());
				this.label = new ReadOnlyStringWrapper(this.getModel(), "Label",
						initialLabel != null ? initialLabel : "");
				this.label.addListener((lister, oldValue, newValue) -> {

					// Drop out (of potential loop) if changing to same model value
					String currentValue = this.getFactory().getLabel.apply(this.getModel());
					currentValue = currentValue == null ? "" : currentValue;
					newValue = newValue == null ? "" : newValue;
					if (currentValue.equals(newValue)) {
						return;
					}

					// Attempt to change the label
					Change<M> change = this.getFactory().setLabel.changeLabel(this.getModel(), newValue);
					Conflict[] conflicts = change.getConflicts();
					if (conflicts.length > 0) {

						// Load the conflict error
						StringBuilder message = new StringBuilder(conflicts[0].getConflictDescription());
						for (int i = 1; i < conflicts.length; i++) {
							message.append("\n");
							message.append(conflicts[i].getConflictDescription());
						}
						this.labelConflict.set(message.toString());
						return;
					}

					// As here, no conflicts
					this.labelConflict.set("");

					// Set to new label before changing in model (stops loops)
					this.label.set(newValue);

					// Under take the change
					this.getChangeExecutor().execute(change);
				});

				// Observe if label changed externally
				this.registerEventListener(this.getFactory().labelEvents, (event) -> {
					String newLabel = (String) event.getNewValue();
					this.label.set(newLabel);
				});
			}
		}

		@Override
		public List<AdaptedConnection<?>> getConnections() {

			// Load the connections
			List<AdaptedConnection<?>> connections = new ArrayList<>();

			// Load direct connections
			for (ModelToConnection<R, O, M, ?, ? extends ConnectionModel> modelToConnection : this
					.getFactory().connections.values()) {

				// Obtain the connections
				List<? extends ConnectionModel> connectionModels = modelToConnection.getConnections(this.getModel());

				// Adapt the connections
				for (ConnectionModel connectionModel : connectionModels) {

					// Adapt the connection
					AdaptedConnection<?> adaptedConnection = (AdaptedConnection<?>) this.getFactory()
							.getContentPartFactory().createAdaptedModel(connectionModel, null);
					connections.add(adaptedConnection);
				}
			}

			// Load the descendant connections
			for (ChildrenGroup<M, ?> childrenGroup : this.childrenGroups) {
				for (AdaptedChild<?> adaptedChild : childrenGroup.getChildren()) {
					connections.addAll(adaptedChild.getConnections());
				}
			}

			// Return the connections
			return connections;
		}

		@Override
		public ReadOnlyStringProperty getLabel() {
			return this.label == null ? null : this.label.getReadOnlyProperty();
		}

		@Override
		public StringProperty getEditLabel() {
			return this.label;
		}

		@Override
		public ReadOnlyProperty<URL> getStylesheetUrl() {
			return this.getFactory().stylesheetUrl;
		}

		@Override
		public List<ChildrenGroup<M, ?>> getChildrenGroups() {
			return this.childrenGroups;
		}

		@Override
		public List<AdaptedConnector<M>> getAdaptedConnectors() {
			return new ArrayList<>(this.connectors.values());
		}

		@Override
		public <T extends Model> AdaptedPotentialConnection getPotentialConnection(AdaptedChild<T> target) {
			return this.getConnectionFactory(target);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Model> void createConnection(AdaptedChild<T> target, AdaptedConnectorRole sourceRole) {

			// Determine if connection already to target
			for (AdaptedConnection<?> connection : this.getConnections()) {
				if (sourceRole == null) {
					// No particular role, so match any connection
					if ((connection.getSource() == target) || (connection.getTarget() == target)) {
						return; // already connected
					}
				} else {
					// Match based on role
					switch (sourceRole) {
					case SOURCE:
						if (connection.getTarget() == target) {
							return; // already connected as source
						}
						break;
					case TARGET:
						if (connection.getSource() == target) {
							return; // already connected as target
						}
						break;
					}
				}
			}

			// Ensure able to connect
			AdaptedConnectionFactory<R, O, ?, ?, ?> connectionFactory = this.getConnectionFactory(target);
			if (connectionFactory == null) {
				return; // no connection
			}
			if (!connectionFactory.canCreateConnection()) {
				return; // not able to create the connection
			}

			// Determine the source target order
			M sourceModel = this.getModel();
			T targetModel = target.getModel();
			boolean isSwap = false;
			if (connectionFactory.getSourceModelClass().equals(connectionFactory.getTargetModelClass())) {
				// Connecting to same type, so order based on role
				if (AdaptedConnectorRole.TARGET.equals(sourceRole)) {
					// This is target, so swap
					isSwap = true;
				}
			} else {
				// Ensure order matches source / target
				if (sourceModel.getClass().equals(connectionFactory.getTargetModelClass())) {
					// This is target, so swap
					isSwap = true;
				}
			}
			if (isSwap) {
				M swap = sourceModel;
				sourceModel = (M) targetModel;
				targetModel = (T) swap;
			}

			// Create the connection
			connectionFactory.createConnection(sourceModel, targetModel);
		}

		@Override
		public AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
				AdaptedConnectorRole type) {
			AdaptedConnector<M> connector = this.connectors.get(new ConnectorKey(connectionClass, type));
			if (connector == null) {
				throw new IllegalStateException("No connector for connection " + connectionClass.getName()
						+ " from model " + this.getModel().getClass().getName());
			}
			return connector;
		}

		@Override
		public Pane createVisual(AdaptedModelVisualFactoryContext<M> context) {
			return this.getFactory().viewFactory.createVisual(this.getModel(), context);
		}

		/**
		 * Obtains the {@link AdaptedConnectionFactory} for the {@link ConnectionModel}
		 * to the {@link AdaptedChild}.
		 * 
		 * @param target
		 *            Target {@link AdaptedChild}.
		 * @return {@link AdaptedConnectionFactory} or <code>null</code> if no
		 *         {@link ConnectionModel} to {@link AdaptedChild}.
		 */
		private AdaptedConnectionFactory<R, O, ?, ?, ?> getConnectionFactory(AdaptedChild<?> target) {
			Class<?> sourceModelClass = this.getModel().getClass();
			Class<?> targetModelClass = target.getModel().getClass();
			ConnectionKey key = new ConnectionKey(sourceModelClass, targetModelClass);
			return this.getFactory().connectionFactories.get(key);
		}

		/*
		 * ================= ModelActionContext ======================
		 */

		@Override
		public AdaptedModel<M> getAdaptedModel() {
			return this;
		}

		@Override
		public void overlay(OverlayVisualFactory overlayVisualFactory) {

			// Obtain the location of this parent
			Model model = this.getModel();

			// Add the overlay
			this.getFactory().getContentPartFactory().overlay(model.getX(), model.getY(), overlayVisualFactory);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <r extends Model, o> void action(ModelAction<r, o, M> action) {
			this.getErrorHandler().isError(() -> {
				action.execute((ModelActionContext<r, o, M>) this);
			});
		}
	}

}