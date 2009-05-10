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
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.work.TaskType;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} to select the {@link TaskType} instances of the
 * {@link SectionType} to include.
 * 
 * @author Daniel
 */
public class SectionSourceNameWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link OfficeSection} name.
	 */
	private Text sectionName;
	
	/**
	 * {@link SectionSourceInstance}.
	 */
	private SectionSourceInstance sectionSourceInstance;

	/**
	 * Initiate.
	 */
	protected SectionSourceNameWizardPage() {
		super("SectionSource name");

		// Specify page details
		this.setTitle("Name section");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link SectionSourceInstance}.
	 * 
	 * @param sectionSourceInstance
	 *            {@link SectionSourceInstance}.
	 */
	public void loadSectionSourceInstance(
			SectionSourceInstance sectionSourceInstance) {

		// Do nothing if same section source
		if (this.sectionSourceInstance == sectionSourceInstance) {
			return;
		}

		// Specify section source instance and obtain section type (may be null)
		this.sectionSourceInstance = sectionSourceInstance;
		String suggestedSectionName = (this.sectionSourceInstance != null ? this.sectionSourceInstance
				.getSuggestedSectionName()
				: "");

		// Specify the suggested section name
		this.sectionName.setText(suggestedSectionName);

		// Initiate state
		this.handlePageChange();
	}

	/**
	 * Obtains the name of the {@link OfficeSection}.
	 * 
	 * @return Name of the {@link OfficeSection}.
	 */
	public String getSectionName() {
		return this.sectionName.getText();
	}

	@Override
	public void createControl(Composite parent) {

		// Create the page for the section source
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Provide control to specify name
		Composite name = new Composite(page, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.setLayout(new GridLayout(2, false));
		new Label(name, SWT.None).setText("Section name: ");
		this.sectionName = new Text(name, SWT.SINGLE | SWT.BORDER);
		this.sectionName.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		this.sectionName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				SectionSourceNameWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no section source instance)
		this.loadSectionSourceInstance(null);
		this.handlePageChange();

		// Specify control
		this.setControl(page);
	}

	/**
	 * Handles changes to the page.
	 */
	private void handlePageChange() {

		// Ensure section has a name
		String sectionName = this.sectionName.getText();
		if ((sectionName == null) || (sectionName.trim().length() == 0)) {
			this.setErrorMessage("Must provide section name");
			this.setPageComplete(false);
			return;
		}

		// Make complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}
}