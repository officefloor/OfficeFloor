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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.ide.OfficeFloorIdePlugin;
import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractItem;
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

		// Sort the editors (too keep deterministic in order displayed)
		Arrays.sort(this.editors);

		// Allow configurations for each editor
		for (AbstractIdeEditor<?, ?, ?> editor : this.editors) {

			// Indicate the editor
			Label editorName = new Label(parent, SWT.TITLE);
			editorName.setText(editor.getClass().getSimpleName());
			editorName.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

			// Indicate the parents
			for (AbstractItem item : editor.getParents()) {

				// Create row for the item
				Composite itemRow = new Composite(parent, SWT.NONE);
				itemRow.setLayout(new GridLayout(3, false));

				// Obtain details for the item
				Model prototype = item.prototype();
				IdeLabeller labeller = item.label();
				String itemName = (labeller == null) ? null : labeller.getLabel(prototype);
				if ((itemName == null) || (itemName.trim().length() == 0)) {
					itemName = item.getClass().getSimpleName();
				}

				// Indicate the label for item
				Label itemLabel = new Label(parent, SWT.NONE);
				itemLabel.setText(itemName);

				// Obtain the visual for item
				Pane visual = item.visual(prototype, null);

				// Provide the view of the item
				FXCanvasEx canvas = new FXCanvasEx(parent, SWT.NONE);
				canvas.setScene(new Scene(visual));
			}
		}
	}

	/**
	 * Loads the {@link ITheme} {@link Color} instances.
	 * 
	 * @param parent
	 *            Parent {@link Composite} to allow creation of dummy {@link Widget}
	 *            instances.
	 * @return {@link Map} of CSS property to {@link Color}.
	 */
	private Map<String, Color> loadThemeColours(Composite parent) {

		// Obtain the theme details
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		BundleContext bundleContext = bundle.getBundleContext();
		ServiceReference<IThemeManager> themeManagerReference = bundleContext.getServiceReference(IThemeManager.class);

		// Obtain the theme manager
		IThemeManager themeManager = bundle.getBundleContext().getService(themeManagerReference);
		IThemeEngine themeEngine = themeManager.getEngineForDisplay(parent.getDisplay());
		return this.extractColours(new TreeViewer(parent, SWT.NONE), themeEngine, true);
	}

	/**
	 * Extract the colours from the {@link Widget} (to obtain current theme styles).
	 * 
	 * @param uiObject
	 *            UI object.
	 * @param themeEngine
	 *            {@link IThemeEngine}.
	 * @param isDispose
	 *            Indicates to dispose the {@link Widget} once complete.
	 * @return {@link Map} of colour property to {@link Color}.
	 */
	private Map<String, Color> extractColours(Object uiObject, IThemeEngine themeEngine, boolean isDispose) {

		// Obtain the widget
		Widget widget;
		if (uiObject instanceof Widget) {
			widget = (Widget) uiObject;
		} else if (uiObject instanceof Viewer) {
			widget = ((Viewer) uiObject).getControl();
		} else {
			throw new IllegalStateException("Unknown UI object type " + uiObject.getClass().getName());
		}

		// Style the widget (so has CSS loaded)
		themeEngine.applyStyles(widget, true);

		// Extract the colours from the widget
		Map<String, Color> colours = new HashMap<>();
		this.extractColours(widget, "", colours, themeEngine);

		// Determine if dipose widget (now that have colours)
		if (isDispose) {
			widget.dispose();
		}

		// Return the colours
		return colours;
	}

	/**
	 * Extracts the colours from the {@link ITheme}.
	 * 
	 * @param widget
	 *            {@link Widget} to inspect.
	 * @param prefix
	 *            Prefix on the return colour.
	 * @param colours
	 *            {@link Map} to load the {@link Color}.
	 * @param themeEngine
	 *            {@link IThemeEngine}.
	 */
	private void extractColours(Widget widget, String prefix, Map<String, Color> colours, IThemeEngine themeEngine) {

		// Extract the colours from the widget
		CSSStyleDeclaration style = themeEngine.getStyle(widget);
		if (style != null) {
			for (int i = 0; i < style.getLength(); i++) {
				String name = style.item(i);
				CSSValue value = style.getPropertyCSSValue(name);
				if (value instanceof RGBColor) {
					RGBColor valueColour = (RGBColor) value;
					double red = Double.parseDouble(valueColour.getRed().getCssText()) / 255;
					double green = Double.parseDouble(valueColour.getGreen().getCssText()) / 255;
					double blue = Double.parseDouble(valueColour.getBlue().getCssText()) / 255;
					Color colour = Color.color(red, green, blue);
					colours.put(prefix + name, colour);
				}
			}
		}

		// Extract colours from possible children
		if (widget instanceof Composite) {
			Composite composite = (Composite) widget;
			for (Control child : composite.getChildren()) {
				this.extractColours(child, prefix + widget.getClass().getSimpleName() + ".", colours, themeEngine);
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

}