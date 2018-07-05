/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.embed.swt.FXCanvas;
import net.officefloor.eclipse.editor.AdaptedModelStyler;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.EditorStyler;
import net.officefloor.eclipse.editor.PaletteIndicatorStyler;
import net.officefloor.eclipse.editor.PaletteStyler;
import net.officefloor.eclipse.editor.SelectOnly;
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OfficeFloorIdePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OfficeFloorIdePreferencePage.class);

	/**
	 * {@link IWorkbench}.
	 */
	private IWorkbench workbench;

	/**
	 * {@link EditorWrapper} instances.
	 */
	private EditorWrapper[] editors = null;

	/**
	 * {@link TabFolder} for the {@link EditorWrapper} instances.
	 */
	private TabFolder editorTabs;

	/**
	 * Obtains the active {@link EditorWrapper}.
	 * 
	 * @return Active {@link EditorWrapper}.
	 */
	private EditorWrapper getActiveEditor() {
		TabItem editorTab = this.editorTabs.getSelection()[0];
		return (EditorWrapper) editorTab.getData();
	}

	/**
	 * Loads the preference page.
	 * 
	 * @param parent Parent.
	 */
	protected void loadPreferencePage(Composite parent) {

		// Sort the editors (too keep deterministic in order displayed)
		Arrays.sort(this.editors);

		// Create tabs for each editor
		this.editorTabs = new TabFolder(parent, SWT.BORDER | SWT.INHERIT_FORCE);
		this.editorTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Obtain the parent shell
		Shell parentShell = parent.getShell();

		// Allow configurations for each editor
		for (EditorWrapper wrapper : this.editors) {
			AbstractIdeEditor<?, ?, ?> editor = wrapper.ideEditor;

			// Create tab for editor
			TabItem editorTab = new TabItem(this.editorTabs, SWT.INHERIT_FORCE);
			editorTab.setText(wrapper.editorName);
			wrapper.setTabItem(editorTab);

			// Load the editor to configure preferences
			try {

				// Create the root model
				Model rootModel = editor.prototype();

				// Initialise the editor
				IEditorSite editorSite = new PreferencesEditorSite(wrapper.editorName, this.workbench,
						parent.getShell());
				IEditorInput editorInput = new PreferencesEditorInput(wrapper.editorName, rootModel);
				editor.init(editorSite, editorInput);

				// Provide select only for styling
				Map<Model, ModelPreferenceStyler> modelStylers = new HashMap<>();
				Map<Class<? extends Model>, ModelPreferenceStyler> parentStylers = new HashMap<>();
				editor.setSelectOnly(new SelectOnly() {

					private NodePreferenceStyler paletteIndicator = null;

					private NodePreferenceStyler palette = null;

					private NodePreferenceStyler content = null;

					@Override
					public void paletteIndicator(PaletteIndicatorStyler styler) {
						if (this.paletteIndicator == null) {
							this.paletteIndicator = new NodePreferenceStyler("Palette Indicator",
									"JavaFx CSS rules for the palette indicator", styler.getPaletteIndicator(),
									editor.getPaletteIndicatorStyleId(), styler.paletteIndicatorStyle(),
									editor.paletteIndicatorStyle(), wrapper.preferencesToChange,
									editor.getCanvas().getScene(), parentShell);
						}
						this.paletteIndicator.open();
					}

					@Override
					public void palette(PaletteStyler styler) {
						if (this.palette == null) {
							this.palette = new NodePreferenceStyler("Palette", "JavaFx CSS rules for the palette",
									styler.getPalette(), editor.getPaletteStyleId(), styler.paletteStyle(),
									editor.paletteStyle(), wrapper.preferencesToChange, editor.getCanvas().getScene(),
									parentShell);
						}
						this.palette.open();
					}

					@Override
					public void editor(EditorStyler styler) {
						if (this.content == null) {
							this.content = new NodePreferenceStyler("Editor", "JavaFx CSS rules for the editor",
									styler.getEditor(), editor.getEditorStyleId(), styler.editorStyle(),
									editor.editorStyle(), wrapper.preferencesToChange, editor.getCanvas().getScene(),
									parentShell);
						}
						this.content.open();
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
				editor.createPartControl(this.editorTabs);
				editorTab.setControl(editor.getCanvas());

				// Load the prototype of all models
				AbstractItem<?, ?, ?, ?, ?, ?>[] parentItems = editor.getParents();
				for (int i = 0; i < parentItems.length; i++) {
					AbstractItem<?, ?, ?, ?, ?, ?> parentItem = parentItems[i];

					// Load the prototype model
					final int index = i;
					this.loadPrototypeModel(rootModel, parentItem, true, modelStylers, wrapper, parent.getShell(),
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
				Text error = new Text(this.editorTabs, SWT.MULTI);
				StringWriter stackTrace = new StringWriter();
				ex.printStackTrace(new PrintWriter(stackTrace));
				error.setText("Failed to load editor for configuring.\n\n" + stackTrace.toString());
				error.setEditable(false);
				editorTab.setControl(error);
			}
		}

		// Let the active editor handle being active
		try {
			EditorWrapper activeEditor = this.getActiveEditor();
			if (activeEditor != null) {
				activeEditor.handleBecomingActive();
			}
		} catch (Throwable ex) {
			// Best attempt to handle becoming active
		}
	}

	/**
	 * Recursively loads the prototype {@link Model}.
	 * 
	 * @param parentModel   Parent {@link Model}.
	 * @param item          {@link AbstractItem} to have its prototype loaded into
	 *                      the {@link Model}.
	 * @param isParent      Indicate if {@link AdaptedParent}.
	 * @param modelStylers  {@link Map} of {@link Model} to
	 *                      {@link ModelPreferenceStyler} to be populated.
	 * @param editorWrapper {@link EditorWrapper}.
	 * @param parentShell   Parent {@link Shell}.
	 * @param decorator     Decorator on the {@link Model}.
	 * @return Prototype {@link Model} from the {@link AbstractItem}.
	 */
	private Model loadPrototypeModel(Model parentModel, AbstractItem item, boolean isParent,
			Map<Model, ModelPreferenceStyler> modelStylers, EditorWrapper editorWrapper, Shell parentShell,
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

		// Obtain preference style identifier
		String preferenceStyleId = item.getPreferenceStyleId();

		// Obtain the style property to change the appearance
		Property<String> style = item.getBuilder().style();

		// Obtain the styling
		IPreferenceStore preferences = this.getPreferenceStore();
		String defaultStyle = item.style();
		String overrideStyle = preferences.getString(preferenceStyleId);

		// Create the style property
		Property<String> rawStyle = new SimpleStringProperty(
				(overrideStyle != null) && (overrideStyle.trim().length() > 0) ? overrideStyle : defaultStyle);
		rawStyle.addListener((event, oldValue, newValue) -> {
			// Translate and update the style
			String translatedStyle = AbstractIdeEditor.translateStyle(newValue, item);
			style.setValue(translatedStyle);
		});
		preferences.addPropertyChangeListener((event) -> {
			// Update style on preference changes (typically reseting to defaults)
			if (preferenceStyleId.equals(event.getProperty())) {
				String updatedStyle = preferences.getString(preferenceStyleId);
				String newRawStyle = (updatedStyle != null) && (updatedStyle.trim().length() > 0) ? updatedStyle
						: defaultStyle;
				rawStyle.setValue(newRawStyle);
			}
		});

		// Create and register the model styler
		ModelPreferenceStyler styler = new ModelPreferenceStyler(parentShell, item, itemModel, itemLabel, isParent,
				rawStyle, defaultStyle, editorWrapper.preferencesToChange);
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
				this.loadPrototypeModel(itemModel, child, false, modelStylers, editorWrapper, parentShell, null);
			}
		}

		// Return the item model
		return itemModel;
	}

	/*
	 * ================ IWorkbenchPreferencePage =================
	 */

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return OfficeFloorIdePlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected void performDefaults() {

		// Obtain the active editor
		EditorWrapper editor = this.getActiveEditor();

		// Ensure want to reset to defaults
		if (!MessageDialog.openConfirm(this.getShell(), "Reset defaults",
				"Please confirm you want to reset defaults for editor " + editor.editorName
						+ ".\n\nThis can not be undone.")) {
			return; // do not reset
		}

		// Restore defaults for editor
		IPreferenceStore preferences = this.getPreferenceStore();
		editor.visitPreferences((preferenceId) -> {
			preferences.setToDefault(preferenceId);
		});
		editor.preferencesToChange.clear();

		// Update as the active editor
		editor.handleBecomingActive();

		// Continue defaults
		super.performDefaults();
	}

	@Override
	protected void performApply() {

		// Obtain the active editor
		EditorWrapper editor = this.getActiveEditor();

		// Apply preference changes for active editor
		for (String name : editor.preferencesToChange.keySet()) {
			String value = editor.preferencesToChange.get(name);
			this.getPreferenceStore().setValue(name, value);
		}
		editor.preferencesToChange.clear();

		// Update as the active editor
		editor.handleBecomingActive();

		// Continue applying
		super.performApply();
	}

	@Override
	public boolean okToLeave() {
		// TODO Auto-generated method stub
		return super.okToLeave();
	}

	@Override
	public boolean performOk() {

		// Apply all changes
		for (EditorWrapper editor : this.editors) {
			// Apply preference changes for active editor
			for (String name : editor.preferencesToChange.keySet()) {
				String value = editor.preferencesToChange.get(name);
				this.getPreferenceStore().setValue(name, value);
			}
		}

		// Continue applying changes
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {

		// Provide container
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(1, false));

		// Provide progress on loading editors
		Composite progressContainer = new Composite(container, SWT.NONE);
		GridDataFactory.defaultsFor(progressContainer).align(SWT.FILL, SWT.TOP).grab(true, false)
				.applyTo(progressContainer);
		progressContainer.setLayout(new GridLayout(2, false));

		// Provide progress of loading
		Label label = new Label(progressContainer, SWT.NONE);
		label.setText("Loading editors  ");
		ProgressBar progress = new ProgressBar(progressContainer, SWT.NONE);
		GridDataFactory.defaultsFor(progress).align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(progress);

		// Load the editors
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.ui.editors");
		progress.setMaximum(elements.length);
		this.loadEditors(elements, progress, progressContainer, container, parent.getDisplay());

		// Return the container
		return container;
	}

	@Override
	public synchronized void createControl(Composite parent) {
		super.createControl(parent);

		// Initial buttons are not visible
		this.getApplyButton().setVisible(false);
		this.getDefaultsButton().setVisible(false);
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
					String editorName = element.getAttribute("class");
					if (editorName == null) {
						editorName = element.getNamespaceIdentifier();
					}
					LOGGER.info("Failed to load " + editorName + " : " + ex.getMessage());
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
					OfficeFloorIdePreferencePage.this.editors = new EditorWrapper[finalEditors.length];
					for (int i = 0; i < finalEditors.length; i++) {
						OfficeFloorIdePreferencePage.this.editors[i] = new EditorWrapper(finalEditors[i]);
					}
				}

				// Load the preference page
				OfficeFloorIdePreferencePage.this.loadPreferencePage(container);

				// Render the changes
				container.layout();
			});
		}).start();
	}

	/**
	 * Editor wrapper.
	 */
	private class EditorWrapper implements Comparable<EditorWrapper> {

		/**
		 * {@link AbstractIdeEditor} being wrapped.
		 */
		private final AbstractIdeEditor<?, ?, ?> ideEditor;

		/**
		 * Name of the {@link AbstractIdeEditor}.
		 */
		private final String editorName;

		/**
		 * {@link ObservableMap} of preferences to change within the
		 * {@link IPreferenceStore}.
		 */
		private final ObservableMap<String, String> preferencesToChange = FXCollections.observableHashMap();

		/**
		 * {@link TabItem} for this {@link AbstractIdeEditor}.
		 */
		private TabItem editorTab;

		/**
		 * Preference change listener to update the apply button.
		 */
		private final MapChangeListener<String, String> preferenceChangeListener = (event) -> {

			// Ensure have editor
			if ((this.editorTab == null)
					&& (this.editorTab != OfficeFloorIdePreferencePage.this.editorTabs.getSelection()[0])) {
				return; // not the active tab
			}

			// Indicate if changes
			boolean isChanges = this.preferencesToChange.size() > 0;
			OfficeFloorIdePreferencePage.this.getApplyButton().setVisible(isChanges);
		};

		/**
		 * Instantiate.
		 * 
		 * @param ideEditor {@link AbstractIdeEditor} being wrapped.
		 */
		private EditorWrapper(AbstractIdeEditor<?, ?, ?> ideEditor) {
			this.ideEditor = ideEditor;

			// Indicate the editor
			this.editorName = this.ideEditor.getClass().getSimpleName();

			// Hook in listening changes
			this.preferencesToChange.addListener(preferenceChangeListener);
		}

		/**
		 * Specifies the {@link TabItem} for this {@link EditorWrapper}.
		 * 
		 * @param editorTab {@link TabItem} for this {@link EditorWrapper}.
		 */
		private void setTabItem(TabItem editorTab) {
			this.editorTab = editorTab;

			// Associate this with the tab
			this.editorTab.setData(this);

			// Listen to becoming active
			OfficeFloorIdePreferencePage.this.editorTabs.addListener(SWT.Selection, (event) -> {
				// Determine if become active
				if (this == OfficeFloorIdePreferencePage.this.getActiveEditor()) {
					this.handleBecomingActive();
				}
			});
		}

		/**
		 * Handles becoming the active {@link EditorWrapper}.
		 */
		private void handleBecomingActive() {
			// Update to be active editor
			this.preferenceChangeListener.onChanged(null);
			this.updateDefaultsButton();
		}

		/**
		 * Indicates if only defaults configured for the {@link AbstractIdeEditor}.
		 * 
		 * @return <code>true<code> indicates if only defaults for the
		 *         {@link AbstractIdeEditor}.
		 */
		private void updateDefaultsButton() {

			// Determine if only defaults
			boolean[] isDefaults = new boolean[] { true };
			this.visitPreferences((preferenceId) -> {
				if (!OfficeFloorIdePreferencePage.this.getPreferenceStore().isDefault(preferenceId)) {
					isDefaults[0] = false;
				}
			});

			// Update defaults button to indicate if reset to default
			OfficeFloorIdePreferencePage.this.getDefaultsButton().setVisible(!isDefaults[0]);
		}

		/**
		 * Visits all preferences for the {@link AbstractIdeEditor}.
		 * 
		 * @param visitor {@link Consumer} to visit each preference identifier for the
		 *                {@link AbstractIdeEditor}.
		 */
		private void visitPreferences(Consumer<String> visitor) {
			visitor.accept(this.ideEditor.getPaletteIndicatorStyleId());
			visitor.accept(this.ideEditor.getPaletteStyleId());
			visitor.accept(this.ideEditor.getEditorStyleId());
			for (AbstractItem<?, ?, ?, ?, ?, ?> item : this.ideEditor.getParents()) {
				this.visitItemPreferences(item, visitor);
			}
		}

		/**
		 * Visits the {@link AbstractItem}.
		 * 
		 * @param item    {@link AbstractItem} being visited.
		 * @param visitor {@link Consumer} visitor.
		 */
		private void visitItemPreferences(AbstractItem item, Consumer<String> visitor) {
			visitor.accept(item.getPreferenceStyleId());
			for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
				for (AbstractItem<?, ?, ?, ?, ?, ?> child : childrenGroup.getChildren()) {
					this.visitItemPreferences(child, visitor);
				}
			}
		}

		/*
		 * ============= Comparable ========================
		 */

		@Override
		public int compareTo(EditorWrapper that) {
			return this.editorName.compareTo(that.editorName);
		}
	}

}