/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.ListBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionNamespaceModel.FunctionNamespaceEvent;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * Configuration for {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, FunctionNamespaceModel, FunctionNamespaceEvent, FunctionNamespaceItem> {

	/**
	 * Loads the {@link FunctionNamespaceType} for the
	 * {@link FunctionNamespaceItem}.
	 * 
	 * @param item      {@link FunctionNamespaceItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link FunctionNamespaceType}.
	 * @throws Exception If fails to load the {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceType loadFunctionNamespaceType(FunctionNamespaceItem item,
			EnvironmentBridge envBridge) throws Exception {
		ManagedFunctionLoader loader = envBridge.getOfficeFloorCompiler().getManagedFunctionLoader();
		Class<? extends ManagedFunctionSource> sourceClass = envBridge.loadClass(item.sourceClassName,
				ManagedFunctionSource.class);
		return loader.loadManagedFunctionType(sourceClass, item.properties);
	}

	/**
	 * Choice {@link Class}.
	 */
	private static final int CHOICE_CLASS = 0;

	/**
	 * Name of the {@link DeployedOffice}.
	 */
	private String name;

	/**
	 * Choice.
	 */
	private Integer choice = null;

	/**
	 * {@link ManagedFunctionSource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link FunctionNamespaceType}.
	 */
	private FunctionNamespaceType functionNamespaceType;

	/**
	 * Function names.
	 */
	private List<FunctionName> functionNames = new ArrayList<>();

	/**
	 * Function name.
	 */
	private static class FunctionName {

		/**
		 * Name of the function.
		 */
		private final String name;

		/**
		 * Indicates if function is used.
		 */
		private boolean isUsed = false;

		/**
		 * Instantiate.
		 * 
		 * @param name   Name of the function.
		 * @param isUsed Indicates if used.
		 */
		private FunctionName(String name, boolean isUsed) {
			this.name = name;
			this.isUsed = isUsed;
		}
	}

	/*
	 * ================= AbstractParentConfigurableItem ======================
	 */

	@Override
	public FunctionNamespaceModel prototype() {
		return new FunctionNamespaceModel("Functions", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getFunctionNamespaces(), SectionEvent.ADD_FUNCTION_NAMESPACE,
				SectionEvent.REMOVE_FUNCTION_NAMESPACE);
	}

	@Override
	public void loadToParent(SectionModel parentModel, FunctionNamespaceModel itemModel) {
		parentModel.addFunctionNamespace(itemModel);
	}

	@Override
	public Pane visual(FunctionNamespaceModel model, AdaptedChildVisualFactoryContext<FunctionNamespaceModel> context) {
		VBox container = new VBox();
		context.label(container).getStyleClass().add("title");
		context.addNode(container, context.childGroup(ManagedFunctionItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getFunctionNamespaceName(),
				FunctionNamespaceEvent.CHANGE_FUNCTION_NAMESPACE_NAME);
	}

	@Override
	public String style() {
		IdeStyle background = new IdeStyle().rule("-fx-background-color",
				"radial-gradient(radius 50.0%, blue, lightblue)");
		IdeStyle text = new IdeStyle(".${model} .title").rule("-fx-text-fill", "white");
		return background.toString() + text.toString();
	}

	@Override
	public FunctionNamespaceItem item(FunctionNamespaceModel model) {
		FunctionNamespaceItem item = new FunctionNamespaceItem();
		if (model != null) {
			item.name = model.getFunctionNamespaceName();
			item.sourceClassName = model.getManagedFunctionSourceClassName();
			if (ClassManagedFunctionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			}
			item.properties = this.translateToPropertyList(model.getProperties(), (property) -> property.getName(),
					(property) -> property.getValue());

			// Load the functions (keep previous selected)
			Set<String> existingFunctions = new HashSet<>();
			for (ManagedFunctionModel managedFunction : model.getManagedFunctions()) {
				existingFunctions.add(managedFunction.getManagedFunctionName());
			}
			for (ManagedFunctionModel function : model.getManagedFunctions()) {
				String functionName = function.getManagedFunctionName();
				item.functionNames.add(new FunctionName(functionName, existingFunctions.contains(functionName)));
			}
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Functions");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must specify name"))
					.setValue((item, value) -> item.name = value);
			ChoiceBuilder<FunctionNamespaceItem> choices = builder.choices("").init((item) -> item.choice)
					.validate(ValueValidator.notNull("Must select")).setValue((item, value) -> {
						if (value == CHOICE_CLASS) {
							item.sourceClassName = ClassManagedFunctionSource.class.getName();
						}
					});

			// Choice: class
			ConfigurationBuilder<FunctionNamespaceItem> classBuilder = choices.choice("Class");
			classBuilder.clazz("Class")
					.init((item) -> item.properties
							.getOrAddProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME).getValue())
					.validate((ctx) -> {
						FunctionNamespaceItem item = ctx.getModel();
						String className = item.properties
								.getOrAddProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME).getValue();
						ValueValidator.notEmptyString(className, "Must specify class", ctx);
					}).setValue((item, value) -> item.properties
							.getOrAddProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME).setValue(value));

			// Choice: source
			ConfigurationBuilder<FunctionNamespaceItem> sourceBuilder = choices.choice("Source");
			sourceBuilder.clazz("Source").init((item) -> item.sourceClassName).superType(ManagedFunctionSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			sourceBuilder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Function names
			ListBuilder<FunctionNamespaceItem, FunctionName> functionsBuilder = builder
					.list("Functions", FunctionName.class).init((item) -> item.functionNames);
			functionsBuilder.text("Function").init((item) -> item.name);
			functionsBuilder.flag("Include").init((item) -> item.isUsed).setValue((item, value) -> item.isUsed = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();

				// Validate the type
				FunctionNamespaceItem item = ctx.getModel();

				// Attempt to load the function name space type
				item.functionNamespaceType = loadFunctionNamespaceType(item, envBridge);

				// Load the managed functions (keeping track of existing)
				Map<String, FunctionName> existingNames = new HashMap<>();
				for (FunctionName function : item.functionNames) {
					existingNames.put(function.name, function);
				}
				item.functionNames = new ArrayList<>();
				for (ManagedFunctionType<?, ?> managedFunctionType : item.functionNamespaceType
						.getManagedFunctionTypes()) {
					String functionName = managedFunctionType.getFunctionName();
					FunctionName function = existingNames.get(functionName);
					if (function == null) {
						function = new FunctionName(functionName, false);
					}
					item.functionNames.add(function);
				}
				ctx.reload(functionsBuilder);

				// Ensure at least one function is selected
				if (!item.functionNames.stream().anyMatch((functionName) -> functionName.isUsed)) {
					ctx.setError("Must select at least one function");
				}
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {

				// Create listing of function names
				List<String> functions = new ArrayList<>(item.functionNames.size());
				for (FunctionName functionName : item.functionNames) {
					if (functionName.isUsed) {
						functions.add(functionName.name);
					}
				}

				// Add
				context.execute(context.getOperations().addFunctionNamespace(item.name, item.sourceClassName,
						item.properties, item.functionNamespaceType, functions.toArray(new String[functions.size()])));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {

				// Create listing of function names
				List<String> functions = new ArrayList<>(item.functionNames.size());
				for (FunctionName functionName : item.functionNames) {
					if (functionName.isUsed) {
						functions.add(functionName.name);
					}
				}

				// TODO implement mapping configuration
				Map<String, String> managedFunctionNameMapping = new HashMap<>();
				Map<String, Map<String, String>> managedFunctionToObjectNameMapping = new HashMap<>();
				Map<String, Map<String, String>> functionToFlowNameMapping = new HashMap<>();
				Map<String, Map<String, String>> functionToEscalationTypeMapping = new HashMap<>();

				// Refactor
				context.execute(context.getOperations().refactorFunctionNamespace(context.getModel(), item.name,
						item.sourceClassName, item.properties, item.functionNamespaceType, managedFunctionNameMapping,
						managedFunctionToObjectNameMapping, functionToFlowNameMapping, functionToEscalationTypeMapping,
						functions.toArray(new String[functions.size()])));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeFunctionNamespace(context.getModel()));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> children) {
		children.add(new IdeChildrenGroup(new ManagedFunctionItem()));
	}

}
