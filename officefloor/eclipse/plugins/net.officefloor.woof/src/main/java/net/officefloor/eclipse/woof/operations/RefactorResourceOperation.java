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
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.woof.editparts.WoofResourceEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;

/**
 * Refactors a {@link WoofResourceModel} to the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorResourceOperation extends AbstractWoofChangeOperation<WoofResourceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorResourceOperation(WoofChanges woofChanges) {
		super("Refactor resource", WoofResourceEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the edit part
		WoofResourceEditPart editPart = context.getEditPart();

		// Obtain the resource to refactor
		WoofResourceModel resource = editPart.getCastedModel();

		// Create the populated Resource
		WoofResourceModel bean = new WoofResourceModel(resource.getResourcePath());
		BeanDialog dialog = context.getEditPart().createBeanDialog(bean, "Woof Resource Name", "X", "Y");
		dialog.addIgnoreType(WoofTemplateOutputToWoofResourceModel.class);
		dialog.addIgnoreType(WoofSectionOutputToWoofResourceModel.class);
		dialog.addIgnoreType(WoofExceptionToWoofResourceModel.class);
		dialog.registerPropertyInput("Resource Path", new ClasspathFileInput(editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created
			return null;
		}

		// Create the change
		Change<WoofResourceModel> change = changes.refactorResource(resource, bean.getResourcePath());

		// Return the change
		return change;
	}

}