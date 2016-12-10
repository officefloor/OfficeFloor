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

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

/**
 * Tests being able to add, remove items from a CSV listing.
 * 
 * @author Daniel Sagenschneider
 */
public class ListInputTest extends OfficeFrameTestCase {

	@Test
	@GuiTest
	public void testListInput() throws Exception {

		// Ensure only run if required
		if (!("yes".equalsIgnoreCase(System.getProperty("gui.blocking.test")))) {
			return; // do not run test
		}

		// Create display for testing
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Test");
		shell.setBounds(100, 100, 500, 500);
		shell.setLayout(new GridLayout(1, false));

		// Create container for testing
		final Composite test = new Composite(shell, SWT.NONE);
		test.setLayout(new GridLayout(2, false));
		test.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide value and error
		final Text failureText = new Text(test, SWT.NONE);
		final Text valueText = new Text(test, SWT.NONE);
		valueText.setText("VALUE");

		// Provide label for testing
		new Label(test, SWT.NONE).setText("Test:");

		// Provide input handler
		final InputHandler<String[]> handler = new InputHandler<String[]>(
				test,
				new ListInput<Text>(String.class, shell, new MockInputFactory()),
				new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						Object[] list = (Object[]) value;
						StringBuilder text = new StringBuilder();
						for (Object item : list) {
							text.append(item + ",");
						}
						valueText.setText(text.toString());
					}

					@Override
					public void notifyValueInvalid(String message) {
						failureText.setText(message);
					}
				});

		// Provide ability to get the value
		final Text result = new Text(test, SWT.NONE);
		result.setText("Press button");
		Button button = new Button(test, SWT.PUSH);
		button.setText("VALUE");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] values = handler.getTrySafeValue();
				if (values == null) {
					result.setText("<null>");
				} else {
					StringBuilder text = new StringBuilder();
					for (String value : values) {
						text.append(value + ",");
					}
					result.setText(text.toString());
				}
			}
		});

		// Run display
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * {@link InputFactory} for testing.
	 */
	private static class MockInputFactory implements InputFactory<Text>,
			Input<Text> {

		@Override
		public Input<Text> createInput() {
			return new MockInputFactory();
		}

		@Override
		public Text buildControl(final InputContext context) {
			Composite parent = context.getParent();
			final Text text = new Text(parent, SWT.NONE);
			text.setText("A");
			text.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					context.notifyValueChanged(text.getText());
				}
			});
			return text;
		}

		@Override
		public Object getValue(Text control, InputContext context) {
			return control.getText();
		}
	}

}