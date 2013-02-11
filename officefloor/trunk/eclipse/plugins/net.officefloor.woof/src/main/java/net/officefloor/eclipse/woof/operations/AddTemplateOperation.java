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

import java.util.Map;

import net.officefloor.compile.section.SectionType;
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
		String uri = instance.getUri();
		String path = instance.getTemplatePath();
		String logicClassName = instance.getLogicClassName();
		SectionType type = instance.getTemplateSectionType();
		boolean isTemplateSecure = instance.isTemplateSecure();
		Map<String, Boolean> linksSecure = instance.getLinksSecure();
		String[] renderRedirectHttpMethods = instance
				.getRenderRedirectHttpMethods();
		String entryPointClassName = instance.getGwtEntryPointClassName();
		String[] serviceAsyncInterfaces = instance
				.getGwtServerAsyncInterfaceNames();
		boolean isEnableComet = instance.isEnableComet();
		String cometManualPublishMethodName = instance
				.getCometManualPublishMethodName();

		// TODO provide continue rendering
		boolean isContinueRendering = true;

		// Create change to add template
		Change<WoofTemplateModel> change = changes.addTemplate(uri, path,
				logicClassName, type, isTemplateSecure, linksSecure,
				renderRedirectHttpMethods, isContinueRendering,
				entryPointClassName, serviceAsyncInterfaces, isEnableComet,
				cometManualPublishMethodName);

		// Position template
		context.positionModel(change.getTarget());

		// Return the change to add the template
		return change;
	}

}