/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.internal.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.eclipse.editor.AdaptedAreaBuilder;
import net.officefloor.eclipse.editor.AdaptedChildVisualFactory;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.ParentToAreaConnectionModel;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
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
	 * {@link ModelToAction} instances.
	 */
	private final List<ModelToAction<R, O, M>> modelToActions = new LinkedList<>();

	/**
	 * Listing of means to obtain areas from the parent.
	 */
	private final List<Function<M, List<? extends Model>>> areas = new LinkedList<>();

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
		this.modelToActions.add(new ModelToAction<>(action, visualFactory));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <AM extends Model, AE extends Enum<AE>, RE extends Enum<RE>> AdaptedAreaBuilder<R, O, AM, AE> area(
			AM areaPrototype, Function<M, List<AM>> getAreas, Function<AM, Dimension> getDimension,
			BiConsumer<AM, Dimension> setDimension, AdaptedModelVisualFactory<AM> viewFactory, E... changeAreaEvents) {
		this.areas.add((Function) getAreas);
		AdaptedAreaFactory<R, O, AM, AE> factory = new AdaptedAreaFactory<>(this.getConfigurationPath(), areaPrototype,
				this, getDimension, setDimension, viewFactory);

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
	 * {@link Model} to {@link ModelAction}.
	 */
	private static class ModelToAction<R extends Model, O, M extends Model> {

		/**
		 * {@link ModelAction}.
		 */
		private final ModelAction<R, O, M> action;

		/**
		 * {@link AdaptedActionVisualFactory}.
		 */
		private final AdaptedActionVisualFactory visualFactory;

		/**
		 * Instantiate.
		 * 
		 * @param action        {@link ModelAction}.
		 * @param visualFactory {@link AdaptedActionVisualFactory}.
		 */
		private ModelToAction(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
			this.action = action;
			this.visualFactory = visualFactory;
		}
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
		private AdaptedActions<R, O, M> actions = null;

		@Override
		protected void init() {
			super.init();

			// Load the adapter actions
			if (this.getParentFactory().modelToActions.size() > 0) {

				// Determine if click only
				boolean isClickOnly = (this.getFactory().getContentPartFactory().getSelectOnly() != null);

				// Load the model actions
				List<AdaptedAction<R, O, M>> actions = new ArrayList<>(this.getParentFactory().modelToActions.size());
				for (ModelToAction<R, O, M> action : this.getParentFactory().modelToActions) {

					// Obtain the action
					ModelAction<R, O, M> modelAction = action.action;
					if (isClickOnly) {
						// Click only, so dummy action
						modelAction = (context) -> {
						};
					}

					// Add the action
					actions.add(new AdaptedAction<>(modelAction, this, action.visualFactory,
							this.getParentFactory().errorHandler));
				}
				this.actions = new AdaptedActions<>(actions);
			}
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