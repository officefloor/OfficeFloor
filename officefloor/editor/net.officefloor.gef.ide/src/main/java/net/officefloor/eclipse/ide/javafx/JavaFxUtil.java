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
package net.officefloor.eclipse.ide.javafx;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Scene;
import net.officefloor.eclipse.ide.swt.SwtUtil;

//import com.sun.javafx.css.CssError;
//import com.sun.javafx.css.StyleManager;
//import javafx.css.CssParser;
//import javafx.css.CssParser.ParseError;

/**
 * Utility methods for JavaFx.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFxUtil {

	/**
	 * Active {@link CssManager} for {@link Scene}.
	 */
	private static Map<Scene, CssManager> activeCssManager = new WeakHashMap<>();

	/**
	 * Obtains an {@link Property} to CSS error of the {@link Scene}.
	 * 
	 * @param scene {@link Scene} to limit the CSS error.
	 * @return {@link Property} to {@link Scene} CSS error.
	 */
	private static void registerScene(Scene scene) {

		// Determine if already CSS Manager for the scene
		CssManager cssManager = activeCssManager.get(scene);
		if (cssManager != null) {
			return;
		}

		// Obtain the error property
		ReadOnlyProperty<String> errorProperty = CssParserJavaFacet.errorProperty(JavaFxUtil.class.getClassLoader());

		// Update CSS manager with error
		errorProperty.addListener((event) -> {

			// Load error to active CSS manager
			CssManager manager = activeCssManager.get(scene);
			if (manager != null) {
				manager.setCssError(errorProperty.getValue());
			}
		});
	}

	/**
	 * CSS manager.
	 */
	public static class CssManager {

		/**
		 * {@link Scene}.
		 */
		private final Scene scene;

		/**
		 * {@link Property} to update with style.
		 */
		private final Property<String> style;

		/**
		 * {@link Label} to report the CssError.
		 */
		private final Label cssErrorLabel;

		/**
		 * CSS {@link ControlDecoration}.
		 */
		private final ControlDecoration cssErrorDecoration;

		/**
		 * Instantiate.
		 * 
		 * @param parent Parent {@link Composite}.
		 * @param scene  {@link Scene} to listen on CSS errors.
		 * @param style  {@link Property} to update with style.
		 */
		private CssManager(Composite parent, Scene scene, Property<String> style) {
			this.scene = scene;
			this.style = style;

			// Provide CSS errors
			this.cssErrorLabel = new Label(parent, SWT.NONE);
			this.cssErrorLabel.setText("");

			// Provide error decoration
			this.cssErrorDecoration = SwtUtil.errorDecoration(this.cssErrorLabel, SWT.TOP | SWT.LEFT);
			this.cssErrorDecoration.hide();

			// Listen in on CSS errors
			registerScene(scene);

			// Keep reference to this alive
			this.cssErrorLabel.setData(this);
		}

		/**
		 * Obtains the control.
		 * 
		 * @return {@link Control}.
		 */
		public Control getControl() {
			return this.cssErrorLabel;
		}

		/**
		 * Registers {@link StyledText} for tracking CSS errors.
		 * 
		 * @param text         {@link StyledText} for tracking CSS errors.
		 * @param initialStyle Initial style.
		 * @param translator   Optional translator of the CSS. May be <code>null</code>.
		 */
		public void registerText(StyledText text, String initialStyle, Function<String, String> translator) {
			if (initialStyle != null) {
				text.setText(initialStyle);
			}
			text.addListener(SWT.Modify, (event) -> this.loadStyle(text.getText(), translator));
		}

		/**
		 * Registers {@link Text} for tracking CSS errors.
		 * 
		 * @param text         {@link Text} for tracking CSS errors.
		 * @param initialStyle Initial style.
		 * @param translator   Optional translator of the CSS. May be <code>null</code>.
		 */
		public void registerText(Text text, String initialStyle, Function<String, String> translator) {
			if (initialStyle != null) {
				text.setText(initialStyle);
			}
			text.addListener(SWT.Modify, (event) -> this.loadStyle(text.getText(), translator));
		}

		/**
		 * Loads styling.
		 * 
		 * @param rawStyle   Raw style. May be <code>null</code>.
		 * @param translator Translator. May be <code>null</code>.
		 */
		public void loadStyle(String rawStyle, Function<String, String> translator) {

			// Register as active CSS Manager
			activeCssManager.put(this.scene, this);

			// Clear CSS error (will possibly reinstate after modification)
			this.cssErrorLabel.setText("");
			this.cssErrorLabel.setToolTipText("");
			this.cssErrorDecoration.hide();

			// Ensure have styling
			if (rawStyle == null) {
				rawStyle = "";
			}

			// Modify the style
			String style = translator != null ? translator.apply(rawStyle) : rawStyle;
			this.style.setValue(style);
		}

		/**
		 * Specifies the CSS error.
		 * 
		 * @param message CSS error message.
		 */
		private void setCssError(String message) {

			// Handle being disposed before CSS error event
			if (this.cssErrorLabel.isDisposed()) {
				return;
			}

			// Display the CSS error
			this.cssErrorLabel.setText(" " + message);
			this.cssErrorLabel.setToolTipText(message);
			this.cssErrorLabel
					.setForeground(this.cssErrorLabel.getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			this.cssErrorDecoration.show();
		}
	}

	/**
	 * Creates a {@link CssManager}.
	 * 
	 * @param parent Parent {@link Composite}.
	 * @param scene  {@link Scene} to listen in on for errors.
	 * @param style  {@link Property} to specify the style.
	 * @return {@link CssManager}.
	 */
	public static CssManager createCssManager(Composite parent, Scene scene, Property<String> style) {
		return new CssManager(parent, scene, style);
	}

	/**
	 * All access via static methods.
	 */
	private JavaFxUtil() {
	}

}