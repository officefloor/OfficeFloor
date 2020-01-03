package net.officefloor.eclipse.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ILaunchConfigurationTabGroup} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTabGroup extends AbstractLaunchConfigurationTabGroup {

	/*
	 * ================ AbstractLaunchConfigurationTabGroup =================
	 */

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new OfficeFloorMainTab(),
				new JavaArgumentsTab(), new JavaJRETab(), new JavaClasspathTab(), new SourceLookupTab(),
				new EnvironmentTab(), new CommonTab() };
		this.setTabs(tabs);
	}
}
