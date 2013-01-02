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
package net.officefloor.eclipse.woof.launch;

import net.officefloor.launch.woof.WoofDevelopmentLauncher;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * {@link ILaunchShortcut} for the {@link WoofOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLaunchShortcut implements ILaunchShortcut {

	/**
	 * Obtains the {@link ILaunchManager}.
	 * 
	 * @return {@link ILaunchManager}.
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Obtains the {@link ILaunchConfigurationType}.
	 * 
	 * @return {@link ILaunchConfigurationType}.
	 */
	private ILaunchConfigurationType getConfigurationType() {
		return this.getLaunchManager().getLaunchConfigurationType(
				"net.officefloor.eclipse.launch.configurationtype.woof");
	}

	/**
	 * Obtains the {@link ILaunchConfiguration} for the {@link IProject}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link ILaunchConfiguration}.
	 */
	private ILaunchConfiguration getLaunchConfiguration(IProject project) {

		// Obtain the launch configuration type
		ILaunchConfigurationType configurationType = this
				.getConfigurationType();

		try {
			// Obtain the candidate configurations
			ILaunchConfiguration[] candidates = DebugPlugin.getDefault()
					.getLaunchManager()
					.getLaunchConfigurations(configurationType);

			// Determine if existing launch configuration
			String projectName = project.getName();
			for (ILaunchConfiguration candidate : candidates) {
				if (candidate
						.getAttribute(
								IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
								"").equals(projectName)) {
					// Found the launch configuration
					return candidate;
				}
			}

		} catch (CoreException ex) {
			this.reportError(ex);
		}

		// As here, did not find launch configuration so provide new
		return this.createLaunchConfiguration(project);
	}

	/**
	 * Creates a new {@link ILaunchConfiguration} for the {@link IProject}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link ILaunchConfiguration}.
	 */
	private ILaunchConfiguration createLaunchConfiguration(IProject project) {

		// Create the configuration
		ILaunchConfiguration launchConfiguration = null;
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {

			// Obtain the project name
			String projectName = project.getName();

			// Obtain the configuration type
			ILaunchConfigurationType configurationType = getConfigurationType();

			// Obtain the launcher for GWT DevMode with WoOF
			String devModeClassName = WoofDevelopmentLauncher.class.getName();

			// Create the working copy of configuration
			workingCopy = configurationType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(
							projectName));
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					devModeClassName);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					projectName);
			workingCopy.setMappedResources(new IResource[] { project });

			// Save configuration and create launch configuration
			launchConfiguration = workingCopy.doSave();

		} catch (CoreException ex) {
			this.reportError(ex);
		}

		// Return the launch configuration
		return launchConfiguration;
	}

	/**
	 * Report {@link CoreException}.
	 * 
	 * @param cause
	 *            Cause.
	 */
	private void reportError(CoreException cause) {
		// Provide error
		Shell activeShell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		MessageDialog.openError(activeShell, "Failed to launch WoOF", cause
				.getStatus().getMessage());
	}

	/**
	 * Launch.
	 * 
	 * @param project
	 *            {@link IProject} to launch.
	 * @param mode
	 *            Mode.
	 */
	private void launch(IProject project, String mode) {

		// Obtain the launch configuration
		ILaunchConfiguration configuration = this
				.getLaunchConfiguration(project);
		if (configuration == null) {
			return; // must have configuration to launch
		}

		// Launch
		DebugUITools.launch(configuration, mode);
	}

	/*
	 * ===================== ILaunchShortcut =========================
	 */

	@Override
	public void launch(ISelection selection, String mode) {
		// Launch for structured selection
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			// Launch only for first found project of selection
			Object[] items = structuredSelection.toArray();
			for (Object item : items) {
				if (item instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) item;
					IResource resource = (IResource) adaptable
							.getAdapter(IResource.class);
					if (resource != null) {
						IProject project = resource.getProject();
						if (project != null) {

							// Found project so launch for project
							this.launch(project, mode);

							// Only launch for first project
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {

		// Obtain the Project
		IEditorInput input = editor.getEditorInput();
		IResource resource = (IResource) input.getAdapter(IResource.class);
		IProject project = resource.getProject();

		// Launch
		this.launch(project, mode);
	}

}