/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.eclipse.ide.preferences;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.fx.swt.canvas.IFXCanvasFactory;
import org.eclipse.gef.mvc.fx.domain.IDomain;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import net.officefloor.eclipse.bridge.AdaptedIdePlugin;
import net.officefloor.eclipse.bridge.EclipseEnvironmentBridge;
import net.officefloor.eclipse.ide.AbstractAdaptedEditorPart;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.preferences.PreferencesEditor;

/**
 * {@link IWorkbenchPreferencePage}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorIdePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OfficeFloorIdePreferencePage.class);

	/**
	 * {@link EditorInstance} instances.
	 */
	private EditorInstance[] editors;

	/**
	 * {@link TabFolder} for the {@link EditorWrapper} instances.
	 */
	private TabFolder editorTabs;

	/**
	 * {@link IDomain}.
	 */
	@Inject
	private IDomain domain;

	/**
	 * {@link IFXCanvasFactory}.
	 */
	@Inject
	private IFXCanvasFactory canvasFactory;

	/*
	 * ================ IWorkbenchPreferencePage =================
	 */

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to initialise
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return AdaptedIdePlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected void performDefaults() {

		// Reset to defaults
		EditorInstance active = this.getActiveEditor();
		active.preferences.resetToDefaults();

		// Continue defaults
		super.performDefaults();
	}

	@Override
	protected void performApply() {

		// Apply changes
		EditorInstance active = this.getActiveEditor();
		active.preferences.apply();

		// Continue applying
		super.performApply();
	}

	@Override
	public boolean performOk() {

		// Apply all changes
		for (EditorInstance instance : this.editors) {
			instance.preferences.apply();
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
		this.loadEditors(progress, progressContainer, container, parent.getDisplay());

		// Return the container
		return container;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		// Initially buttons are not visible
		this.getApplyButton().setVisible(false);
		this.getDefaultsButton().setVisible(false);
	}

	/**
	 * Obtains the active {@link PreferencesEditor}.
	 * 
	 * @return Active {@link PreferencesEditor}.
	 */
	private EditorInstance getActiveEditor() {
		TabItem editorTab = this.editorTabs.getSelection()[0];
		return (EditorInstance) editorTab.getData();
	}

	/**
	 * Loads the {@link AbstractAdaptedIdeEditor} instances.
	 */
	private void loadEditors(ProgressBar progress, Composite progressContainer, Composite container, Display display) {

		// Obtain the editors configuration
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.ui.editors");
		progress.setMaximum(elements.length);

		// Loads the editors in background thread
		new Thread(() -> {

			// Load the editor instances
			List<EditorInstance> editorInstances = new LinkedList<>();
			NEXT_ELEMENT: for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element = elements[i];

				// Obtain the class
				Object extension = display.syncCall(() -> {
					// Display the editor preferences
					try {
						return element.createExecutableExtension("class");
					} catch (Exception ex) {
						// Ignore failed to load editors
						String editorName = element.getAttribute("class");
						if (editorName == null) {
							editorName = element.getNamespaceIdentifier();
						}
						LOGGER.info("Failed to load " + editorName + " : " + ex.getMessage(), ex);
						return null; // no class
					}
				});
				if (extension == null) {
					continue NEXT_ELEMENT; // failed loading
				}

				// Ensure appropriate editor
				if (!(extension instanceof AbstractAdaptedEditorPart)) {
					continue NEXT_ELEMENT;
				}
				AbstractAdaptedEditorPart<?, ?, ?> editPart = (AbstractAdaptedEditorPart<?, ?, ?>) extension;

				// Create the preferences editor
				try {
					EclipseEnvironmentBridge envBridge = new EclipseEnvironmentBridge();
					AbstractAdaptedIdeEditor<?, ?, ?> editor = editPart.createEditor(envBridge);
					PreferencesEditor<?> preferences = new PreferencesEditor<>(editor, envBridge);
					editorInstances.add(new EditorInstance(editor, preferences, envBridge));
				} catch (Exception ex) {
					LOGGER.info("Failed to load preferences editor from " + editPart.getClass().getName() + " : "
							+ ex.getMessage(), ex);
					continue NEXT_ELEMENT;
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

			// Specify the editors
			final EditorInstance[] finalEditors;
			synchronized (OfficeFloorIdePreferencePage.this) {
				finalEditors = editorInstances.toArray(new EditorInstance[editorInstances.size()]);
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

				// Load the editors (safely into GUI thread)
				synchronized (OfficeFloorIdePreferencePage.this) {
					this.editors = finalEditors;
				}

				// Load the preference page
				OfficeFloorIdePreferencePage.this.loadPreferencePage(container);

				// Render the changes
				container.layout();
			});
		}).start();
	}

	/**
	 * Loads the preference page.
	 * 
	 * @param parent Parent.
	 */
	protected void loadPreferencePage(Composite parent) {

		// Sort the editors (too keep deterministic in order displayed)
		Arrays.sort(this.editors,
				(a, b) -> a.editor.getClass().getSimpleName().compareTo(b.editor.getClass().getSimpleName()));

		// Create tabs for each editor
		this.editorTabs = new TabFolder(parent, SWT.BORDER | SWT.INHERIT_FORCE);
		this.editorTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Obtain the parent shell
		Shell parentShell = parent.getShell();

		// Load in the preferences editors
		for (EditorInstance instance : this.editors) {

			// Configure environment
			instance.envBridge.init(parentShell);

			// Create tab for editor
			TabItem editorTab = new TabItem(this.editorTabs, SWT.INHERIT_FORCE);
			editorTab.setText(instance.editor.getClass().getSimpleName());
			editorTab.setData(instance);

			// Ensure have canvas factory
			if (this.canvasFactory == null) {
				AbstractAdaptedEditorPart.initEditor(instance.editor, (injector) -> {
					injector.injectMembers(this);
					return this.domain;
				});
			}
			FXCanvas canvas = this.canvasFactory.createCanvas(this.editorTabs, SWT.NONE);
			editorTab.setControl(canvas);

			// Load the preferences editor
			instance.preferences.loadView((view) -> {
				canvas.setScene(new Scene(view));
			});
		}

		// Make buttons visible
		this.getDefaultsButton().setVisible(true);
		this.getApplyButton().setVisible(true);
	}

	/**
	 * Editor instance.
	 */
	private class EditorInstance {

		/**
		 * {@link AbstractAdaptedIdeEditor}.
		 */
		private final AbstractAdaptedIdeEditor<?, ?, ?> editor;

		/**
		 * {@link PreferencesEditor}.
		 */
		private final PreferencesEditor<?> preferences;

		/**
		 * {@link EclipseEnvironmentBridge}.
		 */
		private final EclipseEnvironmentBridge envBridge;

		/**
		 * Instantiate.
		 * 
		 * @param editor      {@link AbstractAdaptedIdeEditor}.
		 * @param preferences {@link PreferencesEditor}.
		 * @param envBridge   {@link EclipseEnvironmentBridge}.
		 */
		private EditorInstance(AbstractAdaptedIdeEditor<?, ?, ?> editor, PreferencesEditor<?> preferences,
				EclipseEnvironmentBridge envBridge) {
			this.editor = editor;
			this.preferences = preferences;
			this.envBridge = envBridge;
		}
	}

}
