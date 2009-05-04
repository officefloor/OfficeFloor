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
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.ChangeCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;

/**
 * Abstract {@link DeskChanges} {@link Operation}.
 * 
 * @author Daniel
 */
public abstract class AbstractDeskChangeOperation<E extends EditPart> extends
		AbstractOperation<E> {

	/**
	 * {@link DeskChanges}.
	 */
	private final DeskChanges deskChanges;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param editPartType
	 *            {@link AbstractOfficeFloorEditPart} type that this
	 *            {@link Operation} may be carried out on.
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public AbstractDeskChangeOperation(String actionText,
			Class<E> editPartType, DeskChanges deskChanges) {
		super(actionText, editPartType);
		this.deskChanges = deskChanges;
	}

	/*
	 * ===================== AbstractOperation ===============================
	 */

	@Override
	protected void perform(Context context) {

		// Obtain the change
		final Change<?> change = this.getChange(this.deskChanges, context);
		if (change == null) {
			return; // no change to perform
		}

		// Execute the change
		context.execute(new ChangeCommand(change));
	}

	/**
	 * Obtains the {@link Change}.
	 * 
	 * @param changes
	 *            {@link DeskChanges}.
	 * @param context
	 *            {@link Context}.
	 * @return {@link Change}.
	 */
	protected abstract Change<?> getChange(DeskChanges changes, Context context);

}