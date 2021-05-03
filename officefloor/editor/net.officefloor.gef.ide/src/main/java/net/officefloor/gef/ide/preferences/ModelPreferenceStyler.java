/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
