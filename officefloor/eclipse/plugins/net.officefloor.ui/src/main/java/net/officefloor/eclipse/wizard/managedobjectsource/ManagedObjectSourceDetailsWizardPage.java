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

import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link IWizardPage} to provide details of the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceDetailsWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link ManagedObject} name.
	 */
	private Text managedObjectName;

	/**
	 * {@link Text} containing the default timeout.
	 */
	private Text defaultTimeoutText;

	/**
	 * Default timeout.
	 */
	private long defaultTimeout = -1;

	/**
	 * {@link ManagedObjectSourceInstance}.
	 */
	private ManagedObjectSourceInstance managedObjectSourceInstance;

	/**
	 * Initiate.
	 */
	protected ManagedObjectSourceDetailsWizardPage() {
		super("ManagedObjectSource tasks");

		// Specify page details
		this.setTitle("Select tasks");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link ManagedObjectSourceInstance}.
	 * 
	 * @param managedObjectSourceInstance
	 *            {@link ManagedObjectSourceInstance}.
	 */
	public void loadManagedObjectSourceInstance(ManagedObjectSourceInstance managedObjectSourceInstance) {

		// Do nothing if same managed object source
		if (this.managedObjectSourceInstance == managedObjectSourceInstance) {
			return;
		}

		// Specify managed object source (may be null)
		this.managedObjectSourceInstance = managedObjectSourceInstance;
		String suggestedManagedObjectName = (this.managedObjectSourceInstance != null
				? this.managedObjectSourceInstance.getSuggestedManagedObjectName() : "");

		// Specify the suggested managed object name
		this.managedObjectName.setText(suggestedManagedObjectName);

		// Initiate state
		this.handlePageChange();
	}

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	public String getManagedObjectName() {
		return this.managedObjectName.getText();
	}

	/**
	 * Obtains the default timeout for the {@link ManagedObjectSource}.
	 * 
	 * @return Default timeout for the {@link ManagedObjectSource}.
	 */
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	@Override
	public void createControl(Composite parent) {

		// Create the page for the managed object source
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(2, false));

		// Provide control to specify name
		new Label(page, SWT.None).setText("ManagedObject name: ");
		this.managedObjectName = new Text(page, SWT.SINGLE | SWT.BORDER);
		this.managedObjectName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.managedObjectName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ManagedObjectSourceDetailsWizardPage.this.handlePageChange();
			}
		});

		// Provide control to specify default timeout
		new Label(page, SWT.None).setText("Default timeout: ");
		this.defaultTimeoutText = new Text(page, SWT.SINGLE | SWT.BORDER);
		this.defaultTimeoutText.setText("0");
		this.defaultTimeoutText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.defaultTimeoutText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ManagedObjectSourceDetailsWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no managed object source instance)
		this.loadManagedObjectSourceInstance(null);
		this.handlePageChange();

		// Specify control
		this.setControl(page);
	}

	/**
	 * Handles changes to the page.
	 */
	private void handlePageChange() {

		// Ensure managed object has a name
		String managedObjectName = this.managedObjectName.getText();
		if (EclipseUtil.isBlank(managedObjectName)) {
			this.setErrorMessage("Must provide managed object source name");
			this.setPageComplete(false);
			return;
		}

		// Ensure have numeric default timeout
		String defaultTimeoutValue = this.defaultTimeoutText.getText();
		if (EclipseUtil.isBlank(defaultTimeoutValue)) {
			this.setErrorMessage("Must provide default timeout");
			this.setPageComplete(false);
			return;
		}
		try {
			this.defaultTimeout = Long.parseLong(defaultTimeoutValue);
		} catch (NumberFormatException ex) {
			this.setErrorMessage("Default timeout must be an integer");
			this.setPageComplete(false);
			return;
		}

		// Make complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}