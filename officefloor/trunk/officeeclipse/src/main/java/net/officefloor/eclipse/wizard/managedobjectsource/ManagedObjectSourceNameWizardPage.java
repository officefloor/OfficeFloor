/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.managedobjectsource;

import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} to specify the name of the
 * {@link ManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceNameWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link ManagedObjectSourceModel} name.
	 */
	private Text managedObjectSourceName;

	/**
	 * {@link Composite} containing the {@link Control} instances to specify the
	 * default timeout.
	 */
	private Composite defaultTimeoutContainer;

	/**
	 * Display to obtain the default timeout.
	 */
	private Text defaultTimeout;

	/**
	 * Flag indicating if the default timeout is required.
	 */
	private boolean isRequireDefaultTimeout = false;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	private ManagedObjectSourceModel managedObjectSourceModel = null;

	/**
	 * Initiate.
	 */
	protected ManagedObjectSourceNameWizardPage() {
		super("ManagedObjectSource tasks");

		// Specify page details
		this.setTitle("Specify name");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link ManagedObjectSourceModel}.
	 * 
	 * @param initiatedManagedObjectSource
	 *            {@link ManagedObjectSource} that has been initiated so the
	 *            {@link ManagedObjectSourceMetaData} may be obtained from it.
	 * @param managedObjectSourceModel
	 *            {@link ManagedObjectSourceModel}.
	 * @param suggestedManagedObjectSourceName
	 *            Suggested managed object source name.
	 */
	public void loadManagedObjectSourceModel(
			ManagedObjectSource<?, ?> initiatedManagedObjectSource,
			ManagedObjectSourceModel managedObjectSourceModel,
			String suggestedManagedObjectSourceName) {

		// Do nothing if same managed object source model
		if (this.managedObjectSourceModel == managedObjectSourceModel) {
			return;
		}

		// Specify the managed object source model
		this.managedObjectSourceModel = managedObjectSourceModel;

		// Specify the suggested managed object source name
		this.managedObjectSourceName.setText(suggestedManagedObjectSourceName);

		// Specify whether the default timeout needs to be specified
		this.isRequireDefaultTimeout = false;
		if (initiatedManagedObjectSource != null) {
			try {
				Class<?> managedObjectClass = initiatedManagedObjectSource
						.getMetaData().getManagedObjectClass();
				if (AsynchronousManagedObject.class
						.isAssignableFrom(managedObjectClass)) {
					// Asynchronous so requires default timeout
					this.isRequireDefaultTimeout = true;
				}
			} catch (Throwable ex) {
				// Ignore
			}
		}
		this.defaultTimeoutContainer.setVisible(this.isRequireDefaultTimeout);

		// Initiate state
		this.handlePageChange();
	}

	/**
	 * Obtains the name of the {@link ManagedObjectSourceModel}.
	 * 
	 * @return Name of the {@link ManagedObjectSourceModel}.
	 */
	public String getManagedObjectSourceName() {
		return this.managedObjectSourceName.getText();
	}

	/**
	 * Obtains the default timeout for the {@link ManagedObjectSourceModel}.
	 * 
	 * @return Default timeout for the {@link ManagedObjectSourceModel}.
	 */
	public long getDefaultTimeoutValue() {

		// If not required, provide no timeout
		if (!this.isRequireDefaultTimeout) {
			return 0;
		}

		// Return the default timeout value specified
		return Long.parseLong(this.defaultTimeout.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Provide control to specify name
		Composite name = new Composite(page, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.setLayout(new GridLayout(2, false));
		new Label(name, SWT.NONE).setText("Work name: ");
		this.managedObjectSourceName = new Text(name, SWT.SINGLE | SWT.BORDER);
		this.managedObjectSourceName.setLayoutData(new GridData(SWT.FILL,
				SWT.NONE, true, false));
		this.managedObjectSourceName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ManagedObjectSourceNameWizardPage.this.handlePageChange();
			}
		});

		// Provide control to specify default timeout
		this.defaultTimeoutContainer = new Composite(page, SWT.NONE);
		this.defaultTimeoutContainer.setLayoutData(new GridData(SWT.FILL,
				SWT.NONE, true, false));
		this.defaultTimeoutContainer.setLayout(new GridLayout(2, false));
		new Label(this.defaultTimeoutContainer, SWT.NONE)
				.setText("Default timeout: ");
		this.defaultTimeout = new Text(this.defaultTimeoutContainer, SWT.SINGLE
				| SWT.BORDER);
		this.defaultTimeout.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, false));
		this.defaultTimeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ManagedObjectSourceNameWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no managed object source)
		this.loadManagedObjectSourceModel(null, null, "");
		this.handlePageChange();

		// Specify control
		this.setControl(page);
	}

	/**
	 * Handles changes to the page.
	 */
	private void handlePageChange() {

		// Ensure managed object source has a name
		String managedObjectSourceName = this.managedObjectSourceName.getText();
		if ((managedObjectSourceName == null)
				|| (managedObjectSourceName.trim().length() == 0)) {
			this.setErrorMessage("Must provide managed object source name");
			this.setPageComplete(false);
			return;
		}

		// Ensure default timeout specified if required
		if (this.isRequireDefaultTimeout) {

			// Ensure have a default timeout value
			String defaultTimeoutValue = this.defaultTimeout.getText();
			if ((defaultTimeoutValue == null)
					|| (defaultTimeoutValue.trim().length() == 0)) {
				this.setErrorMessage("Must provide default timeout value");
				this.setPageComplete(false);
				return;
			}

			// Ensure default timeout an integer
			long value = -1;
			try {
				value = Long.parseLong(defaultTimeoutValue);
			} catch (NumberFormatException ex) {
				this
						.setErrorMessage("Default timeout must be an integer value");
				this.setPageComplete(false);
				return;
			}

			// Ensure value is greater than zero
			if (value <= 0) {
				this.setErrorMessage("Default timeout must be great than zero");
				this.setPageComplete(false);
				return;
			}
		}

		// Make complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}
}
