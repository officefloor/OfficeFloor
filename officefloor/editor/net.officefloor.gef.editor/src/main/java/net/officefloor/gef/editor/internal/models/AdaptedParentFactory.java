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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import com.google.inject.Injector;

import net.officefloor.gef.editor.AdaptedActionVisualFactory;
import net.officefloor.gef.editor.AdaptedArea;
import net.officefloor.gef.editor.AdaptedAreaBuilder;
import net.officefloor.gef.editor.AdaptedChildVisualFactory;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.ModelActionContext;
import net.officefloor.gef.editor.OverlayVisualFactory;
import net.officefloor.gef.editor.ParentToAreaConnectionModel;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

/**
 * Factory for an {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedParentFactory<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedChildFactory<R, O, M, E, AdaptedParent<M>> implements AdaptedParentBuilder<R, O, M, E> {

	/**
	 * {@link AdaptedErrorHandler}.
	 */
	private final AdaptedErrorHandler errorHandler;

	/**
	 * {@link ModelAction} to provide the parent.
	 */
	private ModelAction<R, O, M> parentModelProvider = null;

	/**
	 * Listing of means to obtain areas from the parent.
	 */
	private final List<Function<M, List<? extends Model>>> areas = new LinkedList<>();

	/**
	 * Listing of {@link AdaptedArea} change events.
	 */
	private final Set<String> areaChangeEvents = new HashSet<>();

	/**
	 * {@link AdaptedActionsFactory}.
	 */
	private final AdaptedActionsFactory<R, O, M> actionsFactory;

	/**
	 * Instantiate.
	 * 
	 * @param configurationPathPrefix Prefix to the configuration path.
	 * @param modelPrototype          {@link Model} prototype.
	 * @param viewFactory             {@link AdaptedChildVisualFactory}.
	 * @param contentFactory          {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedParentFactory(String configurationPathPrefix, M modelPrototype,
			AdaptedChildVisualFactory<M> viewFactory, OfficeFloorContentPartFactory<R, O> contentFactory) {
		super(configurationPathPrefix, modelPrototype, () -> new AdaptedParentImpl<>(), viewFactory, contentFactory);
		this.errorHandler = contentFactory.getErrorHandler();
		this.actionsFactory = new AdaptedActionsFactory<>(contentFactory);
	}

	/**
	 * Indicates whether can add a new {@link Model}.
	 * 
	 * @return <code>true</code> if can add a new {@link Model}.
	 */
	public boolean isCreate() {
		return this.parentModelProvider != null;
	}

	/**
	 * Creates the {@link AdaptedModel} from this {@link AdaptedParent} prototype.
	 * 
	 * @param factory {@link OfficeFloorContentPartFactory}.
	 * @return {@link AdaptedModel} for the prototype.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedModel<M> createPrototype(OfficeFloorContentPartFactory<R, O> factory) {
		AdaptedParentImpl<R, O, M, E> prototype = (AdaptedParentImpl<R, O, M, E>) factory
				.createAdaptedModel(this.modelPrototype, null);
		prototype.isPalettePrototype = true;
		return prototype;
	}

	/*
	 * ================ AdaptedParentBuilder ==================
	 */

	@Override
	public void create(ModelAction<R, O, M> parentModelProvider) {
		this.parentModelProvider = parentModelProvider;
	}

	@Override
	public void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
		this.actionsFactory.addAction(action, visualFactory);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <AM extends Model, AE extends Enum<AE>, RE extends Enum<RE>> AdaptedAreaBuilder<R, O, AM, AE> area(
			AM areaPrototype, Function<M, List<AM>> getAreas, Function<AM, Dimension> getDimension,
			BiConsumer<AM, Dimension> setDimension, E... changeAreaEvents) {
		this.areas.add((Function) getAreas);
		for (E changeAreaEvent : changeAreaEvents) {
			this.areaChangeEvents.add(changeAreaEvent.name());
		}

		// Create the factory
		AdaptedAreaFactory<R, O, AM, AE> factory = new AdaptedAreaFactory<>(this.getConfigurationPath(), areaPrototype,
				this, getDimension, setDimension);

		// Provide connection to area
		factory.connectOne(ParentToAreaConnectionModel.class, (area) -> {
			AdaptedArea<?> adaptedArea = (AdaptedArea<?>) this.getContentPartFactory().createAdaptedModel(area, null);
			return adaptedArea.getParentConnection();
		}, (conn) -> (AM) conn.getAreaModel()).toMany(this.getModelClass(), (parent) -> {
			AdaptedParent<?> adaptedParent = (AdaptedParent<?>) this.getContentPartFactory().createAdaptedModel(parent,
					null);
			List<ParentToAreaConnectionModel> connections = new LinkedList<>();
			for (AdaptedArea<?> adaptedArea : adaptedParent.getAdaptedAreas()) {
				connections.add(adaptedArea.getParentConnection());
			}
			return connections;
		}, (conn) -> (M) conn.getParentModel());

		return factory;
	}

	/**
	 * {@link AdaptedParent} implementation.
	 */
	public static class AdaptedParentImpl<R extends Model, O, M extends Model, E extends Enum<E>>
			extends AdaptedChildImpl<R, O, M, E, AdaptedParent<M>> implements AdaptedParent<M>, AdaptedPrototype<M> {

		/**
		 * Default not palette prototype.
		 */
		private boolean isPalettePrototype = false;

		/**
		 * {@link AdaptedActions}.
		 */
		private AdaptedActions<R, O, M> actions;

		@Override
		protected void init() {
			super.init();

			// Load the adapter actions
			this.actions = this.getParentFactory().actionsFactory.createAdaptedActions(this);
		}

		/**
		 * Obtains the {@link AdaptedParentFactory}.
		 * 
		 * @return {@link AdaptedParentFactory}.
		 */
		private AdaptedParentFactory<R, O, M, E> getParentFactory() {
			return (AdaptedParentFactory<R, O, M, E>) this.getFactory();
		}

		/*
		 * ================== AdaptedParent ===================
		 */

		@Override
		public boolean isPalettePrototype() {
			return this.isPalettePrototype;
		}

		@Override
		public List<AdaptedArea<?>> getAdaptedAreas() {

			// Load the areas
			List<AdaptedArea<?>> areas = new LinkedList<>();
			for (Function<M, List<? extends Model>> getAreas : this.getParentFactory().areas) {
				for (Model areaModel : getAreas.apply(this.getModel())) {

					// Adapt the area
					AdaptedArea<?> adaptedArea = (AdaptedArea<?>) this.getFactory().getContentPartFactory()
							.createAdaptedModel(areaModel, this.getAdaptedModel());

					// Only add area once
					if (!areas.contains(adaptedArea)) {
						areas.add(adaptedArea);
					}
				}
			}

			// Return the areas
			return areas;
		}

		@Override
		public boolean isAreaChangeEvent(String eventName) {
			return this.getParentFactory().areaChangeEvents.contains(eventName);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getAdapter(Class<T> classKey) {

			// Attempt to handle adapting
			if (AdaptedPrototype.class.equals(classKey)) {
				if (this.getParentFactory().parentModelProvider != null) {
					return (T) this;
				}
			} else if (AdaptedActions.class.equals(classKey)) {
				return (T) this.actions;
			}

			// Not able to adapt
			return null;
		}

		@Override
		public void changeLocation(int x, int y) {
			this.getChangeExecutor().execute(new ChangeLocationOperation<>(this.getModel(), x, y));
		}

		/*
		 * ================ AdaptedPrototype ===================
		 */

		@Override
		public void newAdaptedParent(Point location) {
			this.getParentFactory().errorHandler.isError(
					() -> this.getParentFactory().parentModelProvider.execute(new ModelActionContext<R, O, M>() {

						@Override
						public R getRootModel() {
							return AdaptedParentImpl.this.getRootModel();
						}

						@Override
						public O getOperations() {
							return AdaptedParentImpl.this.getOperations();
						}

						@Override
						public M getModel() {
							return AdaptedParentImpl.this.getModel();
						}

						@Override
						public AdaptedModel<M> getAdaptedModel() {
							return AdaptedParentImpl.this.getAdaptedModel();
						}

						@Override
						public Injector getInjector() {
							return AdaptedParentImpl.this.getInjector();
						}

						@Override
						public void overlay(OverlayVisualFactory overlayVisualFactory) {
							// Use location of dropping new parent
							AdaptedParentImpl.this.getParentFactory().getContentPartFactory().overlay(location.x,
									location.y, overlayVisualFactory);
						}

						@Override
						public ChangeExecutor getChangeExecutor() {
							return AdaptedParentImpl.this.getChangeExecutor();
						}

						@Override
						public M position(M model) {
							model.setX((int) location.x);
							model.setY((int) location.y);
							return model;
						}
					}));
		}
	}

	/**
	 * {@link ITransactionalOperation} to change the location.
	 */
	private static class ChangeLocationOperation<M extends Model> extends AbstractOperation
			implements ITransactionalOperation {

		/**
		 * {@link Model}.
		 */
		private final M model;

		/**
		 * Original X.
		 */
		private final int originalX;

		/**
		 * Original Y.
		 */
		private final int originalY;

		/**
		 * New X.
		 */
		private final int newX;

		/**
		 * New Y.
		 */
		private final int newY;

		/**
		 * Instantiate.
		 */
		public ChangeLocationOperation(M model, int newX, int newY) {
			super("change location");
			this.model = model;
			this.newX = newX;
			this.newY = newY;
			this.originalX = this.model.getX();
			this.originalY = this.model.getY();
		}

		/*
		 * ================== ITransactionalOperation =======================
		 */

		@Override
		public boolean isContentRelevant() {
			return true;
		}

		@Override
		public boolean isNoOp() {
			return false;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			this.model.setX(this.newX);
			this.model.setY(this.newY);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			this.model.setX(this.originalX);
			this.model.setY(this.originalY);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.execute(monitor, info);
		}
	}

}
