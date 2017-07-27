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
import net.officefloor.eclipse.common.dialog.input.InvalidValueException;
import net.officefloor.eclipse.common.dialog.input.ValueTranslator;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;

/**
 * {@link Input} to select a {@link ManagedObjectScope}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectScopeInput implements Input<List>, ValueTranslator {

	/**
	 * Listing of the {@link ManagedObjectScope} names.
	 */
	private static final String[] scopeNames = new String[] { "Process", "Thread", "Work" };

	/**
	 * Listing of {@link ManagedObjectScope} instances that correspond to the
	 * names.
	 */
	private static final ManagedObjectScope[] scopes = new ManagedObjectScope[] { ManagedObjectScope.PROCESS,
			ManagedObjectScope.THREAD, ManagedObjectScope.FUNCTION };

	/*
	 * ======================== Input ======================================
	 */

	@Override
	public List buildControl(final InputContext context) {

		// Create the list containing the scopes
		final List scopeList = new List(context.getParent(), SWT.SINGLE | SWT.BORDER);
		for (String scopeName : scopeNames) {
			scopeList.add(scopeName);
		}

		// Determine if initial scope
		Object initialValue = context.getInitialValue();
		if (initialValue instanceof ManagedObjectScope) {
			switch ((ManagedObjectScope) initialValue) {
			case PROCESS:
				scopeList.setSelection(0);
				break;
			case THREAD:
				scopeList.setSelection(1);
				break;
			case FUNCTION:
				scopeList.setSelection(2);
				break;
			}
		}

		// Add listener for changes
		scopeList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtain the value
				Object value = ManagedObjectScopeInput.this.getValue(scopeList, context);

				// Indicate value changes
				context.notifyValueChanged(value);
			}
		});

		// Return the list
		return scopeList;
	}

	@Override
	public Object getValue(List control, InputContext context) {

		// Obtain the selected item
		int selection = control.getSelectionIndex();
		if (selection < 0) {
			// Nothing selected
			return null;
		} else {
			// Return the selected scope
			return scopes[selection];
		}
	}

	/*
	 * ==================== ValueTranslator ===============================
	 */

	@Override
	public Object translate(Object inputValue) throws InvalidValueException {
		if (inputValue == null) {
			// No value
			return null;
		} else if (inputValue instanceof ManagedObjectScope) {
			// Correctly a managed object scope
			return inputValue;
		} else {
			// Invalid value
			throw new InvalidValueException("Unknown value type: " + inputValue.getClass().getName());
		}
	}

}