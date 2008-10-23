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
package net.officefloor.eclipse.office.operations;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectEditPart;
import net.officefloor.model.office.ExternalManagedObjectModel;

/**
 * Manages the scope of the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class CycleManagedObjectScopeOperation extends
		AbstractOperation<ExternalManagedObjectEditPart> {

	/**
	 * Initiate.
	 */
	public CycleManagedObjectScopeOperation() {
		super("Cycle managed object scope", ExternalManagedObjectEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the edit part
		final ExternalManagedObjectEditPart editPart = context.getEditPart();

		// Obtain the initial and next scope
		final String initialScope = editPart.getCastedModel().getScope();
		final String nextScope = editPart.getNextScope(initialScope);

		// Make changes
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().setScope(nextScope);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().setScope(initialScope);
			}
		});
	}
}
