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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link ActivityRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityRepositoryImpl implements ActivityRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository {@link ModelRepository}.
	 */
	public ActivityRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ======================= ActivityRepository ==========================
	 */

	@Override
	public void retrieveActivity(ActivityModel woof, ConfigurationItem configuration) throws Exception {

		// Load the Activity from the configuration
		this.modelRepository.retrieve(woof, configuration);

		// Create the set of Section Inputs
		DoubleKeyMap<String, String, ActivitySectionInputModel> sectionInputs = new DoubleKeyMap<>();
		for (ActivitySectionModel section : woof.getActivitySections()) {
			for (ActivitySectionInputModel input : section.getInputs()) {
				sectionInputs.put(section.getActivitySectionName(), input.getActivitySectionInputName(), input);
			}
		}

		// Create the set of Procedures
		Map<String, ActivityProcedureModel> procedures = new HashMap<>();
		for (ActivityProcedureModel procedure : woof.getActivityProcedures()) {
			procedures.put(procedure.getActivityProcedureName(), procedure);
		}

		// Create the set of Outputs
		Map<String, ActivityOutputModel> outputs = new HashMap<>();
		for (ActivityOutputModel output : woof.getActivityOutputs()) {
			outputs.put(output.getActivityOutputName(), output);
		}

		// Connect Inputs
		for (ActivityInputModel input : woof.getActivityInputs()) {
			Connector<ActivityInputModel> connector = new Connector<>(input);

			// Section Inputs
			connector.connect(input.getActivitySectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setActivityInput(input);
						conn.setActivitySectionInput(target);
					});

			// Procedures
			connector.connect(input.getActivityProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setActivityInput(source);
						conn.setActivityProcedure(target);
					});

			// Outputs
			connector.connect(input.getActivityOutput(), (conn) -> outputs.get(conn.getOutputName()),
					(conn, source, target) -> {
						conn.setActivityInput(source);
						conn.setActivityOutput(target);
					});
		}

		// Connect Section Outputs
		for (ActivitySectionModel section : woof.getActivitySections()) {
			for (ActivitySectionOutputModel sectionOutput : section.getOutputs()) {
				Connector<ActivitySectionOutputModel> connector = new Connector<>(sectionOutput);

				// Section Inputs
				connector.connect(sectionOutput.getActivitySectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setActivitySectionOutput(source);
							conn.setActivitySectionInput(target);
						});

				// Procedures
				connector.connect(sectionOutput.getActivityProcedure(),
						(conn) -> procedures.get(conn.getProcedureName()), (conn, source, target) -> {
							conn.setActivitySectionOutput(source);
							conn.setActivityProcedure(target);
						});

				// Outputs
				connector.connect(sectionOutput.getActivityOutput(), (conn) -> outputs.get(conn.getOutputName()),
						(conn, source, target) -> {
							conn.setActivitySectionOutput(source);
							conn.setActivityOutput(target);
						});
			}
		}

		// Connect Procedure Next
		for (ActivityProcedureModel procedure : woof.getActivityProcedures()) {

			// Link next
			ActivityProcedureNextModel procedureNext = procedure.getNext();
			if (procedureNext != null) {
				Connector<ActivityProcedureNextModel> connector = new Connector<>(procedureNext);

				// Section Inputs
				connector.connect(procedureNext.getActivitySectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setActivityProcedureNext(source);
							conn.setActivitySectionInput(target);
						});

				// Procedures
				connector.connect(procedureNext.getActivityProcedure(),
						(conn) -> procedures.get(conn.getProcedureName()), (conn, source, target) -> {
							conn.setActivityProcedureNext(source);
							conn.setActivityProcedure(target);
						});

				// Outputs
				connector.connect(procedureNext.getActivityOutput(), (conn) -> outputs.get(conn.getOutputName()),
						(conn, source, target) -> {
							conn.setActivityProcedureNext(source);
							conn.setActivityOutput(target);
						});
			}

			// Link outputs
			for (ActivityProcedureOutputModel procedureOutput : procedure.getOutputs()) {
				Connector<ActivityProcedureOutputModel> connector = new Connector<>(procedureOutput);

				// Section Inputs
				connector.connect(procedureOutput.getActivitySectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setActivityProcedureOutput(source);
							conn.setActivitySectionInput(target);
						});

				// Procedures
				connector.connect(procedureOutput.getActivityProcedure(),
						(conn) -> procedures.get(conn.getProcedureName()), (conn, source, target) -> {
							conn.setActivityProcedureOutput(source);
							conn.setActivityProcedure(target);
						});

				// Outputs
				connector.connect(procedureOutput.getActivityOutput(), (conn) -> outputs.get(conn.getOutputName()),
						(conn, source, target) -> {
							conn.setActivityProcedureOutput(source);
							conn.setActivityOutput(target);
						});
			}
		}

		// Connect Exceptions
		for (ActivityExceptionModel exception : woof.getActivityExceptions()) {
			Connector<ActivityExceptionModel> connector = new Connector<>(exception);

			// Section Inputs
			connector.connect(exception.getActivitySectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setActivityException(source);
						conn.setActivitySectionInput(target);
					});

			// Procedures
			connector.connect(exception.getActivityProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setActivityException(source);
						conn.setActivityProcedure(target);
					});

			// Outputs
			connector.connect(exception.getActivityOutput(), (conn) -> outputs.get(conn.getOutputName()),
					(conn, source, target) -> {
						conn.setActivityException(source);
						conn.setActivityOutput(target);
					});
		}
	}

	/**
	 * Convenience class to make connections easier.
	 */
	private static class Connector<S extends Model> {

		/**
		 * Source {@link Model}.
		 */
		private final S source;

		/**
		 * {@link Function} interface to obtain the target for the
		 * {@link ConnectionModel}.
		 */
		private static interface TargetFactory<C, T> {
			T getTarget(C connection);
		}

		/**
		 * {@link Function} interface to connect source and target with connection.
		 */
		private static interface Connect<C, S, T> {
			void connect(C connction, S source, T target);
		}

		/**
		 * Instantiate.
		 * 
		 * @param source Source {@link Model}.
		 */
		private Connector(S source) {
			this.source = source;
		}

		/**
		 * Undertakes linking connection.
		 * 
		 * @param connection    {@link ConnectionModel}. May be <code>null</code> if no
		 *                      link.
		 * @param targetFactory {@link TargetFactory}. Only invoked if
		 *                      {@link ConnectionModel}. May return <code>null</code>.
		 * @param connector     {@link Connect} to connect source and target.
		 */
		private <C extends ConnectionModel, T extends Model> void connect(C connection,
				TargetFactory<C, T> targetFactory, Connect<C, S, T> connector) {
			if (connection != null) {
				// Obtain the target
				T target = targetFactory.getTarget(connection);
				if (target != null) {
					connector.connect(connection, this.source, target);
					connection.connect();
				}
			}
		}
	}

	@Override
	public void storeActivity(ActivityModel woof, WritableConfigurationItem configuration) throws Exception {

		// Specify section inputs
		for (ActivitySectionModel section : woof.getActivitySections()) {
			for (ActivitySectionInputModel input : section.getInputs()) {

				// Specify inputs
				for (ActivityInputToActivitySectionInputModel conn : input.getActivityInputs()) {
					conn.setSectionName(section.getActivitySectionName());
					conn.setInputName(input.getActivitySectionInputName());
				}

				// Specify section outputs
				for (ActivitySectionOutputToActivitySectionInputModel conn : input.getActivitySectionOutputs()) {
					conn.setSectionName(section.getActivitySectionName());
					conn.setInputName(input.getActivitySectionInputName());
				}

				// Specify procedure next
				for (ActivityProcedureNextToActivitySectionInputModel conn : input.getActivityProcedureNexts()) {
					conn.setSectionName(section.getActivitySectionName());
					conn.setInputName(input.getActivitySectionInputName());
				}

				// Specify procedure output
				for (ActivityProcedureOutputToActivitySectionInputModel conn : input.getActivityProcedureOutputs()) {
					conn.setSectionName(section.getActivitySectionName());
					conn.setInputName(input.getActivitySectionInputName());
				}

				// Specify exceptions
				for (ActivityExceptionToActivitySectionInputModel conn : input.getActivityExceptions()) {
					conn.setSectionName(section.getActivitySectionName());
					conn.setInputName(input.getActivitySectionInputName());
				}
			}
		}

		// Specify procedure
		for (ActivityProcedureModel procedure : woof.getActivityProcedures()) {

			// Specify inputs
			for (ActivityInputToActivityProcedureModel conn : procedure.getActivityInputs()) {
				conn.setProcedureName(procedure.getActivityProcedureName());
			}

			// Specify section outputs
			for (ActivitySectionOutputToActivityProcedureModel conn : procedure.getActivitySectionOutputs()) {
				conn.setProcedureName(procedure.getActivityProcedureName());
			}

			// Specify procedure next
			for (ActivityProcedureNextToActivityProcedureModel conn : procedure.getActivityProcedureNexts()) {
				conn.setProcedureName(procedure.getActivityProcedureName());
			}

			// Specify procedure output
			for (ActivityProcedureOutputToActivityProcedureModel conn : procedure.getActivityProcedureOutputs()) {
				conn.setProcedureName(procedure.getActivityProcedureName());
			}

			// Specify exceptions
			for (ActivityExceptionToActivityProcedureModel conn : procedure.getActivityExceptions()) {
				conn.setProcedureName(procedure.getActivityProcedureName());
			}
		}

		// Specify outputs
		for (ActivityOutputModel output : woof.getActivityOutputs()) {

			// Specify inputs
			for (ActivityInputToActivityOutputModel conn : output.getActivityInputs()) {
				conn.setOutputName(output.getActivityOutputName());
			}

			// Specify section outputs
			for (ActivitySectionOutputToActivityOutputModel conn : output.getActivitySectionOutputs()) {
				conn.setOutputName(output.getActivityOutputName());
			}

			// Specify procedure next
			for (ActivityProcedureNextToActivityOutputModel conn : output.getActivityProcedureNexts()) {
				conn.setOutputName(output.getActivityOutputName());
			}

			// Specify procedure output
			for (ActivityProcedureOutputToActivityOutputModel conn : output.getActivityProcedureOutputs()) {
				conn.setOutputName(output.getActivityOutputName());
			}

			// Specify exceptions
			for (ActivityExceptionToActivityOutputModel conn : output.getActivityExceptions()) {
				conn.setOutputName(output.getActivityOutputName());
			}
		}

		// Store the Activity
		this.modelRepository.store(woof, configuration);
	}

}
