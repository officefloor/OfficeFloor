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
package net.officefloor.eclipse.wizard.officesource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link OfficeSourceWizard}.
	 */
	private final OfficeSourceWizard officeSourceWizard;

	/**
	 * {@link OfficeSourceInstance} instance.
	 */
	private final OfficeSourceInstance officeSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param officeSourceWizard
	 *            Owning {@link OfficeSourceWizard}.
	 * @param officeSourceInstance
	 *            {@link OfficeSourceInstance} instances.
	 */
	OfficeSourcePropertiesWizardPage(OfficeSourceWizard officeSourceWizard,
			OfficeSourceInstance officeSourceInstance) {
		super("OfficeSource properties");
		this.officeSourceWizard = officeSourceWizard;
		this.officeSourceInstance = officeSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.officeSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.officeSourceInstance.notifyPropertiesChanged();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the office loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.officeSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}