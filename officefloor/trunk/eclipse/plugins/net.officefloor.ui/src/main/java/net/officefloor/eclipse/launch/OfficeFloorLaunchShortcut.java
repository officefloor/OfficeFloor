/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.frame.api.manage.OfficeFloor;

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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * {@link ILaunchShortcut} for the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLaunchShortcut implements ILaunchShortcut {

	/*
	 * ================== ILaunchShortcut ================================
	 */

	@Override
	public void launch(ISelection selection, String mode) {
		try {

			// Obtain as structured selection
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			// Obtain the first item
			Object item = structuredSelection.getFirstElement();
			if (item == null) {
				return;
			}

			// Obtain as adaptable
			if (!(item instanceof IAdaptable)) {
				return;
			}
			IAdaptable adaptable = (IAdaptable) item;

			// Obtain as resource
			IResource resource = (IResource) adaptable
					.getAdapter(IResource.class);
			if (resource == null) {
				return;
			}

			// Obtain the java package fragment
			IJavaElement javaElement = JavaCore.create(resource.getParent());
			if (!(javaElement instanceof IPackageFragment)) {
				return;
			}
			IPackageFragment packageFragment = (IPackageFragment) javaElement;

			// Obtain the office floor launch path
			String officeFloorLaunchPath = packageFragment.getElementName()
					.replace('.', '/')
					+ "/" + resource.getName();

			// Obtain the Launch Manager
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();

			// Create launch configuration type
			ILaunchConfigurationType launchConfigType = launchManager
					.getLaunchConfigurationType(OfficeFloorLauncher.ID_OFFICE_FLOOR_CONFIGURATION_TYPE);

			// Find existing launch configuration
			String resourceName = resource.getName();
			String extension = resource.getFileExtension();
			if (extension != null) {
				// Ignore extension
				resourceName = resourceName.replace("." + extension, "");
			}
			ILaunchConfiguration launchConfig = null;
			for (ILaunchConfiguration existingConfig : launchManager
					.getLaunchConfigurations(launchConfigType)) {
				if (resourceName.equalsIgnoreCase(existingConfig.getName())) {
					launchConfig = existingConfig;
				}
			}

			// Create launch configuration if not one existing by same name
			if (launchConfig == null) {
				// Ensure unique configuration name for launch
				String uniqueConfigName = launchManager
						.generateUniqueLaunchConfigurationNameFrom(resourceName);

				// Create launch configuration (include project for defaults)
				ILaunchConfigurationWorkingCopy launchConfigWorkingCopy = launchConfigType
						.newInstance(null, uniqueConfigName);
				launchConfigWorkingCopy.setAttribute(
						OfficeFloorLauncher.ATTR_OFFICE_FLOOR_FILE,
						officeFloorLaunchPath);
				launchConfigWorkingCopy.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
						resource.getProject().getName());
				launchConfig = launchConfigWorkingCopy.doSave();
			}

			// Launch
			DebugUITools.launch(launchConfig, mode);

		} catch (CoreException ex) {
			// Display error in launching
			ErrorDialog.openError(null, "Error launching", "Failed to launch",
					ex.getStatus());
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		MessageDialog.openError(editor.getEditorSite().getShell(), "Error",
				"Should not run from editor");
	}
}
