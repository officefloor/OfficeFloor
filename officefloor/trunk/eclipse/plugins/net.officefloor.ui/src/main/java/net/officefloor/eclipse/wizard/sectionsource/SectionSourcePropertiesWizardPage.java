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
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link SectionSourceWizard}.
	 */
	private final SectionSourceWizard sectionSourceWizard;

	/**
	 * {@link SectionSourceInstance} instance.
	 */
	private final SectionSourceInstance sectionSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param sectionSourceWizard
	 *            Owning {@link SectionSourceWizard}.
	 * @param sectionSourceInstance
	 *            {@link SectionSourceInstance} instances.
	 */
	SectionSourcePropertiesWizardPage(SectionSourceWizard sectionSourceWizard,
			SectionSourceInstance sectionSourceInstance) {
		super("SectionSource properties");
		this.sectionSourceWizard = sectionSourceWizard;
		this.sectionSourceInstance = sectionSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.sectionSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.sectionSourceInstance.notifyPropertiesChanged();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the section loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.sectionSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}