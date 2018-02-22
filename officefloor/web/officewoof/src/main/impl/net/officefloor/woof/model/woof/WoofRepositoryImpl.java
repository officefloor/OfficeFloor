/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
	 * @param modelRepository
	 *            {@link ModelRepository}.
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

		// Create the set of application paths
		Map<String, WoofHttpContinuationModel> applicationPaths = new HashMap<>();
		for (WoofHttpContinuationModel applicationPath : woof.getWoofApplicationPaths()) {
			applicationPaths.put(applicationPath.getApplicationPath(), applicationPath);
		}

		// Create the set of Section Inputs
		DoubleKeyMap<String, String, WoofSectionInputModel> sectionInputs = new DoubleKeyMap<>();
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {
				sectionInputs.put(section.getWoofSectionName(), input.getWoofSectionInputName(), input);
			}
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

		// Connect Application Paths
		for (WoofHttpContinuationModel applicationPath : woof.getWoofApplicationPaths()) {
			Connector<WoofHttpContinuationModel> connector = new Connector<>(applicationPath);

			// Section Inputs
			connector.connect(applicationPath.getWoofSectionInput(),
					(conn) -> sectionInputs.get(conn.getSectionName(), conn.getInputName()), (conn, source, target) -> {
						conn.setWoofApplicationPath(source);
						conn.setWoofSectionInput(target);
					});

			// Templates
			connector.connect(applicationPath.getWoofTemplate(), (conn) -> templates.get(conn.getApplicationPath()),
					(conn, source, target) -> {
						conn.setWoofApplicationPath(source);
						conn.setWoofTemplate(target);
					});

			// Resources
			connector.connect(applicationPath.getWoofResource(), (conn) -> resources.get(conn.getResourcePath()),
					(conn, source, target) -> {
						conn.setWoofApplicationPath(source);
						conn.setWoofResource(target);
					});

			// Securities
			connector.connect(applicationPath.getWoofSecurity(), (conn) -> securities.get(conn.getHttpSecurityName()),
					(conn, source, target) -> {
						conn.setWoofApplicationPath(source);
						conn.setWoofSecurity(target);
					});

			// Redirects
			connector.connect(applicationPath.getWoofApplicationPath(),
					(conn) -> applicationPaths.get(conn.getApplicationPath()), (conn, source, target) -> {
						conn.setWoofApplicationPath(source);
						conn.setWoofRedirect(target);
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
				outputConnector.connect(templateOutput.getWoofApplicationPath(),
						(conn) -> applicationPaths.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofTemplateOutput(source);
							conn.setWoofApplicationPath(target);
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
				connector.connect(sectionOutput.getWoofApplicationPath(),
						(conn) -> applicationPaths.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofSectionOutput(source);
							conn.setWoofApplicationPath(target);
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
				connector.connect(securityOutput.getWoofApplicationPath(),
						(conn) -> applicationPaths.get(conn.getApplicationPath()), (conn, source, target) -> {
							conn.setWoofSecurityOutput(source);
							conn.setWoofApplicationPath(target);
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
			connector.connect(exception.getWoofApplicationPath(),
					(conn) -> applicationPaths.get(conn.getApplicationPath()), (conn, source, target) -> {
						conn.setWoofException(source);
						conn.setWoofApplicationPath(target);
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
		 * {@link Function} interface to connect source and target with
		 * connection.
		 */
		private static interface Connect<C, S, T> {
			void connect(C connction, S source, T target);
		}

		/**
		 * Instantiate.
		 * 
		 * @param source
		 *            Source {@link Model}.
		 */
		private Connector(S source) {
			this.source = source;
		}

		/**
		 * Undertakes linking connection.
		 * 
		 * @param connection
		 *            {@link ConnectionModel}. May be <code>null</code> if no
		 *            link.
		 * @param targetFactory
		 *            {@link TargetFactory}. Only invoked if
		 *            {@link ConnectionModel}. May return <code>null</code>.
		 * @param connector
		 *            {@link Connect} to connect source and target.
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

		// Specify application paths
		for (WoofHttpContinuationModel applicationPath : woof.getWoofApplicationPaths()) {

			// Specify section outputs
			for (WoofSectionOutputToWoofHttpContinuationModel conn : applicationPath.getWoofSectionOutputs()) {
				conn.setApplicationPath(applicationPath.getApplicationPath());
			}

			// Specify template outputs
			for (WoofTemplateOutputToWoofHttpContinuationModel conn : applicationPath.getWoofTemplateOutputs()) {
				conn.setApplicationPath(applicationPath.getApplicationPath());
			}

			// Specify security outputs
			for (WoofSecurityOutputToWoofHttpContinuationModel conn : applicationPath.getWoofSecurityOutputs()) {
				conn.setApplicationPath(applicationPath.getApplicationPath());
			}

			// Specify exceptions
			for (WoofExceptionToWoofHttpContinuationModel conn : applicationPath.getWoofExceptions()) {
				conn.setApplicationPath(applicationPath.getApplicationPath());
			}

			// Specify redirects
			for (WoofHttpContinuationToWoofHttpContinuationModel conn : applicationPath.getWoofRedirects()) {
				conn.setApplicationPath(applicationPath.getApplicationPath());
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

				// Specify exceptions
				for (WoofExceptionToWoofSectionInputModel conn : input.getWoofExceptions()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify application paths
				for (WoofHttpContinuationToWoofSectionInputModel conn : input.getWoofApplicationPaths()) {
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

			// Specify application paths
			for (WoofHttpContinuationToWoofTemplateModel conn : template.getWoofApplicationPaths()) {
				conn.setApplicationPath(template.getApplicationPath());
			}
		}

		// Specify securities
		for (WoofSecurityModel security : woof.getWoofSecurities()) {

			// Specify section outputs
			for (WoofSectionOutputToWoofSecurityModel conn : security.getWoofSectionOutputs()) {
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

			// Specify application paths
			for (WoofHttpContinuationToWoofSecurityModel conn : security.getWoofApplicationPaths()) {
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

			// Specify exceptions
			for (WoofExceptionToWoofResourceModel conn : resource.getWoofExceptions()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify application paths
			for (WoofHttpContinuationToWoofResourceModel conn : resource.getWoofApplicationPaths()) {
				conn.setResourcePath(resource.getResourcePath());
			}
		}

		// Store the WoOF
		this.modelRepository.store(woof, configuration);
	}

}