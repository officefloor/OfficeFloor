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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.jws.WebParam.Mode;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.common.javafx.structure.StructureLogger;
import net.officefloor.eclipse.editor.AdaptedEditorPlugin;
import net.officefloor.eclipse.editor.AdaptedModelStyler;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.ContentStyler;
import net.officefloor.eclipse.editor.PaletteIndicatorStyler;
import net.officefloor.eclipse.editor.PaletteStyler;
import net.officefloor.eclipse.editor.SelectOnly;
import net.officefloor.eclipse.editor.preview.AdaptedEditorPreview;
import net.officefloor.eclipse.ide.OfficeFloorIdePlugin;
import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.eclipse.ide.editor.AbstractItem.IdeChildrenGroup;
import net.officefloor.eclipse.ide.editor.AbstractItem.IdeLabeller;
import net.officefloor.model.Model;

/**
 * {@link IWorkbenchPreferencePage}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("restriction")
public class OfficeFloorIdePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * {@link IWorkbench}.
	 */
	private IWorkbench workbench;

	/**
	 * {@link AbstractIdeEditor} instances.
	 */
	private AbstractIdeEditor<?, ?, ?>[] editors = null;

	/**
	 * Currently displaying {@link ItemStructureDialogue}.
	 */
	private ItemStructureDialogue itemStructureDialogue = null;

	/**
	 * Instantiate.
	 */
	public OfficeFloorIdePreferencePage() {
		this.setPreferenceStore(OfficeFloorIdePlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Loads the preference page.
	 * 
	 * @param parent
	 *            Parent.
	 */
	protected void loadPreferencePage(Composite parent) {

		// Load preferences
		this.getPreferenceStore().setDefault("TEST", "default");
		String result = this.getPreferenceStore().getString("TEST");
		System.out.println("RESULT: " + result);
		this.getPreferenceStore().addPropertyChangeListener((event) -> {
			System.out.println("PROPERTY CHANGE: " + event.getProperty() + " from " + event.getOldValue() + " to "
					+ event.getNewValue());
		});
		this.getPreferenceStore().setValue("TEST", "different");

		// Sort the editors (too keep deterministic in order displayed)
		Arrays.sort(this.editors);

		// Obtain the default styling
		String defaultStyleSheet = AdaptedEditorPlugin.getDefaultStyleSheet();

		// Create tabs for each editor
		TabFolder editors = new TabFolder(parent, SWT.BORDER | SWT.INHERIT_FORCE);
		editors.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Allow configurations for each editor
		for (AbstractIdeEditor<?, ?, ?> editor : this.editors) {

			// Indicate the editor
			final String editorName = editor.getClass().getSimpleName();

			// Create tab for editor
			TabItem editorTab = new TabItem(editors, SWT.NONE);
			editorTab.setText(editorName);

			// Load the editor to configure preferences
			try {

				// Create the root model
				Model rootModel = editor.prototype();

				// Initialise the editor
				IEditorSite editorSite = new PreferencesEditorSite(editorName, this.workbench, parent.getShell());
				IEditorInput editorInput = new PreferencesEditorInput(editorName, rootModel);
				editor.init(editorSite, editorInput);

				// Provide select only for styling
				Map<Model, ModelPreferenceStyler> modelStylers = new HashMap<>();
				Map<Class<? extends Model>, ModelPreferenceStyler> parentStylers = new HashMap<>();
				editor.setSelectOnly(new SelectOnly() {

					@Override
					public void paletteIndicator(PaletteIndicatorStyler styler) {
						System.out.println("palette indicator selected");
					}

					@Override
					public void palette(PaletteStyler styler) {
						System.out.println("palette selected");
					}

					@Override
					public void content(ContentStyler contentStyler) {
						System.out.println("content selected");
					}

					@Override
					public void model(AdaptedModelStyler styler) {
						// Open styling for the model
						Model model = styler.getModel();
						ModelPreferenceStyler modelStyler = modelStylers.get(model);
						if (modelStyler == null) {
							modelStyler = parentStylers.get(model.getClass());
						}
						modelStyler.open();
					}
				});

				// Display the editor
				editor.createPartControl(editors);
				editorTab.setControl(editor.getCanvas());

				// Load the prototype of all models
				AbstractItem<?, ?, ?, ?, ?, ?>[] parentItems = editor.getParents();
				for (int i = 0; i < parentItems.length; i++) {
					AbstractItem<?, ?, ?, ?, ?, ?> parentItem = parentItems[i];

					// Load the prototype model
					final int index = i;
					this.loadPrototypeModel(rootModel, parentItem, true, modelStylers, parent.getShell(),
							(model, styler) -> {
								// Space out the prototypes
								model.setX(300);
								model.setY(10 + (100 * index));

								// Register parents (prototypes in palette different instance)
								parentStylers.put(model.getClass(), styler);
							});
				}

			} catch (Throwable ex) {

				// Dispose the canvas if created
				FXCanvas canvas = editor.getCanvas();
				if (canvas != null) {
					canvas.dispose();
				}

				// Indicate failure to load editor
				Text error = new Text(editors, SWT.MULTI);
				StringWriter stackTrace = new StringWriter();
				ex.printStackTrace(new PrintWriter(stackTrace));
				error.setText("Failed to load editor for configuring.\n\n" + stackTrace.toString());
				error.setEditable(false);
				editorTab.setControl(error);
			}
		}
	}

	/**
	 * Recursively loads the prototype {@link Model}.
	 * 
	 * @param parentModel
	 *            Parent {@link Model}.
	 * @param item
	 *            {@link AbstractItem} to have its prototype loaded into the
	 *            {@link Model}.
	 * @param isParent
	 *            Indicate if {@link AdaptedParent}.
	 * @param modelStylers
	 *            {@link Map} of {@link Model} to {@link ModelPreferenceStyler} to
	 *            be populated.
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param decorator
	 *            Decorator on the {@link Model}.
	 * @return Prototype {@link Mode} from the {@link AbstractItem}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Model loadPrototypeModel(Model parentModel, AbstractItem item, boolean isParent,
			Map<Model, ModelPreferenceStyler> modelStylers, Shell parentShell,
			BiConsumer<Model, ModelPreferenceStyler> decorator) {

		// Obtain the prototype for the item
		Model itemModel = item.prototype();

		// Obtain label for the item
		IdeLabeller labeller = item.label();
		String itemName = (labeller == null) ? null : labeller.getLabel(itemModel);
		if ((itemName == null) || (itemName.trim().length() == 0)) {
			itemName = item.getClass().getSimpleName();
		}
		final String itemLabel = itemName;

		// Obtain configuration path identifying the item
		String configurationPath = item.getBuilder().getConfigurationPath();

		// Obtain the style property to change the appearance
		Property<String> style = item.getBuilder().style();

		// Obtain the styling
		String defaultStyle = item.style();
		String overrideStyle = this.getPreferenceStore().getString(configurationPath);

		// Create the style property
		Property<String> rawStyle = new SimpleStringProperty(
				(overrideStyle != null) && (overrideStyle.trim().length() > 0) ? overrideStyle : defaultStyle);
		rawStyle.addListener((event, oldValue, newValue) -> {
			// Translate and update the style
			String translatedStyle = AbstractIdeEditor.translateStyle(newValue, item);
			javafx.application.Platform.runLater(() -> {
				// Ensure on JavaFx thread to update style
				style.setValue(translatedStyle);
			});
		});

		// Create and register the model styler
		ModelPreferenceStyler styler = new ModelPreferenceStyler(parentShell, item, itemModel, itemLabel, isParent,
				rawStyle);
		modelStylers.put(itemModel, styler);

		// Determine if decorate the model
		if (decorator != null) {
			decorator.accept(itemModel, styler);
		}

		// Connect into the model
		item.loadToParent(parentModel, itemModel);

		// Load child items
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem child : childrenGroup.getChildren()) {
				this.loadPrototypeModel(itemModel, child, false, modelStylers, parentShell, null);
			}
		}

		// Return the item model
		return itemModel;
	}

	/**
	 * Loads the item and its children.
	 * 
	 * @param item
	 *            {@link AbstractItem}.
	 * @param depth
	 *            Depth of the {@link AbstractItem}.
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param backgroundColour
	 *            Background {@link Color}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadItem(AbstractItem item, int depth, Composite parent, Color backgroundColour) {

		// Create row for item
		Composite row = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginLeft = 20 * depth;
		row.setLayout(rowLayout);

		// Obtain details for the item
		Model prototype = item.prototype();
		IdeLabeller labeller = item.label();
		String itemName = (labeller == null) ? null : labeller.getLabel(prototype);
		if ((itemName == null) || (itemName.trim().length() == 0)) {
			itemName = item.getClass().getSimpleName();
		}
		final String itemLabel = itemName;

		// Provide the view of the item
		FXCanvasEx canvas = new FXCanvasEx(row, SWT.NONE);
		boolean isParent = (depth == 1);
		AdaptedEditorPreview preview = new AdaptedEditorPreview(prototype, itemName, isParent,
				(model, context) -> item.visual(model, context));
		canvas.setScene(preview.getPreviewScene());
		canvas.getScene().setFill(backgroundColour);

		// Click on canvas to display structure
		canvas.setToolTipText("Click to display item's JavaFx structure");
		canvas.addListener(SWT.MouseDown, (event) -> this.displayItemStructure(itemLabel, preview.getPreviewVisual()));

		// Obtain the defaulting styling
		String defaultStylingRules = item.style();

		// TODO load override styling from preferences
		defaultStylingRules = item.getBuilder().getConfigurationPath();

		// Obtain the styling
		Property<String> style = preview.style();

		// Detail the styling
		Text styling = new Text(row, SWT.MULTI);
		styling.setEditable(false);
		if ((defaultStylingRules == null) || (defaultStylingRules.trim().length() == 0)) {
			// No styling
			styling.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
			styling.setText("no styling");
		} else {
			// Display the styling
			styling.setText(defaultStylingRules);

			// Translate and style
			String translatedStyle = AbstractIdeEditor.translateStyle(defaultStylingRules, item);
			style.setValue(translatedStyle);
		}

		// Provide means to change the styling
		// ItemStyler styler = new ItemStyler(parent.getShell(), itemLabel, new
		// SimpleStringProperty(defaultStylingRules));
		// styling.addListener(SWT.MouseDown, (event) -> {
		// styler.open();
		// });

		// Load the children
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem child : childrenGroup.getChildren()) {
				this.loadItem(child, depth + 1, parent, backgroundColour);
			}
		}
	}

	/*
	 * ================ IWorkbenchPreferencePage =================
	 */

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}

	@Override
	protected Control createContents(Composite parent) {

		// Provide container
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(1, false));

		// Provide progress on loading editors
		Composite progressContainer = new Composite(container, SWT.NONE);
		progressContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		progressContainer.setLayout(new GridLayout(2, false));
		Label label = new Label(progressContainer, SWT.NONE);
		label.setText("Loading editors  ");
		ProgressBar progress = new ProgressBar(progressContainer, SWT.NONE);
		progress.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		// Load the editors
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.ui.editors");
		progress.setMaximum(elements.length);
		this.loadEditors(elements, progress, progressContainer, container, parent.getDisplay());

		// Return the container
		return container;
	}

	/**
	 * Loads the {@link AbstractIdeEditor} instances.
	 */
	private void loadEditors(IConfigurationElement[] elements, ProgressBar progress, Composite progressContainer,
			Composite container, Display display) {

		// Loads the editors in background thread
		new Thread(() -> {

			// Load the editors
			List<AbstractIdeEditor<?, ?, ?>> editors = new LinkedList<>();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element = elements[i];

				// Obtain the class
				try {
					Object extension = element.createExecutableExtension("class");
					if (extension instanceof AbstractIdeEditor) {
						editors.add((AbstractIdeEditor<?, ?, ?>) extension);
					}

				} catch (CoreException ex) {
					// Ignore failed to load editors
				}

				// Update progress
				final int progressSelection = i;
				display.asyncExec(() -> {
					if (progress.isDisposed()) {
						return;
					}
					progress.setSelection(progressSelection);
				});
			}

			// Create the editors
			final AbstractIdeEditor<?, ?, ?>[] finalEditors;
			synchronized (OfficeFloorIdePreferencePage.this) {
				finalEditors = editors.toArray(new AbstractIdeEditor[editors.size()]);
			}

			// Display the editor preferences
			display.asyncExec(() -> {

				// Dispose of progress
				if (!progressContainer.isDisposed()) {
					progressContainer.dispose();
				}

				// Ensure container still available
				if (container.isDisposed()) {
					return;
				}

				// Load the editors
				synchronized (OfficeFloorIdePreferencePage.this) {
					OfficeFloorIdePreferencePage.this.editors = finalEditors;
				}

				// Load the preference page
				OfficeFloorIdePreferencePage.this.loadPreferencePage(container);

				// Render the changes
				container.layout();
			});
		}).start();
	}

	/**
	 * Displays the {@link AbstractItem} structure.
	 * 
	 * @param itemLabel
	 *            Label for the {@link AbstractItem}.
	 * @param itemVisual
	 *            Visual for the {@link AbstractItem}.
	 */
	private void displayItemStructure(String itemLabel, Node itemVisual) {

		// Lazy display dialogue for display structure
		if (this.itemStructureDialogue == null) {
			this.itemStructureDialogue = new ItemStructureDialogue(this.getShell());
			this.itemStructureDialogue.open();

			// Handle clearing on close (so can open again)
			this.itemStructureDialogue.getShell().addListener(SWT.Dispose,
					(event) -> this.itemStructureDialogue = null);
		}

		// Display structure
		this.itemStructureDialogue.displayStructure(itemLabel, itemVisual);
		this.itemStructureDialogue.getShell().setFocus();
	}

	/**
	 * Provides structure of the {@link AbstractItem}.
	 */
	private class ItemStructureDialogue extends TitleAreaDialog {

		/**
		 * Displays the structure.
		 */
		private Text text;

		/**
		 * Instantiate.
		 * 
		 * @param parentShell
		 *            Parent {@link Shell}.
		 */
		private ItemStructureDialogue(Shell parentShell) {
			super(parentShell);
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
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			container.setLayout(new FillLayout());
			this.text = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.text.setEditable(false);
			return container;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
		}

		/**
		 * Displays a new {@link AbstractItem}.
		 * 
		 * @param itemLabel
		 *            Label for the {@link AbstractItem}.
		 * @param itemVisual
		 *            {@link Parent} for the {@link AbstractItem}.
		 */
		private void displayStructure(String itemLabel, Node itemVisual) {

			// Update title of dialogue to new item
			this.setTitle(itemLabel);
			this.setMessage("JavaFx structure to aid styling");

			// Indicate structure
			try {
				StringWriter structure = new StringWriter();
				StructureLogger.log(itemVisual, structure);
				this.text.setText(structure.toString());

			} catch (Exception ex) {
				// Indicate error in obtaining structure
				StringWriter error = new StringWriter();
				ex.printStackTrace(new PrintWriter(error));
				this.text.setText("Error loading structure\n\n" + error.toString());
			}
		}
	}

}