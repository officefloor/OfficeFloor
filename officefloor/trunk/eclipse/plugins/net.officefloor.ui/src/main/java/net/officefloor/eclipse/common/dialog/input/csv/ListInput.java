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
package net.officefloor.eclipse.common.dialog.input.csv;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.layout.NoMarginGridLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Provides a listing of {@link Input} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ListInput<C extends Control> implements Input<Composite> {

	/**
	 * {@link ListItem} instances.
	 */
	private final List<ListItem> items = new ArrayList<ListItem>();

	/**
	 * Component type for the array.
	 */
	private final Class<?> componentType;

	/**
	 * {@link Composite} to use to refresh the layout.
	 */
	private final Composite refreshLayoutComposite;

	/**
	 * {@link InputFactory}.
	 */
	private final InputFactory<C> factory;

	/**
	 * {@link InputContext}.
	 */
	private InputContext context;

	/**
	 * {@link Composite}.
	 */
	private Composite container;

	/**
	 * Initiate.
	 * 
	 * @param componentType
	 *            Component type for the array.
	 * @param refreshLayoutComposite
	 *            {@link Composite} to use to refresh the layout.
	 * @param factory
	 *            {@link InputFactory}.
	 */
	public ListInput(Class<?> componentType, Composite refreshLayoutComposite,
			InputFactory<C> factory) {
		this.componentType = componentType;
		this.refreshLayoutComposite = refreshLayoutComposite;
		this.factory = factory;
	}

	/**
	 * Adds an {@link Input}.
	 * 
	 * @param initialValue
	 *            Initial value for the added {@link Input}.
	 */
	private void addInput(Object initialValue) {

		// Create the input Container
		final Composite inputContainer = new Composite(this.container, SWT.NONE);
		inputContainer.setLayout(NoMarginGridLayout.create(2, false));
		inputContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));

		// Create the input
		Input<C> input = this.factory.createInput();
		InputHandler<Object> handler = new InputHandler<Object>(inputContainer,
				input, initialValue);
		handler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Create the list item (and register for listening)
		final ListItem item = new ListItem(handler);
		this.items.add(item);
		handler.setInputListener(item);

		// Add button to remove Input
		Button button = new Button(inputContainer, SWT.PUSH);
		button.setText("-");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Remove item from return values
				ListInput.this.items.remove(item);

				// Remove graphically
				inputContainer.dispose();

				// Notify values changed
				ListInput.this.context.notifyValueChanged(ListInput.this
						.getValues());

				// Ensure graphically show removed
				ListInput.this.refreshLayoutComposite.layout();
			}
		});
	}

	/**
	 * Obtains the values.
	 * 
	 * @return Values.
	 */
	private Object[] getValues() {

		// Obtain the values
		Object[] values = (Object[]) Array.newInstance(this.componentType,
				this.items.size());
		for (int i = 0; i < values.length; i++) {
			values[i] = this.items.get(i).handler.getTrySafeValue();
		}

		// Return the values
		return values;
	}

	/*
	 * ===================== Input ===========================
	 */

	@Override
	public Composite buildControl(InputContext context) {
		this.context = context;

		// Obtain the parent
		Composite parent = this.context.getParent();

		// Create control for this input
		Group control = new Group(parent, SWT.NONE);
		control.setLayout(NoMarginGridLayout.create());
		control.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Create container for Inputs
		this.container = new Composite(control, SWT.NONE);
		this.container.setLayout(NoMarginGridLayout.create());
		this.container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));

		// Add the initial values
		Object initialValues = context.getInitialValue();
		if ((initialValues != null) && (initialValues.getClass().isArray())) {
			for (Object initialValue : ((Object[]) initialValues)) {
				this.addInput(initialValue);
			}
		}

		// Button to add Input
		Button button = new Button(control, SWT.PUSH);
		button.setText("+");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Add the Input (with no initial value)
				ListInput.this.addInput(null);

				// Notify the value changed
				ListInput.this.context.notifyValueChanged(ListInput.this
						.getValues());

				// Ensure Input visible
				ListInput.this.refreshLayoutComposite.layout();
			}
		});

		// Return the control
		return control;
	}

	@Override
	public Object getValue(Composite control, InputContext context) {
		return this.getValues();
	}

	/**
	 * Item within the list.
	 */
	private class ListItem implements InputListener {

		/**
		 * {@link InputHandler} for this {@link ListItem}.
		 */
		private final InputHandler<Object> handler;

		/**
		 * Initiate.
		 * 
		 * @param handler
		 *            {@link InputHandler} for this {@link ListItem}.
		 */
		public ListItem(InputHandler<Object> handler) {
			this.handler = handler;
		}

		/*
		 * ================= InputListener ===================
		 */

		@Override
		public void notifyValueChanged(Object value) {
			ListInput.this.context.notifyValueChanged(ListInput.this
					.getValues());
		}

		@Override
		public void notifyValueInvalid(String message) {
			ListInput.this.context.notifyValueInvalid(message);
		}
	}

}