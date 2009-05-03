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
import net.officefloor.eclipse.desk.editparts.WorkTaskObjectEditPart;

/**
 * Toggles whether the {@link DeskTaskObjectModel} is a parameter.
 * 
 * @author Daniel
 */
public class ToggleTaskObjectParameterOperation extends
		AbstractOperation<WorkTaskObjectEditPart> {

	/**
	 * Initiate.
	 */
	public ToggleTaskObjectParameterOperation() {
		super("Toggle as parameter", WorkTaskObjectEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {
//
//		// Obtain the edit part
//		DeskTaskObjectEditPart editPart = context.getEditPart();
//		final DeskTaskObjectModel deskTaskObject = editPart.getCastedModel();
//		final DeskTaskObjectToExternalManagedObjectModel moConnection = deskTaskObject
//				.getManagedObject();
//
//		// Indicates if initially parameter
//		final boolean isInitiallyParameter = deskTaskObject.getIsParameter();
//
//		// Obtain the existing parameter
//		DeskTaskObjectModel parameter = null;
//		DeskTaskEditPart deskTaskEditPart = (DeskTaskEditPart) editPart
//				.getParent();
//		for (DeskTaskObjectModel object : deskTaskEditPart.getCastedModel()
//				.getObjects()) {
//			if (object.getIsParameter()) {
//				parameter = object;
//			}
//		}
//		final DeskTaskObjectModel currentParameter = parameter;
//
//		// Make changes
//		context.execute(new OfficeFloorCommand() {
//
//			@Override
//			protected void doCommand() {
//				if (!isInitiallyParameter) {
//					// Remove managed object connection
//					if (moConnection != null) {
//						moConnection.remove();
//					}
//
//					// Unset current parameter
//					if (currentParameter != null) {
//						currentParameter.setIsParameter(false);
//					}
//				}
//
//				// Toggle as parameter
//				deskTaskObject.setIsParameter(!isInitiallyParameter);
//			}
//
//			@Override
//			protected void undoCommand() {
//				if (!isInitiallyParameter) {
//					// Reinstate managed object connection
//					if (moConnection != null) {
//						moConnection.connect();
//					}
//
//					// Reset current parameter
//					if (currentParameter != null) {
//						currentParameter.setIsParameter(true);
//					}
//				}
//
//				// Toggle as parameter
//				deskTaskObject.setIsParameter(isInitiallyParameter);
//			}
//		});
	}

}
