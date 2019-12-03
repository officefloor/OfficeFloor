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
package net.officefloor.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.activity.model.ActivityInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureNextModel;
import net.officefloor.activity.model.ActivityProcedureOutputModel;
import net.officefloor.activity.model.ActivityRepositoryImpl;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivitySectionOutputModel;
import net.officefloor.activity.model.PropertyModel;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;

/**
 * {@link ActivityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityLoaderImpl implements ActivityLoader {

	/*
	 * ================== ActivityLoader ========================
	 */

	@Override
	public void loadActivityConfiguration(ActivityContext context) throws Exception {

		// Load the model
		ActivityModel activityModel = new ActivityModel();
		new ActivityRepositoryImpl(new ModelRepositoryImpl()).retrieveActivity(activityModel,
				context.getConfiguration());

		// Obtain the various helpers
		ProcedureArchitect<SubSection> procedureArchitect = context.getProcedureArchitect();
		ProcedureLoader procedureLoader = context.getProcedureLoader();
		SectionDesigner designer = context.getSectionDesigner();
		SectionSourceContext sourceContext = context.getSectionSourceContext();

		// Load the procedures and their types
		ProcedureConnector procedures = new ProcedureConnector(activityModel, designer, procedureArchitect,
				procedureLoader, sourceContext);

		// Load the sections and their types
		SectionConnector sections = new SectionConnector(activityModel, designer, sourceContext);

		// Load outputs
		OutputConnector outputs = new OutputConnector(activityModel, designer, sourceContext);

		// Link the inputs
		for (ActivityInputModel inputModel : activityModel.getActivityInputs()) {

			// Obtain the input
			String inputName = inputModel.getActivityInputName();
			SectionInput input = designer.addSectionInput(inputName, inputModel.getArgumentType());

			// Undertake links
			Supplier<SectionFlowSourceNode> inputFactory = () -> input;
			procedures.linkToProcedure(inputFactory, inputModel.getActivityProcedure(),
					(link) -> link.getActivityProcedure());
			sections.linkToSectionInput(inputFactory, inputModel.getActivitySectionInput(),
					(link) -> link.getActivitySectionInput());
			outputs.linkToOutput(inputFactory, inputModel.getActivityOutput(), (link) -> link.getActivityOutput());
		}

		// Link the procedures
		for (ActivityProcedureModel procedureModel : activityModel.getActivityProcedures()) {

			// Obtain the auto-wire section
			String procedureName = procedureModel.getActivityProcedureName();
			SubSection procedure = procedures.procedures.get(procedureName);

			// Link next for procedure
			ActivityProcedureNextModel nextModel = procedureModel.getNext();
			if (nextModel != null) {

				// Obtain the output argument type
				String nextArgumentTypeName = nextModel.getArgumentType();
				if (CompileUtil.isBlank(nextArgumentTypeName)) {
					nextArgumentTypeName = null;
				}

				// Undertake links
				Supplier<SectionFlowSourceNode> nextFlow = () -> procedure
						.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME);
				procedures.linkToProcedure(nextFlow, nextModel.getActivityProcedure(),
						(link) -> link.getActivityProcedure());
				sections.linkToSectionInput(nextFlow, nextModel.getActivitySectionInput(),
						(link) -> link.getActivitySectionInput());
				outputs.linkToOutput(nextFlow, nextModel.getActivityOutput(), (link) -> link.getActivityOutput());
			}

			// Link outputs for procedure
			for (ActivityProcedureOutputModel outputModel : procedureModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getActivityProcedureOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<SectionFlowSourceNode> outputFlow = () -> procedure.getSubSectionOutput(outputName);
				procedures.linkToProcedure(outputFlow, outputModel.getActivityProcedure(),
						(link) -> link.getActivityProcedure());
				sections.linkToSectionInput(outputFlow, outputModel.getActivitySectionInput(),
						(link) -> link.getActivitySectionInput());
				outputs.linkToOutput(outputFlow, outputModel.getActivityOutput(), (link) -> link.getActivityOutput());
			}
		}

		// Link the section outputs
		for (ActivitySectionModel sectionModel : activityModel.getActivitySections()) {

			// Obtain the auto-wire section
			String sectionName = sectionModel.getActivitySectionName();
			SubSection section = sections.sections.get(sectionName);

			// Link outputs for the section
			for (ActivitySectionOutputModel outputModel : sectionModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getActivitySectionOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<SectionFlowSourceNode> outputFlow = () -> section.getSubSectionOutput(outputName);
				procedures.linkToProcedure(outputFlow, outputModel.getActivityProcedure(),
						(link) -> link.getActivityProcedure());
				sections.linkToSectionInput(outputFlow, outputModel.getActivitySectionInput(),
						(link) -> link.getActivitySectionInput());
				outputs.linkToOutput(outputFlow, outputModel.getActivityOutput(), (link) -> link.getActivityOutput());
			}
		}
	}

	/**
	 * Connector for the {@link ActivityProcedureModel} instances.
	 */
	private static class ProcedureConnector {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner sectionDesigner;

		/**
		 * {@link Procedure} instances by name.
		 */
		private final Map<String, SubSection> procedures = new HashMap<>();

		/**
		 * {@link ProcedureType} instances by name.
		 */
		private final Map<String, ProcedureType> procedureTypes = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param activity             {@link ActivityModel}.
		 * @param sectionDesigner      {@link OfficeArchitect}.
		 * @param procedureArchitect   {@link ProcedureArchitect}.
		 * @param procedureLoader      {@link ProcedureLoader}.
		 * @param sectionSourceContext {@link SectionSourceContext}.
		 */
		private ProcedureConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				ProcedureArchitect<SubSection> procedureArchitect, ProcedureLoader procedureLoader,
				SectionSourceContext sectionSourceContext) {
			this.sectionDesigner = sectionDesigner;

			// Configure the procedures
			for (ActivityProcedureModel procedureModel : activity.getActivityProcedures()) {

				// Obtain the procedure details
				String sectionName = procedureModel.getActivityProcedureName();
				String resource = procedureModel.getResource();
				String sourceName = procedureModel.getSourceName();
				String procedureName = procedureModel.getProcedureName();

				// Determine if next
				ActivityProcedureNextModel nextModel = procedureModel.getNext();
				boolean isNext = (nextModel != null) && ((nextModel.getActivityProcedure() != null)
						|| (nextModel.getActivityOutput() != null) || (nextModel.getActivitySectionInput() != null));

				// Load the properties
				PropertyList properties = sectionSourceContext.createPropertyList();
				for (PropertyModel propertyModel : procedureModel.getProperties()) {
					properties.addProperty(propertyModel.getName()).setValue(propertyModel.getValue());
				}

				// Configure the procedure
				SubSection procedure = procedureArchitect.addProcedure(sectionName, resource, sourceName, procedureName,
						isNext, properties);
				this.procedures.put(sectionName, procedure);

				// Load the types
				ProcedureType procedureType = procedureLoader.loadProcedureType(resource, sourceName, procedureName,
						properties);
				this.procedureTypes.put(sectionName, procedureType);
			}
		}

		/**
		 * Link to {@link Procedure}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link SectionFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link ActivityProcedureModel}.
		 * @param procedureFactory  Factory to extract procedure
		 *                          {@link ActivityProcedureModel} from
		 *                          {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToProcedure(Supplier<SectionFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, ActivityProcedureModel> procedureFactory) {

			// Determine if linking
			if (connectionModel != null) {
				ActivityProcedureModel procedure = procedureFactory.apply(connectionModel);
				if (procedure != null) {

					// Obtain the target procedure
					String targetProcedureName = procedure.getActivityProcedureName();
					SubSection targetProcedure = this.procedures.get(targetProcedureName);

					// Link the flow to the procedure
					this.sectionDesigner.link(flowSourceFactory.get(),
							targetProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
				}
			}
		}
	}

	/**
	 * Connector for the {@link ActivitySectionModel} instances.
	 */
	private static class SectionConnector {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner sectionDesigner;

		/**
		 * {@link SubSection} instances by name.
		 */
		private final Map<String, SubSection> sections = new HashMap<>();

		/**
		 * {@link SectionType} instances by name.
		 */
		private final Map<String, SectionType> sectionTypes = new HashMap<>();

		/**
		 * {@link ActivitySectionInputModel} mapping to its
		 * {@link ActivitySectionModel}.
		 */
		private final Map<ActivitySectionInputModel, ActivitySectionModel> inputToSection = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param activity             {@link ActivityModel}.
		 * @param sectionDesigner      {@link SectionDesigner}.
		 * @param sectionSourceContext {@link SectionSourceContext}.
		 */
		private SectionConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				SectionSourceContext sectionSourceContext) {
			this.sectionDesigner = sectionDesigner;

			// Configure the sections
			for (ActivitySectionModel sectionModel : activity.getActivitySections()) {

				// Obtains details for section
				String sectionName = sectionModel.getActivitySectionName();
				String sectionSourceClassName = sectionModel.getSectionSourceClassName();
				String location = sectionModel.getSectionLocation();
				PropertyList properties = sectionSourceContext.createPropertyList();
				for (PropertyModel propertyModel : sectionModel.getProperties()) {
					String name = propertyModel.getName();
					String value = propertyModel.getValue();
					properties.addProperty(name).setValue(value);
				}

				// Load and register the section
				SubSection section = sectionDesigner.addSubSection(sectionName, sectionSourceClassName, location);
				properties.configureProperties(section);
				this.sections.put(sectionName, section);

				// Load and register the section type
				SectionType sectionType = sectionSourceContext.loadSectionType(sectionName, sectionSourceClassName,
						location, properties);
				this.sectionTypes.put(sectionName, sectionType);

				// Maintain references from inputs to section
				for (ActivitySectionInputModel inputModel : sectionModel.getInputs()) {
					this.inputToSection.put(inputModel, sectionModel);
				}
			}
		}

		/**
		 * Link to {@link SectionSectionInput}.
		 * 
		 * @param flowSourceFactory   {@link Supplier} of the
		 *                            {@link SectionFlowSourceNode}.
		 * @param connectionModel     {@link ConnectionModel} to
		 *                            {@link ActivitySectionInputModel}.
		 * @param sectionInputFactory Factory to extract
		 *                            {@link ActivitySectionInputModel} from
		 *                            {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToSectionInput(Supplier<SectionFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, ActivitySectionInputModel> sectionInputFactory) {

			// Determine if linking
			if (connectionModel != null) {
				ActivitySectionInputModel sectionInput = sectionInputFactory.apply(connectionModel);
				if (sectionInput != null) {
					// Obtain target input name
					String targetInputName = sectionInput.getActivitySectionInputName();

					// Obtain the target section
					ActivitySectionModel sectionModel = this.inputToSection.get(sectionInput);
					SubSection targetSection = this.sections.get(sectionModel.getActivitySectionName());

					// Link the flow to the section input
					this.sectionDesigner.link(flowSourceFactory.get(),
							targetSection.getSubSectionInput(targetInputName));
				}
			}
		}
	}

	/**
	 * Connector for the {@link ActivityOutputModel} instances.
	 */
	private static class OutputConnector {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner sectionDesigner;

		/**
		 * {@link SectionOutput} instances by name.
		 */
		private final Map<String, SectionOutput> outputs = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param activity             {@link ActivityModel}.
		 * @param sectionDesigner      {@link SectionDesigner}.
		 * @param sectionSourceContext {@link SectionSourceContext}.
		 */
		private OutputConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				SectionSourceContext sectionSourceContext) {
			this.sectionDesigner = sectionDesigner;

			// Configure the outputs
			for (ActivityOutputModel outputModel : activity.getActivityOutputs()) {
				String outputName = outputModel.getActivityOutputName();
				SectionOutput output = sectionDesigner.addSectionOutput(outputName, outputModel.getParameterType(),
						false);
				this.outputs.put(outputName, output);
			}
		}

		/**
		 * Link to {@link SectionOutput}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link SectionFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link ActivitySectionInputModel}.
		 * @param outputFactory     Factory to extract {@link ActivitySectionInputModel}
		 *                          from {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToOutput(Supplier<SectionFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, ActivityOutputModel> outputFactory) {

			// Determine if linking
			if (connectionModel != null) {
				ActivityOutputModel output = outputFactory.apply(connectionModel);
				if (output != null) {

					// Obtain the target output
					String targetOutputName = output.getActivityOutputName();
					SectionOutput targetOutput = this.outputs.get(targetOutputName);

					// Link the flow to the output
					this.sectionDesigner.link(flowSourceFactory.get(), targetOutput);
				}
			}
		}
	}

}