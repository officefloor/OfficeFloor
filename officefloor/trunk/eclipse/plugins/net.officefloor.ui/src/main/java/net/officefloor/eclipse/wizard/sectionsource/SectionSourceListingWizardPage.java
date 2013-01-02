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
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} providing the listing of {@link SectionSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceListingWizardPage extends WizardPage implements
		CompilerIssues {

	/**
	 * Choices for selection.
	 */
	private enum Choice {
		CLASS, SELECT
	}

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
	 * {@link SectionInstance} to refactor or <code>null</code> if creating new
	 * {@link SectionInstance}.
	 */
	private final SectionInstance sectionInstance;

	/**
	 * {@link Choice}.
	 */
	private Choice choice;

	/**
	 * {@link Text} of the {@link OfficeSection} name.
	 */
	private Text sectionName;

	/**
	 * List containing the {@link SectionSource} labels.
	 */
	private List list;

	/**
	 * Selected {@link SectionSourceInstance}.
	 */
	private SectionSourceInstance selectedInstance = null;

	/**
	 * Name of the class for the {@link ClassSectionSource}.
	 */
	private String sectionClassName;

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
	 * @param sectionInstance
	 *            {@link SectionInstance} to refactor or <code>null</code> if
	 *            creating new.
	 */
	SectionSourceListingWizardPage(
			SectionSourceInstance[] sectionSourceInstances, IProject project,
			SectionInstance sectionInstance) {
		super("SectionSource listing");
		this.sectionSourceInstances = sectionSourceInstances;
		this.project = project;
		this.sectionInstance = sectionInstance;

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
		return this.selectedInstance;
	}

	/**
	 * Indicates if a {@link ClassSectionSource}.
	 * 
	 * @return <code>true</code> if a {@link ClassSectionSource}.
	 */
	public boolean isClassSectionSource() {
		return (Choice.CLASS.equals(this.choice));
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Obtain the initial values
		String initialSectionName = "";
		int initialSectionSourceIndex = -1;
		String initialSectionLocation = "";
		if (this.sectionInstance != null) {
			// Have section instance, so provide initial name, source, location
			initialSectionName = this.sectionInstance.getSectionName();
			initialSectionLocation = this.sectionInstance.getSectionLocation();
			String sectionSourceClassName = this.sectionInstance
					.getSectionSourceClassName();
			for (int i = 0; i < this.sectionSourceInstances.length; i++) {
				if (sectionSourceClassName
						.equals(this.sectionSourceInstances[i]
								.getSectionSourceClassName())) {
					initialSectionSourceIndex = i;
				}
			}
		}

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
		this.sectionName.setText(initialSectionName);
		this.sectionName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// Add buttons to choose between Class and Selection
		Composite choiceComposite = new Composite(page, SWT.NONE);
		choiceComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		choiceComposite.setLayout(new GridLayout(2, false));
		Button classButton = new Button(choiceComposite, SWT.RADIO);
		classButton.setText("Class");
		Button selectButton = new Button(choiceComposite, SWT.RADIO);
		selectButton.setText("Select");

		// Composite for populate
		Composite populateComposite = new Composite(page, SWT.NONE);
		StackLayout populateLayout = new StackLayout();
		populateComposite.setLayout(populateLayout);
		final Composite selectionComposite = new Composite(populateComposite,
				SWT.NONE);
		selectionComposite.setLayout(new GridLayout(1, true));
		final Composite classComposite = new Composite(populateComposite,
				SWT.NONE);
		classComposite.setLayout(new GridLayout(2, false));

		// Specify the composite visible on button selection
		classButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				selectionComposite.setVisible(false);
				classComposite.setVisible(true);
				SectionSourceListingWizardPage.this.choice = Choice.CLASS;
				SectionSourceListingWizardPage.this.handleChange();
			}
		});
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				selectionComposite.setVisible(true);
				classComposite.setVisible(false);
				SectionSourceListingWizardPage.this.choice = Choice.SELECT;
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// By default class selected
		this.choice = Choice.CLASS;
		classButton.setSelection(true);
		populateLayout.topControl = classComposite;

		// ----------------- Class -----------------------

		// Add listing for selecting the class
		new Label(classComposite, SWT.NONE).setText("Class:");
		this.sectionClassName = initialSectionLocation;
		InputHandler<String> sectionClass = new InputHandler<String>(
				classComposite, new ClasspathClassInput(this.project,
						page.getShell()), this.sectionClassName,
				new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify the class name and indicate changed
						String className = (value == null ? "" : value
								.toString());
						SectionSourceListingWizardPage.this.sectionClassName = className;
						SectionSourceListingWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						SectionSourceListingWizardPage.this
								.setErrorMessage(message);
					}
				});
		sectionClass.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// --------------- Selection ---------------------

		// Add listing of section sources
		this.list = new List(selectionComposite, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.sectionSourceLabels);
		this.list.setSelection(initialSectionSourceIndex);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// Provide means to specify section location
		Composite locationComposite = new Composite(selectionComposite,
				SWT.NONE);
		locationComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		locationComposite.setLayout(new GridLayout(2, false));
		Label locationLabel = new Label(locationComposite, SWT.NONE);
		locationLabel.setText("Location: ");
		this.sectionLocation = initialSectionLocation;
		InputHandler<String> location = new InputHandler<String>(
				locationComposite, new ClasspathFileInput(this.project,
						page.getShell()), initialSectionLocation,
				new InputListener() {
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

		// Indicate initial state
		this.handleChange();

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

		// Reset to no selection
		this.selectedInstance = null;

		// Ensure have section name
		String name = this.sectionName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of section");
			this.setPageComplete(false);
			return;
		}

		// Determine appropriate selection
		String location;
		switch (this.choice) {
		case CLASS:
			// Find the ClassSectionSource
			String classSectionSourceName = ClassSectionSource.class.getName();
			for (int i = 0; i < this.sectionSourceInstances.length; i++) {
				SectionSourceInstance instance = this.sectionSourceInstances[i];
				if (classSectionSourceName.equals(instance
						.getSectionSourceClassName())) {
					// Found the ClassSectionSource
					this.selectedInstance = instance;
				}
			}
			if (this.selectedInstance == null) {
				this.setErrorMessage("Unable to find " + classSectionSourceName);
				this.setPageComplete(false);
				return;
			}

			// Ensure have class name (as location)
			if (EclipseUtil.isBlank(this.sectionClassName)) {
				this.setErrorMessage("Must specify class name");
				this.setPageComplete(false);
				return;
			}
			location = this.sectionClassName;
			break;

		case SELECT:
			// Determine if section source selected
			int selectionIndex = this.list.getSelectionIndex();
			if (selectionIndex >= 0) {
				// Selected SectionSource
				this.selectedInstance = this.sectionSourceInstances[selectionIndex];
			} else {
				// No SelectionSource selected
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
			location = this.sectionLocation;
			break;
		default:
			throw new IllegalStateException("Unknown choice");
		}

		// Specify location for section source and is complete
		this.selectedInstance.setSectionNameAndLocation(name, location);

		// No properties for ClassSectionSource, so validate it now
		if (Choice.CLASS.equals(this.choice)) {
			if (!this.selectedInstance.loadSectionType(this)) {
				this.setPageComplete(false);
				return;
			}
		}

		// No issue
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

	/*
	 * ========================== CompilerIssues =========================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Provide as error message
		this.setErrorMessage(issueDescription);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {
		// Provide as error message
		this.setErrorMessage(issueDescription + " ("
				+ cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ ")");
	}

}