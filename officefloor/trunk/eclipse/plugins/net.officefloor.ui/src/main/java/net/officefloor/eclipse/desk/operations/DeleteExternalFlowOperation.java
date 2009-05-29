/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.desk.editparts.ExternalFlowEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.ExternalFlowModel;

/**
 * {@link Operation} to delete an {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteExternalFlowOperation extends
		AbstractDeskChangeOperation<ExternalFlowEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public DeleteExternalFlowOperation(DeskChanges deskChanges) {
		super("Delete External Flow", ExternalFlowEditPart.class, deskChanges);
	}

	/*
	 * ============= AbstractDeskChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the external flow
		ExternalFlowModel externalFlow = context.getEditPart().getCastedModel();

		// Remove the external flow
		return changes.removeExternalFlow(externalFlow);
	}

}