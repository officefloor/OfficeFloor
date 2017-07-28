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
package net.officefloor.eclipse.wizard.managedfunctionsource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link ManagedFunctionSourceWizard}.
	 */
	private final ManagedFunctionSourceWizard managedFunctionSourceWizard;

	/**
	 * {@link ManagedFunctionSourceInstance} instance.
	 */
	private final ManagedFunctionSourceInstance managedFunctionSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionSourceWizard
	 *            Owning {@link ManagedFunctionSourceWizard}.
	 * @param managedFunctionSourceInstance
	 *            {@link ManagedFunctionSourceInstance} instances.
	 */
	ManagedFunctionSourcePropertiesWizardPage(ManagedFunctionSourceWizard managedFunctionSourceWizard,
			ManagedFunctionSourceInstance managedFunctionSourceInstance) {
		super("ManagedFunctionSource properties");
		this.managedFunctionSourceWizard = managedFunctionSourceWizard;
		this.managedFunctionSourceInstance = managedFunctionSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.managedFunctionSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Loads the {@link FunctionNamespaceType} for the
	 * {@link ManagedFunctionSourceInstance}.
	 */
	public void loadFunctionNamespaceType() {
		this.managedFunctionSourceInstance.loadFunctionNamespaceType();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.managedFunctionSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}