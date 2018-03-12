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
package net.officefloor.eclipse.editor.model;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

/**
 * Adapts the {@link Model} for use in GEF.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedModelFactory<M extends Model, E extends Enum<E>, A extends AdaptedModel<M>> {

	/**
	 * Registers {@link PropertyChangeListener} for the events.
	 * 
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
	 * Aids in identifying the {@link Model} in configuration.
	 */
	private final Class<M> modelClass;

	/**
	 * {@link IContentPart} {@link Class}.
	 */
	private final Class<?> partClass;

	/**
	 * {@link Supplier} for a new {@link AdaptedModel} implementation.
	 */
	private final Supplier<A> newAdaptedModel;

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private final OfficeFloorContentPartFactory contentPartFactory;

	/**
	 * Instantiate.
	 * 
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param partClass
	 *            {@link IContentPart} {@link Class}.
	 * @param newAdaptedModel
	 *            {@link Supplier} for a new {@link AbstractAdaptedModel}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AbstractAdaptedModelFactory(Class<M> modelClass, Class<?> partClass, Supplier<A> newAdaptedModel,
			OfficeFloorContentPartFactory contentPartFactory) {
		this.modelClass = modelClass;
		this.partClass = partClass;
		this.newAdaptedModel = newAdaptedModel;
		this.contentPartFactory = contentPartFactory;

		// Register this model adapter
		this.contentPartFactory.registerModel(this);
	}

	/**
	 * Instantiate from existing {@link AbstractAdaptedModelFactory}.
	 * 
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param partClass
	 *            {@link IContentPart} {@link Class}.
	 * @param newAdaptedModel
	 *            {@link Supplier} for a new {@link AbstractAdaptedModel}.
	 * @param parentAdaptedModel
	 *            Parent {@link AbstractAdaptedModel}.
	 */
	public AbstractAdaptedModelFactory(Class<M> modelClass, Class<?> partClass, Supplier<A> newAdaptedModel,
			AbstractAdaptedModelFactory<?, ?, ?> parentAdaptedModel) {
		this(modelClass, partClass, newAdaptedModel, parentAdaptedModel.contentPartFactory);
	}

	/**
	 * Undertake validation.
	 * 
	 * @param models
	 *            {@link Map} of {@link Model} {@link Class} to
	 *            {@link AbstractAdaptedModelFactory}.
	 * @throws IllegalStateException
	 *             If invalid.
	 */
	public void validate(Map<Class<?>, AbstractAdaptedModelFactory<?, ?, ?>> models) throws IllegalStateException {
	}

	/**
	 * Creates the {@link AbstractAdaptedModelFactory} for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param changeExecutor
	 *            {@link ChangeExecutor}.
	 * @return {@link AbstractAdaptedModelFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public A createAdaptedModel(M model, ChangeExecutor changeExecutor) {
		A adapted = this.newAdaptedModel.get();
		AbstractAdaptedModel<M, E, A, ?> abstractAdapted = (AbstractAdaptedModel<M, E, A, ?>) adapted;
		abstractAdapted.factory = this;
		abstractAdapted.model = model;
		abstractAdapted.changeExecutor = changeExecutor;
		abstractAdapted.init();
		return adapted;
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
	 * Obtains the {@link IContentPart} {@link Class}.
	 * 
	 * @return {@link IContentPart} {@link Class}.
	 */
	public Class<?> getPartClass() {
		return this.partClass;
	}

	/**
	 * Builder to create the {@link AbstractAdaptedModelFactory} for a particular
	 * {@link Model}.
	 */
	protected static abstract class AbstractAdaptedModel<M extends Model, E extends Enum<E>, A extends AdaptedModel<M>, F extends AbstractAdaptedModelFactory<M, E, A>>
			implements AdaptedModel<M> {

		/**
		 * {@link AbstractAdaptedModelFactory}.
		 */
		private AbstractAdaptedModelFactory<M, E, A> factory;

		/**
		 * {@link Model},
		 */
		private M model;

		/**
		 * {@link ChangeExecutor}.
		 */
		private ChangeExecutor changeExecutor;

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

		/**
		 * Obtains the {@link AbstractAdaptedModelFactory}.
		 * 
		 * @return {@link AbstractAdaptedModelFactory}.
		 */
		@SuppressWarnings("unchecked")
		protected F getFactory() {
			return (F) this.factory;
		}

		/**
		 * Obtains the {@link ChangeExecutor}.
		 * 
		 * @return {@link ChangeExecutor}.
		 */
		protected ChangeExecutor getChangeExecutor() {
			return this.changeExecutor;
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
			AbstractAdaptedModelFactory.registerEventListener(this.model, events, listener);
		}
	}

}