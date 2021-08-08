/*-
 * #%L
 * Activity
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

package net.officefloor.activity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.activity.model.ActivityExceptionModel;
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
import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.escalate.Escalation;
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

		// Function to create objects
		DoubleKeyMap<String, String, SectionObject> objects = new DoubleKeyMap<>();
		BiFunction<String, String, SectionObject> objectFactory = (objectType, typeQualifier) -> {

			// Derive the object name
			String objectName;
			if (CompileUtil.isBlank(typeQualifier)) {
				typeQualifier = "";
				objectName = objectType;
			} else {
				objectName = typeQualifier + "-" + objectType;
			}

			// Lazy create the object
			SectionObject object = objects.get(objectType, typeQualifier);
			if (object == null) {
				object = designer.addSectionObject(objectName, objectType);
				if (!CompileUtil.isBlank(typeQualifier)) {
					object.setTypeQualifier(typeQualifier);
				}
				objects.put(objectType, typeQualifier, object);
			}
			return object;
		};

		// Load the procedures and their types
		ProcedureConnector procedures = new ProcedureConnector(activityModel, designer, procedureArchitect,
				procedureLoader, sourceContext, objectFactory);

		// Load the sections and their types
		SectionConnector sections = new SectionConnector(activityModel, designer, sourceContext, objectFactory);

		// Load outputs
		OutputConnector outputs = new OutputConnector(activityModel, designer);

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

		// Link the escalations
		ExceptionConnector exceptions = new ExceptionConnector(activityModel, designer, sourceContext, procedures,
				sections, outputs);
		procedures.linkEscalations(exceptions);
		sections.linkEscalations(exceptions);
	}

	/**
	 * Connector for the {@link ActivityProcedureModel} instances.
	 */
	private static class ProcedureConnector {

		/**
		 * {@link ActivityModel}.
		 */
		private final ActivityModel activity;

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
		 * @param objectFactory        Creates the {@link SectionObject}.
		 */
		private ProcedureConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				ProcedureArchitect<SubSection> procedureArchitect, ProcedureLoader procedureLoader,
				SectionSourceContext sectionSourceContext, BiFunction<String, String, SectionObject> objectFactory) {
			this.activity = activity;
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

				// Load the type
				ProcedureType procedureType = procedureLoader.loadProcedureType(resource, sourceName, procedureName,
						properties);
				this.procedureTypes.put(sectionName, procedureType);

				// Link objects
				for (ProcedureObjectType objectType : procedureType.getObjectTypes()) {

					// Obtain the object details
					String objectName = objectType.getObjectName();
					Class<?> objectClass = objectType.getObjectType();
					String typeQualifier = objectType.getTypeQualifier();

					// Obtain the object
					SectionObject object = objectFactory.apply(objectClass.getName(), typeQualifier);

					// Link to object
					sectionDesigner.link(procedure.getSubSectionObject(objectName), object);
				}
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

		/**
		 * Links the {@link Escalation}.
		 * 
		 * @param exceptions {@link ExceptionConnector}.
		 */
		private void linkEscalations(ExceptionConnector exceptions) {

			// Configure the escalations
			for (ActivityProcedureModel procedureModel : this.activity.getActivityProcedures()) {

				// Obtain the procedure
				String procedureName = procedureModel.getActivityProcedureName();
				SubSection procedure = this.procedures.get(procedureName);
				ProcedureType procedureType = this.procedureTypes.get(procedureName);

				// Link the escalations
				for (ProcedureEscalationType escalation : procedureType.getEscalationTypes()) {
					String escalationName = escalation.getEscalationName();
					Class<?> escalationType = escalation.getEscalationType();
					exceptions.handleEscalation(escalationType, procedureName + "-" + escalationName,
							() -> procedure.getSubSectionOutput(escalationName));
				}
			}
		}
	}

	/**
	 * Connector for the {@link ActivitySectionModel} instances.
	 */
	private static class SectionConnector {

		/**
		 * {@link ActivityModel}.
		 */
		private final ActivityModel activity;

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner sectionDesigner;

		/**
		 * {@link SectionSourceContext}.
		 */
		private final SectionSourceContext sectionSourceContext;

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
		 * @param objectFactory        Creates the {@link SectionObject}.
		 */
		private SectionConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				SectionSourceContext sectionSourceContext, BiFunction<String, String, SectionObject> objectFactory) {
			this.activity = activity;
			this.sectionDesigner = sectionDesigner;
			this.sectionSourceContext = sectionSourceContext;

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

				// Maintain references from inputs to section
				for (ActivitySectionInputModel inputModel : sectionModel.getInputs()) {
					this.inputToSection.put(inputModel, sectionModel);
				}

				// Load the section type
				SectionType sectionType = sectionSourceContext.loadSectionType(sectionName, sectionSourceClassName,
						location, properties);
				this.sectionTypes.put(sectionName, sectionType);

				// Link objects
				for (SectionObjectType objectType : sectionType.getSectionObjectTypes()) {

					// Obtain the object details
					String objectName = objectType.getSectionObjectName();
					String objectClass = objectType.getObjectType();
					String typeQualifier = objectType.getTypeQualifier();

					// Obtain the object
					SectionObject object = objectFactory.apply(objectClass, typeQualifier);

					// Link to object
					sectionDesigner.link(section.getSubSectionObject(objectName), object);
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

		/**
		 * Links the {@link Escalation}.
		 * 
		 * @param exceptions {@link ExceptionConnector}.
		 */
		private void linkEscalations(ExceptionConnector exceptions) {

			// Configure the escalations
			for (ActivitySectionModel sectionModel : this.activity.getActivitySections()) {

				// Obtain the section
				String sectionName = sectionModel.getActivitySectionName();
				SubSection section = this.sections.get(sectionName);
				SectionType sectionType = this.sectionTypes.get(sectionName);

				// Link the escalations
				NEXT_OUTPUT: for (SectionOutputType output : sectionType.getSectionOutputTypes()) {

					// Non-escalation only should be configured
					if (!output.isEscalationOnly()) {
						continue NEXT_OUTPUT;
					}

					// Handle the escalation
					String escalationName = output.getSectionOutputName();
					String escalationTypeName = output.getArgumentType();
					if (CompileUtil.isBlank(escalationTypeName)) {
						this.sectionDesigner.addIssue(
								"No escalation type for section " + sectionName + " escalation " + escalationName);
					} else {
						Class<?> escalationType = this.sectionSourceContext.loadClass(escalationTypeName);
						exceptions.handleEscalation(escalationType, sectionName + "-" + escalationName,
								() -> section.getSubSectionOutput(escalationName));
					}
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
		private OutputConnector(ActivityModel activity, SectionDesigner sectionDesigner) {
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

	/**
	 * Connector for the {@link ActivityExceptionModel} instances.
	 */
	private static class ExceptionConnector {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner sectionDesigner;

		/**
		 * {@link Catch} instances in ascending typing (from more specific to less
		 * specific).
		 */
		private final Catch[] catches;

		/**
		 * {@link ProcedureConnector}.
		 */
		private final ProcedureConnector procedureConnector;

		/**
		 * {@link SectionConnector}.
		 */
		private final SectionConnector sectionConnector;

		/**
		 * {@link OutputConnector}.
		 */
		private final OutputConnector outputConnector;

		/**
		 * Instantiate.
		 * 
		 * @param activity             {@link ActivityModel}.
		 * @param sectionDesigner      {@link SectionDesigner}.
		 * @param sectionSourceContext {@link SectionSourceContext}.
		 * @param procedureConnector   {@link ProcedureConnector}.
		 * @param sectionConnector     {@link SectionConnector}.
		 * @param outputConnector      {@link OutputConnector}.
		 */
		private ExceptionConnector(ActivityModel activity, SectionDesigner sectionDesigner,
				SectionSourceContext sectionSourceContext, ProcedureConnector procedureConnector,
				SectionConnector sectionConnector, OutputConnector outputConnector) {
			this.sectionDesigner = sectionDesigner;
			this.procedureConnector = procedureConnector;
			this.sectionConnector = sectionConnector;
			this.outputConnector = outputConnector;

			// Configure and sort catches
			List<Catch> catchList = new LinkedList<>();
			for (ActivityExceptionModel exceptionModel : activity.getActivityExceptions()) {
				String exceptionTypeName = exceptionModel.getClassName();
				Class<?> exceptionType = sectionSourceContext.loadClass(exceptionTypeName);
				catchList.add(new Catch(exceptionType, exceptionModel));
			}

			// Sort and specify the catches
			catchList.sort((a, b) -> {
				if (a.escalationType.equals(b.escalationType)) {
					return 0; // same
				} else if (a.escalationType.isAssignableFrom(b.escalationType)) {
					return 1;
				} else if (b.escalationType.isAssignableFrom(a.escalationType)) {
					return -1;
				} else {
					return 0; // no relationship, so consider same
				}
			});
			this.catches = catchList.toArray(new Catch[catchList.size()]);
		}

		/**
		 * Handles the {@link Escalation}.
		 * 
		 * @param escalationType    Type of {@link Escalation}.
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link SectionFlowSourceNode} triggering the
		 *                          {@link Escalation}.
		 */
		private void handleEscalation(Class<?> escalationType, String escalationOutputName,
				Supplier<SectionFlowSourceNode> flowSourceFactory) {

			// Find the first handling escalation
			for (Catch handlingCatch : this.catches) {
				if (handlingCatch.escalationType.isAssignableFrom(escalationType)) {

					// Found handling catch, so link
					this.procedureConnector.linkToProcedure(flowSourceFactory,
							handlingCatch.exceptionModel.getActivityProcedure(), (link) -> link.getActivityProcedure());
					this.sectionConnector.linkToSectionInput(flowSourceFactory,
							handlingCatch.exceptionModel.getActivitySectionInput(),
							(link) -> link.getActivitySectionInput());
					this.outputConnector.linkToOutput(flowSourceFactory,
							handlingCatch.exceptionModel.getActivityOutput(), (link) -> link.getActivityOutput());

					// Handled
					return;
				}
			}

			// Not handled, so link to section output
			SectionOutput output = this.sectionDesigner.addSectionOutput(escalationOutputName, escalationType.getName(),
					true);
			this.sectionDesigner.link(flowSourceFactory.get(), output);
		}
	}

	/**
	 * Catch for {@link Escalation}.
	 */
	private static class Catch {

		/**
		 * Handled {@link Escalation} type.
		 */
		private final Class<?> escalationType;

		/**
		 * {@link ActivityExceptionModel} for the {@link Escalation}.
		 */
		private final ActivityExceptionModel exceptionModel;

		/**
		 * Instantiate.
		 * 
		 * @param escalationType Handled {@link Escalation} type.
		 * @param exceptionModel {@link ActivityExceptionModel} for the
		 *                       {@link Escalation}.
		 */
		private Catch(Class<?> escalationType, ActivityExceptionModel exceptionModel) {
			this.escalationType = escalationType;
			this.exceptionModel = exceptionModel;
		}
	}

}
