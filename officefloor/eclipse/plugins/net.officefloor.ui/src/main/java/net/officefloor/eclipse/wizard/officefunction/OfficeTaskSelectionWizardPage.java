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
package net.officefloor.eclipse.wizard.officefunction;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.OfficeSectionInput;
import net.officefloor.frame.spi.administration.Duty;

/**
 * {@link IWizardPage} to select the {@link OfficeTaskType} from the
 * {@link OfficeSectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTaskSelectionWizardPage extends WizardPage {

	/**
	 * {@link OfficeSectionType}.
	 */
	private final OfficeSectionType sectionType;

	/**
	 * Radio {@link Button} indicating if pre {@link Duty}.
	 */
	private Button preDuty;

	/**
	 * Selected {@link OfficeTaskType}.
	 */
	private OfficeTaskType selectedOfficeTaskType = null;

	/**
	 * Initiate.
	 * 
	 * @param section
	 *            {@link OfficeSectionType}. May be <code>null</code> if failed
	 *            to load {@link OfficeSectionType}.
	 */
	OfficeTaskSelectionWizardPage(OfficeSectionType sectionType) {
		super("OfficeSection load issues");
		this.sectionType = sectionType;

		// Specify page details
		this.setTitle("Select " + OfficeTaskType.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link OfficeTaskType}.
	 * 
	 * @return Selected {@link OfficeTaskType} or <code>null</code> if no
	 *         {@link OfficeTaskType} is selected.
	 */
	public OfficeTaskType getSelectedOfficeTaskType() {
		return this.selectedOfficeTaskType;
	}

	/**
	 * Indicates if pre {@link Duty} rather than post {@link Duty}.
	 * 
	 * @return <code>true</code> if pre {@link Duty}, <code>false</code> if post
	 *         {@link Duty}.
	 */
	public boolean isPreRatherThanPost() {
		// If not pre duty, then must be post duty
		return this.preDuty.getSelection();
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Determine if have office section type
		if (this.sectionType == null) {
			// No office section so provide error
			Label label = new Label(page, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			label.setForeground(ColorConstants.red);
			label.setText("Failed to load the " + OfficeSection.class.getSimpleName());
			return;
		}

		// Add radio buttons to specify if pre or post duty
		Composite duty = new Composite(page, SWT.NONE);
		duty.setLayout(new RowLayout());
		new Label(duty, SWT.NONE).setText("Administer ");
		this.preDuty = new Button(duty, SWT.RADIO);
		this.preDuty.setText("pre");
		this.preDuty.setSelection(true);
		Button postDuty = new Button(duty, SWT.RADIO);
		postDuty.setText("post");
		new Label(duty, SWT.NONE).setText(" task");

		// Have section so provide ability to select task Type
		new InputHandler<OfficeTaskType>(page, new OfficeSectionInput(this.sectionType, OfficeTaskType.class),
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						if (value instanceof OfficeTaskType) {
							// Office task selected
							OfficeTaskSelectionWizardPage.this.selectedOfficeTaskType = (OfficeTaskType) value;
							OfficeTaskSelectionWizardPage.this.setErrorMessage(null);
							OfficeTaskSelectionWizardPage.this.setPageComplete(true);

						} else {
							// Office task not selected
							OfficeTaskSelectionWizardPage.this.selectedOfficeTaskType = null;
							OfficeTaskSelectionWizardPage.this
									.setErrorMessage("Select an " + OfficeTaskType.class.getSimpleName());
							OfficeTaskSelectionWizardPage.this.setPageComplete(false);
						}
					}
				});

		// Specify the control
		this.setControl(page);

		// Must select task before can complete page
		this.setPageComplete(false);
	}

}