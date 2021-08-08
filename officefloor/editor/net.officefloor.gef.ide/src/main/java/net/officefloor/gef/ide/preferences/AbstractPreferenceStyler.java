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

package net.officefloor.gef.ide.preferences;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.ide.javafx.CssParserJavaFacet;

/**
 * Abstract styler for particular preference.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPreferenceStyler implements PreferenceStyler {

	/**
	 * Preferences to change.
	 */
	protected final ObservableMap<String, PreferenceValue> preferencesToChange;

	/**
	 * {@link EnvironmentBridge}.
	 */
	protected final EnvironmentBridge envBridge;

	/**
	 * Background {@link Color}.
	 */
	protected final Color backgroundColour;

	/**
	 * Instantiate.
	 * 
	 * @param preferencesToChange Loaded with the preferences changes.
	 * @param envBridge           {@link EnvironmentBridge}.
	 * @param backgroundColour    Background {@link Color}.
	 */
	public AbstractPreferenceStyler(ObservableMap<String, PreferenceValue> preferencesToChange,
			EnvironmentBridge envBridge, Color backgroundColour) {
		this.preferencesToChange = preferencesToChange;
		this.envBridge = envBridge;
		this.backgroundColour = backgroundColour;
	}

	/**
	 * Preference configuration.
	 */
	protected static class PreferenceConfiguration {

		/**
		 * Identifier of preference being configured.
		 */
		private final String preferenceId;

		/**
		 * Visual being configured. This allows for details of structure.
		 */
		private final Node visual;

		/**
		 * Default style.
		 */
		private final String defaultStyle;

		/**
		 * Allows updating the style of the visual.
		 */
		private final Property<String> styleUpdater;

		/**
		 * Preview pane for the visual. This is optional.
		 */
		private final Pane preview;

		/**
		 * Translates the raw style. This is optional.
		 */
		private final Function<String, String> translateStyle;

		/**
		 * 
		 * @param preferenceId   Identifier of preference being configured.
		 * @param visual         Visual being configured. This allows for details of
		 *                       structure.
		 * @param defaultStyle   Default style.
		 * @param styleUpdater   Allows updating the style of the visual.
		 * @param preview        Preview pane for the visual. This is optional.
		 * @param translateStyle Translates the raw style. This is optional.
		 */
		public PreferenceConfiguration(String preferenceId, Node visual, String defaultStyle,
				Property<String> styleUpdater, Pane preview, Function<String, String> translateStyle) {
			this.preferenceId = preferenceId;
			this.defaultStyle = defaultStyle;
			this.visual = visual;
			this.styleUpdater = styleUpdater;
			this.preview = preview;
			this.translateStyle = translateStyle != null ? translateStyle : (style) -> style;
		}
	}

	/**
	 * Initialises.
	 * 
	 * @return {@link PreferenceConfiguration} to configure preference editing.
	 */
	protected abstract PreferenceConfiguration init();

	/**
	 * Defaults the value if blank.
	 * 
	 * @param value        Value to check if blank.
	 * @param defaultValue Default value to use if value is blank.
	 * @return Value if not blank, otherwise default value.
	 */
	protected String defaultString(String value, String defaultValue) {
		return !CompileUtil.isBlank(value) ? value : defaultValue;
	}

	/*
	 * ======================= PreferenceStyler =========================
	 */

	@Override
	public Pane createVisual(Runnable close) {

		// Initialise
		PreferenceConfiguration configuration = this.init();

		// Backgrounds
		Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
		Background defaultBackground = new Background(new BackgroundFill(this.backgroundColour, null, null));
		Background warnBackground = new Background(new BackgroundFill(Color.YELLOW, null, null));

		// Obtain the cancel style
		String cancelStyle = configuration.styleUpdater.getValue();

		// Create the view
		BorderPane view = new BorderPane();
		view.setBackground(transparentBackground);

		// Provide content
		VBox center = new VBox();
		center.setBackground(transparentBackground);
		center.setFillWidth(true);
		view.setCenter(center);

		// Feedback
		HBox feedback = new HBox();
		VBox.setVgrow(feedback, Priority.ALWAYS);
		feedback.setFillHeight(true);
		feedback.setBackground(transparentBackground);
		center.getChildren().add(feedback);

		// Load possible preview
		if (configuration.preview != null) {
			configuration.preview.setBackground(defaultBackground);
			HBox.setHgrow(configuration.preview, Priority.ALWAYS);
			feedback.getChildren().add(configuration.preview);
		} else {
			Pane seeThrough = new Pane();
			seeThrough.setBackground(transparentBackground);
			HBox.setHgrow(seeThrough, Priority.ALWAYS);
			feedback.getChildren().add(seeThrough);
		}

		// Provide structure of item visual
		TextArea structureNode = new TextArea();
		structureNode.setEditable(false);
		HBox.setHgrow(structureNode, Priority.ALWAYS);
		try {
			StringWriter structure = new StringWriter();
			StructureLogger.log(configuration.visual, structure);
			structureNode.setText(structure.toString());
			structureNode.setStyle("-fx-control-inner-background: gainsboro");
		} catch (Exception ex) {
			// Indicate error in obtaining structure
			StringWriter error = new StringWriter();
			ex.printStackTrace(new PrintWriter(error));
			structureNode.setText("Error loading structure\n\n" + error.toString());
			structureNode.setStyle("-fx-control-inner-background: yellow");
		}
		feedback.getChildren().add(structureNode);

		// Provider CSS errors
		HBox errorPane = new HBox();
		errorPane.setBackground(warnBackground);
		center.getChildren().add(errorPane);
		Label errorLabel = new Label();
		errorLabel.setWrapText(true);
		errorLabel.setPadding(new Insets(10));
		errorLabel.setTextFill(Color.DARKRED);

		// Provide means to change the styling
		TextArea styleText = new TextArea();
		VBox.setVgrow(styleText, Priority.ALWAYS);
		center.getChildren().add(styleText);

		// Obtain the default style
		String defaultStyle = this.defaultString(configuration.defaultStyle, "");

		// Provide buttons
		BorderPane bottom = new BorderPane();
		bottom.setBackground(defaultBackground);
		bottom.setPadding(new Insets(10));
		view.setBottom(bottom);

		// Provide reset button
		Button resetButton = new Button("Reset to default");
		styleText.textProperty()
				.addListener((event) -> resetButton.setDisable(defaultStyle.equals(styleText.getText())));
		resetButton.setOnAction((event) -> styleText.setText(defaultStyle));
		bottom.setLeft(resetButton);

		// Provide completion buttons
		HBox buttons = new HBox();
		buttons.setSpacing(10);
		bottom.setRight(buttons);

		// Save changes to preference
		Button saveButton = new Button("Apply");
		saveButton.setOnAction((event) -> {

			// Obtain preference value for change
			String changeValue;
			String preferredStyle = styleText.getText();
			if (this.defaultString(preferredStyle, "").equals(defaultStyle)) {
				changeValue = null; // reset to default
			} else {
				changeValue = preferredStyle;
			}

			// Load preference to be changed
			this.preferencesToChange.put(configuration.preferenceId, new PreferenceValue(changeValue));

			// As saved, close
			close.run();
		});
		buttons.getChildren().add(saveButton);

		// No change to preferences
		Button closeButton = new Button("Cancel");
		closeButton.setOnAction((event) -> {

			// Reset to initial style
			configuration.styleUpdater.setValue(cancelStyle);

			// Close
			close.run();
		});
		buttons.getChildren().add(closeButton);

		// Provide translation of style
		Property<String> styler = CssParserJavaFacet.translateProperty(styleText.textProperty(),
				(rawStyle) -> configuration.translateStyle.apply(rawStyle));
		Property<String> cssErrors = CssParserJavaFacet.cssErrorProperty(styler, configuration.styleUpdater);

		// Listen to styling
		errorLabel.textProperty().bind(cssErrors);
		cssErrors.addListener((event) -> {
			ObservableList<Node> children = errorPane.getChildren();
			if (CompileUtil.isBlank(cssErrors.getValue())) {
				// No errors
				children.clear();
				saveButton.setDisable(false);
			} else if (children.size() == 0) {
				// Has error
				children.add(errorLabel);
				saveButton.setDisable(true);
			}
		});

		// Load the initial styling
		String initialStyle;
		PreferenceValue preference = this.preferencesToChange.get(configuration.preferenceId);
		if ((preference != null) && (!CompileUtil.isBlank(preference.value))) {
			initialStyle = preference.value; // changes takes priority
		} else {
			String configureStyle = this.envBridge.getPreference(configuration.preferenceId);
			if (!CompileUtil.isBlank(configureStyle)) {
				initialStyle = configureStyle; // current preference is next priority
			} else {
				initialStyle = defaultStyle; // default is last priority
			}
		}
		styleText.textProperty().setValue(initialStyle);

		// Return the view
		return view;
	}

}
