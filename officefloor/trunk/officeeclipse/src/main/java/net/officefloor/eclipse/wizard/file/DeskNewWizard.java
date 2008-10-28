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
package net.officefloor.eclipse.wizard.file;

import net.officefloor.eclipse.wizard.file.AbstractNewWizard;
import net.officefloor.model.desk.DeskModel;

import org.eclipse.jface.wizard.IWizard;

/**
 * {@link IWizard} to create a {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskNewWizard extends AbstractNewWizard {

	/**
	 * Initiate.
	 */
	public DeskNewWizard() {
		super("New Desk", "Create a new Desk", "new_desk", "desk", "<desk/>");
	}

}
