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

package net.officefloor.activity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.AggregateChange;
import net.officefloor.model.impl.change.NoChange;

/**
 * {@link Change} for the {@link ActivityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityChangesImpl implements ActivityChanges {

	/**
	 * Sorts the models by name.
	 * 
	 * @param models        Models.
	 * @param nameExtractor {@link Function} to extract name.
	 */
	private static <M> void sortModelList(List<M> models, final Function<M, String> nameExtractor) {
		Collections.sort(models, (a, b) -> {
			String nameA = nameExtractor.apply(a);
			String nameB = nameExtractor.apply(b);
			return String.CASE_INSENSITIVE_ORDER.compare(nameA, nameB);
		});
	}

	/**
	 * Determines if the identifier is unique.
	 * 
	 * @param identifier    Identifier to determine is unique.
	 * @param changingModel Changing {@link Model}. May be <code>null</code> for new
	 *                      {@link Model} being added.
	 * @param models        Models.
	 * @param nameExtractor {@link Function} to extract name.
	 * @return <code>true</code> if identifier is unique.
	 */
	private static <M> boolean isUniqueModelIdentifier(String identifier, M changingModel, List<M> models,
			final Function<M, String> nameExtractor) {
		return models.stream()
				.allMatch((item) -> (item == changingModel) || (!identifier.equals(nameExtractor.apply(item))));
	}

	/**
	 * Sorts the {@link ActivityProcedureOutputModel} instances of the
	 * {@link ActivityProcedureModel}.
	 * 
	 * @param procedure {@link ActivityProcedureModel}.
	 */
	private static void sortProcedureOutputs(ActivityProcedureModel procedure) {
		sortModelList(procedure.getOutputs(), (model) -> model.getActivityProcedureOutputName());
	}

	/**
	 * Sorts the {@link ActivitySectionInputModel} and
	 * {@link ActivitySectionOutputModel} instances of the
	 * {@link ActivitySectionModel}.
	 * 
	 * @param section {@link ActivitySectionModel}.
	 */
	private static void sortSectionInputOutputs(ActivitySectionModel section) {
		sortModelList(section.getInputs(), (model) -> model.getActivitySectionInputName());
		sortModelList(section.getOutputs(), (model) -> model.getActivitySectionOutputName());
	}

	/**
	 * Obtains the unique name.
	 * 
	 * @param name          Base name.
	 * @param model         Model being named. May be <code>null</code>.
	 * @param models        Listing of the existing models.
	 * @param nameExtractor {@link Function} to extract the name.
	 * @return Unique name.
	 */
	private static <M> String getUniqueName(final String name, M model, List<M> models,
			Function<M, String> nameExtractor) {

		// Determine suffix
		String uniqueName = name;
		int suffix = 1;
		boolean isNameExist = false; // first time not include suffix
		do {
			// Increment suffix should name exist
			if (isNameExist) {
				suffix++;
				uniqueName = name + "-" + suffix;
			}

			// Check if name already exists
			isNameExist = false;
			for (M check : models) {
				if (check == model) {
					continue; // ignore same model
				}
				String extractedName = nameExtractor.apply(check);
				if (uniqueName.equals(extractedName)) {
					isNameExist = true;
				}
			}
		} while (isNameExist);

		// Return the unique name
		return uniqueName;
	}

	/**
	 * Removes the {@link ConnectionModel}.
	 * 
	 * @param connection {@link ConnectionModel} to remove. May be <code>null</code>
	 *                   if nothing to remove.
	 * @param list       List to add the removed {@link ConnectionModel} instances.
	 */
	private static void removeConnection(ConnectionModel connection, List<ConnectionModel> list) {

		// Ensure have connection to remove
		if (connection == null) {
			return;
		}

		// Remove the connection
		connection.remove();
		list.add(connection);
	}

	/**
	 * Removes the {@link ConnectionModel} instances.
	 * 
	 * @param connections Listing of {@link ConnectionModel} instances to remove.
	 *                    May be <code>null</code> if nothing to remove.
	 * @param list        List to add the removed {@link ConnectionModel} instances.
	 */
	private static <C extends ConnectionModel> void removeConnections(List<C> connections, List<ConnectionModel> list) {

		// Ensure have connections
		if (connections == null) {
			return;
		}

		// Remove the connections
		for (C conn : new ArrayList<C>(connections)) {
			removeConnection(conn, list);
		}
	}

	/**
	 * Reconnect the {@link ConnectionModel} instances.
	 * 
	 * @param connections {@link ConnectionModel} instances to reconnect. May be
	 *                    <code>null</code> if nothing to reconnect.
	 */
	private static <C extends ConnectionModel> void reconnectConnections(C[] connections) {

		// Ensure have connections
		if (connections == null) {
			return;
		}

		// Re-connect
		for (int i = 0; i < connections.length; i++) {
			connections[i].connect();
		}
	}

	/**
	 * {@link ActivityModel}.
	 */
	private final ActivityModel model;

	/**
	 * Initiate.
	 * 
	 * @param model {@link ActivityModel} to change.
	 */
	public ActivityChangesImpl(ActivityModel model) {
		this.model = model;
	}

	/**
	 * Sorts the {@link ActivityInputModel} instances.
	 */
	private void sortInputs() {
		sortModelList(this.model.getActivityInputs(), (model) -> model.getActivityInputName());
	}

	/**
	 * Sorts the {@link ActivityProcedureModel} instances.
	 */
	private void sortProcedures() {
		sortModelList(this.model.getActivityProcedures(), (model) -> model.getActivityProcedureName());
	}

	/**
	 * Sorts the {@link ActivitySectionModel} instances.
	 */
	private void sortSections() {
		sortModelList(this.model.getActivitySections(), (model) -> model.getActivitySectionName());
	}

	/**
	 * Sorts the {@link ActivityOutputModel} instances.
	 */
	private void sortOutputs() {
		sortModelList(this.model.getActivityOutputs(), (model) -> model.getActivityOutputName());
	}

	/**
	 * Sorts the {@link ActivityExceptionModel} instances.
	 */
	private void sortExceptions() {
		sortModelList(this.model.getActivityExceptions(), (model) -> model.getClassName());
	}

	/**
	 * Obtains the {@link ActivitySectionModel} for the
	 * {@link ActivitySectionInputModel}.
	 * 
	 * @param input {@link ActivitySectionInputModel}.
	 * @return {@link ActivitySectionModel} containing the
	 *         {@link ActivitySectionInputModel} or <code>null</code> if not within
	 *         {@link ActivityModel}.
	 */
	public ActivitySectionModel getSection(ActivitySectionInputModel input) {

		// Find the containing section
		ActivitySectionModel containingSection = null;
		for (ActivitySectionModel section : this.model.getActivitySections()) {
			for (ActivitySectionInputModel check : section.getInputs()) {
				if (check == input) {
					// Found containing section
					containingSection = section;
				}
			}
		}

		// Return the containing section
		return containingSection;
	}

	/*
	 * ======================= ActivityChanges =======================
	 */

	@Override
	public Change<ActivityInputModel> addInput(String inputName, String argumentType) {

		// Obtain the unique input name
		inputName = getUniqueName(inputName, null, this.model.getActivityInputs(),
				(model) -> model.getActivityInputName());

		// Create the input
		final ActivityInputModel input = new ActivityInputModel(inputName, argumentType);

		// Return change to add input
		return new AbstractChange<ActivityInputModel>(input, "Add Input") {
			@Override
			public void apply() {
				ActivityChangesImpl.this.model.addActivityInput(input);
				ActivityChangesImpl.this.sortInputs();
			}

			@Override
			public void revert() {
				ActivityChangesImpl.this.model.removeActivityInput(input);
			}
		};
	}

	@Override
	public Change<ActivityInputModel> refactorInput(final ActivityInputModel input, String inputName,
			String argumentType) {

		// Obtain the unique input name
		final String finalInputName = getUniqueName(inputName, input, this.model.getActivityInputs(),
				(model) -> model.getActivityInputName());

		// Capture existing details
		final String existingInputName = input.getActivityInputName();
		final String existingArgumentType = input.getArgumentType();

		// Return change to refactor
		return new AbstractChange<ActivityInputModel>(input, "Refactor Input") {
			@Override
			public void apply() {
				input.setActivityInputName(finalInputName);
				input.setArgumentType(argumentType);
				ActivityChangesImpl.this.sortInputs();
			}

			@Override
			public void revert() {
				input.setActivityInputName(existingInputName);
				input.setArgumentType(existingArgumentType);
				ActivityChangesImpl.this.sortInputs();
			}
		};
	}

	@Override
	public Change<ActivityInputModel> removeInput(ActivityInputModel input) {

		// Ensure HTTP input available to remove
		boolean isInModel = false;
		for (ActivityInputModel model : this.model.getActivityInputs()) {
			if (model == input) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Application path model not in model
			return new NoChange<ActivityInputModel>(input, "Remove input " + input.getActivityInputName(),
					" is not in Activity model");
		}

		// Return change to remove input
		return new AbstractChange<ActivityInputModel>(input, "Remove input " + input.getActivityInputName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(input.getActivitySectionInput(), list);
				removeConnection(input.getActivityOutput(), list);
				removeConnection(input.getActivityProcedure(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the input
				ActivityChangesImpl.this.model.removeActivityInput(input);
			}

			@Override
			public void revert() {
				// Add back the HTTP input
				ActivityChangesImpl.this.model.addActivityInput(input);
				reconnectConnections(this.connections);
				ActivityChangesImpl.this.sortInputs();
			}
		};
	}

	@Override
	public Change<ActivitySectionModel> addSection(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties, SectionType section) {

		// Obtain the unique section name
		sectionName = getUniqueName(sectionName, null, this.model.getActivitySections(),
				(model) -> model.getActivitySectionName());

		// Create the section
		final ActivitySectionModel woofSection = new ActivitySectionModel(sectionName, sectionSourceClassName,
				sectionLocation);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofSection.addProperty(new PropertyModel(property.getName(), property.getValue()));
			}
		}

		// Add the inputs
		for (SectionInputType input : section.getSectionInputTypes()) {
			String inputName = input.getSectionInputName();
			String parameterType = input.getParameterType();
			woofSection.addInput(new ActivitySectionInputModel(inputName, parameterType));
		}

		// Add the outputs
		for (SectionOutputType output : section.getSectionOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the output
			String outputName = output.getSectionOutputName();
			String argumentType = output.getArgumentType();
			woofSection.addOutput(new ActivitySectionOutputModel(outputName, argumentType));
		}

		// Sort the inputs/outputs
		sortSectionInputOutputs(woofSection);

		// Return the change to add section
		return new AbstractChange<ActivitySectionModel>(woofSection, "Add Section") {
			@Override
			public void apply() {
				ActivityChangesImpl.this.model.addActivitySection(woofSection);
				ActivityChangesImpl.this.sortSections();
			}

			@Override
			public void revert() {
				ActivityChangesImpl.this.model.removeActivitySection(woofSection);
			}
		};
	}

	@Override
	public Change<ActivitySectionModel> refactorSection(final ActivitySectionModel section, final String sectionName,
			final String sectionSourceClassName, final String sectionLocation, final PropertyList properties,
			final SectionType sectionType, Map<String, String> sectionInputNameMapping,
			Map<String, String> sectionOutputNameMapping) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (ActivitySectionModel model : this.model.getActivitySections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<ActivitySectionModel>(section, "Refactor section",
					"Section " + section.getActivitySectionName() + " is not in Activity model");
		}

		// Create change to sort inputs/outputs
		Change<ActivitySectionModel> sortChange = new AbstractChange<ActivitySectionModel>(section,
				"Sort inputs/outputs") {
			@Override
			public void apply() {
				sortSectionInputOutputs(section);
			}

			@Override
			public void revert() {
				this.apply(); // sort
			}
		};

		// Provide list of changes to aggregate
		List<Change<?>> changes = new LinkedList<Change<?>>();

		// Sort inputs/outputs at start (so revert has right order)
		changes.add(sortChange);

		// Obtain the existing details
		final String existingSectionName = section.getActivitySectionName();
		final String existingSectionSourceClassName = section.getSectionSourceClassName();
		final String existingSectionLocation = section.getSectionLocation();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(section.getProperties());

		// Create change to attributes and properties
		Change<ActivitySectionModel> attributeChange = new AbstractChange<ActivitySectionModel>(section,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				section.setActivitySectionName(sectionName);
				section.setSectionSourceClassName(sectionSourceClassName);
				section.setSectionLocation(sectionLocation);

				// Refactor the properties
				section.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						section.addProperty(new PropertyModel(property.getName(), property.getValue()));
					}
				}
			}

			@Override
			public void revert() {
				// Revert attributes
				section.setActivitySectionName(existingSectionName);
				section.setSectionSourceClassName(existingSectionSourceClassName);
				section.setSectionLocation(existingSectionLocation);

				// Revert the properties
				section.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					section.addProperty(property);
				}
			}
		};
		changes.add(attributeChange);

		// Obtain the mapping of existing inputs
		Map<String, ActivitySectionInputModel> existingInputNameMapping = new HashMap<>();
		for (ActivitySectionInputModel input : section.getInputs()) {
			existingInputNameMapping.put(input.getActivitySectionInputName(), input);
		}

		// Refactor the inputs (either refactoring, adding or removing)
		for (final SectionInputType inputType : sectionType.getSectionInputTypes()) {

			// Obtain the mapped section input model
			final String inputName = inputType.getSectionInputName();
			String mappedInputName = sectionInputNameMapping.get(inputName);
			final ActivitySectionInputModel existingInputModel = existingInputNameMapping.remove(mappedInputName);

			// Obtain further type details
			final String parameterType = inputType.getParameterType();

			// Determine action to take based on existing input
			Change<ActivitySectionInputModel> sectionInputChange;
			if (existingInputModel != null) {
				// Create change to refactor existing input
				final String existingInputName = existingInputModel.getActivitySectionInputName();
				final String existingParameterType = existingInputModel.getParameterType();
				sectionInputChange = new AbstractChange<ActivitySectionInputModel>(existingInputModel,
						"Refactor Section Input") {
					@Override
					public void apply() {
						existingInputModel.setActivitySectionInputName(inputName);
						existingInputModel.setParameterType(parameterType);

						// Rename connections links
						this.renameConnections(existingInputModel, sectionName, inputName);
					}

					@Override
					public void revert() {
						existingInputModel.setActivitySectionInputName(existingInputName);
						existingInputModel.setParameterType(existingParameterType);

						// Revert connection links
						this.renameConnections(existingInputModel, existingSectionName, existingInputName);
					}

					/**
					 * Renames the {@link ActivitySectionInputModel} connection names.
					 * 
					 * @param input       {@link ActivitySectionInputModel}.
					 * @param sectionName {@link ActivitySectionModel} name.
					 * @param inputName   {@link ActivitySectionInputModel} name.
					 */
					private void renameConnections(ActivitySectionInputModel input, String sectionName,
							String inputName) {

						// Rename exception connections
						for (ActivityExceptionToActivitySectionInputModel conn : input.getActivityExceptions()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename section output connections
						for (ActivitySectionOutputToActivitySectionInputModel conn : input
								.getActivitySectionOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the input connections
						for (ActivityInputToActivitySectionInputModel conn : input.getActivityInputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the procedure next connections
						for (ActivityProcedureNextToActivitySectionInputModel conn : input
								.getActivityProcedureNexts()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the procedure output connections
						for (ActivityProcedureOutputToActivitySectionInputModel conn : input
								.getActivityProcedureOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}
					}
				};

			} else {
				// Create change to add input (with no URI)
				final ActivitySectionInputModel newInputModel = new ActivitySectionInputModel(inputName, parameterType);
				sectionInputChange = new AbstractChange<ActivitySectionInputModel>(newInputModel, "Add Section Input") {
					@Override
					public void apply() {
						section.addInput(newInputModel);
					}

					@Override
					public void revert() {
						section.removeInput(newInputModel);
					}
				};
			}
			changes.add(sectionInputChange);
		}
		for (final ActivitySectionInputModel unmappedInputModel : existingInputNameMapping.values()) {
			// Create change to remove the unmapped input model
			Change<ActivitySectionInputModel> unmappedInputChange = new AbstractChange<ActivitySectionInputModel>(
					unmappedInputModel, "Remove Section Input") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnections(unmappedInputModel.getActivityExceptions(), list);
					removeConnections(unmappedInputModel.getActivitySectionOutputs(), list);
					removeConnections(unmappedInputModel.getActivityInputs(), list);
					removeConnections(unmappedInputModel.getActivityProcedureNexts(), list);
					removeConnections(unmappedInputModel.getActivityProcedureOutputs(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the section input
					section.removeInput(unmappedInputModel);
				}

				@Override
				public void revert() {

					// Add input back to section
					section.addInput(unmappedInputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedInputChange);
		}

		// Obtain the mapping of existing outputs
		Map<String, ActivitySectionOutputModel> existingOutputNameMapping = new HashMap<String, ActivitySectionOutputModel>();
		for (ActivitySectionOutputModel output : section.getOutputs()) {
			existingOutputNameMapping.put(output.getActivitySectionOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final SectionOutputType outputType : sectionType.getSectionOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the mapped section output model
			final String outputName = outputType.getSectionOutputName();
			String mappedOutputName = sectionOutputNameMapping.get(outputName);
			final ActivitySectionOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = outputType.getArgumentType();

			// Determine action to take based on existing output
			Change<ActivitySectionOutputModel> sectionOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getActivitySectionOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				sectionOutputChange = new AbstractChange<ActivitySectionOutputModel>(existingOutputModel,
						"Refactor Section Output") {
					@Override
					public void apply() {
						existingOutputModel.setActivitySectionOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel.setActivitySectionOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output (with no URI)
				final ActivitySectionOutputModel newOutputModel = new ActivitySectionOutputModel(outputName,
						argumentType);
				sectionOutputChange = new AbstractChange<ActivitySectionOutputModel>(newOutputModel,
						"Add Section Output") {
					@Override
					public void apply() {
						section.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						section.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(sectionOutputChange);
		}
		for (final ActivitySectionOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<ActivitySectionOutputModel> unmappedOutputChange = new AbstractChange<ActivitySectionOutputModel>(
					unmappedOutputModel, "Remove Section Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getActivityOutput(), list);
					removeConnection(unmappedOutputModel.getActivitySectionInput(), list);
					removeConnection(unmappedOutputModel.getActivityProcedure(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the section output
					section.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to section
					section.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort inputs/outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<ActivitySectionModel>(section, "Refactor Section",
				changes.toArray(new Change[changes.size()]));
	}

	@Override
	public Change<ActivitySectionModel> removeSection(final ActivitySectionModel section) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (ActivitySectionModel model : this.model.getActivitySections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<ActivitySectionModel>(section, "Remove section " + section.getActivitySectionName(),
					"Section " + section.getActivitySectionName() + " is not in Activity model");
		}

		// Return change to remove section
		return new AbstractChange<ActivitySectionModel>(section, "Remove section " + section.getActivitySectionName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				for (ActivitySectionInputModel input : section.getInputs()) {
					removeConnections(input.getActivitySectionOutputs(), list);
					removeConnections(input.getActivityExceptions(), list);
					removeConnections(input.getActivityInputs(), list);
					removeConnections(input.getActivityProcedureNexts(), list);
					removeConnections(input.getActivityProcedureOutputs(), list);
				}
				for (ActivitySectionOutputModel output : section.getOutputs()) {
					removeConnection(output.getActivitySectionInput(), list);
					removeConnection(output.getActivityOutput(), list);
					removeConnection(output.getActivityProcedure(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the section
				ActivityChangesImpl.this.model.removeActivitySection(section);
			}

			@Override
			public void revert() {
				// Add back the section
				ActivityChangesImpl.this.model.addActivitySection(section);
				reconnectConnections(this.connections);
				ActivityChangesImpl.this.sortSections();
			}
		};
	}

	@Override
	public Change<ActivityProcedureModel> addProcedure(String procedureName, String resource, String sourceName,
			String procedure, PropertyList properties, ProcedureType procedureType) {

		// Obtain the unique procedure name
		procedureName = getUniqueName(procedureName, null, this.model.getActivityProcedures(),
				(model) -> model.getActivityProcedureName());

		// Create the procedure
		final ActivityProcedureModel woofProcedure = new ActivityProcedureModel(procedureName, resource, sourceName,
				procedure);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofProcedure.addProperty(new PropertyModel(property.getName(), property.getValue()));
			}
		}

		// Add the flows
		for (ProcedureFlowType flow : procedureType.getFlowTypes()) {

			// Add the flow
			String flowName = flow.getFlowName();
			Class<?> argumentType = flow.getArgumentType();
			String argumentTypeName = argumentType == null ? null : argumentType.getName();
			woofProcedure.addOutput(new ActivityProcedureOutputModel(flowName, argumentTypeName));
		}

		// Add next details
		Class<?> nextArgumentType = procedureType.getNextArgumentType();
		String nextArgumentTypeName = nextArgumentType == null ? null : nextArgumentType.getName();
		woofProcedure.setNext(new ActivityProcedureNextModel(nextArgumentTypeName));

		// Sort the outputs
		sortProcedureOutputs(woofProcedure);

		// Return the change to add procedure
		return new AbstractChange<ActivityProcedureModel>(woofProcedure, "Add Procedure") {
			@Override
			public void apply() {
				ActivityChangesImpl.this.model.addActivityProcedure(woofProcedure);
				ActivityChangesImpl.this.sortProcedures();
			}

			@Override
			public void revert() {
				ActivityChangesImpl.this.model.removeActivityProcedure(woofProcedure);
			}
		};
	}

	@Override
	public Change<ActivityProcedureModel> refactorProcedure(ActivityProcedureModel procedureModel, String procedureName,
			String resource, String sourceName, String procedure, PropertyList properties, ProcedureType procedureType,
			Map<String, String> outputNameMapping) {

		// Ensure procedure available to remove
		boolean isInModel = false;
		for (ActivityProcedureModel model : this.model.getActivityProcedures()) {
			if (model == procedureModel) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Procedure model not in model
			return new NoChange<ActivityProcedureModel>(procedureModel, "Refactor procedure",
					"Procedure " + procedureModel.getActivityProcedureName() + " is not in Activity model");
		}

		// Create change to sort outputs
		Change<ActivityProcedureModel> sortChange = new AbstractChange<ActivityProcedureModel>(procedureModel,
				"Sort outputs") {
			@Override
			public void apply() {
				sortProcedureOutputs(procedureModel);
			}

			@Override
			public void revert() {
				this.apply(); // sort
			}
		};

		// Provide list of changes to aggregate
		List<Change<?>> changes = new LinkedList<Change<?>>();

		// Sort outputs at start (so revert has right order)
		changes.add(sortChange);

		// Obtain the existing details
		final String existingProcedureName = procedureModel.getActivityProcedureName();
		final String existingResource = procedureModel.getResource();
		final String existingSourceName = procedureModel.getSourceName();
		final String existingProcedure = procedureModel.getProcedureName();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(procedureModel.getProperties());

		// Obtain the next argument type (ensuring always next)
		ActivityProcedureNextModel nextModel = procedureModel.getNext();
		if (nextModel == null) {
			nextModel = new ActivityProcedureNextModel();
			procedureModel.setNext(nextModel);
		}
		final String existingNextArgumentType = nextModel.getArgumentType();

		// Create change to attributes and properties
		Change<ActivityProcedureModel> attributeChange = new AbstractChange<ActivityProcedureModel>(procedureModel,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				procedureModel.setActivityProcedureName(procedureName);
				procedureModel.setResource(resource);
				procedureModel.setSourceName(sourceName);
				procedureModel.setProcedureName(procedure);

				// Refactor the properties
				procedureModel.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						procedureModel.addProperty(new PropertyModel(property.getName(), property.getValue()));
					}
				}

				// Specify next argument type
				Class<?> nextArgumentType = procedureType.getNextArgumentType();
				procedureModel.getNext().setArgumentType(nextArgumentType == null ? null : nextArgumentType.getName());
			}

			@Override
			public void revert() {
				// Revert attributes
				procedureModel.setActivityProcedureName(existingProcedureName);
				procedureModel.setResource(existingResource);
				procedureModel.setSourceName(existingSourceName);
				procedureModel.setProcedureName(existingProcedure);

				// Revert the properties
				procedureModel.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					procedureModel.addProperty(property);
				}

				// Revert next argument type
				procedureModel.getNext().setArgumentType(existingNextArgumentType);
			}
		};
		changes.add(attributeChange);

		// Create change to refactor use of procedure
		Change<ActivityProcedureModel> procedureUseChange = new AbstractChange<ActivityProcedureModel>(procedureModel,
				"Refactor Procedure Use") {
			@Override
			public void apply() {
				this.renameConnections(procedureModel, procedureName);
			}

			@Override
			public void revert() {
				this.renameConnections(procedureModel, existingProcedureName);
			}

			/**
			 * Renames the {@link ActivityProcedureModel} connection names.
			 * 
			 * @param procedureModel {@link ActivityProcedureModel}.
			 * @param procedureName  {@link ActivityProcedureModel} name.
			 */
			private void renameConnections(ActivityProcedureModel procedureModel, String procedureName) {

				// Rename exception connections
				for (ActivityExceptionToActivityProcedureModel conn : procedureModel.getActivityExceptions()) {
					conn.setProcedureName(procedureName);
				}

				// Rename section output connections
				for (ActivitySectionOutputToActivityProcedureModel conn : procedureModel.getActivitySectionOutputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the input connections
				for (ActivityInputToActivityProcedureModel conn : procedureModel.getActivityInputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the procedure next connections
				for (ActivityProcedureNextToActivityProcedureModel conn : procedureModel.getActivityProcedureNexts()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the procedure output connections
				for (ActivityProcedureOutputToActivityProcedureModel conn : procedureModel
						.getActivityProcedureOutputs()) {
					conn.setProcedureName(procedureName);
				}
			}
		};
		changes.add(procedureUseChange);

		// Obtain the mapping of existing outputs
		Map<String, ActivityProcedureOutputModel> existingOutputNameMapping = new HashMap<>();
		for (ActivityProcedureOutputModel output : procedureModel.getOutputs()) {
			existingOutputNameMapping.put(output.getActivityProcedureOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final ProcedureFlowType flowType : procedureType.getFlowTypes()) {

			// Obtain the mapped procedure output model
			final String outputName = flowType.getFlowName();
			String mappedOutputName = outputNameMapping.get(outputName);
			final ActivityProcedureOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			Class<?> argumentType = flowType.getArgumentType();
			final String argumentTypeName = argumentType == null ? null : argumentType.getName();

			// Determine action to take based on existing output
			Change<ActivityProcedureOutputModel> procedureOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getActivityProcedureOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				procedureOutputChange = new AbstractChange<ActivityProcedureOutputModel>(existingOutputModel,
						"Refactor Procedure Output") {
					@Override
					public void apply() {
						existingOutputModel.setActivityProcedureOutputName(outputName);
						existingOutputModel.setArgumentType(argumentTypeName);
					}

					@Override
					public void revert() {
						existingOutputModel.setActivityProcedureOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output
				final ActivityProcedureOutputModel newOutputModel = new ActivityProcedureOutputModel(outputName,
						argumentTypeName);
				procedureOutputChange = new AbstractChange<ActivityProcedureOutputModel>(newOutputModel,
						"Add Procedure Output") {
					@Override
					public void apply() {
						procedureModel.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						procedureModel.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(procedureOutputChange);
		}
		for (final ActivityProcedureOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<ActivityProcedureOutputModel> unmappedOutputChange = new AbstractChange<ActivityProcedureOutputModel>(
					unmappedOutputModel, "Remove Procedure Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getActivityOutput(), list);
					removeConnection(unmappedOutputModel.getActivitySectionInput(), list);
					removeConnection(unmappedOutputModel.getActivityProcedure(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the procedure output
					procedureModel.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to procedure
					procedureModel.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<ActivityProcedureModel>(procedureModel, "Refactor Procedure",
				changes.toArray(new Change[changes.size()]));
	}

	@Override
	public Change<ActivityProcedureModel> removeProcedure(ActivityProcedureModel procedure) {

		// Ensure procedure available to remove
		boolean isInModel = false;
		for (ActivityProcedureModel model : this.model.getActivityProcedures()) {
			if (model == procedure) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Procedure model not in model
			return new NoChange<ActivityProcedureModel>(procedure,
					"Remove procedure " + procedure.getActivityProcedureName(),
					"Procedure " + procedure.getActivityProcedureName() + " is not in Activity model");
		}

		// Return change to remove section
		return new AbstractChange<ActivityProcedureModel>(procedure,
				"Remove procedure " + procedure.getActivityProcedureName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(procedure.getActivitySectionOutputs(), list);
				removeConnections(procedure.getActivityExceptions(), list);
				removeConnections(procedure.getActivityInputs(), list);
				removeConnections(procedure.getActivityProcedureNexts(), list);
				removeConnections(procedure.getActivityProcedureOutputs(), list);
				ActivityProcedureNextModel next = procedure.getNext();
				if (next != null) {
					removeConnection(next.getActivitySectionInput(), list);
					removeConnection(next.getActivityOutput(), list);
					removeConnection(next.getActivityProcedure(), list);
				}
				for (ActivityProcedureOutputModel output : procedure.getOutputs()) {
					removeConnection(output.getActivitySectionInput(), list);
					removeConnection(output.getActivityOutput(), list);
					removeConnection(output.getActivityProcedure(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the procedure
				ActivityChangesImpl.this.model.removeActivityProcedure(procedure);
			}

			@Override
			public void revert() {
				// Add back the procedure
				ActivityChangesImpl.this.model.addActivityProcedure(procedure);
				reconnectConnections(this.connections);
				ActivityChangesImpl.this.sortProcedures();
			}
		};
	}

	@Override
	public Change<ActivityOutputModel> addOutput(String outputName, String parameterType) {

		// Create the unique output name
		outputName = getUniqueName(outputName, null, this.model.getActivityOutputs(),
				(output) -> output.getActivityOutputName());

		// Create the output
		final ActivityOutputModel output = new ActivityOutputModel(outputName, parameterType);

		// Return change to add output
		return new AbstractChange<ActivityOutputModel>(output, "Add Output") {
			@Override
			public void apply() {
				ActivityChangesImpl.this.model.addActivityOutput(output);
				ActivityChangesImpl.this.sortOutputs();
			}

			@Override
			public void revert() {
				ActivityChangesImpl.this.model.removeActivityOutput(output);
			}
		};
	}

	@Override
	public Change<ActivityOutputModel> refactorOutput(ActivityOutputModel output, String outputName,
			String parameterType) {

		// Create the unique output name
		outputName = getUniqueName(outputName, null, this.model.getActivityOutputs(),
				(model) -> model.getActivityOutputName());

		// Track existing values
		final String existingOutputName = output.getActivityOutputName();
		final String existingParameterType = output.getParameterType();

		// Return change to output
		final String finalOutputName = outputName;
		return new AbstractChange<ActivityOutputModel>(output, "Refactor Output") {
			@Override
			public void apply() {
				output.setActivityOutputName(finalOutputName);
				output.setParameterType(parameterType);
				ActivityChangesImpl.this.sortOutputs();
				this.renameConnections();
			}

			@Override
			public void revert() {
				output.setActivityOutputName(existingOutputName);
				output.setParameterType(existingParameterType);
				ActivityChangesImpl.this.sortOutputs();
				this.renameConnections();
			}

			/**
			 * Renames the {@link ActivityOutputModel} connection names.
			 */
			private void renameConnections() {
				String outputName = output.getActivityOutputName();

				// Rename connections
				for (ActivityExceptionToActivityOutputModel conn : output.getActivityExceptions()) {
					conn.setOutputName(outputName);
				}
				for (ActivitySectionOutputToActivityOutputModel conn : output.getActivitySectionOutputs()) {
					conn.setOutputName(outputName);
				}
				for (ActivityInputToActivityOutputModel conn : output.getActivityInputs()) {
					conn.setOutputName(outputName);
				}
				for (ActivityProcedureNextToActivityOutputModel conn : output.getActivityProcedureNexts()) {
					conn.setOutputName(outputName);
				}
				for (ActivityProcedureOutputToActivityOutputModel conn : output.getActivityProcedureOutputs()) {
					conn.setOutputName(outputName);
				}
			}
		};
	};

	@Override
	public Change<ActivityOutputModel> removeOutput(final ActivityOutputModel output) {

		// Ensure output available to remove
		boolean isInModel = false;
		for (ActivityOutputModel model : this.model.getActivityOutputs()) {
			if (model == output) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Output model not in model
			return new NoChange<ActivityOutputModel>(output, "Remove output " + output.getActivityOutputName(),
					"Resource " + output.getActivityOutputName() + " is not in Activity model");
		}

		// Return change to remove output
		return new AbstractChange<ActivityOutputModel>(output, "Remove output " + output.getActivityOutputName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(output.getActivitySectionOutputs(), list);
				removeConnections(output.getActivityExceptions(), list);
				removeConnections(output.getActivityInputs(), list);
				removeConnections(output.getActivityProcedureNexts(), list);
				removeConnections(output.getActivityProcedureOutputs(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the output
				ActivityChangesImpl.this.model.removeActivityOutput(output);
			}

			@Override
			public void revert() {
				// Add back the output
				ActivityChangesImpl.this.model.addActivityOutput(output);
				reconnectConnections(this.connections);
				ActivityChangesImpl.this.sortOutputs();
			}
		};
	}

	@Override
	public Change<ActivityExceptionModel> addException(String exceptionClassName) {

		// Create the exception
		final ActivityExceptionModel exception = new ActivityExceptionModel(exceptionClassName);

		// Ensure exception not already added
		if (!isUniqueModelIdentifier(exceptionClassName, null, this.model.getActivityExceptions(),
				(model) -> model.getClassName())) {
			return new NoChange<ActivityExceptionModel>(exception, "Add Exception",
					"Exception already exists for '" + exceptionClassName + "'");
		}

		// Return change to add exception
		return new AbstractChange<ActivityExceptionModel>(exception, "Add Exception") {

			@Override
			public void apply() {
				ActivityChangesImpl.this.model.addActivityException(exception);
				ActivityChangesImpl.this.sortExceptions();
			}

			@Override
			public void revert() {
				ActivityChangesImpl.this.model.removeActivityException(exception);
			}

		};
	}

	@Override
	public Change<ActivityExceptionModel> refactorException(final ActivityExceptionModel exception,
			final String exceptionClassName) {

		// Ensure exception not already added
		if (!isUniqueModelIdentifier(exceptionClassName, exception, this.model.getActivityExceptions(),
				(model) -> model.getClassName())) {
			return new NoChange<ActivityExceptionModel>(exception, "Refactor Exception",
					"Exception already exists for '" + exceptionClassName + "'");
		}

		// Obtain the existing exception class name (for revert)
		final String existingExceptionClassName = exception.getClassName();

		// Return change to refactor exception
		return new AbstractChange<ActivityExceptionModel>(exception, "Refactor Exception") {
			@Override
			public void apply() {
				exception.setClassName(exceptionClassName);
			}

			@Override
			public void revert() {
				exception.setClassName(existingExceptionClassName);
			}
		};
	}

	@Override
	public Change<ActivityExceptionModel> removeException(final ActivityExceptionModel exception) {

		// Ensure exception available to remove
		boolean isInModel = false;
		for (ActivityExceptionModel model : this.model.getActivityExceptions()) {
			if (model == exception) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Exception model not in model
			return new NoChange<ActivityExceptionModel>(exception, "Remove exception " + exception.getClassName(),
					"Exception " + exception.getClassName() + " is not in Activity model");
		}

		// Return change to remove exception
		return new AbstractChange<ActivityExceptionModel>(exception, "Remove exception " + exception.getClassName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(exception.getActivitySectionInput(), list);
				removeConnection(exception.getActivityOutput(), list);
				removeConnection(exception.getActivityProcedure(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the exception
				ActivityChangesImpl.this.model.removeActivityException(exception);
			}

			@Override
			public void revert() {
				// Add back the exception
				ActivityChangesImpl.this.model.addActivityException(exception);
				reconnectConnections(this.connections);
				ActivityChangesImpl.this.sortExceptions();
			}
		};
	}

	/*
	 * ---------------------- Input links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkInput(C connection, ActivityInputModel input,
			String changeDescription) {
		return new AddLinkChange<C, ActivityInputModel>(connection, input, changeDescription) {
			@Override
			protected void addExistingConnections(ActivityInputModel source, List<ConnectionModel> list) {
				list.add(source.getActivitySectionInput());
				list.add(source.getActivityOutput());
				list.add(source.getActivityProcedure());
			}
		};
	}

	@Override
	public Change<ActivityInputToActivitySectionInputModel> linkInputToSectionInput(ActivityInputModel input,
			ActivitySectionInputModel sectionInput) {

		// Obtain the containing section
		ActivitySectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<ActivityInputToActivitySectionInputModel>(
					new ActivityInputToActivitySectionInputModel(), "Link Input to Section Input",
					"The section input '" + sectionInput.getActivitySectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkInput(
				new ActivityInputToActivitySectionInputModel(section.getActivitySectionName(),
						sectionInput.getActivitySectionInputName(), input, sectionInput),
				input, "Link Input to Section Input");
	}

	@Override
	public Change<ActivityInputToActivitySectionInputModel> removeInputToSectionInput(
			ActivityInputToActivitySectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Input to Section Input");
	}

	@Override
	public Change<ActivityInputToActivityOutputModel> linkInputToOutput(ActivityInputModel input,
			ActivityOutputModel output) {
		return this.linkInput(new ActivityInputToActivityOutputModel(output.getActivityOutputName(), input, output),
				input, "Link Input to Output");
	}

	@Override
	public Change<ActivityInputToActivityOutputModel> removeInputToOutput(ActivityInputToActivityOutputModel link) {
		return new RemoveLinkChange<>(link, "Remove Input to Output");
	}

	@Override
	public Change<ActivityInputToActivityProcedureModel> linkInputToProcedure(ActivityInputModel input,
			ActivityProcedureModel procedure) {
		return this.linkInput(
				new ActivityInputToActivityProcedureModel(procedure.getActivityProcedureName(), input, procedure),
				input, "Link Input to Procedure");
	}

	@Override
	public Change<ActivityInputToActivityProcedureModel> removeInputToProcedure(
			ActivityInputToActivityProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Input to Procedure");
	}

	/*
	 * ---------------------- SectionOutput links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkSectionOutput(C connection,
			ActivitySectionOutputModel sectionOutput, String changeDescription) {
		return new AddLinkChange<C, ActivitySectionOutputModel>(connection, sectionOutput, changeDescription) {
			@Override
			protected void addExistingConnections(ActivitySectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getActivitySectionInput());
				list.add(source.getActivityOutput());
				list.add(source.getActivityProcedure());
			}
		};
	}

	@Override
	public Change<ActivitySectionOutputToActivitySectionInputModel> linkSectionOutputToSectionInput(
			ActivitySectionOutputModel sectionOutput, ActivitySectionInputModel sectionInput) {

		// Obtain the containing section
		ActivitySectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<ActivitySectionOutputToActivitySectionInputModel>(
					new ActivitySectionOutputToActivitySectionInputModel(), "Remove Section Output to Section Input",
					"The section input '" + sectionInput.getActivitySectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkSectionOutput(
				new ActivitySectionOutputToActivitySectionInputModel(section.getActivitySectionName(),
						sectionInput.getActivitySectionInputName(), sectionOutput, sectionInput),
				sectionOutput, "Link Section Output to Section Input");
	}

	@Override
	public Change<ActivitySectionOutputToActivitySectionInputModel> removeSectionOutputToSectionInput(
			ActivitySectionOutputToActivitySectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Section Input");
	}

	@Override
	public Change<ActivitySectionOutputToActivityOutputModel> linkSectionOutputToOutput(
			ActivitySectionOutputModel sectionOutput, ActivityOutputModel output) {
		return this.linkSectionOutput(
				new ActivitySectionOutputToActivityOutputModel(output.getActivityOutputName(), sectionOutput, output),
				sectionOutput, "Link Section Output to Output");
	}

	@Override
	public Change<ActivitySectionOutputToActivityOutputModel> removeSectionOutputToOutput(
			ActivitySectionOutputToActivityOutputModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Output");
	}

	@Override
	public Change<ActivitySectionOutputToActivityProcedureModel> linkSectionOutputToProcedure(
			ActivitySectionOutputModel sectionOutput, ActivityProcedureModel procedure) {
		return this.linkSectionOutput(
				new ActivitySectionOutputToActivityProcedureModel(procedure.getActivityProcedureName(), sectionOutput,
						procedure),
				sectionOutput, "Link Section Output to Procedure");
	}

	@Override
	public Change<ActivitySectionOutputToActivityProcedureModel> removeSectionOutputToProcedure(
			ActivitySectionOutputToActivityProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Procedure");
	}

	/*
	 * ---------------------- Procedure Next links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkProcedureNext(C connection,
			ActivityProcedureNextModel procedureNext, String changeDescription) {
		return new AddLinkChange<C, ActivityProcedureNextModel>(connection, procedureNext, changeDescription) {
			@Override
			protected void addExistingConnections(ActivityProcedureNextModel source, List<ConnectionModel> list) {
				list.add(source.getActivitySectionInput());
				list.add(source.getActivityOutput());
				list.add(source.getActivityProcedure());
			}
		};
	}

	@Override
	public Change<ActivityProcedureNextToActivitySectionInputModel> linkProcedureNextToSectionInput(
			ActivityProcedureNextModel procedureNext, ActivitySectionInputModel sectionInput) {

		// Obtain the containing section
		ActivitySectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<ActivityProcedureNextToActivitySectionInputModel>(
					new ActivityProcedureNextToActivitySectionInputModel(), "Link Procedure Next to Section Input",
					"The section input '" + sectionInput.getActivitySectionInputName() + "' was not found");
		}

		// Return the change to add connection
		return this.linkProcedureNext(
				new ActivityProcedureNextToActivitySectionInputModel(section.getActivitySectionName(),
						sectionInput.getActivitySectionInputName(), procedureNext, sectionInput),
				procedureNext, "Link Procedure Next to Section Input");
	}

	@Override
	public Change<ActivityProcedureNextToActivitySectionInputModel> removeProcedureNextToSectionInput(
			ActivityProcedureNextToActivitySectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Section Input");
	}

	@Override
	public Change<ActivityProcedureNextToActivityOutputModel> linkProcedureNextToOutput(
			ActivityProcedureNextModel procedureNext, ActivityOutputModel output) {
		return this.linkProcedureNext(
				new ActivityProcedureNextToActivityOutputModel(output.getActivityOutputName(), procedureNext, output),
				procedureNext, "Link Procedure Next to Output");
	}

	@Override
	public Change<ActivityProcedureNextToActivityOutputModel> removeProcedureNextToOutput(
			ActivityProcedureNextToActivityOutputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Output");
	}

	@Override
	public Change<ActivityProcedureNextToActivityProcedureModel> linkProcedureNextToProcedure(
			ActivityProcedureNextModel procedureNext, ActivityProcedureModel procedure) {
		return this.linkProcedureNext(
				new ActivityProcedureNextToActivityProcedureModel(procedure.getActivityProcedureName(), procedureNext,
						procedure),
				procedureNext, "Link Procedure Next to Procedure");
	}

	@Override
	public Change<ActivityProcedureNextToActivityProcedureModel> removeProcedureNextToProcedure(
			ActivityProcedureNextToActivityProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Procedure");
	}

	/*
	 * ---------------------- Procedure Output links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkProcedureOutput(C connection,
			ActivityProcedureOutputModel procedureOutput, String changeDescription) {
		return new AddLinkChange<C, ActivityProcedureOutputModel>(connection, procedureOutput, changeDescription) {
			@Override
			protected void addExistingConnections(ActivityProcedureOutputModel source, List<ConnectionModel> list) {
				list.add(source.getActivitySectionInput());
				list.add(source.getActivityOutput());
				list.add(source.getActivityProcedure());
			}
		};
	}

	@Override
	public Change<ActivityProcedureOutputToActivitySectionInputModel> linkProcedureOutputToSectionInput(
			ActivityProcedureOutputModel procedureOutput, ActivitySectionInputModel sectionInput) {

		// Obtain the containing section
		ActivitySectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<ActivityProcedureOutputToActivitySectionInputModel>(
					new ActivityProcedureOutputToActivitySectionInputModel(), "Link Procedure Output to Section Input",
					"The section input '" + sectionInput.getActivitySectionInputName() + "' was not found");
		}

		// Return the change to add connection
		return this.linkProcedureOutput(
				new ActivityProcedureOutputToActivitySectionInputModel(section.getActivitySectionName(),
						sectionInput.getActivitySectionInputName(), procedureOutput, sectionInput),
				procedureOutput, "Link Procedure Output to Section Input");
	}

	@Override
	public Change<ActivityProcedureOutputToActivitySectionInputModel> removeProcedureOutputToSectionInput(
			ActivityProcedureOutputToActivitySectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Section Input");
	}

	@Override
	public Change<ActivityProcedureOutputToActivityOutputModel> linkProcedureOutputToOutput(
			ActivityProcedureOutputModel procedureOutput, ActivityOutputModel output) {
		return this.linkProcedureOutput(new ActivityProcedureOutputToActivityOutputModel(output.getActivityOutputName(),
				procedureOutput, output), procedureOutput, "Link Procedure Output to Output");
	}

	@Override
	public Change<ActivityProcedureOutputToActivityOutputModel> removeProcedureOutputToOutput(
			ActivityProcedureOutputToActivityOutputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Output");
	}

	@Override
	public Change<ActivityProcedureOutputToActivityProcedureModel> linkProcedureOutputToProcedure(
			ActivityProcedureOutputModel procedureOutput, ActivityProcedureModel procedure) {
		return this.linkProcedureOutput(
				new ActivityProcedureOutputToActivityProcedureModel(procedure.getActivityProcedureName(),
						procedureOutput, procedure),
				procedureOutput, "Link Procedure Output to Procedure");
	}

	@Override
	public Change<ActivityProcedureOutputToActivityProcedureModel> removeProcedureOutputToProcedure(
			ActivityProcedureOutputToActivityProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Procedure");
	}

	/*
	 * ---------------------- Exception links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkException(C connection, ActivityExceptionModel exception,
			String changeDescription) {
		return new AddLinkChange<C, ActivityExceptionModel>(connection, exception, changeDescription) {
			@Override
			protected void addExistingConnections(ActivityExceptionModel source, List<ConnectionModel> list) {
				list.add(source.getActivitySectionInput());
				list.add(source.getActivityOutput());
				list.add(source.getActivityProcedure());
			}
		};
	}

	@Override
	public Change<ActivityExceptionToActivitySectionInputModel> linkExceptionToSectionInput(
			ActivityExceptionModel exception, ActivitySectionInputModel sectionInput) {

		// Obtain the containing section
		ActivitySectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<ActivityExceptionToActivitySectionInputModel>(
					new ActivityExceptionToActivitySectionInputModel(), "Remove Exception to Section Input",
					"The section input '" + sectionInput.getActivitySectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkException(
				new ActivityExceptionToActivitySectionInputModel(section.getActivitySectionName(),
						sectionInput.getActivitySectionInputName(), exception, sectionInput),
				exception, "Link Exception to Section Input");
	}

	@Override
	public Change<ActivityExceptionToActivitySectionInputModel> removeExceptionToSectionInput(
			ActivityExceptionToActivitySectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Section Input");
	}

	@Override
	public Change<ActivityExceptionToActivityOutputModel> linkExceptionToOutput(ActivityExceptionModel exception,
			ActivityOutputModel output) {
		return this.linkException(
				new ActivityExceptionToActivityOutputModel(output.getActivityOutputName(), exception, output),
				exception, "Link Exception to Output");
	}

	@Override
	public Change<ActivityExceptionToActivityOutputModel> removeExceptionToOutput(
			ActivityExceptionToActivityOutputModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Output");
	}

	@Override
	public Change<ActivityExceptionToActivityProcedureModel> linkExceptionToProcedure(ActivityExceptionModel exception,
			ActivityProcedureModel procedure) {
		return this.linkException(new ActivityExceptionToActivityProcedureModel(procedure.getActivityProcedureName(),
				exception, procedure), exception, "Link Exception to Procedure");
	}

	@Override
	public Change<ActivityExceptionToActivityProcedureModel> removeExceptionToProcedure(
			ActivityExceptionToActivityProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Procedure");
	}

	/**
	 * Abstract {@link Change} to add a {@link ConnectionModel}.
	 */
	private abstract class AddLinkChange<C extends ConnectionModel, S extends Model> extends AbstractChange<C> {

		/**
		 * Source for {@link ConnectionModel}.
		 */
		private final S source;

		/**
		 * {@link ConnectionModel} instances.
		 */
		private ConnectionModel[] connections;

		/**
		 * Initiate.
		 * 
		 * @param connection        {@link ConnectionModel}.
		 * @param source            Source for {@link ConnectionModel}.
		 * @param changeDescription Change descriptions.
		 */
		public AddLinkChange(C connection, S source, String changeDescription) {
			super(connection, changeDescription);
			this.source = source;
		}

		/**
		 * Adds the existing {@link ConnectionModel} instances.
		 * 
		 * @param source Source of the {@link ConnectionModel}.
		 * @param list   List to add the {@link ConnectionModel} instances.
		 */
		protected abstract void addExistingConnections(S source, List<ConnectionModel> list);

		/*
		 * ====================== Change ======================
		 */

		@Override
		public void apply() {

			// Obtain existing connections
			List<ConnectionModel> existingLinks = new LinkedList<ConnectionModel>();
			this.addExistingConnections(this.source, existingLinks);

			// Remove the existing connections
			List<ConnectionModel> list = new LinkedList<ConnectionModel>();
			for (ConnectionModel existingLink : existingLinks) {
				removeConnection(existingLink, list);
			}
			this.connections = list.toArray(new ConnectionModel[list.size()]);

			// Connect
			this.getTarget().connect();
		}

		@Override
		public void revert() {

			// Remove the connection
			this.getTarget().remove();

			// Reconnect previous connections
			reconnectConnections(this.connections);
		}
	};

	/**
	 * {@link Change} to remove the {@link ConnectionModel}.
	 */
	private class RemoveLinkChange<C extends ConnectionModel> extends AbstractChange<C> {

		/**
		 * Initiate.
		 * 
		 * @param connection        {@link ConnectionModel}.
		 * @param changeDescription Change description.
		 */
		public RemoveLinkChange(C connection, String changeDescription) {
			super(connection, changeDescription);
		}

		/*
		 * ==================== Change =======================
		 */

		@Override
		public void apply() {
			// Remove connection
			this.getTarget().remove();
		}

		@Override
		public void revert() {
			// Reconnect
			this.getTarget().connect();
		}
	}

}
