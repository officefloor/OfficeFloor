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
package net.officefloor.eclipse.editor.preview;

import java.net.URL;

import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedEditorPlugin;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.internal.parts.AdaptedChildPart;
import net.officefloor.eclipse.editor.internal.parts.AdaptedModelVisualFactoryContextImpl;
import net.officefloor.eclipse.editor.internal.parts.AdaptedParentPart;
import net.officefloor.eclipse.editor.internal.style.StyleRegistry;
import net.officefloor.model.Model;

/**
 * Preview of an {@link AdaptedModel} {@link IVisualPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedEditorPreview<M extends Model> {

	/**
	 * Preview {@link Scene}.
	 */
	private final Scene previewScene;

	/**
	 * Preview visual {@link Node}.
	 */
	private final Node previewVisual;

	/**
	 * {@link Property} to obtain specific styling for the {@link IVisualPart}.
	 */
	private final Property<String> styling = new SimpleStringProperty();

	/**
	 * Instantiate.
	 * 
	 * @param model         Model.
	 * @param label         Label.
	 * @param isParent      Indicates if {@link AdaptedParent}.
	 * @param visualFactory {@link AdaptedModelVisualFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedEditorPreview(M model, String label, boolean isParent, AdaptedModelVisualFactory<M> visualFactory) {

		// Create the visual
		this.previewVisual = visualFactory.createVisual(model,
				new AdaptedModelVisualFactoryContextImpl<>((Class<M>) model.getClass(), false, () -> {
					// Always have a label
					return new SimpleStringProperty(label);
				}, (childGroupName, node) -> {
					// Always registered
					return true;
				}, (connectionClasses, role, assocations, node) -> {
					// Always registered
				}, (action) -> {
					// Never execute action
				}));

		// Provide transparent padding around visual
		Pane visualContainer = new Pane(this.previewVisual);
		visualContainer.setPadding(new Insets(10));
		visualContainer.setBackground(null);

		// Load into the preview scene
		this.previewScene = new Scene(visualContainer);

		// Load specific styling
		AdaptedChildPart.loadStyling(this.previewVisual, model.getClass(), null);
		if (isParent) {
			AdaptedParentPart.loadStyling(this.previewVisual);
		}

		// Load the default styling
		AdaptedEditorPlugin.loadDefaulStylesheet(this.previewScene);

		// Load specific styling (if able)
		if (this.previewVisual instanceof Parent) {
			Parent previewParent = (Parent) this.previewVisual;
			StyleRegistry styleRegistry = AdaptedEditorPlugin.createStyleRegistry();
			ReadOnlyProperty<URL> styleUrl = styleRegistry.registerStyle("_preview_", this.styling);
			styleUrl.addListener((event, oldUrl, newUrl) -> {
				if (oldUrl != null) {
					previewParent.getStylesheets().remove(oldUrl.toExternalForm());
				}
				if (newUrl != null) {
					previewParent.getStylesheets().add(newUrl.toExternalForm());
				}
			});
		}
	}

	/**
	 * Obtains the preview {@link Scene}.
	 * 
	 * @return Preview {@link Scene}.
	 */
	public Scene getPreviewScene() {
		return this.previewScene;
	}

	/**
	 * Obtains the preview visual.
	 * 
	 * @return Preview visual.
	 */
	public Node getPreviewVisual() {
		return this.previewVisual;
	}

	/**
	 * Obtains the style {@link Property}.
	 * 
	 * @return Style {@link Property}.
	 */
	public Property<String> style() {
		return this.styling;
	}

}