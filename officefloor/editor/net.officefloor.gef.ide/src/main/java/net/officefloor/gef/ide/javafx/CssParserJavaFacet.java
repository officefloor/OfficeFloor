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

package net.officefloor.gef.ide.javafx;

import java.util.function.Function;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import net.officefloor.frame.compatibility.ClassCompatibility;
import net.officefloor.frame.compatibility.ClassCompatibility.ObjectCompatibility;
import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * CssParser {@link JavaFacet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CssParserJavaFacet implements JavaFacet {

	/**
	 * CSS error {@link Property}.
	 */
	private static Property<String> cssErrorProperty = null;

	/**
	 * Active error {@link Property}.
	 */
	private static SimpleStringProperty activeErrorProperty = null;

	/**
	 * Translates the {@link Property}.
	 * 
	 * @param rawStyle   Raw style.
	 * @param translator Translator. May be <code>null</code>.
	 * @return Translated {@link Property}.
	 */
	public static Property<String> translateProperty(Property<String> rawStyle, Function<String, String> translator) {

		// Translate the raw style
		SimpleStringProperty translatedStyle = new SimpleStringProperty();
		rawStyle.addListener((event) -> {

			// Translate the style
			String raw = rawStyle.getValue();
			String translated = translator != null ? translator.apply(raw) : raw;

			// Update the translated style
			translatedStyle.setValue(translated);
		});

		// Return the translated
		return translatedStyle;
	}

	/**
	 * Creates a {@link Property} to listen on CSS errors for the style.
	 * 
	 * @param style        {@link Property} specifying the style.
	 * @param styleUpdater Updates the Style.
	 * @return {@link Property} for CSS errors.
	 */
	public static Property<String> cssErrorProperty(Property<String> style, Property<String> styleUpdater) {

		// Listen on style changes, to setup error handling
		SimpleStringProperty errorProperty = new SimpleStringProperty();
		style.addListener((event) -> {

			// Specify error as active for style change
			activeErrorProperty = errorProperty;

			// Clear the error (as no event if no CSS error)
			errorProperty.setValue("");
			cssErrorProperty.setValue("");

			// Update the style (may trigger CSS error)
			String newStyle = style.getValue();
			styleUpdater.setValue(newStyle);
		});

		// Ensure have CSS error property registered
		if (cssErrorProperty == null) {
			cssErrorProperty = CssParserJavaFacet.errorProperty(CssParserJavaFacet.class.getClassLoader());
			cssErrorProperty.addListener((event) -> {

				// Notify error handler of CSS error (active for last style change)
				SimpleStringProperty active = activeErrorProperty;
				active.setValue(cssErrorProperty.getValue());
			});
		}

		// Return the error property
		return errorProperty;
	}

	/**
	 * Obtains the {@link StringProperty} to the CSS errors.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link StringProperty} to the CSS errors.
	 */
	private static Property<String> errorProperty(ClassLoader classLoader) {

		// Create the observable list to errors
		ClassCompatibility css;
		if (JavaFacet.isSupported(new CssParserJavaFacet())) {
			css = new ClassCompatibility("javafx.css.CssParser", classLoader);
		} else {
			css = new ClassCompatibility("com.sun.javafx.css.StyleManager", classLoader);
		}

		// Create the string property
		SimpleStringProperty property = new SimpleStringProperty("");

		// Obtain the observable list for errors
		ObjectCompatibility list = css.$("errorsProperty");
		ObservableList<?> observableList = list.get(ObservableList.class);
		observableList.addListener((Observable event) -> {

			// Extract the message for the last event
			int size = observableList.size();
			ObjectCompatibility lastEvent = ClassCompatibility.object(observableList.get(size - 1));
			String message = lastEvent.$("getMessage").get(String.class);
			if (message == null) {
				message = "";
			}

			// Load the error
			message = message.split(" in stylesheet")[0];
			property.setValue(message);
		});

		// Return the property
		return property;
	}

	/*
	 * ================ JavaFacet ======================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		return context.getFeature() >= 9;
	}

}
