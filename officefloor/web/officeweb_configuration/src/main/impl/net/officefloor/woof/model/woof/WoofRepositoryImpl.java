/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

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
 * {@link WoofRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofRepositoryImpl implements WoofRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository {@link ModelRepository}.
	 */
	public WoofRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ======================= WoofRepository ==========================
	 */

	@Override
	public void retrieveWoof(WoofModel woof, ConfigurationItem configuration) throws Exception {

		// Load the WoOF from the configuration
		this.modelRepository.retrieve(woof, configuration);

		// Create the set of HTTP continuations
		Map<String, WoofHttpContinuationModel> httpContinuations = new HashMap<>();
		for (WoofHttpContinuationModel httpContinuation : woof.getWoofHttpContinuations()) {
			httpContinuations.put(httpContinuation.getApplicationPath(), httpContinuation);
		}

		// Create the set of Section Inputs
		DoubleKeyMap<String, String, WoofSectionInputModel> sectionInputs = new DoubleKeyMap<>();
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {
				sectionInputs.put(section.getWoofSectionName(), input.getWoofSectionInputName(), input);
			}
		}

		// Create the set of Procedures
		Map<String, WoofProcedureModel> procedures = new HashMap<>();
		for (WoofProcedureModel procedure : woof.getWoofProcedures()) {
			procedures.put(procedure.getWoofProcedureName(), procedure);
		}

		// Create the set of Templates
		Map<String, WoofTemplateModel> templates = new HashMap<>();
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			templates.put(template.getApplicationPath(), template);
		}

		// Create the set of Securities
		Map<String, WoofSecurityModel> securities = new HashMap<>();
		for (WoofSecurityModel security : woof.getWoofSecurities()) {
			securities.put(security.getHttpSecurityName(), security);
		}

		// Create the set of Resources
		Map<String, WoofResourceModel> resources = new HashMap<>();
		for (WoofResourceModel resource : woof.getWoofResources()) {
			resources.put(resource.getResourcePath(), resource);
		}

		// Connect HTTP Continuations
		for (WoofHttpContinuationModel httpContinuation : woof.getWoofHttpContinuations()) {
			Connector<WoofHttpContinuationModel> connector = new Connector<>(httpContinuation);

			// Section Inputs
			connector.connect(httpContinuation.getWoofSectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofSectionInput(target);
					});

			// Templates
			connector.connect(httpContinuation.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
					(conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofTemplate(target);
					});

			// Resources
			connector.connect(httpContinuation.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
					(conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofResource(target);
					});

			// Securities
			connector.connect(httpContinuation.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
					(conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofSecurity(target);
					});

			// Redirects
			connector.connect(httpContinuation.getWoofRedirect(),
					(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofRedirect(target);
					});

			// Procedures
			connector.connect(httpContinuation.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setWoofHttpContinuation(source);
						conn.setWoofProcedure(target);
					});
		}

		// Connect HTTP Inputs
		for (WoofHttpInputModel httpInput : woof.getWoofHttpInputs()) {
			Connector<WoofHttpInputModel> connector = new Connector<>(httpInput);

			// Section Inputs
			connector.connect(httpInput.getWoofSectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofSectionInput(target);
					});

			// Templates
			connector.connect(httpInput.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
					(conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofTemplate(target);
					});

			// Resources
			connector.connect(httpInput.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
					(conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofResource(target);
					});

			// Securities
			connector.connect(httpInput.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
					(conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofSecurity(target);
					});

			// Redirects
			connector.connect(httpInput.getWoofHttpContinuation(),
					(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofHttpContinuation(target);
					});

			// Procedures
			connector.connect(httpInput.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setWoofHttpInput(source);
						conn.setWoofProcedure(target);
					});
		}

		// Connect Template
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			Connector<WoofTemplateModel> connector = new Connector<>(template);

			// Super Template
			connector.connect(template.getSuperWoofTemplate(),
					(conn) -> templates.get(conn.getSuperWoofTemplateApplicationPath()), (conn, source, target) -> {
						conn.setChildWoofTemplate(source);
						conn.setSuperWoofTemplate(target);
					});

			// Connect Template Outputs
			for (WoofTemplateOutputModel templateOutput : template.getOutputs()) {
				Connector<WoofTemplateOutputModel> outputConnector = new Connector<>(templateOutput);

				// Section Inputs
				outputConnector.connect(templateOutput.getWoofSectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofSectionInput(target);
						});

				// Templates
				outputConnector.connect(templateOutput.getWoofTemplate(),
						(conn) -> templates.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofTemplate(target);
						});

				// Resources
				outputConnector.connect(templateOutput.getWoofResource(),
						(conn) -> resources.get(conn.getResourcePath()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofResource(target);
						});

				// Securities
				outputConnector.connect(templateOutput.getWoofSecurity(),
						(conn) -> securities.get(conn.getHttpSecurityName()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofSecurity(target);
						});

				// Redirects
				outputConnector.connect(templateOutput.getWoofHttpContinuation(),
						(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofHttpContinuation(target);
						});

				// Procedures
				outputConnector.connect(templateOutput.getWoofProcedure(),
						(conn) -> procedures.get(conn.getProcedureName()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofProcedure(target);
						});
			}
		}

		// Connect Section Outputs
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionOutputModel sectionOutput : section.getOutputs()) {
				Connector<WoofSectionOutputModel> connector = new Connector<>(sectionOutput);

				// Section Inputs
				connector.connect(sectionOutput.getWoofSectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofSectionInput(target);
						});

				// Templates
				connector.connect(sectionOutput.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
						(conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofTemplate(target);
						});

				// Resources
				connector.connect(sectionOutput.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
						(conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofResource(target);
						});

				// Securities
				connector.connect(sectionOutput.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
						(conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofSecurity(target);
						});

				// Redirects
				connector.connect(sectionOutput.getWoofHttpContinuation(),
						(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofHttpContinuation(target);
						});

				// Procedures
				connector.connect(sectionOutput.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
						(conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofProcedure(target);
						});
			}
		}

		// Connect Procedure Next
		for (WoofProcedureModel procedure : woof.getWoofProcedures()) {

			// Link next
			WoofProcedureNextModel procedureNext = procedure.getNext();
			if (procedureNext != null) {
				Connector<WoofProcedureNextModel> connector = new Connector<>(procedureNext);

				// Section Inputs
				connector.connect(procedureNext.getWoofSectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofSectionInput(target);
						});

				// Templates
				connector.connect(procedureNext.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
						(conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofTemplate(target);
						});

				// Resources
				connector.connect(procedureNext.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
						(conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofResource(target);
						});

				// Securities
				connector.connect(procedureNext.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
						(conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofSecurity(target);
						});

				// Redirects
				connector.connect(procedureNext.getWoofHttpContinuation(),
						(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofHttpContinuation(target);
						});

				// Procedures
				connector.connect(procedureNext.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
						(conn, source, target) -> {
							conn.setWoofProcedureNext(source);
							conn.setWoofProcedure(target);
						});
			}

			// Link outputs
			for (WoofProcedureOutputModel procedureOutput : procedure.getOutputs()) {
				Connector<WoofProcedureOutputModel> connector = new Connector<>(procedureOutput);

				// Section Inputs
				connector.connect(procedureOutput.getWoofSectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofSectionInput(target);
						});

				// Templates
				connector.connect(procedureOutput.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
						(conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofTemplate(target);
						});

				// Resources
				connector.connect(procedureOutput.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
						(conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofResource(target);
						});

				// Securities
				connector.connect(procedureOutput.getWoofSecurity(),
						(conn) -> securities.get(conn.getHttpSecurityName()), (conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofSecurity(target);
						});

				// Redirects
				connector.connect(procedureOutput.getWoofHttpContinuation(),
						(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofHttpContinuation(target);
						});

				// Procedures
				connector.connect(procedureOutput.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
						(conn, source, target) -> {
							conn.setWoofProcedureOutput(source);
							conn.setWoofProcedure(target);
						});
			}
		}

		// Connect Security Outputs
		for (WoofSecurityModel security : woof.getWoofSecurities()) {
			for (WoofSecurityOutputModel securityOutput : security.getOutputs()) {
				Connector<WoofSecurityOutputModel> connector = new Connector<>(securityOutput);

				// Section Inputs
				connector.connect(securityOutput.getWoofSectionInput(),
						(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()),
						(conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofSectionInput(target);
						});

				// Templates
				connector.connect(securityOutput.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
						(conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofTemplate(target);
						});

				// Resources
				connector.connect(securityOutput.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
						(conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofResource(target);
						});

				// Securities
				connector.connect(securityOutput.getWoofSecurity(),
						(conn) -> securities.get(conn.getHttpSecurityName()), (conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofSecurity(target);
						});

				// Redirects
				connector.connect(securityOutput.getWoofHttpContinuation(),
						(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofHttpContinuation(target);
						});

				// Procedures
				connector.connect(securityOutput.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
						(conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofProcedure(target);
						});
			}
		}

		// Connect Exceptions
		for (WoofExceptionModel exception : woof.getWoofExceptions()) {
			Connector<WoofExceptionModel> connector = new Connector<>(exception);

			// Section Inputs
			connector.connect(exception.getWoofSectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofSectionInput(target);
					});

			// Templates
			connector.connect(exception.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
					(conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofTemplate(target);
					});

			// Resources
			connector.connect(exception.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
					(conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofResource(target);
					});

			// Securities
			connector.connect(exception.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
					(conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofSecurity(target);
					});

			// Redirects
			connector.connect(exception.getWoofHttpContinuation(),
					(conn) -> httpContinuations.get(conn.getApplicationPath()), (conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofHttpContinuation(target);
					});

			// Procedures
			connector.connect(exception.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofProcedure(target);
					});
		}

		// Connect Starts
		for (WoofStartModel start : woof.getWoofStarts()) {
			Connector<WoofStartModel> connector = new Connector<>(start);

			// Section Inputs
			connector.connect(start.getWoofSectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setWoofStart(source);
						conn.setWoofSectionInput(target);
					});

			// Procedures
			connector.connect(start.getWoofProcedure(), (conn) -> procedures.get(conn.getProcedureName()),
					(conn, source, target) -> {
						conn.setWoofStart(source);
						conn.setWoofProcedure(target);
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
	public void storeWoof(WoofModel woof, WritableConfigurationItem configuration) throws Exception {

		// Specify HTTP continuations
		for (WoofHttpContinuationModel httpContinuation : woof.getWoofHttpContinuations()) {

			// Specify section outputs
			for (WoofSectionOutputToWoofHttpContinuationModel conn : httpContinuation.getWoofSectionOutputs()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify procedure next
			for (WoofProcedureNextToWoofHttpContinuationModel conn : httpContinuation.getWoofProcedureNexts()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify procedure output
			for (WoofProcedureOutputToWoofHttpContinuationModel conn : httpContinuation.getWoofProcedureOutputs()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofHttpContinuationModel conn : httpContinuation.getWoofTemplateOutputs()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofHttpContinuationModel conn : httpContinuation.getWoofSecurityOutputs()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify exceptions
			for (WoofExceptionToWoofHttpContinuationModel conn : httpContinuation.getWoofExceptions()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify redirects
			for (WoofHttpContinuationToWoofHttpContinuationModel conn : httpContinuation.getWoofHttpContinuations()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}

			// Specify HTTP inputs
			for (WoofHttpInputToWoofHttpContinuationModel conn : httpContinuation.getWoofHttpInputs()) {
				conn.setApplicationPath(httpContinuation.getApplicationPath());
			}
		}

		// Specify section inputs
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {

				// Specify section outputs
				for (WoofSectionOutputToWoofSectionInputModel conn : input.getWoofSectionOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify template outputs
				for (WoofTemplateOutputToWoofSectionInputModel conn : input.getWoofTemplateOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify security outputs
				for (WoofSecurityOutputToWoofSectionInputModel conn : input.getWoofSecurityOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify procedure next
				for (WoofProcedureNextToWoofSectionInputModel conn : input.getWoofProcedureNexts()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify procedure output
				for (WoofProcedureOutputToWoofSectionInputModel conn : input.getWoofProcedureOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify exceptions
				for (WoofExceptionToWoofSectionInputModel conn : input.getWoofExceptions()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify HTTP continuation
				for (WoofHttpContinuationToWoofSectionInputModel conn : input.getWoofHttpContinuations()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify HTTP input
				for (WoofHttpInputToWoofSectionInputModel conn : input.getWoofHttpInputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify starts
				for (WoofStartToWoofSectionInputModel conn : input.getWoofStarts()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}
			}
		}

		// Specify procedure
		for (WoofProcedureModel procedure : woof.getWoofProcedures()) {

			// Specify HTTP continuation
			for (WoofHttpContinuationToWoofProcedureModel conn : procedure.getWoofHttpContinuations()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify HTTP input
			for (WoofHttpInputToWoofProcedureModel conn : procedure.getWoofHttpInputs()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofProcedureModel conn : procedure.getWoofTemplateOutputs()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify section outputs
			for (WoofSectionOutputToWoofProcedureModel conn : procedure.getWoofSectionOutputs()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify procedure next
			for (WoofProcedureNextToWoofProcedureModel conn : procedure.getWoofProcedureNexts()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify procedure output
			for (WoofProcedureOutputToWoofProcedureModel conn : procedure.getWoofProcedureOutputs()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofProcedureModel conn : procedure.getWoofSecurityOutputs()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}

			// Specify exceptions
			for (WoofExceptionToWoofProcedureModel conn : procedure.getWoofExceptions()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}
			
			// Specify starts
			for (WoofStartToWoofProcedureModel conn : procedure.getWoofStarts()) {
				conn.setProcedureName(procedure.getWoofProcedureName());
			}
		}

		// Specify templates
		for (WoofTemplateModel template : woof.getWoofTemplates()) {

			// Specify super templates
			for (WoofTemplateToSuperWoofTemplateModel conn : template.getChildWoofTemplates()) {
				conn.setSuperWoofTemplateApplicationPath(template.getApplicationPath());
			}

			// Specify section outputs
			for (WoofSectionOutputToWoofTemplateModel conn : template.getWoofSectionOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify procedure next
			for (WoofProcedureNextToWoofTemplateModel conn : template.getWoofProcedureNexts()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify procedure output
			for (WoofProcedureOutputToWoofTemplateModel conn : template.getWoofProcedureOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofTemplateModel conn : template.getWoofTemplateOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofTemplateModel conn : template.getWoofSecurityOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify exceptions
			for (WoofExceptionToWoofTemplateModel conn : template.getWoofExceptions()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify HTTP continuations
			for (WoofHttpContinuationToWoofTemplateModel conn : template.getWoofHttpContinuations()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify HTTP inputs
			for (WoofHttpInputToWoofTemplateModel conn : template.getWoofHttpInputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}
		}

		// Specify securities
		for (WoofSecurityModel security : woof.getWoofSecurities()) {

			// Specify section outputs
			for (WoofSectionOutputToWoofSecurityModel conn : security.getWoofSectionOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify procedure next
			for (WoofProcedureNextToWoofSecurityModel conn : security.getWoofProcedureNexts()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify procedure output
			for (WoofProcedureOutputToWoofSecurityModel conn : security.getWoofProcedureOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofSecurityModel conn : security.getWoofTemplateOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofSecurityModel conn : security.getWoofSecurityOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify exceptions
			for (WoofExceptionToWoofSecurityModel conn : security.getWoofExceptions()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify HTTP continuations
			for (WoofHttpContinuationToWoofSecurityModel conn : security.getWoofHttpContinuations()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify HTTP inputs
			for (WoofHttpInputToWoofSecurityModel conn : security.getWoofHttpInputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}
		}

		// Specify resources
		for (WoofResourceModel resource : woof.getWoofResources()) {

			// Specify section outputs
			for (WoofSectionOutputToWoofResourceModel conn : resource.getWoofSectionOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofResourceModel conn : resource.getWoofTemplateOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofResourceModel conn : resource.getWoofSecurityOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify procedure next
			for (WoofProcedureNextToWoofResourceModel conn : resource.getWoofProcedureNexts()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify procedure output
			for (WoofProcedureOutputToWoofResourceModel conn : resource.getWoofProcedureOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify exceptions
			for (WoofExceptionToWoofResourceModel conn : resource.getWoofExceptions()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify HTTP continuations
			for (WoofHttpContinuationToWoofResourceModel conn : resource.getWoofHttpContinuations()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify HTTP inputs
			for (WoofHttpInputToWoofResourceModel conn : resource.getWoofHttpInputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}
		}

		// Store the WoOF
		this.modelRepository.store(woof, configuration);
	}

}
