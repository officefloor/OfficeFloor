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
package net.officefloor.eclipse.woof.operations;

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.ChangeCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChanges;

/**
 * Abstract {@link WoofChanges} {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofChangeOperation<E extends EditPart> extends AbstractOperation<E> {

	/**
	 * {@link WoofChanges}.
	 */
	private final WoofChanges woofChanges;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            Action text.
	 * @param editPartType
	 *            {@link AbstractOfficeFloorEditPart} type that this
	 *            {@link Operation} may be carried out on.
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AbstractWoofChangeOperation(String actionText, Class<E> editPartType, WoofChanges woofChanges) {
		super(actionText, editPartType);
		this.woofChanges = woofChanges;
	}

	/*
	 * ===================== AbstractOperation ===============================
	 */

	@Override
	protected void perform(Context context) {

		// Obtain the change
		final Change<?> change = this.getChange(this.woofChanges, context);
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
	 *            {@link WoofChanges}.
	 * @param context
	 *            {@link Context}.
	 * @return {@link Change}.
	 */
	protected abstract Change<?> getChange(WoofChanges changes, Context context);

}