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
package net.officefloor.eclipse.wizard.officefunction;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.model.office.OfficeSectionModel;

/**
 * {@link IWizard} to select an {@link OfficeFunctionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFunctionWizard extends Wizard {

	/**
	 * Convenience method to obtain the {@link OfficeFunctionInstance}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} from which to select the
	 *            {@link OfficeFunctionType}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} requiring the
	 *            {@link OfficeFunctionType}.
	 * @return {@link OfficeFunctionInstance} or <code>null</code> if
	 *         {@link OfficeFunctionType} not selected.
	 */
	public static OfficeFunctionInstance getOfficeFunction(OfficeSectionModel officeSection,
			AbstractOfficeFloorEditor<?, ?> editor) {
		// Create and run the wizard
		OfficeFunctionWizard wizard = new OfficeFunctionWizard(officeSection, editor);
		if (WizardUtil.runWizard(wizard, editor)) {
			// Successful so return the office instance
			return wizard.getOfficeFunctionInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * {@link OfficeSectionType}.
	 */
	private final OfficeSectionType sectionType;

	/**
	 * {@link OfficeSectionLoadIssuesWizardPage}.
	 */
	private final OfficeSectionLoadIssuesWizardPage loadIssuesPage;

	/**
	 * {@link OfficeFunctionSelectionWizardPage}.
	 */
	private final OfficeFunctionSelectionWizardPage selectFunctionPage;

	/**
	 * {@link OfficeFunctionInstance}.
	 */
	private OfficeFunctionInstance officeFunctionInstance;

	/**
	 * Initiate.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to select
	 *            {@link OfficeFunctionType} from.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public OfficeFunctionWizard(OfficeSectionModel officeSection, AbstractOfficeFloorEditor<?, ?> editor) {

		// Obtain the compiler that collects issues
		final List<String> sectionIssues = new LinkedList<String>();
		OfficeFloorCompiler compiler = OfficeFloorPlugin.getDefault().createCompiler(editor);
		CompilerIssues issues = new AbstractCompilerIssues() {
			@Override
			protected void handleDefaultIssue(DefaultCompilerIssue issue) {
				sectionIssues.add(CompileException.toIssueString(issue));
			}
		};
		compiler.setCompilerIssues(issues);

		// Obtain the office section type
		this.sectionType = ModelUtil.getOfficeSectionType(officeSection, compiler, issues, editor);

		// Provide page if issues in loading office section
		this.loadIssuesPage = (sectionIssues.size() == 0 ? null
				: new OfficeSectionLoadIssuesWizardPage(sectionIssues.toArray(new String[0])));

		// Provide page to select the office function
		this.selectFunctionPage = new OfficeFunctionSelectionWizardPage(this.sectionType);
	}

	/**
	 * Obtains the selected {@link OfficeFunctionInstance}.
	 * 
	 * @return {@link OfficeFunctionInstance}.
	 */
	public OfficeFunctionInstance getOfficeFunctionInstance() {
		return this.officeFunctionInstance;
	}

	/*
	 * ================== Wizard =================================
	 */

	@Override
	public void addPages() {
		if (this.loadIssuesPage != null) {
			this.addPage(this.loadIssuesPage);
		}
		this.addPage(this.selectFunctionPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == this.loadIssuesPage) {
			// Issues reviewed and now selecting task
			return this.selectFunctionPage;

		} else {
			// Function selected, nothing further
			return null;
		}
	}

	@Override
	public boolean canFinish() {

		// Ensure function selected
		if (!this.selectFunctionPage.isPageComplete()) {
			return false;
		}

		// Function selected, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the selected function
		OfficeFunctionType functionType = this.selectFunctionPage.getSelectedOfficeFunctionType();
		boolean isPreRatherThanPost = this.selectFunctionPage.isPreRatherThanPost();

		// Load the office function instance
		this.officeFunctionInstance = new OfficeFunctionInstance(functionType, this.sectionType, isPreRatherThanPost);

		// Finished
		return true;
	}

}