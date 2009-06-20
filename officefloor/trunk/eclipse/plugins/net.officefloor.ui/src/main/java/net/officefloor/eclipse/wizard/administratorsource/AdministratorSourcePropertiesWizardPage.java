/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.wizard.administratorsource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link AdministratorSourceWizard}.
	 */
	private final AdministratorSourceWizard administratorSourceWizard;

	/**
	 * {@link AdministratorSourceInstance} instance.
	 */
	private final AdministratorSourceInstance administratorSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param administratorSourceWizard
	 *            Owning {@link AdministratorSourceWizard}.
	 * @param administratorSourceInstance
	 *            {@link AdministratorSourceInstance} instances.
	 */
	AdministratorSourcePropertiesWizardPage(
			AdministratorSourceWizard administratorSourceWizard,
			AdministratorSourceInstance administratorSourceInstance) {
		super("AdministratorSource properties");
		this.administratorSourceWizard = administratorSourceWizard;
		this.administratorSourceInstance = administratorSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.administratorSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.administratorSourceInstance.notifyPropertiesChanged();
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
		this.administratorSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}