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

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.manage.Office;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} providing the listing of {@link OfficeSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceListingWizardPage extends WizardPage {

	/**
	 * {@link OfficeSourceInstance} listing.
	 */
	private final OfficeSourceInstance[] officeSourceInstances;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link OfficeInstance}.
	 */
	private final OfficeInstance officeInstance;

	/**
	 * Listing of {@link OfficeSource} labels in order of
	 * {@link OfficeSourceInstance} listing.
	 */
	private final String[] officeSourceLabels;

	/**
	 * {@link Text} of the {@link Office} name.
	 */
	private Text officeName;

	/**
	 * List containing the {@link OfficeSource} labels.
	 */
	private List list;

	/**
	 * Location of the {@link Office}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param officeSourceInstances
	 *            Listing of {@link OfficeSourceInstance}.
	 * @param project
	 *            {@link IProject}.
	 * @param officeInstance
	 *            {@link OfficeInstance} to be refactored or <code>null</code>
	 *            if creating new.
	 */
	OfficeSourceListingWizardPage(OfficeSourceInstance[] officeSourceInstances,
			IProject project, OfficeInstance officeInstance) {
		super("OfficeSource listing");
		this.officeSourceInstances = officeSourceInstances;
		this.project = project;
		this.officeInstance = officeInstance;

		// Create the listing of labels
		this.officeSourceLabels = new String[this.officeSourceInstances.length];
		for (int i = 0; i < this.officeSourceLabels.length; i++) {
			this.officeSourceLabels[i] = this.officeSourceInstances[i]
					.getOfficeSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + OfficeSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link OfficeSourceInstance}.
	 * 
	 * @return Selected {@link OfficeSourceInstance} or <code>null</code> if not
	 *         selected.
	 */
	public OfficeSourceInstance getSelectedOfficeSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected office source instance
			return null;
		} else {
			// Return the selected office source instance
			return this.officeSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Obtain the initial values
		String initialOfficeName = "";
		int initialOfficeSourceIndex = -1;
		String initialOfficeLocation = "";
		if (this.officeInstance != null) {
			// Obtain default values from office instance
			initialOfficeName = this.officeInstance.getOfficeName();
			initialOfficeLocation = this.officeInstance.getOfficeLocation();
			String officeSourceClassName = this.officeInstance
					.getOfficeSourceClassName();
			for (int i = 0; i < this.officeSourceInstances.length; i++) {
				if (officeSourceClassName.equals(this.officeSourceInstances[i]
						.getOfficeSourceClassName())) {
					initialOfficeSourceIndex = i;
				}
			}
		}

		// Add means to specify office name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Office name: ");
		this.officeName = new Text(nameComposite, SWT.BORDER);
		this.officeName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		this.officeName.setText(initialOfficeName);
		this.officeName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				OfficeSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of office sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.officeSourceLabels);
		this.list.setSelection(initialOfficeSourceIndex);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OfficeSourceListingWizardPage.this.handleChange();
			}
		});

		// Provide means to specify office location
		Composite locationComposite = new Composite(page, SWT.NONE);
		locationComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		locationComposite.setLayout(new GridLayout(2, false));
		Label locationLabel = new Label(locationComposite, SWT.NONE);
		locationLabel.setText("Location: ");
		this.officeLocation = initialOfficeLocation;
		InputHandler<String> location = new InputHandler<String>(
				locationComposite, new ClasspathFileInput(this.project,
						page.getShell()), initialOfficeLocation,
				new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {

						// Obtain the location
						String location = (value == null ? "" : value
								.toString());

						// Determine if default office name
						String name = OfficeSourceListingWizardPage.this.officeName
								.getText();
						if ((!EclipseUtil.isBlank(location))
								&& ((EclipseUtil.isBlank(name)) || (location
										.startsWith(name)))) {
							// Use simple name of location
							name = location;
							int index = name.lastIndexOf('/');
							if (index >= 0) {
								name = name.substring(index + "/".length());
							}
							index = name.indexOf('.');
							if (index >= 0) {
								name = name.substring(0, index);
							}
							OfficeSourceListingWizardPage.this.officeName
									.setText(name);
						}

						// Specify the location
						OfficeSourceListingWizardPage.this.officeLocation = location;

						// Flag the location changed
						OfficeSourceListingWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						OfficeSourceListingWizardPage.this
								.setErrorMessage(message);
					}
				});
		location.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Indicate initial state
		this.handleChange();

		// Provide error if no office loaders available
		if (this.officeSourceInstances.length == 0) {
			this.setErrorMessage("No OfficeSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have office name
		String name = this.officeName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of office");
			this.setPageComplete(false);
			return;
		}

		// Determine if office source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select OfficeSource");
			this.setPageComplete(false);
			return;
		}

		// Ensure have office location
		if (EclipseUtil.isBlank(this.officeLocation)) {
			this.setErrorMessage("Must specify location of office");
			this.setPageComplete(false);
			return;
		}

		// Specify location for office source and is complete
		this.officeSourceInstances[selectionIndex].setOfficeNameAndLocation(
				name, this.officeLocation);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}