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

import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.common.javafx.structure.StructureLogger;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.preview.AdaptedEditorPreview;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.eclipse.ide.swt.SwtTheme;
import net.officefloor.model.Model;

/**
 * {@link TitleAreaDialog} for styling the {@link AdaptedModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelPreferenceStyler<M extends Model> {

	/**
	 * Parent {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * {@link AbstractItem}.
	 */
	private final AbstractItem<?, ?, ?, ?, M, ?> item;

	/**
	 * Prototype {@link Model} for the item.
	 */
	private final M prototype;

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
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Active {@link ItemStyleDialogue} for the {@link AbstractItem}.
	 */
	private ItemStyleDialogue itemStyleDialogue = null;

	/**
	 * Instantiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param prototype
	 *            Prototype {@link Model} for the item.
	 * @param itemLabel
	 *            Label for the item.
	 * @param isParent
	 *            Indicates if {@link AdaptedParent}.
	 * @param style
	 *            {@link Property} to receive changes to the style. Also, provides
	 *            the initial style.
	 */
	public ModelPreferenceStyler(Shell parentShell, AbstractItem<?, ?, ?, ?, M, ?> item, M prototype, String itemLabel,
			boolean isParent, Property<String> style) {
		this.parentShell = parentShell;
		this.item = item;
		this.prototype = prototype;
		this.itemLabel = itemLabel;
		this.isParent = isParent;
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
		private StyledText text;

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
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected Control createDialogArea(Composite parent) {

			// Default sizing
			int INDENT = 5;

			// Obtain the background color
			Color backgroundColour = SwtTheme.loadThemeColours(parent, false).get(SwtTheme.BACKGROUND_COLOR);

			// Create the area
			Composite area = (Composite) super.createDialogArea(parent);
			GridDataFactory.defaultsFor(area).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(area);

			// Easy access to styler
			ModelPreferenceStyler<M> styler = ModelPreferenceStyler.this;

			// Create container for contents
			Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.defaultsFor(container).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
			container.setLayout(new GridLayout(2, false));

			// Indicate details
			this.setTitle(styler.itemLabel);
			this.setMessage("JavaFx CSS rules for the item");

			// Provide the preview of the item
			FXCanvasEx canvas = new FXCanvasEx(container, SWT.NONE);
			GridDataFactory.defaultsFor(canvas).align(SWT.BEGINNING, SWT.BEGINNING).grab(false, false)
					.indent(INDENT, INDENT).applyTo(canvas);
			AdaptedEditorPreview<M> preview = new AdaptedEditorPreview<>(styler.prototype, styler.itemLabel,
					styler.isParent, (model, context) -> styler.item.visual(model, context));
			canvas.setScene(preview.getPreviewScene());
			canvas.getScene().setFill(backgroundColour);

			// Provide the structure of the item
			Text structureText = new Text(container, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			GridDataFactory.defaultsFor(structureText).align(SWT.FILL, SWT.BEGINNING).grab(true, false)
					.indent(INDENT, INDENT).applyTo(structureText);
			SwtTheme.autoHideScrollbars(structureText);
			try {
				StringWriter structure = new StringWriter();
				StructureLogger.log(preview.getPreviewVisual(), structure);
				structureText.setText(structure.toString());

			} catch (Exception ex) {
				// Indicate error in obtaining structure
				StringWriter error = new StringWriter();
				ex.printStackTrace(new PrintWriter(error));
				structureText.setText("Error loading structure\n\n" + error.toString());
			}

			// Provide means to change the styling
			this.text = new StyledText(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			GridDataFactory.defaultsFor(this.text).align(SWT.FILL, SWT.FILL).indent(INDENT, INDENT).grab(true, true)
					.span(2, 1).applyTo(this.text);
			SwtTheme.autoHideScrollbars(this.text);
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