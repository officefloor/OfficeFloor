/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.action;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;

/**
 * Abstract {@link Operation}.
 * 
 * @author Daniel
 */
public abstract class AbstractOperation<E extends AbstractOfficeFloorEditPart<?, ?>>
		implements Operation {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link AbstractOfficeFloorEditPart} type. Length always one.
	 */
	private final Class<E>[] editPartTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param editPartType
	 *            {@link AbstractOfficeFloorEditPart} type being handled.
	 */
	@SuppressWarnings("unchecked")
	public AbstractOperation(String actionText, Class<E> editPartType) {
		this.actionText = actionText;
		this.editPartTypes = new Class[] { editPartType };
	}

	/*
	 * ======================= Operation ============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.CommandFactory#getActionText()
	 */
	@Override
	public String getActionText() {
		return this.actionText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.Operation#getEditPartTypes()
	 */
	@Override
	public Class<E>[] getEditPartTypes() {
		return this.editPartTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.Operation#perform(net.officefloor
	 * .eclipse.common.action.OperationContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void perform(OperationContext context) {
		// Run for each edit part
		for (AbstractOfficeFloorEditPart<?, ?> editPart : context
				.getEditParts()) {
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
