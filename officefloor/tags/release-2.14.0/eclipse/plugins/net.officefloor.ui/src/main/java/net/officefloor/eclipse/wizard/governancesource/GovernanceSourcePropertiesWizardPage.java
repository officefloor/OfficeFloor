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
package net.officefloor.eclipse.wizard.governancesource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link GovernanceSourceWizard}.
	 */
	private final GovernanceSourceWizard governanceSourceWizard;

	/**
	 * {@link GovernanceSourceInstance} instance.
	 */
	private final GovernanceSourceInstance governanceSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param governanceSourceWizard
	 *            Owning {@link GovernanceSourceWizard}.
	 * @param governanceSourceInstance
	 *            {@link GovernanceSourceInstance} instances.
	 */
	GovernanceSourcePropertiesWizardPage(GovernanceSourceWizard governanceSourceWizard,
			GovernanceSourceInstance governanceSourceInstance) {
		super("GovernanceSource properties");
		this.governanceSourceWizard = governanceSourceWizard;
		this.governanceSourceInstance = governanceSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.governanceSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.governanceSourceInstance.notifyPropertiesChanged();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the governance loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.governanceSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}