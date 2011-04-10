/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.template.HttpTemplateInstance;
import net.officefloor.eclipse.wizard.template.HttpTemplateWizard;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofTemplateModel;

/**
 * {@link Operation} to add a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTemplateOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AddTemplateOperation(WoofChanges woofChanges) {
		super("Add template", WoofEditPart.class, woofChanges);
	}

	/*
	 * =================== AbstractWoofChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the template instance
		HttpTemplateInstance instance = HttpTemplateWizard
				.getHttpTemplateInstance(context.getEditPart(), null);
		if (instance == null) {
			return null; // must have template
		}

		// Obtain the template details
		OfficeSection section = instance.getOfficeSection();
		String path = instance.getTemplatePath();
		String logicClassName = instance.getLogicClassName();
		String uri = instance.getUri();

		// Create change to add template
		Change<WoofTemplateModel> change = changes.addTemplate(section, path,
				logicClassName, uri);

		// Position template
		context.positionModel(change.getTarget());

		// Return the change to add the template
		return change;
	}

}