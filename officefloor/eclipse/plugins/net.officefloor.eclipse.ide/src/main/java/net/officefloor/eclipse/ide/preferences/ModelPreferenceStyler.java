/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.ide.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import javafx.beans.property.Property;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.ide.editor.AbstractItem;

/**
 * {@link TitleAreaDialog} for styling the {@link AdaptedModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelPreferenceStyler {

	/**
	 * Parent {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * Label for the item.
	 */
	private final String itemLabel;

	/**
	 * {@link Property} to receive changes to the style. Also, provides the initial
	 * style.
	 */
	private final Property<String> style;

	/**
	 * Active {@link ItemStyleDialogue} for the {@link AbstractItem}.
	 */
	private ItemStyleDialogue itemStyleDialogue = null;

	/**
	 * Instantiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param itemLabel
	 *            Label for the item.
	 * @param style
	 *            {@link Property} to receive changes to the style. Also, provides
	 *            the initial style.
	 */
	public ModelPreferenceStyler(Shell parentShell, String itemLabel, Property<String> style) {
		this.parentShell = parentShell;
		this.itemLabel = itemLabel;
		this.style = style;
	}

	/**
	 * Opens the {@link ItemStyleDialogue}.
	 */
	public void open() {

		// Lazy display dialogue for styling
		if (this.itemStyleDialogue == null) {
			this.itemStyleDialogue = new ItemStyleDialogue();
			this.itemStyleDialogue.open();

			// Handle clearing on close (so can open again)
			this.itemStyleDialogue.getShell().addListener(SWT.Dispose, (event) -> this.itemStyleDialogue = null);
		}

		// Ensure gets focus on another open
		this.itemStyleDialogue.getShell().setFocus();
	}

	/**
	 * Provides means to update the styling for an {@link AbstractItem}.
	 */
	private class ItemStyleDialogue extends TitleAreaDialog {

		/**
		 * Displays the style.
		 */
		private Text text;

		/**
		 * Instantiate.
		 * 
		 * @param parentShell
		 *            Parent {@link Shell}.
		 * @param itemLabel
		 *            Label for the item.
		 * @param style
		 *            {@link Property} to receive changes to the style. Also, provides
		 *            the initial style.
		 * @param itemStyler
		 *            {@link ItemStyler} co-ordinating this.
		 */
		private ItemStyleDialogue() {
			super(ModelPreferenceStyler.this.parentShell);

			// Initialise dialogue to non-modal
			this.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
			this.setBlockOnOpen(false);

			// No help (yet)
			this.setHelpAvailable(false);
		}

		/*
		 * ============== Dialog ==================
		 */

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);

			// Easy access to styler
			ModelPreferenceStyler styler = ModelPreferenceStyler.this;

			// Create container for contents
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			container.setLayout(new FillLayout(SWT.VERTICAL));

			// Indicate details
			this.setTitle(styler.itemLabel);
			this.setMessage("JavaFx CSS rules for the item");

			// Provide means to change the styling
			this.text = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			String styleRules = styler.style.getValue();
			if ((styleRules != null) && (styleRules.trim().length() > 0)) {
				this.text.setText(styleRules);
			}

			// Return the container
			return container;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
			createButton(parent, IDialogConstants.OK_ID, "Apply", true);
		}

		@Override
		protected void okPressed() {

			// Update the style
			ModelPreferenceStyler.this.style.setValue(this.text.getText());

			// Close the dialog
			super.okPressed();
		}

	}

}