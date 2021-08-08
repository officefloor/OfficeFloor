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

package net.officefloor.gef.editor.internal.models;

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

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnectableBuilder;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnectionBuilder;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedPotentialConnection;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.ModelActionContext;
import net.officefloor.gef.editor.OverlayVisualFactory;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Factory for a connectable {@link AdaptedModel}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedConnectableFactory<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedModel<M>>
		extends AbstractAdaptedFactory<R, O, M, E, A> implements AdaptedConnectableBuilder<R, O, M, E> {

	/**
	 * {@link Model} prototype.
	 */
	protected final M modelPrototype;

	/**
	 * Stylesheet content.
	 */
	protected final Property<String> stylesheetContent = new SimpleStringProperty();

	/**
	 * {@link ModelToConnection} instances.
	 */
	protected final Map<Class<? extends ConnectionModel>, ModelToConnection<R, O, M, E, ? extends ConnectionModel>> connections = new HashMap<>();

	/**
	 * {@link Map} of {@link ConnectionKey} to {@link AdaptedConnectionFactory}.
	 */
	protected final Map<ConnectionKey, AdaptedConnectionFactory<R, O, ?, ?, ?>> connectionFactories = new HashMap<>();

	/**
	 * {@link ReadOnlyProperty} to the style sheet {@link URL}.
	 */
	protected ReadOnlyProperty<URL> stylesheetUrl;

	/**
	 * Instantiate as {@link AdaptedChild}.
	 * 
	 * @param configurationPathPrefix Prefix on the configuration path.
	 * @param modelPrototype          {@link Model} prototype.
	 * @param newAdaptedModel         {@link Supplier} for the {@link AdaptedModel}.
	 * @param parentAdaptedFactory    Parent {@link AbstractAdaptedFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AbstractAdaptedConnectableFactory(String configurationPathPrefix, M modelPrototype,
			Supplier<A> newAdaptedModel, AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedFactory) {
		super(configurationPathPrefix, (Class<M>) modelPrototype.getClass(), newAdaptedModel, parentAdaptedFactory);
		this.modelPrototype = modelPrototype;
	}

	/**
	 * Allow {@link AdaptedParentBuilder} inheritance.
	 * 
	 * @param configurationPathPrefix Prefix on the configuration path.
	 * @param modelPrototype          {@link Model} prototype.
	 * @param newAdaptedModel         {@link Supplier} for the {@link AdaptedModel}.
	 * @param contentPartFactory      {@link OfficeFloorContentPartFactory}.
	 */
	@SuppressWarnings("unchecked")
	protected AbstractAdaptedConnectableFactory(String configurationPathPrefix, M modelPrototype,
			Supplier<A> newAdaptedModel, OfficeFloorContentPartFactory<R, O> contentPartFactory) {
		super(configurationPathPrefix, (Class<M>) modelPrototype.getClass(), newAdaptedModel, contentPartFactory);
		this.modelPrototype = modelPrototype;
	}

	/**
	 * Loads the {@link Model} to {@link ConnectionModel}.
	 * 
	 * @param connectionClass   {@link Class} of the {@link ConnectionModel}.
	 * @param modelToConnection {@link ModelToConnection}.
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
		IContentPart<?> contentPart = this.getContentPartFactory().createContentPart(adaptedModel, null);
		contentPart.setContent(adaptedModel);
		contentPart.getVisual();
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
		 * @param sourceModelClass Source {@link Model} {@link Class}.
		 * @param targetModelClass Target {@link Model} {@link Class}.
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
	 * ================ AbstractAdaptedConnectableBuilder =====================
	 */

	@Override
	public Property<String> style() {
		return this.stylesheetContent;
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
	protected static abstract class AbstractAdaptedConnectable<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedConnectable<M>, F extends AbstractAdaptedConnectableFactory<R, O, M, E, A>>
			extends AbstractAdaptedModel<R, O, M, E, A, F>
			implements AdaptedConnectable<M>, ModelActionContext<R, O, M> {

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
			 * @param connectionClass {@link ConnectionModel} {@link Class}.
			 * @param type            {@link AdaptedConnectorRole}. May be
			 *                        <code>null</code>.
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

		/**
		 * Allows overriding the {@link AdaptedConnector}.
		 * 
		 * @param connectionClass {@link ConnectionModel} {@link Class}.
		 * @param role            {@link AdaptedConnectorRole}.
		 * @param connector       {@link ModelToConnection}.
		 * @return {@link AdaptedConnector}.
		 */
		protected AdaptedConnector<M> createAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
				AdaptedConnectorRole role, ModelToConnection<R, O, M, E, ?> connector) {
			return new AdaptedConnectorImpl<>(this, connectionClass, role, connector);
		}

		/*
		 * =================== AdaptedChild =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		protected void init() {

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
							this.createAdaptedConnector(connectionClass, AdaptedConnectorRole.SOURCE,
									selfConnector.getSourceToConnection()));
					this.connectors.put(new ConnectorKey(connectionClass, AdaptedConnectorRole.TARGET),
							this.createAdaptedConnector(connectionClass, AdaptedConnectorRole.TARGET,
									selfConnector.getTargetToConnection()));

				} else {
					// Register the adapted connector (for source/target)
					this.connectors.put(new ConnectorKey(connectionClass, null),
							this.createAdaptedConnector(connectionClass, null, connector));
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
			this.loadDescendantConnections(connections);

			// Return the connections
			return connections;
		}

		/**
		 * Loads the descendant {@link AdaptedConnection} instances.
		 * 
		 * @param connections List to add the {@link AdaptedConnection} instances.
		 */
		protected abstract void loadDescendantConnections(List<AdaptedConnection<?>> connections);

		@Override
		public Property<String> getStylesheet() {
			return this.getFactory().stylesheetContent;
		}

		@Override
		public ReadOnlyProperty<URL> getStylesheetUrl() {
			return this.getFactory().stylesheetUrl;
		}

		@Override
		public List<AdaptedConnector<M>> getAdaptedConnectors() {
			return new ArrayList<>(this.connectors.values());
		}

		@Override
		public <T extends Model> AdaptedPotentialConnection getPotentialConnection(AdaptedConnectable<T> target) {
			return this.getConnectionFactory(target);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Model> void createConnection(AdaptedConnectable<T> target, AdaptedConnectorRole sourceRole) {

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

		/**
		 * Obtains the {@link AdaptedConnectionFactory} for the {@link ConnectionModel}
		 * to the {@link AdaptedChild}.
		 * 
		 * @param target Target {@link AdaptedChild}.
		 * @return {@link AdaptedConnectionFactory} or <code>null</code> if no
		 *         {@link ConnectionModel} to {@link AdaptedChild}.
		 */
		private AdaptedConnectionFactory<R, O, ?, ?, ?> getConnectionFactory(AdaptedConnectable<?> target) {
			Class<?> sourceModelClass = this.getModel().getClass();
			Class<?> targetModelClass = target.getModel().getClass();
			ConnectionKey key = new ConnectionKey(sourceModelClass, targetModelClass);
			return this.getFactory().connectionFactories.get(key);
		}

		@Override
		public int getDragLatency() {
			return this.getFactory().getContentPartFactory().getDragLatency();
		}

		@Override
		public SelectOnly getSelectOnly() {
			return this.getFactory().getContentPartFactory().getSelectOnly();
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

			// Only action if not select only
			if (this.getFactory().getContentPartFactory().getSelectOnly() != null) {
				return; // don't action
			}

			// Undertake the action
			this.getErrorHandler().isError(() -> {
				action.execute((ModelActionContext<r, o, M>) this);
			});
		}
	}

}
