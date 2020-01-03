package net.officefloor.eclipse.launch;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import net.officefloor.OfficeFloorMain;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ILaunchConfigurationTab} for running {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMainTab extends JavaMainTab {

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		// Default to OfficeFloor main
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
				OfficeFloorMain.class.getName());
	}

}