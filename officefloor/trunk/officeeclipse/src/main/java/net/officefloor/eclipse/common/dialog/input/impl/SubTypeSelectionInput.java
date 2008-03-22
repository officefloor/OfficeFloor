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
package net.officefloor.eclipse.common.dialog.input.impl;

import java.util.LinkedList;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;

/**
 * {@link List} to hold sub types.
 * 
 * @author Daniel
 */
public class SubTypeSelectionInput implements Input<List> {

	/**
	 * Listing sub {@link IType} instances.
	 */
	private final IType[] subTypes;

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param superClassName
	 *            Super class name.
	 */
	public SubTypeSelectionInput(IProject project, String superClassName)
			throws OfficeFloorPluginFailure {
		try {

			// Ensure class on the class path
			// (necessary to check able to create the class)
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
			LinkedList<IType> subTypes = new LinkedList<IType>();
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
			this.subTypes = subTypes.toArray(new IType[0]);

		} catch (Exception ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.Input#buildControl(net.officefloor.eclipse.common.dialog.input.InputContext)
	 */
	@Override
	public List buildControl(final InputContext context) {
		// Create the list of the sub types
		final List subTypeList = new List(context.getParent(), SWT.SINGLE
				| SWT.BORDER);
		for (IType type : subTypes) {
			subTypeList.add(type.getFullyQualifiedName());
		}

		// Add listener for changes
		subTypeList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtain the value
				String value = SubTypeSelectionInput.this.getValue(subTypeList,
						context);

				// Indicate value changed
				context.notifyValueChanged(value);
			}
		});

		// Return the list
		return subTypeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.Input#getValue(org.eclipse.swt.widgets.Control,
	 *      net.officefloor.eclipse.common.dialog.input.InputContext)
	 */
	@Override
	public String getValue(List control, InputContext context) {
		// Obtain the name of the sub type selected
		String[] selection = control.getSelection();
		if (selection.length == 0) {
			// Nothing selected
			return null;
		} else {
			// First selected
			return selection[0];
		}
	}
}
