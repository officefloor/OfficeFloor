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

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
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
		// TODO implement
		System.out.println("Launch shortcut selection");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart,
	 *      java.lang.String)
	 */
	@Override
	public void launch(IEditorPart editor, String mode) {
		// TODO implement
		System.out.println("Launch shortcut editor");
	}

}
