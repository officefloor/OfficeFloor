/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import net.officefloor.gef.editor.style.AbstractStyleRegistry;
import net.officefloor.gef.editor.style.DefaultStyleRegistry;
import net.officefloor.gef.editor.style.Handler;
import net.officefloor.gef.editor.style.OsgiURLStreamHandlerService;
import net.officefloor.gef.editor.style.StyleRegistry;
import net.officefloor.gef.editor.style.SystemStyleRegistry;

/**
 * {@link AbstractUIPlugin} for the Adapted Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedEditorPlugin extends AbstractUIPlugin {

	/**
	 * Initialises for non OSGi environment.
	 */
	public static void initNonOsgiEnvironment() {
		try {
			// Setup OfficeFloor style URL handling
			URL.setURLStreamHandlerFactory((protocol) -> {
				if (!AbstractStyleRegistry.PROTOCOL.equals(protocol)) {
					return null;
				}
				return new Handler();
			});
		} catch (Throwable ex) {
			// Assume factory already initialised
		}
	}

	/**
	 * Creates a new {@link StyleRegistry}.
	 * 
	 * @return New {@link StyleRegistry}.
	 */
	public static StyleRegistry createStyleRegistry() {

		// Determine if running within OSGi
		if (INSTANCE == null) {

			// Running outside OSGi
			return new SystemStyleRegistry();
		}

		// Running with URL locating
		return new DefaultStyleRegistry();
	}

	/**
	 * Obtains the default style sheet.
	 * 
	 * @return Default style sheet.
	 */
	public static String getDefaultStyleSheet() {

		// Obtain the style sheet
		InputStream defaultStyleSheet = AdaptedEditorModule.class
				.getResourceAsStream(AdaptedEditorModule.class.getSimpleName() + ".css");
		if (defaultStyleSheet == null) {
			// Should not occur
			throw new IllegalStateException("Default style sheet not available on class path");
		}

		// Read in the deafult style sheet
		try (Reader reader = new InputStreamReader(defaultStyleSheet)) {

			// Read in the style sheet
			StringWriter stylesheet = new StringWriter();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				stylesheet.write(character);
			}

			// Return the style sheet
			return stylesheet.toString();

		} catch (IOException ex) {
			// Should not occur
			throw new IllegalStateException("Failed to load default style sheet", ex);
		}
	}

	/**
	 * Loads the default style sheet.
	 * 
	 * @param scene {@link Scene}.
	 */
	public static void loadDefaulStylesheet(Scene scene) {

		// Determine if running within OSGi
		if (INSTANCE == null) {

			// Running outside OSGi environment (so load from class path)
			scene.getStylesheets().add(AdaptedEditorModule.class.getName().replace('.', '/') + ".css");
			return;
		}

		// Ensure have the default style sheet loaded (does not change so URL constant)
		if (DEFAULT_STYLESHEET_URL == null) {

			// Obtain the default style sheet
			String styleSheet = getDefaultStyleSheet();

			// Provide style registry URL to default style sheet
			StyleRegistry registry = createStyleRegistry();
			ReadOnlyProperty<URL> defaultStyleSheetUrl = registry.registerStyle("_default_",
					new SimpleStringProperty(styleSheet));
			DEFAULT_STYLESHEET_URL = defaultStyleSheetUrl.getValue();
		}

		// Load the default style sheet
		scene.getStylesheets().add(DEFAULT_STYLESHEET_URL.toExternalForm());
	}

	/**
	 * Obtains the {@link AdaptedEditorPlugin} singleton.
	 * 
	 * @return {@link AdaptedEditorPlugin} singleton.
	 */
	public AdaptedEditorPlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Default stylesheet {@link URL}.
	 */
	private static URL DEFAULT_STYLESHEET_URL;

	/**
	 * Singleton.
	 */
	private static AdaptedEditorPlugin INSTANCE;

	/**
	 * {@link ServiceRegistration} for the {@link OsgiURLStreamHandlerService}.
	 */
	private ServiceRegistration<?> styleUrlHandler;

	/**
	 * Instantiate.
	 */
	public AdaptedEditorPlugin() {
		INSTANCE = this;
	}

	/*
	 * =============== AbstractUIPlugin =========================
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// Register the URL handler for styling
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put("url.handler.protocol", AbstractStyleRegistry.PROTOCOL);
		this.styleUrlHandler = context.registerService(URLStreamHandlerService.class.getName(),
				new OsgiURLStreamHandlerService(), properties);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		// Unregister style URL handler
		if (this.styleUrlHandler != null) {
			context.ungetService(this.styleUrlHandler.getReference());
			this.styleUrlHandler = null;
		}
	}

}
