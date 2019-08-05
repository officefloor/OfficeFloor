package net.officefloor.gef.ide.javafx;

import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
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
	private static ReadOnlyProperty<String> cssErrorProperty = null;

	/**
	 * Active error {@link Property}.
	 */
	private static SimpleStringProperty activeErrorProperty = null;

	/**
	 * Creates a {@link Property} to listen on CSS errors for the style.
	 * 
	 * @param style        {@link Property} to specify the style.
	 * @param translator   Translator. May be <code>null</code>.
	 * @param styleUpdater Updates the Style.
	 * @return {@link Property} for CSS errors.
	 */
	public static Property<String> cssErrorProperty(Property<String> style, Function<String, String> translator,
			Consumer<String> styleUpdater) {

		// Ensure have CSS error property registered
		if (cssErrorProperty == null) {
			cssErrorProperty = CssParserJavaFacet.errorProperty(CssParserJavaFacet.class.getClassLoader());
			cssErrorProperty.addListener((event) -> {

				// Notify error handler of CSS error (active for last style change)
				SimpleStringProperty active = activeErrorProperty;
				active.setValue(cssErrorProperty.getValue());
			});
		}

		// Translate the raw style
		SimpleStringProperty translatedStyle = new SimpleStringProperty();
		style.addListener((event) -> {

			// Translate the style
			String rawStyle = style.getValue();
			String translated = translator != null ? translator.apply(rawStyle) : rawStyle;

			// Update the translated style
			translatedStyle.setValue(translated);
		});

		// Listen on style changes, to setup error handling
		SimpleStringProperty errorProperty = new SimpleStringProperty();
		translatedStyle.addListener((event) -> {

			// Clear the error (as no event if no CSS error)
			errorProperty.setValue("");

			// Specify error as active for style change
			activeErrorProperty = errorProperty;

			// Update the style (may trigger CSS error)
			String newStyle = translatedStyle.get();
			styleUpdater.accept(newStyle);
		});

		// Return the error property
		return errorProperty;
	}

	/**
	 * Obtains the {@link StringProperty} to the CSS errors.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link StringProperty} to the CSS errors.
	 */
	private static ReadOnlyProperty<String> errorProperty(ClassLoader classLoader) {

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
		list.get(ObservableList.class).addListener((Observable event) -> {

			// Extract the message for the last event
			int size = list.$("size").get(Integer.class);
			ObjectCompatibility lastEvent = list.$("get", css.arg(size - 1, int.class));
			String message = lastEvent.$("getMessage").get(String.class);
			if (message == null) {
				message = "";
			}

			// Load the error
			message = message.split(" in stylesheet")[0];
			property.set(message);
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