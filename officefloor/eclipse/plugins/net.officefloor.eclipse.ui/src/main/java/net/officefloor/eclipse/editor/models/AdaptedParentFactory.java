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
package net.officefloor.eclipse.editor.models;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ParentModelProvider;
import net.officefloor.eclipse.editor.ParentModelProviderContext;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

/**
 * Factory for an {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedParentFactory<R extends Model, M extends Model, E extends Enum<E>>
		extends AdaptedChildFactory<M, E, AdaptedParent<M>> implements AdaptedParentBuilder<R, M, E> {

	/**
	 * Prototype {@link Model}.
	 */
	private M prototype = null;

	/**
	 * {@link ParentModelProvider}.
	 */
	private ParentModelProvider<R> parentModelProvider = null;

	/**
	 * Instantiate.
	 * 
	 * @param modelClass
	 *            {@link Model} {@link Class}.
	 * @param contentFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedParentFactory(Class<M> modelClass, ViewFactory<M, AdaptedParent<M>> viewFactory,
			OfficeFloorContentPartFactory<?> contentFactory) {
		super(modelClass, () -> new AdaptedParentImpl<>(), viewFactory, contentFactory);
	}

	/**
	 * Obtains the palette prototype {@link Model}.
	 * 
	 * @return Palette prototype {@link Model}.
	 */
	public M getPalettePrototype() {
		return this.prototype;
	}

	/*
	 * ================ AdaptedParentBuilder ==================
	 */

	@Override
	public void create(M prototype, ParentModelProvider<R> parentModelProvider) {
		this.prototype = prototype;
		this.parentModelProvider = parentModelProvider;
	}

	/**
	 * {@link AdaptedParent} implementation.
	 */
	public static class AdaptedParentImpl<R extends Model, M extends Model, E extends Enum<E>>
			extends AdaptedChildImpl<M, E, AdaptedParent<M>>
			implements AdaptedParent<M>, AdaptedPrototype<M>, RootModelAware<R> {

		/**
		 * Root {@link Model}.
		 */
		private R rootModel;

		/**
		 * Obtains the {@link AdaptedParentFactory}.
		 * 
		 * @return {@link AdaptedParentFactory}.
		 */
		@SuppressWarnings("unchecked")
		private AdaptedParentFactory<R, M, E> getParentFactory() {
			return (AdaptedParentFactory<R, M, E>) this.getFactory();
		}

		/*
		 * ================= RootModelAware ===================
		 */

		@Override
		public void setRootModel(R rootModel) {
			this.rootModel = rootModel;
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
			this.getParentFactory().parentModelProvider.provideNewParentModel(new ParentModelProviderContext<R>() {

				@Override
				public R getRootModel() {
					return AdaptedParentImpl.this.rootModel;
				}

				@Override
				public ChangeExecutor getChangeExecutor() {
					return AdaptedParentImpl.this.getChangeExecutor();
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
				public <m extends Model> m position(m model) {
					model.setX(this.getX());
					model.setY(this.getY());
					return model;
				}
			});
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