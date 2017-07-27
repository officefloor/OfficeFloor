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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.eclipse.wizard.officesource.OfficeSourceInstance;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * {@link IWizardPage} providing the listing of issues in loading the
 * {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionLoadIssuesWizardPage extends WizardPage {

	/**
	 * Listing of issues.
	 */
	private final String[] issues;

	/**
	 * Initiate.
	 * 
	 * @param officeSourceInstances
	 *            Listing of {@link OfficeSourceInstance}.
	 * @param project
	 *            {@link IProject}.
	 */
	OfficeSectionLoadIssuesWizardPage(String[] issues) {
		super("OfficeSection load issues");
		this.issues = issues;

		// Specify page details
		this.setTitle("Issues in loading "
				+ OfficeSection.class.getSimpleName());
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add listing of issues loading office section
		List list = new List(page, SWT.SINGLE | SWT.BORDER);
		list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		list.setForeground(ColorConstants.red);
		list.setItems(this.issues);

		// Specify the control
		this.setControl(page);

		// Page always complete as only display
		this.setPageComplete(true);
	}

}