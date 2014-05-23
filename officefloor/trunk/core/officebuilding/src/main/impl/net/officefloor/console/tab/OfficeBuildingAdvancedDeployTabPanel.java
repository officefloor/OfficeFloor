/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.console.tab;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.officefloor.building.manager.ArtifactReference;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link JPanel} for the advanced deploy of an {@link OfficeFloor} to an
 * {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingAdvancedDeployTabPanel extends
		AbstractOfficeBuildingPanel {

	/**
	 * Initiate.
	 * 
	 * @param officeBuildingManager
	 *            {@link OfficeBuildingManager}.
	 */
	public OfficeBuildingAdvancedDeployTabPanel(
			OfficeBuildingManagerMBean officeBuildingManager) {
		super(officeBuildingManager);
	}

	/*
	 * ================= AbstractOfficeBuildingPanel ================
	 */

	@Override
	protected void init() throws Exception {

		// Advanced panel
		GridBagLayout layoutManager = new GridBagLayout();
		JPanel panelAdvancedStart = new JPanel(layoutManager);

		// Grid constraints
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.gridx = 0;
		constraint.gridy = -1; // will increment to first row

		// Obtain default open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = this
				.createDefaultOpenOfficeFloorConfiguration();

		// OfficeFloor source
		JTextField officeFloorSource = new JTextField(30);
		officeFloorSource
				.setText(configuration.getOfficeFloorSourceClassName());
		this.addAdvancedEntry(panelAdvancedStart, "OfficeFloor Source Class",
				officeFloorSource, constraint);

		// OfficeFloor location
		JTextField officeFloorLocation = new JTextField(30);
		officeFloorLocation.setText(configuration.getOfficeFloorLocation());
		this.addAdvancedEntry(panelAdvancedStart, "OfficeFloor location",
				officeFloorLocation, constraint);

		// Process name
		JTextField processName = new JTextField(30);
		processName.setText(configuration.getProcessName());
		this.addAdvancedEntry(panelAdvancedStart, "Process name", processName,
				constraint);

		// Artifact references
		OfficeTablePanel artifactReferences = new OfficeTablePanel("Group ID",
				"Artifact Id", "Type", "Version", "Classifier");
		for (ArtifactReference reference : configuration
				.getArtifactReferences()) {
			artifactReferences.addRow(reference.getGroupId(),
					reference.getArtifactId(), reference.getType(),
					reference.getVersion(), reference.getClassifier());
		}
		this.addAdvancedEntry(panelAdvancedStart, "Artifact references",
				artifactReferences, constraint);

		// Class path entries
		OfficeTablePanel classPathEntries = new OfficeTablePanel(
				"Class path entry");
		for (String classPathEntry : configuration.getClassPathEntries()) {
			classPathEntries.addRow(classPathEntry);
		}
		this.addAdvancedEntry(panelAdvancedStart, "Class path",
				classPathEntries, constraint);

		// JVM options
		OfficeTablePanel jvmOptions = new OfficeTablePanel("JVM option");
		for (String jvmOption : configuration.getJvmOptions()) {
			jvmOptions.addRow(jvmOption);
		}
		this.addAdvancedEntry(panelAdvancedStart, "JVM options", jvmOptions,
				constraint);

		// OfficeFloor properties
		OfficeTablePanel officeFloorProperties = new OfficeTablePanel("Name",
				"Value");
		Properties properties = configuration.getOfficeFloorProperties();
		for (String propertyName : properties.stringPropertyNames()) {
			String propertyValue = properties.getProperty(propertyName);
			officeFloorProperties.addRow(propertyName, propertyValue);
		}
		this.addAdvancedEntry(panelAdvancedStart, "OfficeFloor properties",
				officeFloorProperties, constraint);

		// Remove repository URLs
		OfficeTablePanel remoteRepositoryUrls = new OfficeTablePanel(
				"Remote repository URL");
		for (String remoteRepositoryUrl : configuration
				.getRemoteRepositoryUrls()) {
			remoteRepositoryUrls.addRow(remoteRepositoryUrl);
		}
		this.addAdvancedEntry(panelAdvancedStart, "Remote repository URLs",
				remoteRepositoryUrls, constraint);

		// Upload artifacts
		OfficeTablePanel uploadArtifacts = new OfficeTablePanel(
				"Upload Artifact");
		for (UploadArtifact artifact : configuration.getUploadArtifacts()) {
			uploadArtifacts.addRow(artifact.getName());
		}
		this.addAdvancedEntry(panelAdvancedStart, "Upload artifact",
				uploadArtifacts, constraint);

		// Open task
		JPanel panelOpenTask = new JPanel();
		JTextField openTaskOfficeName = (JTextField) panelOpenTask
				.add(new JTextField(10));
		openTaskOfficeName.setText(configuration.getOfficeName());
		JTextField openTaskWorkName = (JTextField) panelOpenTask
				.add(new JTextField(10));
		openTaskWorkName.setText(configuration.getWorkName());
		JTextField openTaskTaskName = (JTextField) panelOpenTask
				.add(new JTextField(10));
		openTaskTaskName.setText(configuration.getTaskName());
		JTextField openTaskParameter = (JTextField) panelOpenTask
				.add(new JTextField(10));
		openTaskParameter.setText(configuration.getParameter());
		this.addAdvancedEntry(panelAdvancedStart, "Open Task", panelOpenTask,
				constraint);

		// Wrap in scroll pane
		JScrollPane scrollPane = new JScrollPane(panelAdvancedStart);

		// Add the scroll pane
		this.add(scrollPane);
	}

	/**
	 * Adds an advanced entry to the {@link JPanel}.
	 * 
	 * @param panelAdvancedStart
	 *            {@link JPanel}.
	 * @param labelText
	 *            Text of the label.
	 * @param component
	 *            {@link Component} to add.
	 * @param constraint
	 *            {@link GridBagConstraints}.
	 */
	private void addAdvancedEntry(JPanel panelAdvancedStart, String labelText,
			Component component, GridBagConstraints constraint) {

		// Next row for entry
		constraint.gridy++;

		// Add the label
		Label label = new Label(labelText);
		constraint.gridx = 0;
		constraint.weightx = 0.1;
		constraint.fill = GridBagConstraints.NONE;
		constraint.anchor = GridBagConstraints.EAST;
		panelAdvancedStart.add(label, constraint);

		// Add the component
		constraint.gridx = 1;
		constraint.weightx = 0.9;
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.anchor = GridBagConstraints.WEST;
		constraint.ipady = (component instanceof OfficeTablePanel) ? 20 : 0;
		panelAdvancedStart.add(component, constraint);
	}

}