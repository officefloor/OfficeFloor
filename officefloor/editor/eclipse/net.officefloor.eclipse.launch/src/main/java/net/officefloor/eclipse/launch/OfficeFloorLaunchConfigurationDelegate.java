/*-
 * #%L
 * [bundle] Eclipse OfficeFloor Launcher
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.eclipse.launch;

import java.io.File;
import java.util.Map;

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

import net.officefloor.OfficeFloorMain;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ILaunchConfigurationDelegate} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate {

	/*
	 * =============== ILaunchConfigurationDelegate ======================
	 */

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		// Ensure have a monitor
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Verify the main type and create the runner
		IVMRunner runner = this.getVMRunner(configuration, mode);

		// Obtain the main type name
		String mainTypeName = OfficeFloorMain.class.getName();

		// Create class path
		String[] classpath = this.getClasspathAndModulepath(configuration)[0];

		// Create the environment
		String[] environment = this.getEnvironment(configuration);

		// Program and VM arguments
		String vmAguments = this.getVMArguments(configuration);
		ExecutionArguments executionArguments = new ExecutionArguments(vmAguments, "");

		// Obtain the working directory
		File workingDir = this.verifyWorkingDirectory(configuration);
		String workingDirName = (workingDir == null ? null : workingDir.getAbsolutePath());

		// Obtain the VM specific attributes
		Map<String, Object> vmAttributesMap = this.getVMSpecificAttributesMap(configuration);

		// Obtain the boot path
		String[] bootpath = this.getBootpath(configuration);

		// Create and initialise the VM configuration
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
		runConfig.setProgramArguments(executionArguments.getProgramArgumentsArray());
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
