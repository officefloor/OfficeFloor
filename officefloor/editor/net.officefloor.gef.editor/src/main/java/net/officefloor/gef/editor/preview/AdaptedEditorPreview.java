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

package net.officefloor.gef.editor.preview;

import java.net.URL;

import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.officefloor.gef.editor.AdaptedChildVisualFactory;
import net.officefloor.gef.editor.AdaptedEditorPlugin;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.internal.parts.AdaptedChildPart;
import net.officefloor.gef.editor.internal.parts.AdaptedChildVisualFactoryContextImpl;
import net.officefloor.gef.editor.internal.parts.AdaptedParentPart;
import net.officefloor.gef.editor.style.StyleRegistry;
import net.officefloor.model.Model;

/**
 * Preview of an {@link AdaptedModel} {@link IVisualPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedEditorPreview<M extends Model> {

	/**
	 * Preview visual {@link Node}.
	 */
	private final Node previewVisual;

	/**
	 * Preview container.
	 */
	private final Pane previewContainer;

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
	 * @param visualFactory {@link AdaptedChildVisualFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedEditorPreview(M model, String label, boolean isParent, AdaptedChildVisualFactory<M> visualFactory) {

		// Create the visual
		this.previewVisual = visualFactory.createVisual(model,
				new AdaptedChildVisualFactoryContextImpl<>((Class<M>) model.getClass(), false, () -> {
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

		// Load specific styling
		AdaptedChildPart.loadStyling(this.previewVisual, model.getClass(), null);
		if (isParent) {
			AdaptedParentPart.loadStyling(this.previewVisual);
		}

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

		// Center preview visual
		Pane topMargin = new Pane();
		VBox.setVgrow(topMargin, Priority.ALWAYS);
		Pane bottomMargin = new Pane();
		VBox.setVgrow(bottomMargin, Priority.ALWAYS);
		Pane leftMargin = new Pane();
		HBox.setHgrow(leftMargin, Priority.ALWAYS);
		Pane rightMargin = new Pane();
		HBox.setHgrow(rightMargin, Priority.ALWAYS);
		this.previewContainer = new VBox(topMargin, new HBox(leftMargin, this.previewVisual, rightMargin),
				bottomMargin);
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
	 * Obtains the preview container.
	 * 
	 * @return Preview container.
	 */
	public Pane getPreviewContainer() {
		return this.previewContainer;
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
