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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;

/**
 * {@link IWizardPage} to provide refactoring of {@link DeployedOfficeModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSourceAlignDeployedOfficeWizardPage extends WizardPage {

	/**
	 * {@link OfficeInstance}.
	 */
	private final OfficeInstance officeInstance;

	/**
	 * {@link ConformInput} for objects.
	 */
	private final ConformInput objectConform = new ConformInput();

	/**
	 * {@link OfficeInstance} objects.
	 */
	private final String[] officeObjects;

	/**
	 * {@link ConformInput} for inputs.
	 */
	private final ConformInput inputConform = new ConformInput();

	/**
	 * {@link OfficeInstance} inputs.
	 */
	private final String[] officeInputs;

	/**
	 * {@link ConformInput} teams.
	 */
	private final ConformInput teamConform = new ConformInput();

	/**
	 * {@link OfficeInstance} teams.
	 */
	private final String[] officeTeams;

	/**
	 * Initiate.
	 * 
	 * @param officeInstance
	 *            {@link OfficeInstance}.
	 */
	protected OfficeSourceAlignDeployedOfficeWizardPage(OfficeInstance officeInstance) {
		super("Refactor Office");
		this.officeInstance = officeInstance;
		this.setTitle("Refactor Office");

		// Create the objects/inputs/teams from office
		DeployedOfficeModel office = this.officeInstance.getDeployedOfficeModel();
		List<String> objects = new LinkedList<String>();
		for (DeployedOfficeObjectModel object : office.getDeployedOfficeObjects()) {
			objects.add(object.getDeployedOfficeObjectName());
		}
		this.officeObjects = objects.toArray(new String[0]);
		List<String> inputs = new LinkedList<String>();
		for (DeployedOfficeInputModel input : office.getDeployedOfficeInputs()) {
			String name = input.getSectionName() + OfficeFloorChanges.SECTION_INPUT_SEPARATOR
					+ input.getSectionInputName();
			inputs.add(name);
		}
		this.officeInputs = inputs.toArray(new String[0]);
		List<String> teams = new LinkedList<String>();
		for (DeployedOfficeTeamModel team : office.getDeployedOfficeTeams()) {
			teams.add(team.getDeployedOfficeTeamName());
		}
		this.officeTeams = teams.toArray(new String[0]);
	}

	/**
	 * Loads the {@link OfficeSourceInstance}.
	 *
	 * @param instance
	 *            {@link OfficeSourceInstance}.
	 */
	public void loadOfficeSourceInstance(OfficeSourceInstance instance) {

		// Ensure have office type (may still require properties)
		OfficeType officeType = instance.getOfficeType();
		if (officeType == null) {
			return;
		}

		// Load the object conforms
		List<String> objects = new LinkedList<String>();
		for (OfficeManagedObjectType object : officeType.getOfficeManagedObjectTypes()) {
			objects.add(object.getOfficeManagedObjectName());
		}
		this.objectConform.setConform(this.officeObjects, objects.toArray(new String[0]));

		// Load the input conforms
		List<String> inputs = new LinkedList<String>();
		for (OfficeAvailableSectionInputType inputType : officeType.getOfficeSectionInputTypes()) {
			String name = inputType.getOfficeSectionName() + OfficeFloorChanges.SECTION_INPUT_SEPARATOR
					+ inputType.getOfficeSectionInputName();
			inputs.add(name);
		}
		this.inputConform.setConform(this.officeInputs, inputs.toArray(new String[0]));

		// Load the team conforms
		List<String> teams = new LinkedList<String>();
		for (OfficeTeamType team : officeType.getOfficeTeamTypes()) {
			teams.add(team.getOfficeTeamName());
		}
		this.teamConform.setConform(this.officeTeams, teams.toArray(new String[0]));
	}

	/**
	 * Obtains the mapping of {@link OfficeManagedObjectType} name to
	 * {@link DeployedOfficeObjectModel} name.
	 *
	 * @return Mapping of {@link OfficeManagedObjectType} name to
	 *         {@link DeployedOfficeObjectModel} name.
	 */
	public Map<String, String> getObjectNameMapping() {
		return this.objectConform.getTargetItemToExistingItemMapping();
	}

	/**
	 * Obtains the mapping of {@link OfficeInputType} name to
	 * {@link DeployedOfficeInputModel} name.
	 *
	 * @return Mapping of {@link OfficeInputType} name to
	 *         {@link DeployedOfficeInputModel} name.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputConform.getTargetItemToExistingItemMapping();
	}

	/**
	 * Obtains the mapping of {@link OfficeTeamType} name to
	 * {@link DeployedOfficeTeamModel} name.
	 *
	 * @return Mapping of {@link OfficeTeamType} name to
	 *         {@link DeployedOfficeTeamModel} name.
	 */
	public Map<String, String> getTeamNameMapping() {
		return this.teamConform.getTargetItemToExistingItemMapping();
	}

	/*
	 * =================== IDialogPage ===================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Create conform for objects
		new Label(page, SWT.NONE).setText("Objects");
		InputHandler<ConformModel> objectHandler = new InputHandler<ConformModel>(page, this.objectConform);
		objectHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create conform for inputs
		new Label(page, SWT.NONE).setText("Inputs");
		InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(page, this.inputConform);
		inputHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create conform for teams
		new Label(page, SWT.NONE).setText("Teams");
		InputHandler<ConformModel> teamHandler = new InputHandler<ConformModel>(page, this.teamConform);
		teamHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Specify the control
		this.setControl(page);
	}

}