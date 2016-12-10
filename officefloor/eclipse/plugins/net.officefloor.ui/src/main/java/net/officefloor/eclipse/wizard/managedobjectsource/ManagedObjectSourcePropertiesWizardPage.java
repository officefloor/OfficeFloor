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
package net.officefloor.eclipse.wizard.managedobjectsource;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link ManagedObjectSourceWizard}.
	 */
	private final ManagedObjectSourceWizard managedObjectSourceWizard;

	/**
	 * {@link ManagedObjectSourceInstance} instance.
	 */
	private final ManagedObjectSourceInstance managedObjectSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceWizard
	 *            Owning {@link ManagedObjectSourceWizard}.
	 * @param managedObjectSourceInstance
	 *            {@link ManagedObjectSourceInstance} instances.
	 */
	ManagedObjectSourcePropertiesWizardPage(
			ManagedObjectSourceWizard managedObjectSourceWizard,
			ManagedObjectSourceInstance managedObjectSourceInstance) {
		super("ManagedObjectSource properties");
		this.managedObjectSourceWizard = managedObjectSourceWizard;
		this.managedObjectSourceInstance = managedObjectSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.managedObjectSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Loads the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSourceInstance}.
	 */
	public void loadManagedObjectType() {
		this.managedObjectSourceInstance.loadManagedObjectType();
	}

	/*
	 * ===================== IDialogPage ==================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the managed object source
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		this.managedObjectSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}