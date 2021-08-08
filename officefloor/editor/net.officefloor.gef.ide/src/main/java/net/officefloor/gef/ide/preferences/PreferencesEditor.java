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
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.EditorStyler;
import net.officefloor.gef.editor.PaletteIndicatorStyler;
import net.officefloor.gef.editor.PaletteStyler;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor.ViewManager;
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
	 * {@link EnvironmentBridge}.
	 */
	private final EnvironmentBridge envBridge;

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
	 * @param editor    {@link AbstractAdaptedIdeEditor} to configure preferences.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public PreferencesEditor(AbstractAdaptedIdeEditor<R, ?, ?> editor, EnvironmentBridge envBridge) {
		this.editor = editor;
		this.envBridge = envBridge;

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
		this.visitPreferences((preferenceId) -> this.preferencesToChange.put(preferenceId, new PreferenceValue(null)));
	}

	/**
	 * Applies the preferences.
	 */
	public void apply() {
		for (String preferenceId : this.preferencesToChange.keySet()) {
			PreferenceValue value = this.preferencesToChange.get(preferenceId);
			if (value.value == null) {
				this.envBridge.resetPreference(preferenceId);
			} else {
				this.envBridge.setPreference(preferenceId, value.value);
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

		// Provide select only for styling
		PreferencesEditor<R> thiz = this;
		Map<Class<? extends Model>, ModelPreferenceStyler<?>> modelStylers = new HashMap<>();
		this.editor.setSelectOnly(new SelectOnly() {

			@Override
			public void paletteIndicator(PaletteIndicatorStyler styler) {
				this.showStyler("Palette Indicator",
						new NodePreferenceStyler(thiz.editor.getPaletteIndicatorStyleId(), styler.getPaletteIndicator(),
								thiz.editor.paletteIndicatorStyle(), styler.paletteIndicatorStyle(),
								thiz.preferencesToChange, thiz.envBridge, backgroundColour));
			}

			@Override
			public void palette(PaletteStyler styler) {
				this.showStyler("Palette",
						new NodePreferenceStyler(thiz.editor.getPaletteStyleId(), styler.getPalette(),
								thiz.editor.paletteStyle(), styler.paletteStyle(), thiz.preferencesToChange,
								thiz.envBridge, backgroundColour));
			}

			@Override
			public void editor(EditorStyler styler) {
				this.showStyler("Editor",
						new NodePreferenceStyler(thiz.editor.getEditorStyleId(), styler.getEditor(),
								thiz.editor.editorStyle(), styler.editorStyle(), thiz.preferencesToChange,
								thiz.envBridge, backgroundColour));
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

		// Initialise the editor
		this.editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});

		// Load the models to enable configuration
		R rootModel = this.editor.prototype();
		this.editor.setModel(rootModel);

		// Load editor view (with scene available)
		VBox editorView = new VBox();
		stackView.getChildren().add(editorView);
		ViewManager<R> viewManager = this.editor.loadView((view) -> {
			VBox.setVgrow(view, Priority.ALWAYS);
			editorView.getChildren().add(view);
			loader.accept(stackView);
		});

		// Add preference change listeners
		this.preferencesToChange.addListener(this.createPreferenceListener(this.editor.getPaletteIndicatorStyleId(),
				viewManager.paletteIndicatorStyle(), this.editor.paletteIndicatorStyle()));
		this.preferencesToChange.addListener(this.createPreferenceListener(this.editor.getPaletteStyleId(),
				viewManager.paletteStyle(), this.editor.paletteStyle()));
		this.preferencesToChange.addListener(this.createPreferenceListener(this.editor.getEditorStyleId(),
				viewManager.editorStyle(), this.editor.editorStyle()));

		// Load the prototype of all models
		AbstractItem<?, ?, ?, ?, ?, ?>[] parentItems = this.editor.getParents();
		for (int i = 0; i < parentItems.length; i++) {
			AbstractItem<?, ?, ?, ?, ?, ?> parentItem = parentItems[i];

			// Load the prototype model
			final int index = i;
			this.loadPrototypeModel(rootModel, parentItem, true, backgroundColour, modelStylers, (model, styler) -> {
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
	 * @param parentModel      Parent {@link Model}.
	 * @param item             {@link AbstractItem} to have its prototype loaded
	 *                         into the {@link Model}.
	 * @param isParent         Indicate if {@link AdaptedParent}.
	 * @param backgroundColour Background {@link Color}.
	 * @param modelStylers     {@link Map} of {@link Model} to
	 *                         {@link ModelPreferenceStyler} to be populated.
	 * @param editorWrapper    {@link EditorWrapper}.
	 * @param decorator        Decorator on the {@link Model}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadPrototypeModel(Model parentModel, AbstractItem item, boolean isParent, Color backgroundColour,
			Map<Class<? extends Model>, ModelPreferenceStyler<?>> modelStylers,
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
		this.envBridge.addPreferenceListener((event) -> {
			// Update style on preference changes (typically reseting to defaults)
			if (preferenceStyleId.equals(event.preferenceId)) {
				String updatedStyle = this.envBridge.getPreference(preferenceStyleId);
				String newRawStyle = (updatedStyle != null) && (updatedStyle.trim().length() > 0) ? updatedStyle
						: defaultStyle;
				rawStyle.setValue(newRawStyle);
			}
		});
		this.preferencesToChange.addListener(this.createPreferenceListener(preferenceStyleId, rawStyle, defaultStyle));

		// Create the item model
		Model itemModel = item.prototype();

		// Create and register the model styler
		ModelPreferenceStyler<?> styler = new ModelPreferenceStyler<>(item, isParent, this.preferencesToChange,
				this.envBridge, backgroundColour);
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
				this.loadPrototypeModel(itemModel, child, false, backgroundColour, modelStylers, null);
			}
		}
	}

	/**
	 * Creates preference change listener.
	 * 
	 * @param preferenceStyleId Identifier of preference.
	 * @param rawStyle          {@link Property} to change raw styling of property.
	 * @param defaultStyle      Default style.
	 * @return {@link MapChangeListener} for handling preference changes.
	 */
	private MapChangeListener<String, PreferenceValue> createPreferenceListener(String preferenceStyleId,
			Property<String> rawStyle, String defaultStyle) {
		return (Change<? extends String, ? extends PreferenceValue> event) -> {
			// Update the style on configuration changes
			if (preferenceStyleId.contentEquals(event.getKey())) {
				PreferenceValue preferredStyle = event.getValueAdded();
				String newRawStyle;
				if (preferredStyle == null) {
					// Clearing style, so reset to configuration/default
					newRawStyle = this.envBridge.getPreference(preferenceStyleId);
					if (newRawStyle == null) {
						newRawStyle = defaultStyle;
					}
				} else {
					// Use the new preferred style (or default if resetting)
					newRawStyle = preferredStyle.value != null ? preferredStyle.value : defaultStyle;
				}
				rawStyle.setValue(newRawStyle);
			}
		};
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
	@SuppressWarnings("rawtypes")
	private void visitItemPreferences(AbstractItem item, Consumer<String> visitor) {
		visitor.accept(item.getPreferenceStyleId());
		for (IdeChildrenGroup childrenGroup : item.getChildrenGroups()) {
			for (AbstractItem<?, ?, ?, ?, ?, ?> child : childrenGroup.getChildren()) {
				this.visitItemPreferences(child, visitor);
			}
		}
	}

}
