/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.io.File;
import java.util.Map;

import net.officefloor.console.OpenOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/**
 * {@link ILaunchConfigurationDelegate} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLaunchConfigurationDelegate extends
		AbstractJavaLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	/*
	 * =============== ILaunchConfigurationDelegate ======================
	 */

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		// Ensure have a monitor
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Verify the main type and create the runner
		IVMRunner runner = this.getVMRunner(configuration, mode);

		// Obtain the main type name
		// TODO validate can open OfficeFloor
		String mainTypeName = OpenOfficeFloor.class.getName();

		// Create class path
		String[] classpath = this.getClasspath(configuration);

		// Create the environment
		String[] environment = this.getEnvironment(configuration);

		// Program and VM arguments
		String programArguments = configuration.getAttribute(
				OfficeFloorLauncher.ATTR_OFFICE_FLOOR_FILE, "");
		String officeName = configuration.getAttribute(
				OfficeFloorLauncher.ATTR_OFFICE_NAME, "");
		if ((officeName != null) && (officeName.trim().length() > 0)) {
			String workName = configuration.getAttribute(
					OfficeFloorLauncher.ATTR_WORK_NAME, "");

			// Flag to invoke work (within office)
			programArguments += " " + officeName + " " + workName;
		}
		String vmAguments = this.getVMArguments(configuration);
		ExecutionArguments executionArguments = new ExecutionArguments(
				vmAguments, programArguments);

		// Obtain the working directory
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = (workingDir == null ? null : workingDir
				.getAbsolutePath());

		// Obtain the VM specific attributes
		Map<?, ?> vmAttributesMap = this
				.getVMSpecificAttributesMap(configuration);

		// Obtain the boot path
		String[] bootpath = this.getBootpath(configuration);

		// Create and initialise the VM configuration
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
				mainTypeName, classpath);
		runConfig.setProgramArguments(executionArguments
				.getProgramArgumentsArray());
		runConfig.setEnvironment(environment);
		runConfig.setVMArguments(executionArguments.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setBootClassPath(bootpath);

		// Stop in main
		this.prepareStopInMain(configuration);

		// Specify the default source locator if required
		this.setDefaultSourceLocator(launch, configuration);

		// Launch the configuration
		runner.run(runConfig, launch, monitor);

		// Done
		monitor.done();
	}

}