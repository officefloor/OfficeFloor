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
package net.officefloor.gef.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.core.runtime.Plugin;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import net.officefloor.gef.editor.style.DefaultStyleRegistry;
import net.officefloor.gef.editor.style.StyleRegistry;
import net.officefloor.gef.editor.style.SystemStyleRegistry;

/**
 * {@link Plugin} for the {@link AdaptedEditorModule}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedEditorStyle {

	/**
	 * Indicates if style {@link URL} location is active.
	 */
	private static boolean isUrlStyleLocationActive = false;

	/**
	 * Default stylesheet {@link URL}.
	 */
	private static URL DEFAULT_STYLESHEET_URL;

	/**
	 * Flags {@link URL} location active.
	 */
	public static void urlStyleLocationActive() {
		isUrlStyleLocationActive = true;
	}

	/**
	 * Creates a new {@link StyleRegistry}.
	 * 
	 * @return New {@link StyleRegistry}.
	 */
	public static StyleRegistry createStyleRegistry() {

		// Determine if able to locate via URL
		if (isUrlStyleLocationActive) {

			// Running without URL locating
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

		// Determine if running with URL locating
		if (isUrlStyleLocationActive) {

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

}