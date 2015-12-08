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
package net.officefloor.eclipse.wizard.officetask;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.model.office.OfficeSectionModel;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to select an {@link OfficeTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTaskWizard extends Wizard {

	/**
	 * Convenience method to obtain the {@link OfficeTaskInstance}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} from which to select the
	 *            {@link OfficeTask}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} requiring the
	 *            {@link OfficeTask}.
	 * @return {@link OfficeTaskInstance} or <code>null</code> if
	 *         {@link OfficeTask} not selected.
	 */
	public static OfficeTaskInstance getOfficeTask(
			OfficeSectionModel officeSection,
			AbstractOfficeFloorEditor<?, ?> editor) {
		// Create and run the wizard
		OfficeTaskWizard wizard = new OfficeTaskWizard(officeSection, editor);
		if (WizardUtil.runWizard(wizard, editor)) {
			// Successful so return the office instance
			return wizard.getOfficeTaskInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection section;

	/**
	 * {@link OfficeSectionLoadIssuesWizardPage}.
	 */
	private final OfficeSectionLoadIssuesWizardPage loadIssuesPage;

	/**
	 * {@link OfficeTaskSelectionWizardPage}.
	 */
	private final OfficeTaskSelectionWizardPage selectTaskPage;

	/**
	 * {@link OfficeTaskInstance}.
	 */
	private OfficeTaskInstance officeTaskInstance;

	/**
	 * Initiate.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to select {@link OfficeTask} from.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public OfficeTaskWizard(OfficeSectionModel officeSection,
			AbstractOfficeFloorEditor<?, ?> editor) {

		// Obtain the compiler that collects issues
		final List<String> sectionIssues = new LinkedList<String>();
		OfficeFloorCompiler compiler = OfficeFloorPlugin.getDefault()
				.createCompiler(editor);
		CompilerIssues issues = new CompilerIssues() {
			@Override
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription) {
				sectionIssues.add(issueDescription);
			}

			@Override
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription, Throwable cause) {
				String failure = "";
				if (cause != null) {
					failure = " " + cause.getMessage() + " ["
							+ cause.getClass().getSimpleName() + "]";
				}
				sectionIssues.add(issueDescription + failure);
			}
		};
		compiler.setCompilerIssues(issues);

		// Obtain the office section
		this.section = ModelUtil.getOfficeSection(officeSection, compiler,
				issues, editor);

		// Provide page if issues in loading office section
		this.loadIssuesPage = (sectionIssues.size() == 0 ? null
				: new OfficeSectionLoadIssuesWizardPage(sectionIssues
						.toArray(new String[0])));

		// Provide page to select the office task
		this.selectTaskPage = new OfficeTaskSelectionWizardPage(this.section);
	}

	/**
	 * Obtains the selected {@link OfficeTaskInstance}.
	 * 
	 * @return {@link OfficeTaskInstance}.
	 */
	public OfficeTaskInstance getOfficeTaskInstance() {
		return this.officeTaskInstance;
	}

	/*
	 * ================== Wizard =================================
	 */

	@Override
	public void addPages() {
		if (this.loadIssuesPage != null) {
			this.addPage(this.loadIssuesPage);
		}
		this.addPage(this.selectTaskPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == this.loadIssuesPage) {
			// Issues reviewed and now selecting task
			return this.selectTaskPage;

		} else {
			// Task selected, nothing further
			return null;
		}
	}

	@Override
	public boolean canFinish() {

		// Ensure task selected
		if (!this.selectTaskPage.isPageComplete()) {
			return false;
		}

		// Task selected, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the selected task
		OfficeTask task = this.selectTaskPage.getSelectedOfficeTask();
		boolean isPreRatherThanPost = this.selectTaskPage.isPreRatherThanPost();

		// Load the office task instance
		this.officeTaskInstance = new OfficeTaskInstance(task, this.section,
				isPreRatherThanPost);

		// Finished
		return true;
	}

}