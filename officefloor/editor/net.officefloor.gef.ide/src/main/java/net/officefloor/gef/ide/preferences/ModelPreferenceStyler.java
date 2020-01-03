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