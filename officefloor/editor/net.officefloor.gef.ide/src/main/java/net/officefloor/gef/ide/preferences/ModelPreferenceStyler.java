/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.gef.ide.preferences;

import java.io.PrintWriter;
import java.io.StringWriter;

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
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.preview.AdaptedEditorPreview;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.ide.editor.AbstractItem.IdeLabeller;
import net.officefloor.gef.ide.javafx.CssParserJavaFacet;
import net.officefloor.model.Model;

/**
 * Style for the {@link Model} preferences.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelPreferenceStyler<M extends Model> implements PreferenceStyler {

	/**
	 * {@link AbstractItem}.
	 */
	private final AbstractItem<?, ?, ?, ?, M, ?> item;

	/**
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Preferences to change.
	 */
	private final ObservableMap<String, String> preferencesToChange;

	/**
	 * Background {@link Color}.
	 */
	private final Color backgroundColour;

	/**
	 * Instantiate.
	 * 
	 * @param item                {@link AbstractItem}.
	 * @param prototype           Prototype {@link Model} for the item.
	 * @param isParent            Indicates if {@link AdaptedParent}.
	 * @param preferencesToChange Loaded with the {@link EditorPreferences} changes.
	 * @param backgroundColour    Background {@link Color}.
	 */
	public ModelPreferenceStyler(AbstractItem<?, ?, ?, ?, M, ?> item, boolean isParent,
			ObservableMap<String, String> preferencesToChange, Color backgroundColour) {
		this.item = item;
		this.isParent = isParent;
		this.preferencesToChange = preferencesToChange;
		this.backgroundColour = backgroundColour;
	}

	/*
	 * ======================= PreferenceStyler =========================
	 */

	@Override
	public Pane createVisual(Runnable close) {

		// Create the view
		BorderPane view = new BorderPane();
		view.setBackground(new Background(new BackgroundFill(this.backgroundColour, null, null)));

		// Provide content
		VBox center = new VBox();
		center.setFillWidth(true);
		view.setCenter(center);

		// Feedback
		HBox feedback = new HBox();
		VBox.setVgrow(feedback, Priority.ALWAYS);
		feedback.setFillHeight(true);
		center.getChildren().add(feedback);

		// Obtain the prototype
		M prototype = this.item.prototype();

		// Obtain label for the item
		IdeLabeller labeller = item.label();
		String itemName = (labeller == null) ? null : labeller.getLabel(prototype);
		if ((itemName == null) || (itemName.trim().length() == 0)) {
			itemName = item.getClass().getSimpleName();
		}
		final String itemLabel = itemName;

		// Provide the preview
		AdaptedEditorPreview<M> preview = new AdaptedEditorPreview<>(prototype, itemLabel, this.isParent,
				(model, context) -> this.item.visual(model, context));
		Pane previewPane = preview.getPreviewContainer();
		HBox.setHgrow(previewPane, Priority.ALWAYS);
		feedback.getChildren().add(previewPane);

		// Provide structure of item visual
		TextArea structureNode = new TextArea();
		structureNode.setEditable(false);
		HBox.setHgrow(structureNode, Priority.ALWAYS);
		try {
			StringWriter structure = new StringWriter();
			StructureLogger.log(preview.getPreviewVisual(), structure);
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
		errorPane.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
		center.getChildren().add(errorPane);
		Label errorLabel = new Label();
		errorLabel.setWrapText(true);
		errorLabel.setPadding(new Insets(10));
		errorLabel.setTextFill(Color.DARKRED);

		// Provide means to change the styling
		TextArea styleText = new TextArea();
		VBox.setVgrow(styleText, Priority.ALWAYS);
		center.getChildren().add(styleText);

		// Provide translation of style
		Property<String> styler = CssParserJavaFacet.translateProperty(styleText.textProperty(),
				(rawStyle) -> AbstractAdaptedIdeEditor.translateStyle(rawStyle, this.item));

		// Provide updates to styling
		Property<String> styleUpdater = preview.style();
		Property<String> cssErrors = CssParserJavaFacet.cssErrorProperty(styler, styleUpdater);

		// Listing to styling
		errorLabel.textProperty().bind(cssErrors);
		cssErrors.addListener((event) -> {
			ObservableList<Node> children = errorPane.getChildren();
			if (CompileUtil.isBlank(cssErrors.getValue())) {
				children.clear(); // no errors
			} else if (children.size() == 0) {
				children.add(errorLabel);
			}
		});

		// Load the initial styling
		String initialStyle = this.preferencesToChange.get(this.item.getPreferenceStyleId());
		if (CompileUtil.isBlank(initialStyle)) {
			initialStyle = this.item.style();
		}
		styleText.textProperty().setValue(initialStyle);

		// Provide buttons
		BorderPane bottom = new BorderPane();
		view.setBottom(bottom);

		// Provide button to close
		HBox buttons = new HBox();
		buttons.setPadding(new Insets(10));
		buttons.setSpacing(10);
		bottom.setRight(buttons);

		// Save changes to preference
		Button saveButton = new Button("Apply");
		saveButton.setOnAction((event) -> {

			// TODO save preference change

			// As saved, close
			close.run();
		});
		buttons.getChildren().add(saveButton);

		// No change to preferences
		Button closeButton = new Button("Cancel");
		closeButton.setOnAction((event) -> close.run());
		buttons.getChildren().add(closeButton);

		// Return the view
		return view;
	}

}