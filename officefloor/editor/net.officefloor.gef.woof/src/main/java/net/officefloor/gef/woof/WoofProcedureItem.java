/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.gef.woof;

import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.Builder;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ClassBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.OptionalBuilder;
import net.officefloor.gef.configurer.ResourceBuilder;
import net.officefloor.gef.configurer.SelectBuilder;
import net.officefloor.gef.configurer.TextBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.configurer.ValueValidator.ValueValidatorContext;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofStartToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofProcedureModel;

/**
 * Configuration for the {@link WoofProcedureModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofProcedureItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofProcedureModel, WoofProcedureEvent, WoofProcedureItem> {

	/**
	 * Loads the available {@link Procedure} instances.
	 * 
	 * @param validatorContext {@link ValueValidatorContext}.
	 * @param selectProcedures {@link Builder} for selecting the {@link Procedure}.
	 * @param envBridge        {@link EnvironmentBridge}.
	 * @throws Exception If fails to load available {@link Procedure} instances.
	 */
	public static void loadProcedures(ValueValidatorContext<? extends WoofProcedureItem, String> validatorContext,
			SelectBuilder<WoofProcedureItem, Procedure> selectProcedures, EnvironmentBridge envBridge)
			throws Exception {
		WoofProcedureItem item = validatorContext.getModel();
		Procedure[] procedures = new Procedure[0];
		try {
			ProcedureLoader loader = ProcedureEmployer.employProcedureLoader(envBridge.getOfficeFloorCompiler());
			procedures = loader.listProcedures(item.resource);
		} finally {
			// Set if successful, or clear if error
			item.procedures.setAll(procedures);
			validatorContext.reload(selectProcedures);
		}
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link WoofProcedureItem}.
	 * 
	 * @param item      {@link WoofProcedureItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link ProcedureType}.
	 * @throws Exception If fails to load the {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(WoofProcedureItem item, EnvironmentBridge envBridge)
			throws Exception {
		ProcedureLoader loader = ProcedureEmployer.employProcedureLoader(envBridge.getOfficeFloorCompiler());
		return loader.loadProcedureType(item.resource, item.sourceName, item.procedure, item.properties);
	}

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Resource.
	 */
	private String resource;

	/**
	 * {@link Procedure} instances.
	 */
	private ObservableList<Procedure> procedures = FXCollections.observableArrayList();

	/**
	 * Source.
	 */
	private String sourceName;

	/**
	 * {@link Procedure} name.
	 */
	private String procedure;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link ProcedureType}.
	 */
	private ProcedureType procedureType;

	/**
	 * {@link ProcedureFlowType} name mapping.
	 */
	private Map<String, String> outputNameMapping;

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public WoofProcedureModel prototype() {
		return new WoofProcedureModel("Procedure", null, null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getWoofProcedures(), WoofEvent.ADD_WOOF_PROCEDURE,
				WoofEvent.REMOVE_WOOF_PROCEDURE);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofProcedureModel itemModel) {
		parentModel.addWoofProcedure(itemModel);
	}

	@Override
	public Pane visual(WoofProcedureModel model, AdaptedChildVisualFactoryContext<WoofProcedureModel> context) {
		VBox container = new VBox();
		HBox procedure = context.addNode(container, new HBox());
		context.addNode(procedure,
				context.connector(DefaultConnectors.FLOW, WoofHttpContinuationToWoofProcedureModel.class,
						WoofHttpInputToWoofProcedureModel.class, WoofExceptionToWoofProcedureModel.class,
						WoofStartToWoofProcedureModel.class, WoofSectionOutputToWoofProcedureModel.class,
						WoofSecurityOutputToWoofProcedureModel.class, WoofTemplateOutputToWoofProcedureModel.class,
						WoofProcedureNextToWoofProcedureModel.class, WoofProcedureOutputToWoofProcedureModel.class)
						.getNode());
		context.label(procedure);
		context.addNode(procedure, context.childGroup(WoofProcedureNextItem.class.getSimpleName(), new VBox()));
		HBox children = context.addNode(container, new HBox());
		context.addIndent(children);
		context.addNode(children, context.childGroup(WoofProcedureOutputItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofProcedureName(), WoofProcedureEvent.CHANGE_WOOF_PROCEDURE_NAME);
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(
				new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 100.0%, green, mediumseagreen)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "honeydew"));
	}

	@Override
	public WoofProcedureItem item(WoofProcedureModel model) {
		WoofProcedureItem item = new WoofProcedureItem();
		if (model != null) {
			item.name = model.getWoofProcedureName();
			item.resource = model.getResource();
			item.sourceName = model.getSourceName();
			item.procedure = model.getProcedureName();
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
		}
		return item;
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new WoofProcedureNextItem()));
		childGroups.add(new IdeChildrenGroup(new WoofProcedureOutputItem()));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Procedure");

			final int CLASS_INDEX = 0;
			final int RESOURCE_INDEX = 1;
			ChoiceBuilder<WoofProcedureItem> choices = builder.choices("").init((item) -> {
				// Determine if resource or class
				if (item.resource == null) {
					return CLASS_INDEX; // default to class
				}
				if (item.resource.contains("/")) {
					return RESOURCE_INDEX; // directory, so must be resource
				}
				return CLASS_INDEX; // assume class
			}).validate(ValueValidator.notNull("Must select"));

			// Choice: class
			ConfigurationBuilder<WoofProcedureItem> classBuilder = choices.choice("Class");
			ClassBuilder<WoofProcedureItem> resourceClassBuilder = classBuilder.clazz("Class")
					.init((item) -> item.resource).validate(ValueValidator.notEmptyString("Must specify class"))
					.setValue((item, value) -> item.resource = value);

			// Choice: resource
			ConfigurationBuilder<WoofProcedureItem> sourceBuilder = choices.choice("Resource");
			ResourceBuilder<WoofProcedureItem> resourceResourceBuilder = sourceBuilder.resource("Resource")
					.init((item) -> item.resource).validate(ValueValidator.notEmptyString("Must specify resource"))
					.setValue((item, value) -> item.resource = value);

			// Select procedure
			SelectBuilder<WoofProcedureItem, Procedure> selectProceduresBuilder = builder
					.select("Procedure", (item) -> item.procedures).itemLabel((procedure) -> {
						String procedureName = procedure.getProcedureName();
						return ((procedureName == null) ? "<enter>" : procedureName) + " [" + procedure.getServiceName()
								+ "]";
					}).init((item) -> {
						// Check for provided procedure
						for (Procedure procedure : item.procedures) {
							if (procedure.isProcedure(item.sourceName, item.procedure)) {
								return procedure;
							}
						}
						// Check for manual specified procedure
						for (Procedure procedure : item.procedures) {
							if (procedure.isProcedure(item.sourceName, null)) {
								return procedure;
							}
						}
						return null; // no match
					}).validate(ValueValidator.notNull("Must select procedure")).setValue((item, procedure) -> {
						if (procedure != null) {
							item.sourceName = procedure.getServiceName();
							item.procedure = procedure.getProcedureName();

							// Default name (if not specified)
							if (CompileUtil.isBlank(item.name)) {
								item.name = item.procedure;
							}

						} else {
							item.sourceName = null;
							item.procedure = null;
						}
					});

			// Reload procedures on resource change
			resourceClassBuilder.validate((validateContext) -> loadProcedures(validateContext, selectProceduresBuilder,
					this.getConfigurableContext().getEnvironmentBridge()));
			resourceResourceBuilder.validate((validateContext) -> loadProcedures(validateContext,
					selectProceduresBuilder, this.getConfigurableContext().getEnvironmentBridge()));

			// Determine if manual enter procedure
			OptionalBuilder<WoofProcedureItem> optionalProcedureBuilder = builder
					.optional((model) -> model.sourceName != null && model.procedure == null);
			optionalProcedureBuilder.text("Procedure").init((item) -> item.procedure)
					.validate(ValueValidator.notEmptyString("Must specify procedure"))
					.setValue((item, value) -> item.procedure = value);

			// Reload optional procedure on procedure selection
			selectProceduresBuilder.validate((validateContext) -> validateContext.reload(optionalProcedureBuilder));

			// Name for procedure
			TextBuilder<WoofProcedureItem> nameBuilder = builder.text("Name").init((item) -> item.name)
					.validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			selectProceduresBuilder.validate((validateContext) -> validateContext.reload(nameBuilder));

			// Optional properties
			builder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();

				// Validate the type
				WoofProcedureItem item = ctx.getModel();

				// Attempt to load the type
				item.procedureType = loadProcedureType(item, envBridge);

				// Load the mappings
				item.outputNameMapping = this.translateToNameMappings(item.procedureType.getFlowTypes(),
						(output) -> output.getFlowName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addProcedure(item.name, item.resource, item.sourceName,
						item.procedure, item.properties, item.procedureType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorProcedure(context.getModel(), item.name, item.resource,
						item.sourceName, item.procedure, item.properties, item.procedureType, item.outputNameMapping));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeProcedure(context.getModel()));
		});
	}

}