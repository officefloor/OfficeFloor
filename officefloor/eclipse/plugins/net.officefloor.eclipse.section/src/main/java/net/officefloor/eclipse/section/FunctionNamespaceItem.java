/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractParentConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionNamespaceModel.FunctionNamespaceEvent;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.PropertyModel;
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
		AbstractParentConfigurableItem<SectionModel, SectionEvent, SectionChanges, FunctionNamespaceModel, FunctionNamespaceEvent, FunctionNamespaceItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		SectionEditor.launchConfigurer(new FunctionNamespaceItem(), (model) -> {
			model.setManagedFunctionSourceClassName(ClassManagedFunctionSource.class.getName());
			model.addProperty(new PropertyModel(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
					FunctionNamespaceItem.class.getName()));
			model.addManagedFunction(new ManagedFunctionModel("main"));
		});
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
		private boolean isUsed = true;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name of the function.
		 */
		private FunctionName(String name) {
			this.name = name;
		}
	}

	/*
	 * ================= AbstractParentConfigurableItem ======================
	 */

	@Override
	protected FunctionNamespaceModel createPrototype() {
		return new FunctionNamespaceModel("Functions", null);
	}

	@Override
	protected List<FunctionNamespaceModel> getModels(SectionModel parentModel) {
		return parentModel.getFunctionNamespaces();
	}

	@Override
	protected Pane createVisual(FunctionNamespaceModel model,
			AdaptedModelVisualFactoryContext<FunctionNamespaceModel> context) {
		VBox container = new VBox();
		context.label(container);
		context.addNode(container, context.childGroup(ManagedFunctionItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	protected SectionEvent[] parentChangeEvents() {
		return new SectionEvent[] { SectionEvent.ADD_FUNCTION_NAMESPACE, SectionEvent.REMOVE_FUNCTION_NAMESPACE };
	}

	@Override
	protected FunctionNamespaceEvent[] changeEvents() {
		return new FunctionNamespaceEvent[] { FunctionNamespaceEvent.CHANGE_FUNCTION_NAMESPACE_NAME };
	}

	@Override
	protected String getLabel(FunctionNamespaceModel model) {
		return model.getFunctionNamespaceName();
	}

	@Override
	protected FunctionNamespaceItem createItem(FunctionNamespaceModel model) {
		FunctionNamespaceItem item = new FunctionNamespaceItem();
		if (model != null) {
			item.name = model.getFunctionNamespaceName();
			item.sourceClassName = model.getManagedFunctionSourceClassName();
			if (ClassManagedFunctionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			}
			item.properties = this.translateToPropertyList(model.getProperties(), (property) -> property.getName(),
					(property) -> property.getValue());
			for (ManagedFunctionModel function : model.getManagedFunctions()) {
				item.functionNames.add(new FunctionName(function.getManagedFunctionName()));
			}
		}
		return item;
	}

	@Override
	protected void loadCommonConfiguration(ConfigurationBuilder<FunctionNamespaceItem> builder,
			ConfigurableModelContext<SectionChanges, FunctionNamespaceModel> context) {
		builder.title("Functions");
		builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must specify name"))
				.setValue((item, value) -> item.name = value);
		ChoiceBuilder<FunctionNamespaceItem> choices = builder.choices("").init((item) -> item.choice)
				.setValue((item, value) -> {
					if (value == CHOICE_CLASS) {
						item.sourceClassName = ClassManagedFunctionSource.class.getName();
					}
				});

		// Choice: class
		ConfigurationBuilder<FunctionNamespaceItem> classBuilder = choices.choice("Class");
		classBuilder.clazz("Class")
				.init((item) -> item.properties.getOrAddProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
						.getValue())
				.setValue((item, value) -> item.properties
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
			OfficeFloorOsgiBridge osgiBridge = this.getConfigurableContext().getOsgiBridge();

			// Validate the type
			FunctionNamespaceItem item = ctx.getValue().getValue();
			ManagedFunctionLoader loader = osgiBridge.getOfficeFloorCompiler().getManagedFunctionLoader();
			Class<? extends ManagedFunctionSource> sourceClass = osgiBridge.loadClass(item.sourceClassName,
					ManagedFunctionSource.class);
			item.functionNamespaceType = loader.loadManagedFunctionType(sourceClass, item.properties);

			// Load the managed functions (keeping track of existing)
			Map<String, FunctionName> existingNames = new HashMap<>();
			for (FunctionName function : item.functionNames) {
				existingNames.put(function.name, function);
			}
			item.functionNames = new ArrayList<>();
			for (ManagedFunctionType<?, ?> managedFunctionType : item.functionNamespaceType.getManagedFunctionTypes()) {
				String functionName = managedFunctionType.getFunctionName();
				FunctionName function = existingNames.get(functionName);
				if (function == null) {
					function = new FunctionName(functionName);
				}
				item.functionNames.add(function);
			}
			ctx.reload(functionsBuilder);
		});
	}

	@Override
	protected void loadAddConfiguration(ConfigurationBuilder<FunctionNamespaceItem> builder,
			ConfigurableModelContext<SectionChanges, FunctionNamespaceModel> context) {
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
	}

	@Override
	protected void loadRefactorConfiguration(ConfigurationBuilder<FunctionNamespaceItem> builder,
			ConfigurableModelContext<SectionChanges, FunctionNamespaceModel> context) {

		// Apply refactoring
		builder.apply("Refactor", (item) -> {

			// Create listing of function names
			List<String> functions = new ArrayList<>(item.functionNames.size());
			for (FunctionName functionName : item.functionNames) {
				if (functionName.isUsed) {
					functions.add(functionName.name);
				}
			}

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
	}

	@Override
	protected void deleteModel(ConfigurableModelContext<SectionChanges, FunctionNamespaceModel> context) {
		context.execute(context.getOperations().removeFunctionNamespace(context.getModel()));
	}

	@Override
	protected void loadChildren(List<IdeChildrenGroup> children) {
		children.add(new IdeChildrenGroup(new ManagedFunctionItem()));
	}

}