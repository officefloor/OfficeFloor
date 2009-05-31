/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.util.EclipseUtil;

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
 * {@link IWizardPage} providing the listing of {@link SectionSourceInstance}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionSourceListingWizardPage extends WizardPage {

	/**
	 * {@link SectionSourceInstance} listing.
	 */
	private final SectionSourceInstance[] sectionSourceInstances;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * Listing of {@link SectionSource} labels in order of
	 * {@link SectionSourceInstance} listing.
	 */
	private final String[] sectionSourceLabels;

	/**
	 * {@link Text} of the {@link OfficeSection} name.
	 */
	private Text sectionName;

	/**
	 * List containing the {@link SectionSource} labels.
	 */
	private List list;

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private String sectionLocation;

	/**
	 * Initiate.
	 *
	 * @param sectionSourceInstances
	 *            Listing of {@link SectionSourceInstance}.
	 * @param project
	 *            {@link IProject}.
	 */
	SectionSourceListingWizardPage(
			SectionSourceInstance[] sectionSourceInstances, IProject project) {
		super("SectionSource listing");
		this.sectionSourceInstances = sectionSourceInstances;
		this.project = project;

		// Create the listing of labels
		this.sectionSourceLabels = new String[this.sectionSourceInstances.length];
		for (int i = 0; i < this.sectionSourceLabels.length; i++) {
			this.sectionSourceLabels[i] = this.sectionSourceInstances[i]
					.getSectionSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + SectionSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link SectionSourceInstance}.
	 *
	 * @return Selected {@link SectionSourceInstance} or <code>null</code> if
	 *         not selected.
	 */
	public SectionSourceInstance getSelectedSectionSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected section source instance
			return null;
		} else {
			// Return the selected section source instance
			return this.sectionSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add means to specify section name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Section name: ");
		this.sectionName = new Text(nameComposite, SWT.BORDER);
		this.sectionName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		this.sectionName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of section sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.sectionSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// Provide means to specify section location
		Composite locationComposite = new Composite(page, SWT.NONE);
		locationComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		locationComposite.setLayout(new GridLayout(2, false));
		Label locationLabel = new Label(locationComposite, SWT.NONE);
		locationLabel.setText("Location: ");
		InputHandler<String> location = new InputHandler<String>(
				locationComposite, new ClasspathFileInput(this.project, page
						.getShell()), new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {

						// Obtain the location
						String location = (value == null ? "" : value
								.toString());

						// Determine if default section name
						String name = SectionSourceListingWizardPage.this.sectionName
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
							SectionSourceListingWizardPage.this.sectionName
									.setText(name);
						}

						// Specify the location
						SectionSourceListingWizardPage.this.sectionLocation = location;

						// Flag the location changed
						SectionSourceListingWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						SectionSourceListingWizardPage.this
								.setErrorMessage(message);
					}
				});
		location.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Initially page not complete (as must select section source)
		this.setPageComplete(false);

		// Provide error if no section loaders available
		if (this.sectionSourceInstances.length == 0) {
			this.setErrorMessage("No SectionSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have section name
		String name = this.sectionName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of section");
			this.setPageComplete(false);
			return;
		}

		// Determine if section source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select SectionSource");
			this.setPageComplete(false);
			return;
		}

		// Ensure have section location
		if (EclipseUtil.isBlank(this.sectionLocation)) {
			this.setErrorMessage("Must specify location of section");
			this.setPageComplete(false);
			return;
		}

		// Specify location for section source and is complete
		this.sectionSourceInstances[selectionIndex].setSectionNameAndLocation(
				name, this.sectionLocation);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}