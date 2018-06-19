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
package net.officefloor.eclipse.wizard.administrationsource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link AdministrationSourceWizard}.
	 */
	private final AdministrationSourceWizard administrationSourceWizard;

	/**
	 * {@link AdministrationSourceInstance} instance.
	 */
	private final AdministrationSourceInstance administrationSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param administrationSourceWizard
	 *            Owning {@link AdministrationSourceWizard}.
	 * @param administrationSourceInstance
	 *            {@link AdministrationSourceInstance} instances.
	 */
	AdministrationSourcePropertiesWizardPage(AdministrationSourceWizard administrationSourceWizard,
			AdministrationSourceInstance administrationSourceInstance) {
		super("AdministrationSource properties");
		this.administrationSourceWizard = administrationSourceWizard;
		this.administrationSourceInstance = administrationSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.administrationSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.administrationSourceInstance.notifyPropertiesChanged();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the administrator loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.administrationSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}