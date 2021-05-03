/*-
 * #%L
 * [bundle] Abstract IDE Items
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

package net.officefloor.gef.item;

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
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
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
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Configuration for the abstract {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractProcedureItem<R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I extends AbstractProcedureItem<R, RE, O, M, E, I>>
		extends AbstractConfigurableItem<R, RE, O, M, E, I> {

	/**
	 * Loads the available {@link Procedure} instances.
	 * 
	 * @param validatorContext {@link ValueValidatorContext}.
	 * @param selectProcedures {@link Builder} for selecting the {@link Procedure}.
	 * @param envBridge        {@link EnvironmentBridge}.
	 * @throws Exception If fails to load available {@link Procedure} instances.
	 */
	public static <R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I extends AbstractProcedureItem<R, RE, O, M, E, I>> void loadProcedures(
			ValueValidatorContext<? extends I, String> validatorContext, SelectBuilder<I, Procedure> selectProcedures,
			EnvironmentBridge envBridge) throws Exception {
		AbstractProcedureItem<?, ?, ?, ?, ?, ?> item = validatorContext.getModel();
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
	 * Loads the {@link ProcedureType} for the {@link AbstractProcedureItem}.
	 * 
	 * @param item      {@link AbstractProcedureItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link ProcedureType}.
	 * @throws Exception If fails to load the {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(AbstractProcedureItem<?, ?, ?, ?, ?, ?> item,
			EnvironmentBridge envBridge) throws Exception {
		ProcedureLoader loader = ProcedureEmployer.employProcedureLoader(envBridge.getOfficeFloorCompiler());
		return loader.loadProcedureType(item.resource, item.sourceName, item.procedure, item.properties);
	}

	/**
	 * Name.
	 */
	protected String name;

	/**
	 * Resource.
	 */
	protected String resource;

	/**
	 * {@link Procedure} instances.
	 */
	protected ObservableList<Procedure> procedures = FXCollections.observableArrayList();

	/**
	 * Source.
	 */
	protected String sourceName;

	/**
	 * {@link Procedure} name.
	 */
	protected String procedure;

	/**
	 * {@link PropertyList}.
	 */
	protected PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link ProcedureType}.
	 */
	protected ProcedureType procedureType;

	/**
	 * {@link ProcedureFlowType} name mapping.
	 */
	protected Map<String, String> outputNameMapping;

	/**
	 * Creates the {@link AbstractProcedureItem} implementation.
	 * 
	 * @return {@link AbstractProcedureItem} implementation.
	 */
	protected abstract I createItem();

	/**
	 * Obtains the {@link SubSection} / {@link OfficeSection} name.
	 * 
	 * @param model {@link Model}.
	 * @return {@link SubSection} / {@link OfficeSection} name.
	 */
	protected abstract String getSectionName(M model);

	/**
	 * Obtains the resource.
	 * 
	 * @param model {@link Model}.
	 * @return Resource.
	 */
	protected abstract String getResource(M model);

	/**
	 * Obtains the source name.
	 * 
	 * @param model {@link Model}.
	 * @return Source name.
	 */
	protected abstract String getSourceName(M model);

	/**
	 * Obtains the {@link Procedure} name.
	 * 
	 * @param model {@link Model}.
	 * @return {@link Procedure} name.
	 */
	protected abstract String getProcedureName(M model);

	/**
	 * Obtains the {@link Procedure} {@link PropertyList}.
	 * 
	 * @param model {@link Model}.
	 * @return {@link Procedure} {@link PropertyList}.
	 */
	protected abstract PropertyList getProcedureProperties(M model);

	/**
	 * Obtains the input {@link ConnectionModel} {@link Class} instances.
	 * 
	 * @return Input {@link ConnectionModel} {@link Class} instances.
	 */
	protected abstract Class<? extends ConnectionModel>[] getInputConnectionClasses();

	/**
	 * Creates the {@link Procedure} next {@link AbstractItem} implementation.
	 * 
	 * @return {@link Procedure} next {@link AbstractItem} implementation.
	 */
	protected abstract AbstractItem<R, O, M, E, ?, ?> createNextItem();

	/**
	 * Creates the {@link Procedure} output {@link AbstractItem} implementation.
	 * 
	 * @return {@link Procedure} output {@link AbstractItem} implementation.
	 */
	protected abstract AbstractItem<R, O, M, E, ?, ?> createOutputItem();

	/**
	 * Creates {@link Change} to add {@link Procedure}.
	 * 
	 * @param operations    Operations.
	 * @param name          {@link SubSection} / {@link OfficeSection} name.
	 * @param resource      Resource.
	 * @param sourceName    Source name.
	 * @param procedure     {@link Procedure} name.
	 * @param properties    {@link PropertyList}.
	 * @param procedureType {@link ProcedureType}.
	 * @return {@link Change} to add {@link Procedure}.
	 */
	protected abstract Change<M> addProcedure(O operations, String name, String resource, String sourceName,
			String procedure, PropertyList properties, ProcedureType procedureType);

	/**
	 * Creates {@link Change} to refactor {@link Procedure}.
	 * 
	 * @param operations        Operations.
	 * @param model             {@link Model} to refactor.
	 * @param name              {@link SubSection} / {@link OfficeSection} name.
	 * @param resource          Resource.
	 * @param sourceName        Source name.
	 * @param procedure         {@link Procedure} name.
	 * @param properties        {@link PropertyList}.
	 * @param procedureType     {@link ProcedureType}.
	 * @param outputNameMapping {@link Procedure} output name mapping.
	 * @return {@link Change} to refactor {@link Procedure}.
	 */
	protected abstract Change<M> refactorProcedure(O operations, M model, String name, String resource,
			String sourceName, String procedure, PropertyList properties, ProcedureType procedureType,
			Map<String, String> outputNameMapping);

	/**
	 * Creates {@link Change} to remove {@link Procedure}.
	 * 
	 * @param operations Operations.
	 * @param model      {@link Model} to remove.
	 * @return {@link Change} to remove {@link Procedure}.
	 */
	protected abstract Change<M> removeProcedure(O operations, M model);

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public Pane visual(M model, AdaptedChildVisualFactoryContext<M> context) {
		VBox container = new VBox();
		HBox procedure = context.addNode(container, new HBox());
		context.addNode(procedure,
				context.connector(DefaultConnectors.FLOW, this.getInputConnectionClasses()).getNode());
		context.label(procedure);
		context.addNode(procedure, context.childGroup(this.createNextItem().getClass().getSimpleName(), new VBox()));
		HBox children = context.addNode(container, new HBox());
		context.addIndent(children);
		context.addNode(children, context.childGroup(this.createOutputItem().getClass().getSimpleName(), new VBox()));
		return container;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(
				new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 100.0%, green, mediumseagreen)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "honeydew"));
	}

	@Override
	public I item(M model) {
		I item = this.createItem();
		if (model != null) {
			item.name = this.getSectionName(model);
			item.resource = this.getResource(model);
			item.sourceName = this.getSourceName(model);
			item.procedure = this.getProcedureName(model);
			item.properties = this.getProcedureProperties(model);
		}
		return item;
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(this.createNextItem()));
		childGroups.add(new IdeChildrenGroup(this.createOutputItem()));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Procedure");

			final int CLASS_INDEX = 0;
			final int RESOURCE_INDEX = 1;
			ChoiceBuilder<I> choices = builder.choices("").init((item) -> {
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
			ConfigurationBuilder<I> classBuilder = choices.choice("Class");
			ClassBuilder<I> resourceClassBuilder = classBuilder.clazz("Class").init((item) -> item.resource)
					.validate(ValueValidator.notEmptyString("Must specify class"))
					.setValue((item, value) -> item.resource = value);

			// Choice: resource
			ConfigurationBuilder<I> sourceBuilder = choices.choice("Resource");
			ResourceBuilder<I> resourceResourceBuilder = sourceBuilder.resource("Resource")
					.init((item) -> item.resource).validate(ValueValidator.notEmptyString("Must specify resource"))
					.setValue((item, value) -> item.resource = value);

			// Select procedure
			SelectBuilder<I, Procedure> selectProceduresBuilder = builder.select("Procedure", (item) -> item.procedures)
					.itemLabel((procedure) -> {
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
			OptionalBuilder<I> optionalProcedureBuilder = builder
					.optional((model) -> model.sourceName != null && model.procedure == null);
			optionalProcedureBuilder.text("Procedure").init((item) -> item.procedure)
					.validate(ValueValidator.notEmptyString("Must specify procedure"))
					.setValue((item, value) -> item.procedure = value);

			// Reload optional procedure on procedure selection
			selectProceduresBuilder.validate((validateContext) -> validateContext.reload(optionalProcedureBuilder));

			// Name for procedure
			TextBuilder<I> nameBuilder = builder.text("Name").init((item) -> item.name)
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
				I item = ctx.getModel();

				// Attempt to load the type
				item.procedureType = loadProcedureType(item, envBridge);

				// Load the mappings
				item.outputNameMapping = this.translateToNameMappings(item.procedureType.getFlowTypes(),
						(output) -> output.getFlowName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(this.addProcedure(context.getOperations(), item.name, item.resource, item.sourceName,
						item.procedure, item.properties, item.procedureType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(this.refactorProcedure(context.getOperations(), context.getModel(), item.name,
						item.resource, item.sourceName, item.procedure, item.properties, item.procedureType,
						item.outputNameMapping));
			});

		}).delete((context) -> {
			context.execute(this.removeProcedure(context.getOperations(), context.getModel()));
		});
	}

}
