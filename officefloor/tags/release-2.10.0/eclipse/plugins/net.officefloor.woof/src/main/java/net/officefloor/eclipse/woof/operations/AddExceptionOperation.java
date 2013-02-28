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

import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofModel;

/**
 * Adds a {@link WoofExceptionModel} to the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddExceptionOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AddExceptionOperation(WoofChanges woofChanges) {
		super("Add exception", WoofEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the edit part
		WoofEditPart editPart = context.getEditPart();

		// Create the populated Exception
		final WoofExceptionModel exception = new WoofExceptionModel();
		BeanDialog dialog = context.getEditPart().createBeanDialog(exception,
				"X", "Y");
		dialog.addIgnoreType(WoofExceptionToWoofTemplateModel.class);
		dialog.addIgnoreType(WoofExceptionToWoofSectionInputModel.class);
		dialog.addIgnoreType(WoofExceptionToWoofResourceModel.class);
		dialog.registerPropertyInput("Class Name", new ClasspathClassInput(
				editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created
			return null;
		}

		// Create the change
		Change<WoofExceptionModel> change = changes.addException(exception
				.getClassName());

		// Position the exception
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}