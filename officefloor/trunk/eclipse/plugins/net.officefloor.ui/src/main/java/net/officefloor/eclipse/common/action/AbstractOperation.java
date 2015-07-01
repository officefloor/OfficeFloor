/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.action;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.Model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;

/**
 * Abstract {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOperation<E extends EditPart> implements
		Operation {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link AbstractOfficeFloorEditPart} type.
	 */
	private final Class<E> editPartType;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param editPartType
	 *            {@link AbstractOfficeFloorEditPart} type being handled.
	 */
	public AbstractOperation(String actionText, Class<E> editPartType) {
		this.actionText = actionText;
		this.editPartType = editPartType;
	}

	/**
	 * Returns whether all the {@link EditPart} instances are assignable to the
	 * {@link EditPart} type.
	 * 
	 * @param <T>
	 *            {@link EditPart} type.
	 * @param editPartType
	 *            {@link EditPart} type.
	 * @param editParts
	 *            {@link EditPart} instances.
	 * @return <code>true</code> if all assignable.
	 */
	protected static <T extends EditPart> boolean isAssignable(
			Class<T> editPartType, EditPart[] editParts) {

		// Determine all edit parts are assignable to edit part type
		boolean isAssignable = true;
		for (EditPart editPart : editParts) {
			if (!editPartType.isAssignableFrom(editPart.getClass())) {
				isAssignable = false;
			}
		}

		// Return whether assignable
		return isAssignable;
	}

	/*
	 * ======================= Operation ============================
	 */

	@Override
	public String getActionText() {
		return this.actionText;
	}

	@Override
	public boolean isApplicable(EditPart[] editParts) {
		// Is applicable, if assignable to all edit part types
		return isAssignable(this.editPartType, editParts);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void perform(OperationContext context) {
		// Run for each edit part
		for (EditPart editPart : context.getEditParts()) {
			E officeFloorEditPart = (E) editPart;

			// Perform the operation
			this.perform(new Context(context, officeFloorEditPart));
		}
	}

	/**
	 * Performs the {@link Operation} on the particular
	 * {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param context
	 *            {@link Context}.
	 */
	protected abstract void perform(Context context);

	/**
	 * Context.
	 */
	protected class Context {

		/**
		 * {@link OperationContext}.
		 */
		private final OperationContext context;

		/**
		 * {@link EditPart}.
		 */
		private final E editPart;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link OperationContext}.
		 * @param editPart
		 *            {@link AbstractOfficeFloorEditPart}.
		 */
		public Context(OperationContext context, E editPart) {
			this.context = context;
			this.editPart = editPart;
		}

		/**
		 * Obtains the {@link AbstractOfficeFloorEditPart}.
		 * 
		 * @return {@link AbstractOfficeFloorEditPart}.
		 */
		public E getEditPart() {
			return this.editPart;
		}

		/**
		 * Obtains the location.
		 * 
		 * @return Location.
		 */
		public Point getLocation() {
			return this.context.getLocation();
		}

		/**
		 * Convenience method to load the location onto the {@link Model}.
		 * 
		 * @param model
		 *            {@link Model}.
		 * @see #getLocation()
		 */
		public void positionModel(Model model) {
			Point location = this.getLocation();
			model.setX(location.x);
			model.setY(location.y);
		}

		/**
		 * Executes the {@link OfficeFloorCommand}.
		 * 
		 * @param command
		 *            {@link OfficeFloorCommand}.
		 */
		public void execute(OfficeFloorCommand command) {
			this.context.execute(command);
		}
	}
}