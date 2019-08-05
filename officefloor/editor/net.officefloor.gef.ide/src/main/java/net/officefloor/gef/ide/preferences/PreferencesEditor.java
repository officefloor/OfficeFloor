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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Inject;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.EditorStyler;
import net.officefloor.gef.editor.PaletteIndicatorStyler;
import net.officefloor.gef.editor.PaletteStyler;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.ide.editor.AbstractItem.IdeChildrenGroup;
import net.officefloor.gef.ide.editor.AbstractItem.IdeLabeller;
import net.officefloor.model.Model;

/**
 * Editor for preferences.
 * 
 * @author Daniel Sagenschneider
 */
public class PreferencesEditor<R extends Model> {

	/**
	 * {@link AbstractAdaptedIdeEditor} to configure preferences.
	 */
	private final AbstractAdaptedIdeEditor<R, ?, ?> editor;

	/**
	 * {@link EditorPreferences}.
	 */
	private final EditorPreferences editorPreferences;

	/**
	 * {@link ObservableMap} of preferences to change within the
	 * {@link EditorPreferences}.
	 */
	private final ObservableMap<String, String> preferencesToChange = FXCollections.observableHashMap();

	/**
	 * {@link IDomain}.
	 */
	@Inject
	private IDomain domain;

	/**
	 * Instantiate.
	 * 
	 * @param editor            {@link AbstractAdaptedIdeEditor} to configure
	 *                          preferences.
	 * @param editorPreferences {@link EditorPreferences}.
	 */
	public PreferencesEditor(AbstractAdaptedIdeEditor<R, ?, ?> editor, EditorPreferences editorPreferences) {
		this.editor = editor;
		this.editorPreferences = editorPreferences;
	}

	/**
	 * Loads the view.
	 * 
	 * @param loader Receives the loaded visual.
	 */
	public void loadView(Consumer<Pane> loader) {

		// Initialise the editor
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});

		// Provide select only for styling
		Map<Model, ModelPreferenceStyler> modelStylers = new HashMap<>();
		Map<Class<? extends Model>, ModelPreferenceStyler> parentStylers = new HashMap<>();
		editor.setSelectOnly(new SelectOnly() {

			@Override
			public void paletteIndicator(PaletteIndicatorStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException();
			}

			@Override
			public void palette(PaletteStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException();
			}

			@Override
			public void editor(EditorStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException();
			}

			@Override
			public void model(AdaptedModelStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException();
			}
		});

		// Load the models to enable configuration
		R rootModel = this.editor.prototype();
		this.editor.setModel(rootModel);

		// Load editor view (with scene available)
		this.editor.loadView(loader);

		// Load the prototype of all models
		AbstractItem<?, ?, ?, ?, ?, ?>[] parentItems = this.editor.getParents();
		for (int i = 0; i < parentItems.length; i++) {
			AbstractItem<?, ?, ?, ?, ?, ?> parentItem = parentItems[i];

			// Load the prototype model
			final int index = i;
			this.loadPrototypeModel(rootModel, parentItem, true, this.editorPreferences, modelStylers,
					(model, styler) -> {
						// Space out the prototypes
						model.setX(300);
						model.setY(10 + (100 * index));

						// Register parents (prototypes in palette different instance)
						parentStylers.put(model.getClass(), styler);
					});
		}

		// Activate
		this.domain.activate();
	}

	/**
	 * Recursively loads the prototype {@link Model}.
	 * 
	 * @param parentModel       Parent {@link Model}.
	 * @param item              {@link AbstractItem} to have its prototype loaded
	 *                          into the {@link Model}.
	 * @param isParent          Indicate if {@link AdaptedParent}.
	 * @param editorPreferences {@link EditorPreferences}.
	 * @param modelStylers      {@link Map} of {@link Model} to
	 *                          {@link ModelPreferenceStyler} to be populated.
	 * @param editorWrapper     {@link EditorWrapper}.
	 * @param decorator         Decorator on the {@link Model}.
	 * @return Prototype {@link Model} from the {@link AbstractItem}.
	 */
	private Model loadPrototypeModel(Model parentModel, AbstractItem item, boolean isParent,
			EditorPreferences editorPreferences, Map<Model, ModelPreferenceStyler> modelStylers,
			BiConsumer<Model, ModelPreferenceStyler> decorator) {

		// Obtain the prototype for the item
		Model itemModel = item.prototype();

		// Obtain label for the item
		IdeLabeller labeller = item.label();
		String itemName = (labeller == null) ? null : labeller.getLabel(itemModel);
		if ((itemName == null) || (itemName.trim().length() == 0)) {
			itemName = item.getClass().getSimpleName();
		}
		final String itemLabel = itemName;

		// Obtain preference style identifier
		String preferenceStyleId = item.getPreferenceStyleId();

		// Obtain the style property to change the appearance
		Property<String> style = item.getBuilder().style();

		// Obtain the styling
		String defaultStyle = item.style();
		String overrideStyle = editorPreferences.getPreference(preferenceStyleId);

		// Create the style property
		Property<String> rawStyle = new SimpleStringProperty(
				(overrideStyle != null) && (overrideStyle.trim().length() > 0) ? overrideStyle : defaultStyle);
		rawStyle.addListener((event, oldValue, newValue) -> {
			// Translate and update the style
			String translatedStyle = AbstractAdaptedIdeEditor.translateStyle(newValue, item);
			style.setValue(translatedStyle);
		});
		editorPreferences.addPreferenceListener((event) -> {
			// Update style on preference changes (typically reseting to defaults)
			if (preferenceStyleId.equals(event.preferenceId)) {
				String updatedStyle = editorPreferences.getPreference(preferenceStyleId);
				String newRawStyle = (updatedStyle != null) && (updatedStyle.trim().length() > 0) ? updatedStyle
						: defaultStyle;
				rawStyle.setValue(newRawStyle);
			}
		});

		// Create and register the model styler
		ModelPreferenceStyler styler = new ModelPreferenceStyler(item, itemModel, itemLabel, isParent, rawStyle,
				defaultStyle, this.preferencesToChange);
		modelStylers.put(itemModel, styler);

		// Determine if decorate the model
		if (decorator != null) {
			decorator.accept(itemModel, styler);
		}

		// Connect into the model
		item.loadToParent(parentModel, itemModel);

		// Load child items
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem child : childrenGroup.getChildren()) {
				this.loadPrototypeModel(itemModel, child, false, editorPreferences, modelStylers, null);
			}
		}

		// Return the item model
		return itemModel;
	}

}