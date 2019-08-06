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
import java.util.Map;

import javafx.beans.property.Property;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.preview.AdaptedEditorPreview;
import net.officefloor.gef.ide.editor.AbstractItem;
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
	 * Prototype {@link Model} for the item.
	 */
	private final M prototype;

	/**
	 * Label for the item.
	 */
	private final String itemLabel;

	/**
	 * {@link Property} to receive changes to the style. Also, provides the initial
	 * style.
	 */
	private final Property<String> style;

	/**
	 * Default style.
	 */
	private final String defaultStyle;

	/**
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Background {@link Color}.
	 */
	private final Color backgroundColour;

	/**
	 * Preferences to change.
	 */
	private final Map<String, String> preferencesToChange;

	/**
	 * Instantiate.
	 * 
	 * @param item                {@link AbstractItem}.
	 * @param prototype           Prototype {@link Model} for the item.
	 * @param itemLabel           Label for the item.
	 * @param isParent            Indicates if {@link AdaptedParent}.
	 * @param style               {@link Property} to receive changes to the style.
	 *                            Also, provides the initial style.
	 * @param defaultStyle        Default style.
	 * @param backgroundColour    Background {@link Color}.
	 * @param preferencesToChange {@link Map} to load with the
	 *                            {@link EditorPreferences} changes.
	 */
	public ModelPreferenceStyler(AbstractItem<?, ?, ?, ?, M, ?> item, M prototype, String itemLabel, boolean isParent,
			Property<String> style, String defaultStyle, Color backgroundColour,
			Map<String, String> preferencesToChange) {
		this.item = item;
		this.prototype = prototype;
		this.itemLabel = itemLabel;
		this.isParent = isParent;
		this.style = style;
		this.defaultStyle = defaultStyle == null ? "" : defaultStyle;
		this.backgroundColour = backgroundColour;
		this.preferencesToChange = preferencesToChange;
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

		// Provide the preview
		AdaptedEditorPreview<M> preview = new AdaptedEditorPreview<>(this.prototype, this.itemLabel, this.isParent,
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
		} catch (Exception ex) {
			// Indicate error in obtaining structure
			StringWriter error = new StringWriter();
			ex.printStackTrace(new PrintWriter(error));
			structureNode.setText("Error loading structure\n\n" + error.toString());
		}
		feedback.getChildren().add(structureNode);

		// Obtain the initial styling
		String initialStyle = this.style.getValue();

		// Provide means to change the styling
		TextArea styleText = new TextArea(initialStyle);
		VBox.setVgrow(styleText, Priority.ALWAYS);
		center.getChildren().add(styleText);

		// TODO load styling based on changes to style text
		// preview.style().setValue(initialStyle);

		// Provide buttons
		BorderPane bottom = new BorderPane();
		view.setBottom(bottom);
		HBox buttons = new HBox();
		bottom.setRight(buttons);

		// Provide button to close
		Button closeButton = new Button("Cancel");
		closeButton.setOnAction((event) -> close.run());
		buttons.getChildren().add(closeButton);

		// Return the view
		return view;
	}

}