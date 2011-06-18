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
package net.officefloor.eclipse.wizard.template;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.eclipse.woof.WoofExtensionClasspathProvider;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.EditorPart;

/**
 * Wizard to add and manage {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWizard extends Wizard {

	/**
	 * Facade method to obtain the {@link HttpTemplateInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link HttpTemplateWizard}.
	 * @param templateInstance
	 *            {@link HttpTemplateInstance} to based decisions. May be
	 *            <code>null</code> if creating a new
	 *            {@link HttpTemplateInstance}.
	 * @return {@link HttpTemplateInstance} or <code>null</code> if cancelled.
	 */
	public static HttpTemplateInstance getHttpTemplateInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			HttpTemplateInstance templateInstance) {

		// Create and run the wizard
		HttpTemplateWizard wizard = new HttpTemplateWizard(editPart);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the HTTP Template instance
			return wizard.getHttpTemplateInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * {@link HttpTemplateWizardPage}.
	 */
	private final HttpTemplateWizardPage templatePage;

	/**
	 * {@link HttpTemplateInstance}.
	 */
	private HttpTemplateInstance httpTemplateInstance = null;

	/**
	 * Initiate.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 */
	public HttpTemplateWizard(AbstractOfficeFloorEditPart<?, ?, ?> editPart) {

		// Obtain the project
		EditorPart editorPart = editPart.getEditor();
		IProject project = ProjectConfigurationContext.getProject(editorPart
				.getEditorInput());

		// Ensure WoOF class path
		ClasspathUtil
				.attemptAddExtensionClasspathProvidersToOfficeFloorClasspath(
						editPart, null,
						WoofExtensionClasspathProvider.class.getName());

		// Create the HTTP template wizard page
		this.templatePage = new HttpTemplateWizardPage(project);
	}

	/**
	 * Obtains the {@link HttpTemplateInstance}.
	 * 
	 * @return {@link HttpTemplateInstance}.
	 */
	public HttpTemplateInstance getHttpTemplateInstance() {
		return this.httpTemplateInstance;
	}

	/*
	 * ======================= Wizard =========================
	 */

	@Override
	public void addPages() {
		this.addPage(this.templatePage);
	}

	@Override
	public boolean canFinish() {
		// Able to finish if have HTTP Template Instance
		boolean isCanFinish = (this.templatePage.getHttpTemplateInstance() != null);
		return isCanFinish;
	}

	@Override
	public boolean performFinish() {

		// Obtain the HTTP Template Instance
		this.httpTemplateInstance = this.templatePage.getHttpTemplateInstance();

		// Use the HTTP Template Instance
		return true;
	}

}