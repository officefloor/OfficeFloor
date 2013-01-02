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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.util.EclipseUtil;

/**
 * {@link Input} to select a {@link Method} name for a {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassMethodInput implements Input<Combo> {

	/**
	 * {@link ClassLoader} to load the {@link Class}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Current {@link Class} to provide listing of methods.
	 */
	private Class<?> clazz;

	/**
	 * {@link Combo}.
	 */
	private Combo combo;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ClassMethodInput(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Specifies the name of the {@link Class}.
	 * 
	 * @param className
	 *            Name of the {@link Class}.
	 */
	public void setClassName(String className) {

		// Obtain the class (resetting to null if not able to obtain)
		this.clazz = null;
		if (!(EclipseUtil.isBlank(className))) {
			try {
				this.clazz = this.classLoader.loadClass(className);
			} catch (ClassNotFoundException ex) {
				// Ignore and leave no class
			}
		}

		// Load the methods for the class
		this.loadMethodsForSelection();
	}

	/**
	 * Loads the {@link Method} names for selection.
	 */
	private void loadMethodsForSelection() {

		// Ensure have the Combo
		if (this.combo == null) {
			return; // No Combo yet to populate
		}

		// Clear the selection to re-populate
		int itemCount = this.combo.getItemCount();
		if (itemCount > 0) {
			this.combo.remove(0, itemCount - 1);
		}

		// Ensure have Class
		if (this.clazz == null) {
			return; // nothing to populate
		}

		// Obtain the method names
		Set<String> methodNames = new HashSet<String>();
		try {
			for (Method method : this.clazz.getMethods()) {
				methodNames.add(method.getName());
			}
		} catch (Throwable ex) {
			// Ignore failure and use available methods
		}
		String[] orderedMethodNames = methodNames
				.toArray(new String[methodNames.size()]);
		Arrays.sort(orderedMethodNames);

		// Add the methods
		for (String methodName : orderedMethodNames) {
			this.combo.add(methodName);
		}
	}

	/*
	 * ==================== Input ============================
	 */

	@Override
	public Combo buildControl(final InputContext context) {

		// Create the combo
		this.combo = new Combo(context.getParent(), SWT.NONE);

		// Populate the combo
		this.loadMethodsForSelection();

		// Indicate change in value
		this.combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				context.notifyValueChanged(ClassMethodInput.this.combo
						.getText());
			}
		});

		// Specify the initial value
		Object initialValue = context.getInitialValue();
		String initialMethodName = (initialValue == null ? "" : initialValue
				.toString());
		this.combo.setText(initialMethodName);

		// Return the combo
		return this.combo;
	}

	@Override
	public Object getValue(Combo control, InputContext context) {
		// Obtain the value
		return control.getText();
	}

}