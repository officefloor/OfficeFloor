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
package net.officefloor.eclipse.common.dialog;

import net.officefloor.frame.test.OfficeFrameTestCase;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

/**
 * Tests the {@link net.officefloor.eclipse.common.dialog.BeanDialog}.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanDialogTest extends OfficeFrameTestCase {

	/**
	 * Ensures dialog populates the bean.
	 */
	@Test
	@GuiTest
	public void testPopulateBean() {

		// Do not test if no GUI
		if (!this.isGuiAvailable()) {
			return; // Check necessary with JUnit 4.x
		}

		// Create the shell to display the dialog
		Display display = new Display();
		Shell shell = new Shell(display);

		// Create the mock bean to be populated
		MockBean bean = new MockBean();

		// Populate the bean
		final BeanDialog dialog = new BeanDialog(shell, bean, this.getClass()
				.getClassLoader());
		dialog.setBlockOnOpen(false);
		dialog.open();

		// Specify property values
		Composite dialogArea = (Composite) dialog.getDialogArea();
		Control[] children = dialogArea.getChildren();

		// Name
		((Text) children[1]).setText("name");
		((Text) children[4]).setText("1");
		((Text) children[7]).setText(MockBean.class.getName());

		// Flag close
		dialog.okPressed();

		// Validate the bean is populated
		assertEquals("Invalid name", "name", bean.getName());
		assertEquals("Incorrect count", 1, bean.getCount());
		assertEquals("Incorrect class", MockBean.class, bean.getClazz());
	}

}