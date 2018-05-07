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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.Scene;
import net.officefloor.eclipse.common.javafx.structure.StructureLogger;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.eclipse.ide.javafx.JavaFxUtil;
import net.officefloor.eclipse.ide.javafx.JavaFxUtil.CssManager;
import net.officefloor.eclipse.ide.swt.SwtUtil;

/**
 * {@link TitleAreaDialog} for styling a particular {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public class NodePreferenceStyler {

	/**
	 * Title.
	 */
	private final String title;

	/**
	 * Message.
	 */
	private final String message;

	/**
	 * {@link Node} to extract the structure.
	 */
	private final Node node;

	/**
	 * Identifier within the {@link IPreferenceStore} for the style.
	 */
	private final String preferenceStyleId;

	/**
	 * {@link Property} to receive changes to the style. Also, provides the initial
	 * style.
	 */
	private final Property<String> style;

	/**
	 * Default style.
	 */
	private final String defaultStyle;

	/**
	 * Preferences to change.
	 */
	private final Map<String, String> preferencesToChange;

	/**
	 * {@link Scene}.
	 */
	private final Scene scene;

	/**
	 * Parent {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * Active {@link StyleDialogue} for the {@link AbstractItem}.
	 */
	private StyleDialogue styleDialogue = null;

	/**
	 * Instantiate.
	 * 
	 * @param title
	 *            Title.
	 * @param message
	 *            Message.
	 * @param node
	 *            {@link Node} to extract the structure.
	 * @param preferenceStyleId
	 *            Identifier within the {@link IPreferenceStore} for the style.
	 * @param style
	 *            {@link Property} to receive changes to the style. Also, provides
	 *            the initial style.
	 * @param defaultStyle
	 *            Default style.
	 * @param preferencesToChange
	 *            Preferences to change.
	 * @param scene
	 *            {@link Scene}.
	 * @param parentShell
	 *            Parent {@link Shell}.
	 */
	public NodePreferenceStyler(String title, String message, Node node, String preferenceStyleId,
			Property<String> style, String defaultStyle, Map<String, String> preferencesToChange, Scene scene,
			Shell parentShell) {
		this.title = title;
		this.message = message;
		this.node = node;
		this.preferenceStyleId = preferenceStyleId;
		this.style = style;
		this.defaultStyle = defaultStyle;
		this.preferencesToChange = preferencesToChange;
		this.scene = scene;
		this.parentShell = parentShell;
	}

	/**
	 * Opens the {@link StyleDialogue}.
	 */
	public void open() {

		// Lazy display dialogue for styling
		if (this.styleDialogue == null) {

			// Create the style dialogue (with cancel to current style)
			this.styleDialogue = new StyleDialogue(this.style.getValue());
			this.styleDialogue.open();

			// Handle clearing on close (so can open again)
			this.styleDialogue.getShell().addListener(SWT.Dispose, (event) -> this.styleDialogue = null);
		}

		// Ensure gets focus on another open
		this.styleDialogue.getShell().setFocus();
	}

	/**
	 * Provides means to update the styling for the content.
	 */
	private class StyleDialogue extends TitleAreaDialog {

		/**
		 * Style to reset to on canceling.
		 */
		private final String cancelStyle;

		/**
		 * Displays the style.
		 */
		private StyledText text;

		/**
		 * Instantiate.
		 * 
		 * @param cancelStyle
		 *            Style to reset to on canceling.
		 */
		private StyleDialogue(String cancelStyle) {
			super(NodePreferenceStyler.this.parentShell);
			this.cancelStyle = cancelStyle;

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
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected Control createDialogArea(Composite parent) {

			// Default sizing
			int INDENT = 5;

			// Create the area
			Composite area = (Composite) super.createDialogArea(parent);
			GridDataFactory.defaultsFor(area).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(area);

			// Easy access to styler
			NodePreferenceStyler styler = NodePreferenceStyler.this;

			// Create container for contents
			Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.defaultsFor(container).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
			container.setLayout(new GridLayout(1, true));

			// Indicate details
			this.setTitle(styler.title);
			this.setMessage(styler.message);

			// Provide the structure of the node
			Text structureText = new Text(container, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			try {
				StringWriter structure = new StringWriter();
				StructureLogger.log(styler.node, structure);
				structureText.setText(structure.toString());

			} catch (Exception ex) {
				// Indicate error in obtaining structure
				StringWriter error = new StringWriter();
				ex.printStackTrace(new PrintWriter(error));
				structureText.setText("Error loading structure\n\n" + error.toString());
			}
			SwtUtil.autoHideScrollbars(structureText);
			GridDataFactory.defaultsFor(structureText).align(SWT.FILL, SWT.BEGINNING).grab(true, false)
					.indent(INDENT, INDENT).applyTo(structureText);

			// Provide CSS errors
			CssManager cssManager = JavaFxUtil.createCssManager(container, styler.scene, styler.style);
			GridDataFactory.defaultsFor(cssManager.getControl()).align(SWT.FILL, SWT.TOP).indent(INDENT, INDENT)
					.span(2, 1).applyTo(cssManager.getControl());

			// Obtain the initial styling
			String initialStyle = styler.style.getValue();

			// Provide means to change the styling
			this.text = new StyledText(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			cssManager.registerText(this.text, initialStyle, null);
			SwtUtil.autoHideScrollbars(this.text);
			GridDataFactory.defaultsFor(this.text).align(SWT.FILL, SWT.FILL).indent(INDENT, INDENT).grab(true, true)
					.span(2, 1).applyTo(this.text);

			// Return the container
			return container;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
			createButton(parent, IDialogConstants.CLIENT_ID + 1, "Restore Default", false).addListener(SWT.Selection,
					(event) -> {
						// Reset to default
						this.text.setText(NodePreferenceStyler.this.defaultStyle);
					});
			createButton(parent, IDialogConstants.OK_ID, "Apply and Close", true);
		}

		@Override
		protected void cancelPressed() {

			// Reset to cancel style
			NodePreferenceStyler.this.style.setValue(this.cancelStyle);

			// Continue cancel
			super.cancelPressed();
		}

		@Override
		protected void okPressed() {

			// Easy access to styler
			NodePreferenceStyler styler = NodePreferenceStyler.this;

			// Update the style within preference page
			String style = this.text.getText();
			styler.style.setValue(style);

			// Update to preferences (taking into account default style)
			if (NodePreferenceStyler.this.defaultStyle.equals(style)) {
				style = ""; // reset to default
			}
			styler.preferencesToChange.put(styler.preferenceStyleId, style);

			// Close the dialog
			super.okPressed();
		}
	}

}