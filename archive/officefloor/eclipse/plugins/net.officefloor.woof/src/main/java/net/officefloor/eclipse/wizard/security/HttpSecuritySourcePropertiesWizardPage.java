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
package net.officefloor.eclipse.wizard.security;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link IWizardPage} providing the {@link PropertyList} for the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link HttpSecuritySourceWizard}.
	 */
	private final HttpSecuritySourceWizard httpSecuritySourceWizard;

	/**
	 * {@link HttpSecuritySourceInstance} instance.
	 */
	private final HttpSecuritySourceInstance httpSecuritySourceInstance;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySourceWizard
	 *            Owning {@link HttpSecuritySourceWizard}.
	 * @param httpSecuritySourceInstance
	 *            {@link HttpSecuritySourceInstance} instances.
	 */
	HttpSecuritySourcePropertiesWizardPage(
			HttpSecuritySourceWizard httpSecuritySourceWizard,
			HttpSecuritySourceInstance httpSecuritySourceInstance) {
		super("HttpSecuritySource properties");
		this.httpSecuritySourceWizard = httpSecuritySourceWizard;
		this.httpSecuritySourceInstance = httpSecuritySourceInstance;

		// Specify wizard and initially not complete
		this.setWizard(this.httpSecuritySourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Activates this {@link IWizardPage}.
	 */
	public void activatePage() {
		// Notify properties change to indicate initial state
		this.httpSecuritySourceInstance.notifyPropertiesChanged();
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
		this.httpSecuritySourceInstance.createControls(page);

		// Specify control
		this.setControl(page);
	}

}