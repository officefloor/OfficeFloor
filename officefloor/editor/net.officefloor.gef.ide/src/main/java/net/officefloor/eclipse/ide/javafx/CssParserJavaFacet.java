package net.officefloor.eclipse.ide.javafx;

import javafx.beans.Observable;
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
	 * Obtains the {@link StringProperty} to the CSS errors.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link StringProperty} to the CSS errors.
	 */
	public static ReadOnlyProperty<String> errorProperty(ClassLoader classLoader) {

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