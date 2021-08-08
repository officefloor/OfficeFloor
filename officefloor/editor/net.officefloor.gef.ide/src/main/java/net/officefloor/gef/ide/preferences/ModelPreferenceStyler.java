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

import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.preview.AdaptedEditorPreview;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.Model;

/**
 * Style for the {@link Model} preferences.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelPreferenceStyler<M extends Model> extends AbstractPreferenceStyler {

	/**
	 * {@link AbstractItem}.
	 */
	private final AbstractItem<?, ?, ?, ?, M, ?> item;

	/**
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Instantiate.
	 * 
	 * @param item                {@link AbstractItem}.
	 * @param isParent            Indicates if {@link AdaptedParent}.
	 * @param preferencesToChange Loaded with the preference changes.
	 * @param envBridge           {@link EnvironmentBridge}.
	 * @param backgroundColour    Background {@link Color}.
	 */
	public ModelPreferenceStyler(AbstractItem<?, ?, ?, ?, M, ?> item, boolean isParent,
			ObservableMap<String, PreferenceValue> preferencesToChange, EnvironmentBridge envBridge,
			Color backgroundColour) {
		super(preferencesToChange, envBridge, backgroundColour);
		this.item = item;
		this.isParent = isParent;
	}

	/*
	 * ===================== AbstractPreferenceStyler ========================
	 */

	@Override
	protected PreferenceConfiguration init() {

		// Obtain the prototype
		M prototype = this.item.prototype();

		// Obtain label for the item
		AbstractItem<?, ?, ?, ?, M, ?>.IdeLabeller labeller = this.item.label();
		String itemName = this.defaultString((labeller == null) ? null : labeller.getLabel(prototype),
				this.item.getClass().getSimpleName());

		// Provide the preview
		AdaptedEditorPreview<M> preview = new AdaptedEditorPreview<>(prototype, itemName, this.isParent,
				(model, context) -> this.item.visual(model, context));

		// Create and return configuration
		return new PreferenceConfiguration(this.item.getPreferenceStyleId(), preview.getPreviewVisual(), item.style(),
				preview.style(), preview.getPreviewContainer(),
				(rawStyle) -> AbstractAdaptedIdeEditor.translateStyle(rawStyle, this.item));
	}

}
