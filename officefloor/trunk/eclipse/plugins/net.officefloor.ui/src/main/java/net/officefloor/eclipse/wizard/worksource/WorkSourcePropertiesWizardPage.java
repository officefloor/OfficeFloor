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
package net.officefloor.eclipse.wizard.worksource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkType;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link WorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link WorkSourceWizard}.
	 */
	private final WorkSourceWizard workSourceWizard;

	/**
	 * {@link WorkSourceInstance} instance.
	 */
	private final WorkSourceInstance workSourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param workSourceWizard
	 *            Owning {@link WorkSourceWizard}.
	 * @param workSourceInstance
	 *            {@link WorkSourceInstance} instances.
	 */
	WorkSourcePropertiesWizardPage(WorkSourceWizard workSourceWizard,
			WorkSourceInstance workSourceInstance) {
		super("WorkSource properties");
		this.workSourceWizard = workSourceWizard;
		this.workSourceInstance = workSourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.workSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Loads the {@link WorkType} for the {@link WorkSourceInstance}.
	 */
	public void loadWorkType() {
		this.workSourceInstance.loadWorkType();
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
		this.workSourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}