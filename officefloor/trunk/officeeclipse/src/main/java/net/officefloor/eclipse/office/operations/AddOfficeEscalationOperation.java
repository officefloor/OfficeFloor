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

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;

/**
 * {@link Operation} to add an {@link OfficeEscalationModel}.
 * 
 * @author Daniel
 */
public class AddOfficeEscalationOperation extends
		AbstractOfficeChangeOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public AddOfficeEscalationOperation(OfficeChanges officeChanges) {
		super("Add escalation", OfficeEditPart.class, officeChanges);
	}

	/*
	 * ================= AbstractOfficeChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the office edit part
		final OfficeEditPart editPart = context.getEditPart();

		// Create the populated Office Escalation
		final OfficeEscalationModel escalation = new OfficeEscalationModel();
		BeanDialog dialog = editPart.createBeanDialog(escalation, "X", "Y");
		ClasspathFilter filter = new ClasspathFilter();
		filter.addJavaClassFilter();
		dialog.registerPropertyInput("Escalation Type",
				new ClasspathSelectionInput(editPart.getEditor(), filter));
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the office escalation
		Change<OfficeEscalationModel> change = changes
				.addOfficeEscalation(escalation.getEscalationType());

		// Position the model
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}