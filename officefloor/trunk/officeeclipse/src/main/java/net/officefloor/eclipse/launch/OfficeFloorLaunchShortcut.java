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

import net.officefloor.eclipse.OfficeFloorPluginFailure;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * {@link ILaunchShortcut} for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorLaunchShortcut implements ILaunchShortcut {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection,
	 *      java.lang.String)
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

			// Create unique configuration name
			String resourceName = resource.getName();
			String extension = resource.getFileExtension();
			if (extension != null) {
				// Ignore extension
				resourceName = resourceName.replace("." + extension, "");
			}
			String uniqueConfigName = launchManager
					.generateUniqueLaunchConfigurationNameFrom(resourceName);

			// Create the launch configuration
			ILaunchConfigurationType launchConfigType = launchManager
					.getLaunchConfigurationType(OfficeFloorLauncher.ID_OFFICE_FLOOR_CONFIGURATION_TYPE);
			ILaunchConfigurationWorkingCopy launchConfigWorkingCopy = launchConfigType
					.newInstance(null, uniqueConfigName);

			// Configure the launch configuration (include project for defaults)
			launchConfigWorkingCopy.setAttribute(
					OfficeFloorLauncher.ATTR_OFFICE_FLOOR_FILE,
					officeFloorLaunchPath);
			launchConfigWorkingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					resource.getProject().getName());

			// Save launch configuration
			ILaunchConfiguration launchConfig = launchConfigWorkingCopy
					.doSave();

			// Launch
			DebugUITools.launch(launchConfig, mode);

		} catch (CoreException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart,
	 *      java.lang.String)
	 */
	@Override
	public void launch(IEditorPart editor, String mode) {
		// Should not launch from editor part
		throw new OfficeFloorPluginFailure("Should not shortcut launch from "
				+ IEditorPart.class.getSimpleName());
	}
}
