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
package net.officefloor.eclipse.wizard.template;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * {@link Dialog} to select a {@link HttpTemplateExtensionSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class SelectHttpTemplateExtensionSourceInstanceDialog extends ListDialog {

	/**
	 * Initiate.
	 * 
	 * @param parent
	 *            {@link Shell}.
	 * @param instances
	 *            {@link HttpTemplateExtensionSourceInstance} listing to select
	 *            from.
	 */
	public SelectHttpTemplateExtensionSourceInstanceDialog(Shell shell,
			final HttpTemplateExtensionSourceInstance[] instances) {
		super(shell);

		// Specify title
		this.setTitle("Add extension");
		this.setMessage("Select the extension to add");
		
		// Provide the input
		this.setInput(instances);

		// Provide the content
		this.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Input should not change
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return instances;
			}

			@Override
			public void dispose() {
				// Nothing to dispose
			}
		});

		// Provide the labels
		this.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				HttpTemplateExtensionSourceInstance instance = (HttpTemplateExtensionSourceInstance) element;
				return instance.getWoofTemplateExtensionSourceClassName();
			}
		});
		
		// Ensure selection made
		this.setBlockOnOpen(true);
	}

	
}