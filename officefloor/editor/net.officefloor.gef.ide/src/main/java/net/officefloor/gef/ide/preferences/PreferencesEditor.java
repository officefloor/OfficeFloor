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
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.EditorStyler;
import net.officefloor.gef.editor.PaletteIndicatorStyler;
import net.officefloor.gef.editor.PaletteStyler;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.ide.editor.AbstractItem.IdeChildrenGroup;
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
	private final ObservableMap<String, PreferenceValue> preferencesToChange = FXCollections.observableHashMap();

	/**
	 * Indicates if dirty (requires save).
	 */
	private final SimpleBooleanProperty isDirty = new SimpleBooleanProperty();

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

		// Hook in listening to whether dirty
		this.preferencesToChange.addListener((Change<? extends String, ? extends PreferenceValue> event) -> {
			this.isDirty.setValue(this.preferencesToChange.size() > 0);
		});
	}

	/**
	 * Indicates whether dirty (requires saving).
	 * 
	 * @return {@link Property} to track whether requires saving.
	 */
	public ReadOnlyProperty<Boolean> dirtyProperty() {
		return this.isDirty;
	}

	/**
	 * Resets all preferences to their defaults.
	 */
	public void resetToDefaults() {
		this.visitPreferences((preferenceId) -> this.editorPreferences.resetPreference(preferenceId));
		this.preferencesToChange.clear();
	}

	/**
	 * Applies the preferences.
	 */
	public void apply() {
		for (String preferenceId : this.preferencesToChange.keySet()) {
			PreferenceValue value = this.preferencesToChange.get(preferenceId);
			if (value.value == null) {
				this.editorPreferences.resetPreference(preferenceId);
			} else {
				this.editorPreferences.setPreference(preferenceId, value.value);
			}
		}
		this.preferencesToChange.clear();
	}

	/**
	 * Cancels the changes to the preferences.
	 */
	public void cancel() {
		this.preferencesToChange.clear();
	}

	/**
	 * Loads the view.
	 * 
	 * @param loader Receives the visual for the entire preview editor.
	 * @return Editor view enable decorating.
	 */
	public Pane loadView(Consumer<Pane> loader) {

		// Provide stack pane to have overlay of style editors
		StackPane stackView = new StackPane();

		// Determine the background colour
		Color backgroundColour = Color.LIGHTGREY;

		// Initialise the editor
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});

		// Provide select only for styling
		Map<Class<? extends Model>, ModelPreferenceStyler<?>> modelStylers = new HashMap<>();
		editor.setSelectOnly(new SelectOnly() {

			@Override
			public void paletteIndicator(PaletteIndicatorStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException("TODO STYLE paletteIndicator");
			}

			@Override
			public void palette(PaletteStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException("TODO STYLE palette");
			}

			@Override
			public void editor(EditorStyler styler) {
				// TODO implement
				throw new UnsupportedOperationException("TODO STYLE editor");
			}

			@Override
			public void model(AdaptedModelStyler styler) {
				Class<?> modelType = styler.getModel().getClass();
				this.showStyler("Model " + modelType.getSimpleName(), modelStylers.get(modelType));
			}

			private void showStyler(String typeDescription, PreferenceStyler styler) {

				// Obtain the visual
				Pane visual;

				// Create close operation
				Runnable close = () -> {
					// Remove all views on top of preference editor
					while (stackView.getChildren().size() > 1) {
						stackView.getChildren().remove(1);
					}
				};

				// Ensure have styler
				if (styler == null) {
					VBox noStyler = new VBox();
					noStyler.getChildren().add(new Text("No styler availabe for " + typeDescription));
					Button closeButton = new Button("close");
					closeButton.setOnAction((event) -> close.run());
					noStyler.getChildren().add(closeButton);
					visual = noStyler;
				} else {
					// Create the styler
					visual = styler.createVisual(close);
				}

				// Ensure any previous editors are closed
				close.run();

				// Add the styler
				stackView.getChildren().add(visual);
			}
		});

		// Load the models to enable configuration
		R rootModel = this.editor.prototype();
		this.editor.setModel(rootModel);

		// Load editor view (with scene available)
		VBox editorView = new VBox();
		VBox.setVgrow(editorView, Priority.ALWAYS);
		stackView.getChildren().add(editorView);
		this.editor.loadView((view) -> {
			editorView.getChildren().add(view);
			loader.accept(stackView);
		});

		// Load the prototype of all models
		AbstractItem<?, ?, ?, ?, ?, ?>[] parentItems = this.editor.getParents();
		for (int i = 0; i < parentItems.length; i++) {
			AbstractItem<?, ?, ?, ?, ?, ?> parentItem = parentItems[i];

			// Load the prototype model
			final int index = i;
			this.loadPrototypeModel(rootModel, parentItem, true, backgroundColour, this.editorPreferences, modelStylers,
					(model, styler) -> {
						// Space out the prototypes
						model.setX(300);
						model.setY(10 + (100 * index));
					});
		}

		// Activate
		this.domain.activate();

		// Return the editor view
		return editorView;
	}

	/**
	 * Recursively loads the prototype {@link Model}.
	 * 
	 * @param parentModel       Parent {@link Model}.
	 * @param item              {@link AbstractItem} to have its prototype loaded
	 *                          into the {@link Model}.
	 * @param isParent          Indicate if {@link AdaptedParent}.
	 * @param backgroundColour  Background {@link Color}.
	 * @param editorPreferences {@link EditorPreferences}.
	 * @param modelStylers      {@link Map} of {@link Model} to
	 *                          {@link ModelPreferenceStyler} to be populated.
	 * @param editorWrapper     {@link EditorWrapper}.
	 * @param decorator         Decorator on the {@link Model}.
	 */
	private void loadPrototypeModel(Model parentModel, AbstractItem item, boolean isParent, Color backgroundColour,
			EditorPreferences editorPreferences, Map<Class<? extends Model>, ModelPreferenceStyler<?>> modelStylers,
			BiConsumer<Model, ModelPreferenceStyler<?>> decorator) {

		// Obtain preference style identifier
		String preferenceStyleId = item.getPreferenceStyleId();

		// Obtain the styling
		String defaultStyle = item.style();

		// Create the style property
		Property<String> style = item.getBuilder().style();
		Property<String> rawStyle = new SimpleStringProperty();
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
		this.preferencesToChange.addListener((Change<? extends String, ? extends PreferenceValue> event) -> {
			// Update the style on configuration changes
			if (preferenceStyleId.contentEquals(event.getKey())) {
				PreferenceValue preferredStyle = event.getValueAdded();
				String newRawStyle;
				if (preferredStyle == null) {
					// Clearing style, so reset to configuration/default
					newRawStyle = this.editorPreferences.getPreference(preferenceStyleId);
					if (newRawStyle == null) {
						newRawStyle = defaultStyle;
					}
				} else {
					// Use the new preferred style (or default if resetting)
					newRawStyle = preferredStyle.value != null ? preferredStyle.value : defaultStyle;
				}
				rawStyle.setValue(newRawStyle);
			}
		});

		// Create the item model
		Model itemModel = item.prototype();

		// Create and register the model styler
		ModelPreferenceStyler styler = new ModelPreferenceStyler(item, isParent, this.preferencesToChange,
				this.editorPreferences, backgroundColour);
		modelStylers.put(itemModel.getClass(), styler);

		// Determine if decorate the model
		if (decorator != null) {
			decorator.accept(itemModel, styler);
		}

		// Connect into the model
		item.loadToParent(parentModel, itemModel);

		// Load child items
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem child : childrenGroup.getChildren()) {
				this.loadPrototypeModel(itemModel, child, false, backgroundColour, editorPreferences, modelStylers,
						null);
			}
		}
	}

	/**
	 * Visits all preferences.
	 * 
	 * @param visitor Visitor for the preferences.
	 */
	private void visitPreferences(Consumer<String> visitor) {
		visitor.accept(this.editor.getPaletteIndicatorStyleId());
		visitor.accept(this.editor.getPaletteStyleId());
		visitor.accept(this.editor.getEditorStyleId());
		for (AbstractItem<?, ?, ?, ?, ?, ?> item : this.editor.getParents()) {
			this.visitItemPreferences(item, visitor);
		}
	}

	/**
	 * Visits the {@link AbstractItem}.
	 * 
	 * @param item    {@link AbstractItem} being visited.
	 * @param visitor {@link Consumer} visitor.
	 */
	private void visitItemPreferences(AbstractItem item, Consumer<String> visitor) {
		visitor.accept(item.getPreferenceStyleId());
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem<?, ?, ?, ?, ?, ?> child : childrenGroup.getChildren()) {
				this.visitItemPreferences(child, visitor);
			}
		}
	}

}