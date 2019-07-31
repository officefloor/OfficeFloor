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
package net.officefloor.gef.ide.editor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.editor.AdaptedBuilder;
import net.officefloor.gef.editor.AdaptedChildBuilder;
import net.officefloor.gef.editor.AdaptedEditorModule;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.ChildrenGroupBuilder;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.gef.ide.editor.AbstractItem.IdeChildrenGroup;
import net.officefloor.gef.ide.editor.AbstractItem.PreferenceListener;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Abstract adapted IDE editor.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedIdeEditor<R extends Model, RE extends Enum<RE>, O> {

	/**
	 * Translates the style with the details of the item being rendered.
	 *
	 * @param rawStyle Raw style to be translated.
	 * @param item     {@link AbstractItem}.
	 * @return Ready to use translate.
	 */
	public static String translateStyle(String rawStyle, AbstractItem<?, ?, ?, ?, ?, ?> item) {

		// Ensure have a style
		if (rawStyle == null) {
			return null;
		}

		// Translate the style
		return rawStyle.replace("${model}", item.prototype().getClass().getSimpleName());
	}

	/**
	 * {@link AdaptedBuilder}.
	 */
	private final AdaptedBuilder adaptedBuilder;

	/**
	 * {@link Function} to create the operations from the root {@link Model}.
	 */
	private final Function<R, O> createOperations;

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private WritableConfigurationItem configurationItem;

	/**
	 * {@link AdaptedRootBuilder}.
	 */
	private AdaptedRootBuilder<R, O> rootBuilder;

	/**
	 * {@link Model} being edited.
	 */
	private R model;

	/**
	 * Operations.
	 */
	private O operations;

	/**
	 * {@link AbstractConfigurableItem} instances for this
	 * {@link AbstractIdeEclipseEditor}.
	 */
	private List<AbstractConfigurableItem<R, RE, O, ?, ?, ?>> parents = null;

	/**
	 * {@link ConfigurableContext}.
	 */
	private ConfigurableContext<R, O> configurableContext;

	/**
	 * {@link SelectOnly}.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * <p>
	 * Instantiate to capture {@link AdaptedEditorModule}.
	 * <p>
	 * Allows for alternate {@link AdaptedEditorModule} implementation.
	 * 
	 * @param module           {@link AdaptedEditorModule}.
	 * @param rootModelType    Root {@link Model} type.
	 * @param createOperations {@link Function} to create the operations from the
	 *                         root {@link Model}.
	 * @param envBridge        {@link EnvironmentBridge}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AbstractAdaptedIdeEditor(Class<R> rootModelType, Function<R, O> createOperations,
			EnvironmentBridge envBridge) {
		this.createOperations = createOperations;

		// Create the adapted builder
		this.adaptedBuilder = (context) -> {

			// Create the root model
			this.rootBuilder = context.root(rootModelType, (model) -> {
				// Keep track of operations (for configurable context)
				this.operations = this.createOperations(model);
				return this.operations;
			});

			// Create the configurable context
			this.configurableContext = new ConfigurableContext<R, O>() {

				@Override
				public AdaptedRootBuilder<R, O> getRootBuilder() {
					return AbstractAdaptedIdeEditor.this.rootBuilder;
				}

				@Override
				public EnvironmentBridge getEnvironmentBridge() throws Exception {
					return envBridge;
				}

				@Override
				public O getOperations() {
					return AbstractAdaptedIdeEditor.this.operations;
				}

				@Override
				public ChangeExecutor getChangeExecutor() {
					return AbstractAdaptedIdeEditor.this.rootBuilder.getChangeExecutor();
				}

				@Override
				public String getPreference(String preferenceId) {
//					IPreferenceStore preferences = AdaptedIdeEditor.this.preferenceStore;
//					return preferences == null ? null : preferences.getString(preferenceId);
					return null;
				}

				@Override
				public void addPreferenceListener(String preferenceId, PreferenceListener preferenceListener) {
//					IPreferenceStore preferences = AdaptedIdeEditor.this.preferenceStore;
//					if (preferences != null) {
//
//						// Create and register listening to preference change
//						IPropertyChangeListener changeListener = (event) -> {
//							if (preferenceId.equals(event.getProperty())) {
//
//								// Obtain the value
//								Object value = event.getNewValue();
//								if (!(value instanceof String)) {
//									value = value.toString();
//								}
//
//								// Notify of value change
//								preferenceListener.preferenceValueChanged((String) value);
//							}
//						};
//						preferences.addPropertyChangeListener(changeListener);
//
//						// Register for removing on editor close
//						AdaptedIdeEditor.this.preferenceChangeListeners.add(changeListener);
//					}
				}
			};

			// Allow initialising of editor
			this.init(this.configurableContext);

			// Configure the editor
			for (AbstractConfigurableItem parent : this.getParents()) {

				// Initialise the parent
				parent.init(this.configurableContext);

				// Create the adapted parent
				AdaptedParentBuilder parentBuilder = parent.createAdaptedParent();

				// Load the children
				loadChildren(parent, parentBuilder);
			}
		};
	}

	/**
	 * Obtains the {@link ConfigurableContext}
	 * 
	 * @return
	 */
	public ConfigurableContext<R, O> getConfigurableContext() {
		return this.configurableContext;
	}

	/**
	 * Loads the children and their children recursively.
	 * 
	 * @param parent              Parent {@link AbstractItem}.
	 * @param parentBuilder       Parent {@link AdaptedChildBuilder}.
	 * @param configurableContext {@link ConfigurableContext}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadChildren(AbstractItem parent, AdaptedChildBuilder parentBuilder) {

		// Load the connections
		for (AbstractItem<?, ?, ?, ?, ?, ?>.IdeConnectionTarget<? extends ConnectionModel, ?, ?> connection : parent
				.getConnections()) {
			connection.loadConnection(parentBuilder);
		}

		// Load styling
		this.loadPreferredStyling(parent.getPreferenceStyleId(), parentBuilder.style(), parent.style(),
				(rawStyle) -> translateStyle(rawStyle, parent));

		// Create the children groups
		for (IdeChildrenGroup ideChildrenGroup : parent.getChildrenGroups()) {

			// Add the children group
			ChildrenGroupBuilder childrenGroup = parentBuilder.children(ideChildrenGroup.getChildrenGroupName(),
					ideChildrenGroup, ideChildrenGroup.changeEvents());

			// Create the children
			for (AbstractItem child : ideChildrenGroup.getChildren()) {

				// Initialise the child
				child.init(this.configurableContext);

				// Build the child
				AdaptedChildBuilder childBuilder = child.createChild(childrenGroup);

				// Load the grand children (and connections)
				this.loadChildren(child, childBuilder);
			}
		}
	}

	/**
	 * Loads the preferred styling.
	 * 
	 * @param preferenceId Preference identifier for the preferred styling.
	 * @param style        {@link Property} to be loaded with the styling.
	 * @param defaultStyle Default style. May be <code>null</code>.
	 * @param translator   Optional translator to translate the raw styling. May be
	 *                     <code>null</code>.
	 */
	private void loadPreferredStyling(String preferenceId, Property<String> style, String defaultStyle,
			Function<String, String> translator) {

		// Function to apply the style
		Consumer<String> applyStyle = (rawStyle) -> {

			// Use appropriate style
			if ((rawStyle == null) || (rawStyle.trim().length() == 0)) {
				rawStyle = defaultStyle;
			}

			// Translate the style ready for use
			String translatedStyle = translator == null ? rawStyle : translator.apply(rawStyle);

			// Load the style
			style.setValue(translatedStyle);
		};

		// Obtain the override style
		String overrideStyle = this.configurableContext.getPreference(preferenceId);

		// Set initial style (and listen to changes)
		applyStyle.accept(overrideStyle);
		this.configurableContext.addPreferenceListener(preferenceId, (value) -> applyStyle.accept(value));
	}

	/**
	 * Obtains the preference identifier for the palette indicator styling.
	 * 
	 * @return Preference identifier for the palette indicator styling.
	 */
	public String getPaletteIndicatorStyleId() {
		return this.prototype().getClass().getSimpleName() + ".palette.indicator.style";
	}

	/**
	 * Obtains the preference identifier for the palette styling.
	 * 
	 * @return Preference identifier for the palette styling.
	 */
	public String getPaletteStyleId() {
		return this.prototype().getClass().getSimpleName() + ".palette.style";
	}

	/**
	 * Obtains the preference identifier for the editor styling.
	 * 
	 * @return Preference identifier for the editor styling.
	 */
	public String getEditorStyleId() {
		return this.prototype().getClass().getSimpleName() + ".editor.style";
	}

	/**
	 * Instantiate.
	 * 
	 * @param selectOnly {@link SelectOnly}.
	 */
	public void setSelectOnly(SelectOnly selectOnly) {
		this.selectOnly = selectOnly;
	}

	/**
	 * Specifies the {@link WritableConfigurationItem}.
	 * 
	 * @param configurationItem {@link WritableConfigurationItem}.
	 */
	public void setConfigurationItem(WritableConfigurationItem configurationItem) {
		this.configurationItem = configurationItem;
	}

	/**
	 * Specifies the {@link Model}.
	 * 
	 * @param model {@link Model}.
	 */
	public void setModel(R model) {
		this.model = model;
	}

	/**
	 * Allows overriding to initialise the {@link AbstractIdeEclipseEditor}.
	 * 
	 * @param context {@link ConfigurableContext}.
	 */
	protected void init(ConfigurableContext<R, O> context) {
	}

	/**
	 * Obtains the {@link AbstractConfigurableItem} instances.
	 * 
	 * @return {@link AbstractConfigurableItem} instances.
	 */
	@SuppressWarnings("unchecked")
	public final AbstractConfigurableItem<R, RE, O, ?, ?, ?>[] getParents() {

		// Lazy load the parents
		if (this.parents == null) {
			this.parents = new LinkedList<>();
			this.loadParents(this.parents);
		}

		// Return the parents
		return parents.toArray(new AbstractConfigurableItem[parents.size()]);
	}

	/**
	 * Obtains the default file name for the editor.
	 * 
	 * @return Default file name for the editor.
	 */
	public abstract String fileName();

	/**
	 * Obtains root prototype.
	 * 
	 * @return Root prototype.
	 */
	public abstract R prototype();

	/**
	 * Loads the {@link AbstractConfigurableItem} instances.
	 * 
	 * @param parents {@link List} to be populated with the
	 *                {@link AbstractConfigurableItem} instances.
	 */
	protected abstract void loadParents(List<AbstractConfigurableItem<R, RE, O, ?, ?, ?>> parents);

	/**
	 * Creates the root {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param configurationItem {@link ConfigurationItem} containing the
	 *                          configuration of the {@link Model}.
	 * @return Root {@link Model} within the {@link ConfigurationItem}.
	 * @throws Exception If fails to load the root {@link Model} from the
	 *                   {@link ConfigurationItem}.
	 */
	protected abstract R loadRootModel(ConfigurationItem configurationItem) throws Exception;

	/**
	 * Writes the root {@link Model} to the {@link WritableConfigurationItem}.
	 * 
	 * @param model             Root {@link Model} to be saved.
	 * @param configurationItem {@link WritableConfigurationItem}.
	 * @throws Exception If fails to save the root {@link Model} into the
	 *                   {@link WritableConfigurationItem}.
	 */
	public abstract void saveRootModel(R model, WritableConfigurationItem configurationItem) throws Exception;

	/**
	 * Allows overriding the palette styling.
	 * 
	 * @return Palette styling. May be <code>null</code> for default styling.
	 */
	public String paletteStyle() {
		return null;
	}

	/**
	 * Allows overriding the palette indicator styling.
	 * 
	 * @return Palette indicator styling. May be <code>null</code> for default
	 *         styling.
	 */
	public String paletteIndicatorStyle() {
		return null;
	}

	/**
	 * Allows overriding the editor styling.
	 * 
	 * @return Editor styling. May be <code>null</code> for defaulting styling.
	 */
	public String editorStyle() {
		return null;
	}

	/**
	 * Creates the operations for the root {@link Model}.
	 * 
	 * @param model Root {@link Model}.
	 * @return Operations.
	 */
	public O createOperations(R model) {
		return this.createOperations.apply(model);
	}

	private AdaptedEditorModule module;

	private IDomain domain;

	public void init(Module overrideModule, Function<Injector, IDomain> initialiser) {

		this.module = new AdaptedEditorModule();
		Injector injector = Guice.createInjector(
				overrideModule == null ? this.module : Modules.override(this.module).with(overrideModule));

		// Initialise
		this.domain = initialiser.apply(injector);
		this.module.initialise(this.domain, injector);
	}

	/**
	 * Activates this {@link AbstractAdaptedIdeEditor}.
	 */
	public void loadView(Consumer<Pane> viewLoader) {

		// Provide possible select only
		if (this.selectOnly != null) {
			module.setSelectOnly(this.selectOnly);
		}

		// Create the view
		Pane view = this.module.createParent(this.adaptedBuilder);

		// Populate the view
		viewLoader.accept(view);

		// Configure styling of editor
		this.loadPreferredStyling(this.getEditorStyleId(), this.rootBuilder.editorStyle(), this.editorStyle(), null);

		// Configure styling of palette indicator
		this.loadPreferredStyling(this.getPaletteIndicatorStyleId(), this.rootBuilder.paletteIndicatorStyle(),
				this.paletteIndicatorStyle(), null);

		// Configure styling of palette
		this.loadPreferredStyling(this.getPaletteStyleId(), this.rootBuilder.paletteStyle(), this.paletteStyle(), null);

		// Load the model
		if (this.model == null) {
			this.rootBuilder.getErrorHandler().isError(() -> {
				this.model = this.loadRootModel(this.configurationItem);
			});
		}

		// Load the module with root model
		this.module.loadRootModel(this.model);
	}

	/**
	 * Saves changes.
	 */
	public void save() {
		this.rootBuilder.getErrorHandler().isError(() -> {

			// Save the model
			this.saveRootModel(this.model, this.configurationItem);
		});
	}

}