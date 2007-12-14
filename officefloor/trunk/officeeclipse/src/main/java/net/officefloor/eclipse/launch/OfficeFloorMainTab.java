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
package net.officefloor.eclipse.launch;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.model.office.OfficeModel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link ILaunchConfigurationTab} for running {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorMainTab extends AbstractLaunchConfigurationTab {

	/**
	 * Configuration file.
	 */
	private Text configurationFile;

	/**
	 * Name of {@link OfficeModel} to invoke.
	 */
	private Text officeName;

	/**
	 * Name of {@link WorkManager} to invoke.
	 */
	private Text workName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return "Main";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
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
		new Label(composite, SWT.NONE).setText("Office Floor");
		this.configurationFile = new Text(composite, SWT.NONE);
		this.configurationFile.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		this.configurationFile.addModifyListener(dirtyListener);

		// Specifies the office to invoke
		new Label(composite, SWT.NONE).setText("Office");
		this.officeName = new Text(composite, SWT.NONE);
		this.officeName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.officeName.addModifyListener(dirtyListener);

		// Specifies the work manager to invoke
		new Label(composite, SWT.NONE).setText("Work");
		this.workName = new Text(composite, SWT.NONE);
		this.workName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.workName.addModifyListener(dirtyListener);

		// Specify the composite
		this.setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	@SuppressWarnings("restriction")
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// Obtain the values
			this.configurationFile.setText(configuration.getAttribute(
					OfficeFloorLauncher.ATTR_OFFICE_FLOOR_FILE, ""));
			this.officeName.setText(configuration.getAttribute(
					OfficeFloorLauncher.ATTR_OFFICE_NAME, ""));
			this.workName.setText(configuration.getAttribute(
					OfficeFloorLauncher.ATTR_WORK_NAME, ""));
		} catch (CoreException ex) {
			DebugUIPlugin.log(ex.getStatus());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(OfficeFloorLauncher.ATTR_OFFICE_FLOOR_FILE,
				this.configurationFile.getText());
		configuration.setAttribute(OfficeFloorLauncher.ATTR_OFFICE_NAME,
				this.officeName.getText());
		configuration.setAttribute(OfficeFloorLauncher.ATTR_WORK_NAME,
				this.workName.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// No defaults
	}

}
