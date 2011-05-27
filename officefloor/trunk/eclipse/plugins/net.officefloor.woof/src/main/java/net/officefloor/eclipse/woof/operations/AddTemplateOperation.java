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

import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.wizard.template.HttpTemplateInstance;
import net.officefloor.eclipse.wizard.template.HttpTemplateWizard;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.gwt.module.GwtChangesImpl;
import net.officefloor.plugin.gwt.module.GwtFailureListener;
import net.officefloor.plugin.gwt.module.GwtModuleRepositoryImpl;

/**
 * {@link Operation} to add a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTemplateOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * {@link AbstractOfficeFloorEditor}.
	 */
	private final AbstractOfficeFloorEditor<?, ?> editor;

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public AddTemplateOperation(WoofChanges woofChanges,
			AbstractOfficeFloorEditor<?, ?> editor) {
		super("Add template", WoofEditPart.class, woofChanges);
		this.editor = editor;
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
		String path = instance.getTemplatePath();
		String logicClassName = instance.getLogicClassName();
		SectionType type = instance.getTemplateSectionType();
		String uri = instance.getUri();

		// Create change to add template
		Change<WoofTemplateModel> change = changes.addTemplate(path,
				logicClassName, type, uri);

		// Position template
		context.positionModel(change.getTarget());

		// Obtain the GWT details
		String entryPointClassName = instance.getGwtEntryPointClassName();
		if (!(EclipseUtil.isBlank(entryPointClassName))) {

			// Create change to update GWT Module configuration
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			ConfigurationContext configurationContext = new ProjectConfigurationContext(
					this.editor.getEditorInput());
			GwtFailureListener listener = new GwtFailureListener() {
				@Override
				public void notifyFailure(String message, Throwable cause) {
					// Provide error message of GWT failure
					AddTemplateOperation.this.editor.messageError(message,
							cause);
				}
			};
			GwtChanges gwtChanges = new GwtChangesImpl(
					new GwtModuleRepositoryImpl(new ModelRepositoryImpl(),
							classLoader, "src/main/resources"),
					configurationContext, listener);

			// Create change to add the GWT Module
			GwtModuleModel gwtModule = new GwtModuleModel(uri,
					entryPointClassName);
			Change<GwtModuleModel> gwtChange = gwtChanges.updateGwtModule(
					gwtModule, null);

			// Aggregate the changes
			change = new AggregateChange<WoofTemplateModel>(change.getTarget(),
					change.getChangeDescription(), change, gwtChange);
		}

		// Return the change to add the template
		return change;
	}

}