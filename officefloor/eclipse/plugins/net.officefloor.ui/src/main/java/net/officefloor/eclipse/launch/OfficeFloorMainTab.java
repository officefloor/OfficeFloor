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
package net.officefloor.eclipse.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ILaunchConfigurationTab} for running {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMainTab extends AbstractLaunchConfigurationTab {

	/**
	 * {@link IProject} containing the configuration file.
	 */
	private Text project;

	/*
	 * =================== ILaunchConfigurationTab ==========================
	 */

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public void createControl(Composite parent) {

		// Create the composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Listen to changes
		ModifyListener dirtyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				OfficeFloorMainTab.this.updateLaunchConfigurationDialog();
			}
		};

		// Specifies the configuration file
		new Label(composite, SWT.NONE).setText("Project");
		this.project = new Text(composite, SWT.NONE);
		this.project.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		this.project.addModifyListener(dirtyListener);

		// Specify the composite
		this.setControl(composite);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// Obtain the values
			this.project.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
		} catch (CoreException ex) {
			LogUtil.logError("Failed to initialise from configuration", ex);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, this.project.getText());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// No defaults
	}

}