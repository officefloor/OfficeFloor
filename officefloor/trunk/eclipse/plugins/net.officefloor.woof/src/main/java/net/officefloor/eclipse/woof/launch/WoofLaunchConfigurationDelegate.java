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
package net.officefloor.eclipse.woof.launch;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;

/**
 * {@link ILaunchConfigurationDelegate} for the {@link WoofOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLaunchConfigurationDelegate extends
		AbstractJavaLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	/**
	 * Obtains the source paths.
	 * 
	 * @param launchConfiguration
	 *            {@link ILaunchConfiguration}.
	 * @return Source paths.
	 * @throws CoreException
	 *             If fails to obtain the source paths.
	 */
	public static String[] getSourcePath(
			ILaunchConfiguration launchConfiguration) throws CoreException {

		// Obtain the source containers
		IRuntimeClasspathEntry[] unresolvedSourceEntries = JavaRuntime
				.computeUnresolvedSourceLookupPath(launchConfiguration);
		IRuntimeClasspathEntry[] sourceEntries = JavaRuntime
				.resolveSourceLookupPath(unresolvedSourceEntries,
						launchConfiguration);
		ISourceContainer[] sourceContainers = JavaRuntime
				.getSourceContainers(sourceEntries);

		// Compute the source paths
		List<String> sourcePathEntries = new LinkedList<String>();
		loadSourcePathEntries(sourceContainers, sourcePathEntries);

		// Return the source paths
		return sourcePathEntries.toArray(new String[sourcePathEntries.size()]);
	}

	/**
	 * Loads the source path entries to be included on the class path.
	 * 
	 * @param containers
	 *            {@link ISourceContainer} instances.
	 * @param sourcePathEntries
	 *            Source path entries.
	 * @throws CoreException
	 *             If fails to load source path entries.
	 */
	private static void loadSourcePathEntries(ISourceContainer[] containers,
			List<String> sourcePathEntries) throws CoreException {

		// Load the source path entries
		for (ISourceContainer container : containers) {

			// Only load leaf source containers
			if (container.isComposite()) {
				loadSourcePathEntries(container.getSourceContainers(),
						sourcePathEntries);
				continue;
			}

			// Leaf container, so determine the path
			String path = null;
			if (container instanceof PackageFragmentRootSourceContainer) {
				// Package fragment potentially containing source
				PackageFragmentRootSourceContainer fragment = (PackageFragmentRootSourceContainer) container;
				IPackageFragmentRoot fragmentRoot = fragment
						.getPackageFragmentRoot();
				if (fragmentRoot != null) {
					IResource resource = fragmentRoot.getResource();
					if (resource != null) {
						path = resource.getLocation().toOSString();
					}
				}

			} else if (container instanceof FolderSourceContainer) {
				// Folder
				FolderSourceContainer folder = (FolderSourceContainer) container;
				path = folder.getContainer().getLocation().toOSString();

			} else if (container instanceof ArchiveSourceContainer) {
				// Archive potentially containing source
				ArchiveSourceContainer archive = (ArchiveSourceContainer) container;
				path = archive.getFile().getLocation().toOSString();

			} else if (container instanceof ExternalArchiveSourceContainer) {
				// External archive potentially containing source
				ExternalArchiveSourceContainer archive = (ExternalArchiveSourceContainer) container;
				path = archive.getName();

			} else {
				// Unknown source path container
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								WoofPlugin.PLUGIN_ID,
								"Unknown "
										+ ISourceContainer.class
												.getSimpleName()
										+ " '"
										+ container.getClass().getName()
										+ "' that is not currently handled for launching"));
			}

			// Include the path uniquely
			if (path != null) {
				if (!(sourcePathEntries.contains(path))) {
					sourcePathEntries.add(path);
				}
			}
		}
	}

	/*
	 * ================ ILaunchConfigurationDelegate ==================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		// Ensure have a monitor
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Ensure continue to launch WoOF
		if (monitor.isCanceled()) {
			return; // cancel launch
		}

		try {

			// Indicate verifying launch
			monitor.subTask("Verifying WoOF launch");

			// Ensure valid main type
			String mainTypeName = this.getMainTypeName(configuration);
			if ((mainTypeName == null) || (mainTypeName.trim().length() == 0)) {
				// TODO Default main type
				mainTypeName = "todo.launch.gwt.DevMode";
			}
			IVMRunner runner = this.getVMRunner(configuration, mode);

			// TODO remove
			mainTypeName = WoofOfficeFloorSource.class.getName();

			// Obtain the working directory
			File workingDir = this.verifyWorkingDirectory(configuration);
			String workingDirPath = null;
			if (workingDir != null) {
				workingDirPath = workingDir.getAbsolutePath();
			}

			// Environment variables
			String[] environment = this.getEnvironment(configuration);

			// Obtain the execution arguments
			String vmArgumentsConfiguration = this
					.getVMArguments(configuration);
			String programArgumentsConfiguration = this
					.getProgramArguments(configuration);
			ExecutionArguments executionArguments = new ExecutionArguments(
					vmArgumentsConfiguration, programArgumentsConfiguration);

			// TODO provide configuration for DevMode WoOF Server
			String[] programArguments = executionArguments
					.getProgramArgumentsArray();

			// Obtain the VM specific attributes
			Map<String, String[]> vmAttributes = this
					.getVMSpecificAttributesMap(configuration);

			// Obtain the boot path
			String[] bootPath = this.getBootpath(configuration);

			// Compute class path entries
			List<String> classpathEntries = new LinkedList<String>();

			// TODO Include GWT DevMode on class path

			// Include the source paths on class path
			String[] sourcePath = getSourcePath(configuration);
			classpathEntries.addAll(Arrays.asList(sourcePath));

			// Include runtime class path
			String[] runtimeClasspath = this.getClasspath(configuration);
			classpathEntries.addAll(Arrays.asList(runtimeClasspath));

			// Create configuration for VM
			VMRunnerConfiguration runConfiguration = new VMRunnerConfiguration(
					mainTypeName,
					classpathEntries.toArray(new String[classpathEntries.size()]));
			runConfiguration.setBootClassPath(bootPath);
			runConfiguration.setProgramArguments(programArguments);
			runConfiguration.setEnvironment(environment);
			runConfiguration.setVMArguments(executionArguments
					.getVMArgumentsArray());
			runConfiguration.setWorkingDirectory(workingDirPath);
			runConfiguration.setVMSpecificAttributesMap(vmAttributes);

			// Ensure continue to launch WoOF
			if (monitor.isCanceled()) {
				return; // cancel launch
			}

			// Allow debugging stop in main
			this.prepareStopInMain(configuration);

			// Verification complete
			monitor.worked(1);

			// Specify the default source locator
			monitor.subTask("Setting up Source Locator");
			this.setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// Launch WoOF
			runner.run(runConfiguration, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

		} finally {
			monitor.done();
		}
	}

}