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
package net.officefloor.eclipse.common.operation;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.ItemModel;
import net.officefloor.model.Model;
import net.officefloor.model.RemoveConnectionsAction;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IAction;

/**
 * Abstract {@link Operation} to remove a {@link Model}.
 * 
 * @author Daniel
 */
public abstract class AbstractRemoveItemModelOperation<E extends AbstractOfficeFloorEditPart<R, ?>, R extends ItemModel<R>, P extends Model>
		extends AbstractOperation<E> {

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link IAction} text.
	 * @param editPartType
	 *            {@link EditPart} type.
	 */
	public AbstractRemoveItemModelOperation(String actionText, Class<E> editPartType) {
		super(actionText, editPartType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void perform(Context context) {

		// Obtain the parent model
		final P parent = (P) context.getEditPart().getParent().getModel();

		// Obtain the model to remove
		final R model = context.getEditPart().getCastedModel();

		// Make the change
		context.execute(new OfficeFloorCommand() {

			private RemoveConnectionsAction<R> connections;

			@Override
			protected void doCommand() {
				this.connections = model.removeConnections();
				AbstractRemoveItemModelOperation.this.remove(model, parent);
			}

			@Override
			protected void undoCommand() {
				AbstractRemoveItemModelOperation.this.unremove(model, parent);
				if (this.connections != null) {
					this.connections.reconnect();
				}
			}
		});
	}

	/**
	 * Removes the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model} to remove.
	 * @param parent
	 *            Parent to remove the {@link Model} from.
	 */
	protected abstract void remove(R model, P parent);

	/**
	 * Re-adds the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model} to re-add.
	 * @param parent
	 *            Parent to add the {@link Model}.
	 */
	protected abstract void unremove(R model, P parent);
}
