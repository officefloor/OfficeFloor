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

package net.officefloor.gef.editor.internal.models;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.function.Supplier;

import com.google.inject.Injector;

import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapts the {@link Model} for use in GEF.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedFactory<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedModel<M>> {

	/**
	 * Registers {@link PropertyChangeListener} for the events.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param <E>
	 *            {@link Model} event type.
	 * @param model
	 *            {@link Model}.
	 * @param events
	 *            Events to listen on.
	 * @param listener
	 *            {@link PropertyChangeListener}.
	 */
	public static <M extends Model, E extends Enum<E>> void registerEventListener(M model, E[] events,
			PropertyChangeListener listener) {
		model.addPropertyChangeListener((event) -> {

			// Determine if interested
			String eventName = event.getPropertyName();
			boolean isInterested = false;
			LABEL_CHANGE: for (Enum<?> labelChangeEvent : events) {
				if (labelChangeEvent.name().equals(eventName)) {
					isInterested = true;
					break LABEL_CHANGE;
				}
			}
			if (!isInterested) {
				return;
			}

			// Interested, so notify of change
			listener.propertyChange(event);
		});
	}

	/**
	 * Path to the content from the top level. This allows for prefix to
	 * configuration for the {@link AdaptedModel}.
	 */
	private final String configurationPath;

	/**
	 * Aids in identifying the {@link Model} in configuration.
	 */
	private final Class<M> modelClass;

	/**
	 * {@link Supplier} for a new {@link AdaptedModel} implementation.
	 */
	private final Supplier<A> newAdaptedModel;

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private final OfficeFloorContentPartFactory<R, O> contentPartFactory;

	/**
	 * {@link Injector}.
	 */
	private Injector injector;

	/**
	 * {@link AbstractAdaptedFactory} for the {@link Model} {@link Class}.
	 */
	private Map<Class<?>, AbstractAdaptedFactory<R, O, ?, ?, ?>> modelFactories;

	/**
	 * Instantiate.
	 * 
	 * @param configurationPathPrefix
	 *            Prefix on the configuration path.
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param newAdaptedModel
	 *            {@link Supplier} for a new {@link AbstractAdaptedModel}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AbstractAdaptedFactory(String configurationPathPrefix, Class<M> modelClass, Supplier<A> newAdaptedModel,
			OfficeFloorContentPartFactory<R, O> contentPartFactory) {
		this.configurationPath = ("".equals(configurationPathPrefix) ? "" : configurationPathPrefix + ".")
				+ modelClass.getSimpleName();
		this.modelClass = modelClass;
		this.newAdaptedModel = newAdaptedModel;
		this.contentPartFactory = contentPartFactory;

		// Register this model adapter
		this.contentPartFactory.registerModel(this);
	}

	/**
	 * Instantiate from existing {@link AbstractAdaptedFactory}.
	 * 
	 * @param adaptedPathPrefix
	 *            Prefix on the adapted path.
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param newAdaptedModel
	 *            {@link Supplier} for a new {@link AbstractAdaptedModel}.
	 * @param parentAdaptedModel
	 *            Parent {@link AbstractAdaptedModel}.
	 */
	public AbstractAdaptedFactory(String adaptedPathPrefix, Class<M> modelClass, Supplier<A> newAdaptedModel,
			AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel) {
		this(adaptedPathPrefix, modelClass, newAdaptedModel, parentAdaptedModel.contentPartFactory);
	}

	/**
	 * Obtains the {@link Model} {@link Class}.
	 * 
	 * @return {@link Model} {@link Class}.
	 */
	public Class<M> getModelClass() {
		return this.modelClass;
	}

	/**
	 * Initialises.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 * @param modelFactories
	 *            {@link Map} of {@link Model} {@link Class} to
	 *            {@link AbstractAdaptedFactory}.
	 * @throws IllegalStateException
	 *             If invalid.
	 */
	public void init(Injector injector, Map<Class<?>, AbstractAdaptedFactory<R, O, ?, ?, ?>> modelFactories)
			throws IllegalStateException {
		this.injector = injector;
		this.modelFactories = modelFactories;
	}

	/**
	 * Obtains the {@link Injector}.
	 * 
	 * @return {@link Injector}.
	 */
	protected Injector getInjector() {
		return this.injector;
	}

	/**
	 * Obtains the {@link OfficeFloorContentPartFactory}.
	 * 
	 * @return {@link OfficeFloorContentPartFactory}.
	 */
	protected OfficeFloorContentPartFactory<R, O> getContentPartFactory() {
		return this.contentPartFactory;
	}

	/**
	 * Obtains the configuration path.
	 * 
	 * @return Configuration path.
	 */
	public String getConfigurationPath() {
		return this.configurationPath;
	}

	/**
	 * Obtains the {@link AbstractAdaptedFactory} for the {@link Model}
	 * {@link Class}.
	 * 
	 * @param modelClass
	 *            {@link Model} {@link Class}.
	 * @return {@link AbstractAdaptedFactory}.
	 */
	protected AbstractAdaptedFactory<?, ?, ?, ?, ?> getModelFactory(Class<?> modelClass) {
		return this.modelFactories.get(modelClass);
	}

	/**
	 * Undertake validation.
	 * 
	 * @throws IllegalStateException
	 *             If invalid.
	 */
	public void validate() throws IllegalStateException {
	}

	/**
	 * Creates the {@link AdaptedModel} for the {@link Model}.
	 *
	 * @param <m>
	 *            {@link Model} type.
	 * @param model
	 *            {@link Model}.
	 * @param parentAdaptedModel
	 *            Parent {@link AdaptedModel}.
	 * @return {@link AdaptedModel}.
	 */
	public <m extends Model> AdaptedModel<m> getAdaptedModel(m model, AdaptedModel<?> parentAdaptedModel) {
		return this.contentPartFactory.createAdaptedModel(model, parentAdaptedModel);
	}

	/**
	 * Creates a new {@link AdaptedModel} for the {@link Model}.
	 *
	 * @param rootModel
	 *            Root {@link Model}.
	 * @param operations
	 *            Operations.
	 * @param parentAdaptedModel
	 *            Parent {@link AdaptedModel}.
	 * @param model
	 *            {@link Model}.
	 * @return {@link AbstractAdaptedFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public final A newAdaptedModel(R rootModel, O operations, AdaptedModel<?> parentAdaptedModel, M model) {
		A adapted = this.newAdaptedModel.get();
		AbstractAdaptedModel<R, O, M, E, A, ?> abstractAdapted = (AbstractAdaptedModel<R, O, M, E, A, ?>) adapted;
		abstractAdapted.factory = this;
		abstractAdapted.rootModel = rootModel;
		abstractAdapted.operations = operations;
		abstractAdapted.parentAdaptedModel = parentAdaptedModel;
		abstractAdapted.model = model;
		abstractAdapted.init();
		return adapted;
	}

	/**
	 * Builder to create the {@link AbstractAdaptedFactory} for a particular
	 * {@link Model}.
	 */
	protected static abstract class AbstractAdaptedModel<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedModel<M>, F extends AbstractAdaptedFactory<R, O, M, E, A>>
			implements AdaptedModel<M> {

		/**
		 * {@link AbstractAdaptedFactory}.
		 */
		private AbstractAdaptedFactory<R, O, M, E, A> factory;

		/**
		 * Root {@link Model}.
		 */
		private R rootModel;

		/**
		 * Operations.
		 */
		private O operations;

		/**
		 * Parent {@link AdaptedModel}.
		 */
		private AdaptedModel<?> parentAdaptedModel;

		/**
		 * {@link Model},
		 */
		private M model;

		/**
		 * Initialises this {@link AdaptedModel}.
		 */
		protected abstract void init();

		/*
		 * ================= AdaptedModel ===================
		 */

		@Override
		public M getModel() {
			return this.model;
		}

		@Override
		public AdaptedModel<?> getParent() {
			return this.parentAdaptedModel;
		}

		@Override
		public AdaptedErrorHandler getErrorHandler() {
			return this.getFactory().getContentPartFactory().getErrorHandler();
		}

		/**
		 * Obtains the {@link AbstractAdaptedFactory}.
		 * 
		 * @return {@link AbstractAdaptedFactory}.
		 */
		@SuppressWarnings("unchecked")
		protected F getFactory() {
			return (F) this.factory;
		}

		/**
		 * Obtains the root {@link Model}.
		 * 
		 * @return Root {@link Model}.
		 */
		public R getRootModel() {
			return this.rootModel;
		}

		/**
		 * Obtains the operations.
		 * 
		 * @return Operations.
		 */
		public O getOperations() {
			return this.operations;
		}

		/**
		 * Triggers refreshing the content {@link Model} instances. This includes the
		 * {@link ConnectionModel} instances.
		 */
		public void refreshContent() {
			this.getFactory().getContentPartFactory().loadContentModels();
		}

		/**
		 * Obtains the {@link Injector}.
		 * 
		 * @return {@link Injector}.
		 */
		public Injector getInjector() {
			return this.factory.injector;
		}

		/**
		 * Obtains the {@link ChangeExecutor}.
		 * 
		 * @return {@link ChangeExecutor}.
		 */
		public ChangeExecutor getChangeExecutor() {
			return this.factory.contentPartFactory.getChangeExecutor();
		}

		/**
		 * Positions the model.
		 * 
		 * @param model
		 *            {@link Model}.
		 * @return Input {@link Model}.
		 */
		public M position(M model) {

			// Obtain the parent
			model.setX(this.model.getX());
			model.setY(this.model.getY());

			// Return the model
			return model;
		}

		/**
		 * Registers an event listener.
		 * 
		 * @param events
		 *            Events to listen on.
		 * @param listener
		 *            {@link PropertyChangeListener} for the events.
		 */
		protected void registerEventListener(E[] events, PropertyChangeListener listener) {
			AbstractAdaptedFactory.registerEventListener(this.model, events, listener);
		}
	}

}
