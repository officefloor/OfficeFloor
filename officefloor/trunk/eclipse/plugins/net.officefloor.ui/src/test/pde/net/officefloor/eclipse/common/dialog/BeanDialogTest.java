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
package net.officefloor.eclipse.common.dialog;

import junit.framework.TestCase;

import net.officefloor.eclipse.common.dialog.BeanDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests the {@link net.officefloor.eclipse.common.dialog.BeanDialog}.
 * 
 * @author Daniel
 */
public class BeanDialogTest extends TestCase {

	/**
	 * Ensures dialog populates the bean.
	 */
	public void testPopulateBean() {

		// Create the shell to display the dialog
		Display display = new Display();
		Shell shell = new Shell(display);

		// Create the mock bean to be populated
		MockBean bean = new MockBean();

		// Populate the bean
		final BeanDialog dialog = new BeanDialog(shell, bean,
				this.getClass().getClassLoader());
		dialog.setBlockOnOpen(false);
		dialog.open();

		// Specify property values
		Composite dialogArea = (Composite) dialog.getDialogArea();
		Control[] children = dialogArea.getChildren();
		
		// Name
		((Text)children[1]).setText("name");
		((Text)children[4]).setText("1");
		((Text)children[7]).setText(MockBean.class.getName());
		
		// Flag close
		dialog.okPressed();

		// Validate the bean is populated
		assertEquals("Invalid name", "name", bean.getName());
		assertEquals("Incorrect count", 1, bean.getCount());
		assertEquals("Incorrect class", MockBean.class, bean.getClazz());
	}

}
