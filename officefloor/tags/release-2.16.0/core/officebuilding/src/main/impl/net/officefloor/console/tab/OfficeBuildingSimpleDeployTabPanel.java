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

import java.awt.Label;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link JPanel} for the simple deploy of an {@link OfficeFloor} to an
 * {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingSimpleDeployTabPanel extends
		AbstractOfficeBuildingPanel {

	/**
	 * {@link OfficeBuildingManageTabPanel}.
	 */
	private final OfficeBuildingManageTabPanel manageTab;

	/**
	 * Initiate.
	 * 
	 * @param officeBuildingManager
	 *            {@link OfficeBuildingManagerMBean}.
	 * @param manageTab
	 *            {@link OfficeBuildingManageTabPanel}.
	 */
	public OfficeBuildingSimpleDeployTabPanel(
			OfficeBuildingManagerMBean officeBuildingManager,
			OfficeBuildingManageTabPanel manageTab) {
		super(officeBuildingManager);
		this.manageTab = manageTab;
	}

	/*
	 * ================= AbstractOfficeBuildingPanel ================
	 */

	@Override
	protected void init() throws Exception {

		// Select application file
		this.add(new Label("Application file"));
		final JTextField textFileName = (JTextField) this
				.add(new JTextField(20));
		this.add(new JButton(new OfficeAction<Void>("...") {
			@Override
			public OfficeAsyncAction<Void> doAction() {

				// Select the file
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Applications", "jar", "war");
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser
						.showOpenDialog(OfficeBuildingSimpleDeployTabPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					textFileName.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}

				// No asynchronous operation
				return null;
			}
		}));

		// Start button
		this.add(new JButton(new OfficeAction<String>("deploy") {
			@Override
			public OfficeAsyncAction<String> doAction() throws Exception {

				// Load the artifact
				final String artifactFilePath = textFileName.getText();
				if ((artifactFilePath == null)
						|| (artifactFilePath.trim().length() == 0)) {
					throw new ErrorMessageException("Please select a file");
				}

				// Return the async action to open the OfficeFloor
				return new OfficeAsyncAction<String>("Deploying") {
					@Override
					public String doAction() throws Exception {

						// Obtain default open OfficeFloor configuration
						OpenOfficeFloorConfiguration configuration = OfficeBuildingSimpleDeployTabPanel.this
								.createDefaultOpenOfficeFloorConfiguration();

						// Configure the upload artifact
						UploadArtifact uploadArtifact = new UploadArtifact(
								new File(artifactFilePath));
						configuration.addUploadArtifact(uploadArtifact);

						// Start the OfficeFloor
						String processNameSpace = OfficeBuildingSimpleDeployTabPanel.this.officeBuildingManager
								.openOfficeFloor(configuration);

						// Return the process name space
						return processNameSpace;
					}

					@Override
					public void done(String result) throws Exception {
						// Provide notification that opened
						OfficeBuildingSimpleDeployTabPanel.this
								.notifyUser("Opened under process name space: "
										+ result);

						// Refresh the OfficeFloor processes
						OfficeBuildingSimpleDeployTabPanel.this.manageTab
								.refreshOfficeFloorProcesses();
					}
				};
			}
		}));
	}

}