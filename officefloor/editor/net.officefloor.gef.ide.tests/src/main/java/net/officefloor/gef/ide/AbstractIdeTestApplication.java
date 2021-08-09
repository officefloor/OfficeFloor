/*-
 * #%L
 * net.officefloor.gef.ide.tests
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

package net.officefloor.gef.ide;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Inject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.gef.bridge.ClassLoaderEnvironmentBridge;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.editor.ChangeAdapter;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor.ViewManager;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.ConfigurableModelContext;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.IdeConfiguration;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.ItemConfigurer;
import net.officefloor.gef.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.gef.ide.preferences.PreferencesEditor;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Abstract IDE Editor {@link Application}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractIdeTestApplication<R extends Model, RE extends Enum<RE>, O> extends Application {

	/**
	 * Decorators for the of {@link AbstractConfigurableItem} prototypes.
	 */
	private final Map<Class<?>, Consumer<?>> prototypeDecorators = new HashMap<>();

	@Inject
	private IDomain domain;

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor(EnvironmentBridge envBridge);

	/**
	 * Obtains path to the {@link WritableConfigurationItem}.
	 * 
	 * @return Path to the {@link WritableConfigurationItem}.
	 */
	protected abstract String getConfigurationFileName();

	/**
	 * Obtains path to the {@link WritableConfigurationItem} to replace root
	 * {@link Model}.
	 * 
	 * @return Path to the {@link WritableConfigurationItem} to replace root
	 *         {@link Model}.
	 */
	protected abstract String getReplaceConfigurationFileName();

	/**
	 * Registers a prototype decorator.
	 * 
	 * @param <P>           Prototype type.
	 * @param prototypeType Prototype {@link Class}.
	 * @param decorator     Prototype decorator.
	 */
	protected <P> void register(Class<P> prototypeType, Consumer<P> decorator) {
		this.prototypeDecorators.put(prototypeType, decorator);
	}

	/**
	 * Loads the {@link WritableConfigurationItem}.
	 */
	@FunctionalInterface
	private static interface LoadConfiguration {
		InputStream loadConfiguration(String configurationFileName) throws Exception;
	}

	/*
	 * =============== Application =============================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = this.getClass().getClassLoader();
		}

		// Setup environment
		EnvironmentBridge envBridge = new ClassLoaderEnvironmentBridge(classLoader);

		// Function to load configuration
		LoadConfiguration loadConfiguration = (configurationFileName) -> {
			String configurationPath = this.getClass().getPackage().getName().replace('.', '/') + "/"
					+ configurationFileName;
			InputStream configurationContent = envBridge.getClassLoader().getResourceAsStream(configurationPath);
			if (configurationContent == null) {
				throw new FileNotFoundException("Can not find configuration on class path: " + configurationPath);
			}
			return configurationContent;
		};

		// Obtain the configuration
		InputStream configurationContent = loadConfiguration.loadConfiguration(this.getConfigurationFileName());

		// Obtain the configuration item
		WritableConfigurationItem configurationItem = MemoryConfigurationContext
				.createWritableConfigurationItem(this.getConfigurationFileName());
		configurationItem.setConfiguration(configurationContent);

		// Create tabs for various items being tested
		TabPane folder = new TabPane();

		// Load the editor
		Tab editorTab = new Tab("Editor");
		folder.getTabs().add(editorTab);
		AbstractAdaptedIdeEditor<R, RE, O> editor = this.createEditor(envBridge);
		editor.initNonOsgiEnvironment();
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});
		editor.setConfigurationItem(configurationItem);

		// Display stage (required by editor)
		stage.setScene(new Scene(folder));
		stage.setWidth(1600);
		stage.setHeight(1200);
		stage.show();

		// Provide reload button
		VBox editorContainer = new VBox();
		editorTab.setContent(editorContainer);

		// Load editor view (with scene available)
		ViewManager<R> viewManager = editor.loadView((view) -> {
			VBox.setVgrow(view, Priority.ALWAYS);
			editorContainer.getChildren().add(view);
		});
		this.domain.activate();

		// Add listener for changes
		editor.getConfigurableContext().getChangeExecutor().addChangeListener(new ChangeAdapter() {
			@Override
			public void postApply(Change<?> change) {

				// Write out the change
				viewManager.save();
				System.out.println("=============== " + change.getChangeDescription() + " ===============");
				BufferedReader reader = new BufferedReader(configurationItem.getReader());
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		// Handle replace model
		BorderPane editorButtons = new BorderPane();
		VBox.setVgrow(editorButtons, Priority.NEVER);
		editorButtons.setPadding(new Insets(10));
		editorContainer.getChildren().add(editorButtons);
		Button replaceEditorRootButton = new Button("Replace Root Model");
		editorButtons.setRight(replaceEditorRootButton);
		replaceEditorRootButton.setOnAction((event) -> {
			viewManager.isError(() -> {
				InputStream inputStream = loadConfiguration.loadConfiguration(this.getReplaceConfigurationFileName());
				configurationItem.setConfiguration(inputStream);
				viewManager.reloadFromConfigurationItem();
			});
		});

		// Obtain the configurable context (now available from editor)
		ConfigurableContext<R, O> configurableContext = editor.getConfigurableContext();

		// Load the preferences
		PreferencesEditor<R> preferences = new PreferencesEditor<>(this.createEditor(envBridge), envBridge);
		Tab preferencesTab = new Tab("Preferences");
		folder.getTabs().add(preferencesTab);
		Pane preferencesPane = preferences.loadView((view) -> preferencesTab.setContent(view));
		BorderPane preferenceButtons = new BorderPane();
		VBox.setVgrow(preferenceButtons, Priority.NEVER);
		preferenceButtons.setPadding(new Insets(10));
		preferencesPane.getChildren().add(preferenceButtons);

		// Reset button for preferences
		Button resetPreferences = new Button("Reset All");
		preferenceButtons.setLeft(resetPreferences);
		resetPreferences.setOnAction((event) -> preferences.resetToDefaults());

		// Apply button for preferences
		HBox preferenceRightButtons = new HBox();
		preferenceRightButtons.setSpacing(10);
		preferenceButtons.setRight(preferenceRightButtons);
		Button applyPreferences = new Button("Apply");
		applyPreferences.setOnAction((event) -> preferences.apply());
		Button cancelPreferences = new Button("Cancel");
		cancelPreferences.setOnAction((event) -> preferences.cancel());
		preferenceRightButtons.getChildren().addAll(applyPreferences, cancelPreferences);
		Runnable preferenceButtonEnabler = () -> {
			boolean isDisable = !preferences.dirtyProperty().getValue();
			applyPreferences.setDisable(isDisable);
			cancelPreferences.setDisable(isDisable);
		};
		preferences.dirtyProperty().addListener((event) -> preferenceButtonEnabler.run());
		preferenceButtonEnabler.run(); // initiate

		// Load the configuration items
		for (AbstractConfigurableItem<R, RE, O, ?, ?, ?> parent : editor.getParents()) {
			this.loadItem(parent, configurableContext, folder, envBridge);
		}
	}

	/**
	 * Loads the {@link AbstractConfigurableItem}.
	 * 
	 * @param <M>                 {@link Model} type for item.
	 * @param <I>                 Item type.
	 * @param item                Item.
	 * @param configurableContext {@link ConfigurableContext}.
	 * @param pane                {@link Pane} to configuration item.
	 * @param envBridge           {@link EnvironmentBridge}.
	 */
	private <M extends Model, I> void loadItem(AbstractConfigurableItem<R, RE, O, M, ?, I> item,
			ConfigurableContext<R, O> configurableContext, TabPane pane, EnvironmentBridge envBridge) {

		// Add the tab for item
		String itemName = item.getClass().getSimpleName();
		Tab tab = new Tab(itemName);
		pane.getTabs().add(tab);

		// Obtain the IDE configurer
		AbstractConfigurableItem<R, RE, O, M, ?, I>.IdeConfigurer ideConfigurer = item.configure();
		if (ideConfigurer == null) {
			tab.setContent(new Text("No configuration for " + this.getClass().getSimpleName()));
			return;
		}
		IdeConfiguration<O, M, I> configuration = AbstractConfigurableItem.extractIdeConfiguration(ideConfigurer);

		// Add tabs for each of the operations
		TabPane operationsPane = new TabPane();
		tab.setContent(operationsPane);

		// Create the prototype
		M prototype = item.prototype();

		// Possibly decorate the prototype
		@SuppressWarnings("unchecked")
		Consumer<M> prototypeDecorator = (Consumer<M>) this.prototypeDecorators.get(prototype.getClass());
		if (prototypeDecorator != null) {
			prototypeDecorator.accept(prototype);
		}

		// Specify the configurable context
		item.init(configurableContext);

		// Log changes
		Consumer<Change<?>> logChange = (change) -> {
			// Log running the change
			StringBuilder message = new StringBuilder();
			message.append("Executing change '" + change.getChangeDescription() + "' for target "
					+ change.getTarget().getClass().getName());
			if (!change.canApply()) {
				message.append(" (can not apply)");
				for (Conflict conflict : change.getConflicts()) {
					message.append(System.lineSeparator() + "\t" + conflict.getConflictDescription());
				}
			}
			System.out.println(message.toString());
		};

		// Load the add configuration
		if (configuration.add.length > 0) {
			Tab addTab = new Tab("Add");
			operationsPane.getTabs().add(addTab);
			Pane addParent = new Pane();
			addTab.setContent(addParent);
			I addItem = item.item(null);
			Configurer<I> addConfigurer = new Configurer<>(envBridge);
			ConfigurationBuilder<I> addBuilder = addConfigurer;
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return null;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : configuration.add) {
				itemConfigurer.configure(addBuilder, addContext);
			}
			addConfigurer.loadConfiguration(addItem, addParent);
		}

		// Load the add immediately
		if (configuration.addImmediately != null) {
			Tab addTab = new Tab("Add");
			operationsPane.getTabs().add(addTab);
			Pane addParent = new Pane();
			addTab.setContent(addParent);
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return null; // no modal on add
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			Button addButton = new Button("Add");
			addParent.getChildren().add(addButton);
			addButton.setOnAction((event) -> {
				try {
					configuration.addImmediately.action(addContext);
				} catch (Throwable ex) {
					System.out.println("Failed to add " + ex.getMessage());
				}
			});
		}

		// Load the refactor configuration
		if (configuration.refactor.length > 0) {
			Tab refactorTab = new Tab("Refactor");
			operationsPane.getTabs().add(refactorTab);
			Pane refactorParent = new Pane();
			refactorTab.setContent(refactorParent);
			I refactorItem = item.item(prototype);
			Configurer<I> refactorConfigurer = new Configurer<>(envBridge);
			ConfigurationBuilder<I> refactorBuilder = refactorConfigurer;
			ConfigurableModelContext<O, M> refactorContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : configuration.refactor) {
				itemConfigurer.configure(refactorBuilder, refactorContext);
			}
			refactorConfigurer.loadConfiguration(refactorItem, refactorParent);
		}

		// Load the delete configuration
		if (configuration.delete != null) {
			Tab deleteTab = new Tab("Delete");
			operationsPane.getTabs().add(deleteTab);
			Pane deleteParent = new Pane();
			deleteTab.setContent(deleteParent);
			ConfigurableModelContext<O, M> deleteContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			Button deleteButton = new Button("Delete");
			deleteParent.getChildren().add(deleteButton);
			deleteButton.setOnAction((event) -> {
				try {
					configuration.delete.action(deleteContext);
				} catch (Throwable ex) {
					System.out.println("Failed to delete " + ex.getMessage());
				}
			});
		}
	}

}
