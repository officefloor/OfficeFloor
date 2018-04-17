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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import com.google.inject.Injector;

import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.ParentModelProvider;
import net.officefloor.eclipse.editor.ParentModelProviderContext;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

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
	 * {@link ParentModelProvider}.
	 */
	private ParentModelProvider<R, O, M> parentModelProvider = null;

	/**
	 * {@link ModelToAction} instances.
	 */
	private final List<ModelToAction<R, O, M>> modelToActions = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param modelPrototype
	 *            {@link Model} prototype.
	 * @param viewFactory
	 *            {@link AdaptedModelVisualFactory}.
	 * @param contentFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 * @param errorHandler
	 *            {@link AdaptedErrorHandler}.
	 */
	public AdaptedParentFactory(M modelPrototype, AdaptedModelVisualFactory<M, AdaptedParent<M>> viewFactory,
			OfficeFloorContentPartFactory<R, O> contentFactory, AdaptedErrorHandler errorHandler) {
		super(modelPrototype, () -> new AdaptedParentImpl<>(), viewFactory, contentFactory);
		this.errorHandler = errorHandler;
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
	 * @param factory
	 *            {@link OfficeFloorContentPartFactory}.
	 * @return {@link AdaptedModel} for the prototype.
	 */
	public AdaptedModel<M> createPrototype(OfficeFloorContentPartFactory<R, O> factory) {
		return factory.createAdaptedModel(this.modelPrototype);
	}

	/*
	 * ================ AdaptedParentBuilder ==================
	 */

	@Override
	public void create(ParentModelProvider<R, O, M> parentModelProvider) {
		this.parentModelProvider = parentModelProvider;
	}

	@Override
	public void action(ModelAction<R, O, M, AdaptedParent<M>> action, AdaptedActionVisualFactory visualFactory) {
		this.modelToActions.add(new ModelToAction<>(action, visualFactory));
	}

	/**
	 * {@link Model} to {@link ModelAction}.
	 */
	private static class ModelToAction<R extends Model, O, M extends Model> {

		/**
		 * {@link ModelAction}.
		 */
		private final ModelAction<R, O, M, AdaptedParent<M>> action;

		/**
		 * {@link AdaptedActionVisualFactory}.
		 */
		private final AdaptedActionVisualFactory visualFactory;

		/**
		 * Instantiate.
		 * 
		 * @param action
		 *            {@link ModelAction}.
		 * @param visualFactory
		 *            {@link AdaptedActionVisualFactory}.
		 */
		private ModelToAction(ModelAction<R, O, M, AdaptedParent<M>> action, AdaptedActionVisualFactory visualFactory) {
			this.action = action;
			this.visualFactory = visualFactory;
		}
	}

	/**
	 * {@link AdaptedParent} implementation.
	 */
	public static class AdaptedParentImpl<R extends Model, O, M extends Model, E extends Enum<E>>
			extends AdaptedChildImpl<R, O, M, E, AdaptedParent<M>>
			implements AdaptedParent<M>, AdaptedPrototype<M>, ModelActionContext<R, O, M, AdaptedParent<M>> {

		/**
		 * {@link AdaptedActions}.
		 */
		private AdaptedActions<R, O, M> actions = null;

		@Override
		protected void init() {
			super.init();

			// Load the adapter actions
			if (this.getParentFactory().modelToActions.size() > 0) {

				// Load the model actions
				List<AdaptedAction<R, O, M>> actions = new ArrayList<>(this.getParentFactory().modelToActions.size());
				for (ModelToAction<R, O, M> action : this.getParentFactory().modelToActions) {
					actions.add(new AdaptedAction<>(action.action, this, action.visualFactory,
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
			this.getParentFactory().errorHandler.isError(() -> this.getParentFactory().parentModelProvider
					.provideNewParentModel(new ParentModelProviderContext<R, O, M>() {

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
						public AdaptedParent<M> getAdaptedModel() {
							return AdaptedParentImpl.this;
						}

						@Override
						public Injector getInjector() {
							return AdaptedParentImpl.this.getInjector();
						}

						@Override
						public void overlay(OverlayVisualFactory overlayVisualFactory) {
							AdaptedParentImpl.this.overlay(overlayVisualFactory);
						}

						@Override
						public void execute(Change<?> change) {
							AdaptedParentImpl.this.getChangeExecutor().execute(change);
						}

						@Override
						public int getX() {
							return (int) location.x;
						}

						@Override
						public int getY() {
							return (int) location.y;
						}

						@Override
						public M position(M model) {
							model.setX(this.getX());
							model.setY(this.getY());
							return model;
						}
					}));
		}

		/*
		 * ================= ModelActionContext ================
		 */

		@Override
		public AdaptedParent<M> getAdaptedModel() {
			return this;
		}

		@Override
		public void overlay(OverlayVisualFactory overlayVisualFactory) {

			// TODO implement unsupported
			throw new UnsupportedOperationException("TODO implement overlay");
		}

		@Override
		public void execute(Change<?> change) {
			this.getChangeExecutor().execute(change);
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