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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * {@link Input} for a {@link Boolean}.
 * 
 * @author Daniel Sagenschneider
 */
public class BooleanInput implements Input<Button> {

	/*
	 * ================== Input ==============================
	 */

	@Override
	public Button buildControl(final InputContext context) {

		// Obtain the initial value
		Object initialValue = context.getInitialValue();
		boolean isChecked = Boolean.parseBoolean(initialValue == null ? ""
				: initialValue.toString());

		// Create the checkbox
		final Button checkbox = new Button(context.getParent(), SWT.CHECK);
		checkbox.setSelection(isChecked);

		// Handle selection changes
		checkbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				context.notifyValueChanged(checkbox.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				context.notifyValueChanged(checkbox.getSelection());
			}
		});

		// Return the checkbox
		return checkbox;
	}

	@Override
	public Object getValue(Button control, InputContext context) {
		// True if checked
		return Boolean.valueOf(control.getSelection());
	}

}