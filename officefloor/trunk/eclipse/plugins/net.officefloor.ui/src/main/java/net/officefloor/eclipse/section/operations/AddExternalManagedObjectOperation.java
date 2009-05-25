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
package net.officefloor.eclipse.section.operations;

import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Adds an {@link ExternalManagedObjectModel} to an {@link SectionModel}.
 * 
 * @author Daniel
 */
public class AddExternalManagedObjectOperation extends
		AbstractSectionChangeOperation<SectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public AddExternalManagedObjectOperation(SectionChanges sectionChanges) {
		super("Add external managed object", SectionEditPart.class,
				sectionChanges);
	}

	/*
	 * ==================== AbstractSectionChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the section edit part
		final SectionEditPart editPart = context.getEditPart();

		// Create the populated External Managed Object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel();
		BeanDialog dialog = editPart.createBeanDialog(mo, "X", "Y");
		dialog.registerPropertyInput("Object Type",
				new ClasspathSelectionInput(editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the external managed object
		Change<ExternalManagedObjectModel> change = changes
				.addExternalManagedObject(mo.getExternalManagedObjectName(), mo
						.getObjectType());

		// Position the model
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}