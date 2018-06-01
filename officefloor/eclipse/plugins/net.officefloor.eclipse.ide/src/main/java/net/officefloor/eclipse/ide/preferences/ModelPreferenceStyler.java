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

import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
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
import javafx.embed.swt.FXCanvas;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.common.javafx.structure.StructureLogger;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.preview.AdaptedEditorPreview;
import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.eclipse.ide.javafx.JavaFxUtil;
import net.officefloor.eclipse.ide.javafx.JavaFxUtil.CssManager;
import net.officefloor.eclipse.ide.swt.SwtUtil;
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
	 * Default style.
	 */
	private final String defaultStyle;

	/**
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Preferences to change.
	 */
	private final Map<String, String> preferencesToChange;

	/**
	 * Active {@link StyleDialogue} for the {@link AbstractItem}.
	 */
	private StyleDialogue styleDialogue = null;

	/**
	 * Instantiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param item
	 *            {@link AbstractItem}.
	 * @param prototype
	 *            Prototype {@link Model} for the item.
	 * @param itemLabel
	 *            Label for the item.
	 * @param isParent
	 *            Indicates if {@link AdaptedParent}.
	 * @param style
	 *            {@link Property} to receive changes to the style. Also, provides
	 *            the initial style.
	 * @param defaultStyle
	 *            Default style.
	 * @param preferencesToChange
	 *            {@link Map} to load with the {@link IPreferenceStore} changes.
	 */
	public ModelPreferenceStyler(Shell parentShell, AbstractItem<?, ?, ?, ?, M, ?> item, M prototype, String itemLabel,
			boolean isParent, Property<String> style, String defaultStyle, Map<String, String> preferencesToChange) {
		this.parentShell = parentShell;
		this.item = item;
		this.prototype = prototype;
		this.itemLabel = itemLabel;
		this.isParent = isParent;
		this.style = style;
		this.defaultStyle = defaultStyle == null ? "" : defaultStyle;
		this.preferencesToChange = preferencesToChange;
	}

	/**
	 * Opens the {@link StyleDialogue}.
	 */
	public void open() {

		// Lazy display dialogue for styling
		if (this.styleDialogue == null) {
			this.styleDialogue = new StyleDialogue();
			this.styleDialogue.open();

			// Handle clearing on close (so can open again)
			this.styleDialogue.getShell().addListener(SWT.Dispose, (event) -> this.styleDialogue = null);
		}

		// Ensure gets focus on another open
		this.styleDialogue.getShell().setFocus();
	}

	/**
	 * Provides means to update the styling for an {@link AbstractItem}.
	 */
	private class StyleDialogue extends TitleAreaDialog {

		/**
		 * {@link FXCanvas} for the preview.
		 */
		private FXCanvas canvas;

		/**
		 * Displays the style.
		 */
		private StyledText text;

		/**
		 * Instantiate.
		 */
		private StyleDialogue() {
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
			Color backgroundColour = SwtUtil.loadThemeColours(parent, false).get(SwtUtil.BACKGROUND_COLOR);

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
			this.canvas = new FXCanvasEx(container, SWT.NONE);
			GridDataFactory.defaultsFor(this.canvas).align(SWT.BEGINNING, SWT.BEGINNING).grab(false, false)
					.indent(INDENT, INDENT).applyTo(canvas);
			AdaptedEditorPreview<M> preview = new AdaptedEditorPreview<>(styler.prototype, styler.itemLabel,
					styler.isParent, (model, context) -> styler.item.visual(model, context));
			this.canvas.setScene(preview.getPreviewScene());
			this.canvas.getScene().setFill(backgroundColour);

			// Provide the structure of the item
			Text structureText = new Text(container, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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
			SwtUtil.autoHideScrollbars(structureText);
			GridDataFactory.defaultsFor(structureText).align(SWT.FILL, SWT.BEGINNING).grab(true, false)
					.indent(INDENT, INDENT).applyTo(structureText);

			// Provide CSS errors
			CssManager cssManager = JavaFxUtil.createCssManager(container, preview.getPreviewScene(), preview.style());
			GridDataFactory.defaultsFor(cssManager.getControl()).align(SWT.FILL, SWT.TOP).indent(INDENT, INDENT)
					.span(2, 1).applyTo(cssManager.getControl());

			// Obtain the initial styling
			String initialStyle = styler.style.getValue();

			// Provide means to change the styling
			this.text = new StyledText(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			cssManager.registerText(this.text, initialStyle,
					(rawStyle) -> AbstractIdeEditor.translateStyle(rawStyle, styler.item));
			SwtUtil.autoHideScrollbars(this.text);
			GridDataFactory.defaultsFor(this.text).align(SWT.FILL, SWT.FILL).indent(INDENT, INDENT).grab(true, true)
					.span(2, 1).applyTo(this.text);

			// Load the initial styling of preview
			cssManager.loadStyle(initialStyle,
					(rawStyle) -> AbstractIdeEditor.translateStyle(initialStyle, styler.item));

			// Return the container
			return container;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
			createButton(parent, IDialogConstants.CLIENT_ID + 1, "Restore Default", false).addListener(SWT.Selection,
					(event) -> {
						// Reset to default
						this.text.setText(ModelPreferenceStyler.this.defaultStyle);
					});
			createButton(parent, IDialogConstants.OK_ID, "Apply and Close", true);
		}

		@Override
		protected void okPressed() {

			// Update the style within preference page
			String style = this.text.getText();
			ModelPreferenceStyler.this.style.setValue(style);

			// Update to preferences (taking into account default style)
			String styleId = ModelPreferenceStyler.this.item.getPreferenceStyleId();
			if (ModelPreferenceStyler.this.defaultStyle.equals(style)) {
				style = ""; // reset to default
			}
			ModelPreferenceStyler.this.preferencesToChange.put(styleId, style);

			// Close the dialog
			super.okPressed();
		}
	}

}