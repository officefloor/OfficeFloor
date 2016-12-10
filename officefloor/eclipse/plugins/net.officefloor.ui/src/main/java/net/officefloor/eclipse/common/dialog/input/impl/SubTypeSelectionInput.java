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
package net.officefloor.eclipse.common.dialog.input.impl;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;

/**
 * {@link List} to hold sub types.
 *
 * @author Daniel Sagenschneider
 */
public class SubTypeSelectionInput implements Input<List> {

	/**
	 * Listing sub {@link IType} instances.
	 */
	private IType[] subTypes;

	/**
	 * Initiate.
	 *
	 * @param project
	 *            {@link IProject}.
	 * @param superClassName
	 *            Super class name.
	 */
	public SubTypeSelectionInput(IProject project, String superClassName) {
		try {
			// Obtain the sub types
			this.subTypes = JavaUtil.getSubTypes(project, superClassName);

		} catch (Throwable ex) {
			// Indicate failed to obtain sub types
			LogUtil.logError(
					"Failed to obtain sub types for " + superClassName, ex);
			this.subTypes = new IType[0];
		}
	}

	/*
	 * ==================== Input =======================================
	 */

	@Override
	public List buildControl(final InputContext context) {

		// Create the list of the sub types
		final List subTypeList = new List(context.getParent(), SWT.SINGLE
				| SWT.BORDER);
		for (IType type : this.subTypes) {
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