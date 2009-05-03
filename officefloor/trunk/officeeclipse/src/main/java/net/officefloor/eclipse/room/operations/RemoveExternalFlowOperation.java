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
package net.officefloor.eclipse.room.operations;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.operation.AbstractRemoveItemModelOperation;
import net.officefloor.eclipse.room.editparts.ExternalFlowEditPart;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionModel;

/**
 * {@link Operation} to remove the {@link ExternalFlowModel}.
 * 
 * @author Daniel
 */
public class RemoveExternalFlowOperation
		extends
		AbstractRemoveItemModelOperation<ExternalFlowEditPart, ExternalFlowModel, SectionModel> {

	/**
	 * Initiate.
	 */
	public RemoveExternalFlowOperation() {
		super("Remove external flow", ExternalFlowEditPart.class);
	}

	@Override
	protected void remove(ExternalFlowModel model, SectionModel parent) {
		parent.removeExternalFlow(model);
	}

	@Override
	protected void unremove(ExternalFlowModel model, SectionModel parent) {
		parent.addExternalFlow(model);
	}

}