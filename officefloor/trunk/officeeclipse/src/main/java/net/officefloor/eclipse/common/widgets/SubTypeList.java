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
package net.officefloor.eclipse.common.widgets;

import java.util.LinkedList;

import net.officefloor.eclipse.OfficeFloorPluginFailure;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * {@link List} to hold sub types.
 * 
 * @author Daniel
 */
public class SubTypeList {

	/**
	 * List of the sub types.
	 */
	private final List subTypeList;

	/**
	 * Initiate.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param project
	 *            {@link IProject}.
	 * @param superClassName
	 *            Super class name.
	 */
	public SubTypeList(Composite parent, IProject project, String superClassName)
			throws OfficeFloorPluginFailure {
		try {

			// Ensure team factory on the class path
			// (necessary to check able to create the team)
			IJavaProject javaProject = JavaCore.create(project);
			IType superType = javaProject.findType(superClassName);
			if (superType == null) {
				throw new Exception("Class " + superClassName
						+ " must be on the projects class path");
			}

			// Obtain the list of implementations
			ITypeHierarchy hierarchy = superType.newTypeHierarchy(null);
			IType[] allTypes = hierarchy.getAllClasses();

			// Trim the list to non-abstract implementations
			java.util.List<IType> subTypes = new LinkedList<IType>();
			for (IType type : allTypes) {

				// Ignore object
				if (Object.class.getName().equals(type.getFullyQualifiedName())) {
					continue;
				}

				// Ignore abstract classes
				if (Flags.isAbstract(type.getFlags())) {
					continue;
				}

				// Include type
				subTypes.add(type);
			}

			// Create the list of the sub types
			this.subTypeList = new List(parent, SWT.SINGLE | SWT.BORDER);
			for (IType type : subTypes) {
				this.subTypeList.add(type.getFullyQualifiedName());
			}

		} catch (Exception ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Obtains the class name of the selected sub type.
	 * 
	 * @return Class name of the selected sub type.
	 */
	public String getSubTypeClassName() {
		// Obtain the sub type selected
		String[] selection = this.subTypeList.getSelection();
		if (selection.length == 0) {
			// Nothing selected
			return null;
		} else {
			// First selected
			return selection[0];
		}
	}

}
