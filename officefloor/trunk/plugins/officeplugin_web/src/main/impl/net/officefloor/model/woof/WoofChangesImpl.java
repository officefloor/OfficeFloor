/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.woof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.NoChange;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

	/**
	 * Removes the {@link ConnectionModel}.
	 * 
	 * @param connection
	 *            {@link ConnectionModel} to remove. May be <code>null</code> if
	 *            nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
	 */
	private static void removeConnection(ConnectionModel connection,
			List<ConnectionModel> list) {

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
	 * @param connections
	 *            Listing of {@link ConnectionModel} instances to remove. May be
	 *            <code>null</code> if nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
	 */
	private static <C extends ConnectionModel> void removeConnections(
			List<C> connections, List<ConnectionModel> list) {

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
	 * @param connections
	 *            {@link ConnectionModel} instances to reconnect. May be
	 *            <code>null</code> if nothing to reconnect.
	 */
	private static <C extends ConnectionModel> void reconnectConnections(
			C[] connections) {

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
	 * {@link WoofModel}.
	 */
	private final WoofModel model;

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            {@link WoofModel} to change.
	 */
	public WoofChangesImpl(WoofModel model) {
		this.model = model;
	}

	/**
	 * Sorts the {@link WoofTemplateModel} instances.
	 */
	private void sortTemplates() {
		Collections.sort(this.model.getWoofTemplates(),
				new Comparator<WoofTemplateModel>() {
					@Override
					public int compare(WoofTemplateModel a, WoofTemplateModel b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.getWoofTemplateName(),
								b.getWoofTemplateName());
					}
				});
	}

	/**
	 * Sorts the {@link WoofSectionModel} instances.
	 */
	private void sortSections() {
		Collections.sort(this.model.getWoofSections(),
				new Comparator<WoofSectionModel>() {
					@Override
					public int compare(WoofSectionModel a, WoofSectionModel b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.getWoofSectionName(), b.getWoofSectionName());
					}
				});
	}

	/**
	 * Sorts the {@link WoofResourceModel} instances.
	 */
	private void sortResources() {
		Collections.sort(this.model.getWoofResources(),
				new Comparator<WoofResourceModel>() {
					@Override
					public int compare(WoofResourceModel a, WoofResourceModel b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.getWoofResourceName(),
								b.getWoofResourceName());
					}
				});
	}

	/**
	 * Sorts the {@link WoofExceptionModel} instances.
	 */
	private void sortExceptions() {
		Collections.sort(this.model.getWoofExceptions(),
				new Comparator<WoofExceptionModel>() {
					@Override
					public int compare(WoofExceptionModel a,
							WoofExceptionModel b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.getClassName(), b.getClassName());
					}
				});
	}

	/**
	 * Obtains the {@link WoofSectionModel} for the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param input
	 *            {@link WoofSectionInputModel}.
	 * @return {@link WoofSectionModel} containing the
	 *         {@link WoofSectionInputModel} or <code>null</code> if not within
	 *         {@link WoofModel}.
	 */
	public WoofSectionModel getSection(WoofSectionInputModel input) {

		// Find the containing section
		WoofSectionModel containingSection = null;
		for (WoofSectionModel section : this.model.getWoofSections()) {
			for (WoofSectionInputModel check : section.getInputs()) {
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
	 * ======================= WoofChanges =======================
	 */

	@Override
	public Change<WoofTemplateModel> addTemplate(String templateName,
			String templatePath, String templateLogicClass,
			SectionType section, String uri) {

		// Create the template
		final WoofTemplateModel template = new WoofTemplateModel(templateName,
				uri, "example/Template.ofp", templateLogicClass);

		// Add the outputs for the template
		for (SectionOutputType output : section.getSectionOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the Woof Template Output
			String outputName = output.getSectionOutputName();
			String argumentType = output.getArgumentType();
			template.addOutput(new WoofTemplateOutputModel(outputName,
					argumentType));
		}

		// Return change to add template
		return new AbstractChange<WoofTemplateModel>(template, "Add Template") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofTemplate(template);
				WoofChangesImpl.this.sortTemplates();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofTemplate(template);
			}
		};
	}

	@Override
	public Change<WoofTemplateModel> removeTemplate(
			final WoofTemplateModel template) {

		// Ensure template available to remove
		boolean isInModel = false;
		for (WoofTemplateModel model : this.model.getWoofTemplates()) {
			if (model == template) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Template model not in model
			return new NoChange<WoofTemplateModel>(template, "Remove template "
					+ template.getWoofTemplateName(), "Template "
					+ template.getWoofTemplateName() + " is not in WoOF model");
		}

		// Return change to remove template
		return new AbstractChange<WoofTemplateModel>(template,
				"Remove template " + template.getWoofTemplateName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(template.getWoofTemplateOutputs(), list);
				removeConnections(template.getWoofSectionOutputs(), list);
				removeConnections(template.getWoofExceptions(), list);
				for (WoofTemplateOutputModel output : template.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the template
				WoofChangesImpl.this.model.removeWoofTemplate(template);
			}

			@Override
			public void revert() {
				// Add back the template
				WoofChangesImpl.this.model.addWoofTemplate(template);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortTemplates();
			}
		};
	}

	@Override
	public Change<WoofSectionModel> addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType section,
			Map<String, String> inputToUri) {

		// Create the section
		final WoofSectionModel woofSection = new WoofSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);

		// Add the properties
		for (Property property : properties) {
			woofSection.addProperty(new PropertyModel(property.getName(),
					property.getValue()));
		}

		// Add the inputs
		for (SectionInputType input : section.getSectionInputTypes()) {
			String inputName = input.getSectionInputName();
			String parameterType = input.getParameterType();
			String uri = inputToUri.get(inputName);
			woofSection.addInput(new WoofSectionInputModel(inputName,
					parameterType, uri));
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
			woofSection.addOutput(new WoofSectionOutputModel(outputName,
					argumentType));
		}

		// Return the change to add section
		return new AbstractChange<WoofSectionModel>(woofSection, "Add Section") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofSection(woofSection);
				WoofChangesImpl.this.sortSections();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofSection(woofSection);
			}
		};
	}

	@Override
	public Change<WoofSectionModel> removeSection(final WoofSectionModel section) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (WoofSectionModel model : this.model.getWoofSections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<WoofSectionModel>(section, "Remove section "
					+ section.getWoofSectionName(), "Section "
					+ section.getWoofSectionName() + " is not in WoOF model");
		}

		// Return change to remove section
		return new AbstractChange<WoofSectionModel>(section, "Remove section "
				+ section.getWoofSectionName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				for (WoofSectionInputModel input : section.getInputs()) {
					removeConnections(input.getWoofTemplateOutputs(), list);
					removeConnections(input.getWoofSectionOutputs(), list);
					removeConnections(input.getWoofExceptions(), list);
				}
				for (WoofSectionOutputModel output : section.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the section
				WoofChangesImpl.this.model.removeWoofSection(section);
			}

			@Override
			public void revert() {
				// Add back the section
				WoofChangesImpl.this.model.addWoofSection(section);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortSections();
			}
		};
	}

	@Override
	public Change<WoofResourceModel> addResource(String resourceName,
			String resourcePath) {

		// Create the resource
		final WoofResourceModel resource = new WoofResourceModel(resourceName,
				resourcePath);

		// Return change to add resource
		return new AbstractChange<WoofResourceModel>(resource, "Add Resource") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofResource(resource);
				WoofChangesImpl.this.sortResources();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofResource(resource);
			}
		};
	}

	@Override
	public Change<WoofResourceModel> removeResource(
			final WoofResourceModel resource) {

		// Ensure resource available to remove
		boolean isInModel = false;
		for (WoofResourceModel model : this.model.getWoofResources()) {
			if (model == resource) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Resource model not in model
			return new NoChange<WoofResourceModel>(resource, "Remove resource "
					+ resource.getWoofResourceName(), "Resource "
					+ resource.getWoofResourceName() + " is not in WoOF model");
		}

		// Return change to remove resource
		return new AbstractChange<WoofResourceModel>(resource,
				"Remove resource " + resource.getWoofResourceName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(resource.getWoofTemplateOutputs(), list);
				removeConnections(resource.getWoofSectionOutputs(), list);
				removeConnections(resource.getWoofExceptions(), list);
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the resource
				WoofChangesImpl.this.model.removeWoofResource(resource);
			}

			@Override
			public void revert() {
				// Add back the resource
				WoofChangesImpl.this.model.addWoofResource(resource);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortResources();
			}
		};
	}

	@Override
	public Change<WoofExceptionModel> addException(String exceptionClassName) {

		// Create the exception
		final WoofExceptionModel exception = new WoofExceptionModel(
				exceptionClassName);

		// Return change to add exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Add Exception") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofException(exception);
				WoofChangesImpl.this.sortExceptions();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofException(exception);
			}
		};
	}

	@Override
	public Change<WoofExceptionModel> removeException(
			final WoofExceptionModel exception) {

		// Ensure exception available to remove
		boolean isInModel = false;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {
			if (model == exception) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Exception model not in model
			return new NoChange<WoofExceptionModel>(exception,
					"Remove exception " + exception.getClassName(),
					"Exception " + exception.getClassName()
							+ " is not in WoOF model");
		}

		// Return change to remove exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Remove exception " + exception.getClassName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(exception.getWoofTemplate(), list);
				removeConnection(exception.getWoofSectionInput(), list);
				removeConnection(exception.getWoofResource(), list);
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the exception
				WoofChangesImpl.this.model.removeWoofException(exception);
			}

			@Override
			public void revert() {
				// Add back the exception
				WoofChangesImpl.this.model.addWoofException(exception);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortExceptions();
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(
			final WoofTemplateOutputModel templateOutput,
			WoofTemplateModel template) {

		// Create the connection
		final WoofTemplateOutputToWoofTemplateModel connection = new WoofTemplateOutputToWoofTemplateModel(
				template.getWoofTemplateName(), templateOutput, template);

		// Return change to link
		return new AddLinkChange<WoofTemplateOutputToWoofTemplateModel, WoofTemplateOutputModel>(
				connection, templateOutput, "Link Template Output to Template") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOuputToTemplate(
			final WoofTemplateOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofTemplateModel>(
				link, "Remove Template Output to Template");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			final WoofTemplateOutputModel templateOutput,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofTemplateOutputToWoofSectionInputModel>(
					new WoofTemplateOutputToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofTemplateOutputToWoofSectionInputModel connection = new WoofTemplateOutputToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), templateOutput,
				sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofSectionInputModel, WoofTemplateOutputModel>(
				connection, templateOutput,
				"Link Template Output to Section Input") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOuputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofSectionInputModel>(
				link, "Remove Template Output to Section Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(
			WoofTemplateOutputModel templateOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofTemplateOutputToWoofResourceModel connection = new WoofTemplateOutputToWoofResourceModel(
				resource.getWoofResourceName(), templateOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofResourceModel, WoofTemplateOutputModel>(
				connection, templateOutput, "Link Template Output to Resource") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOuputToResource(
			WoofTemplateOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofResourceModel>(
				link, "Remove Template Output to Resource");
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(
			WoofSectionOutputModel sectionOutput, WoofTemplateModel template) {

		// Create the connection
		final WoofSectionOutputToWoofTemplateModel connection = new WoofSectionOutputToWoofTemplateModel(
				template.getWoofTemplateName(), sectionOutput, template);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofTemplateModel, WoofSectionOutputModel>(
				connection, sectionOutput, "Link Section Output to Template") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> removeSectionOuputToTemplate(
			WoofSectionOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofTemplateModel>(link,
				"Remove Section Output to Template");
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofSectionOutputToWoofSectionInputModel>(
					new WoofSectionOutputToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofSectionOutputToWoofSectionInputModel connection = new WoofSectionOutputToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), sectionOutput,
				sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofSectionInputModel, WoofSectionOutputModel>(
				connection, sectionOutput,
				"Link Section Output to Section Input") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOuputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofSectionInputModel>(
				link, "Remove Section Output to Section Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(
			WoofSectionOutputModel sectionOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofSectionOutputToWoofResourceModel connection = new WoofSectionOutputToWoofResourceModel(
				resource.getWoofResourceName(), sectionOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofResourceModel, WoofSectionOutputModel>(
				connection, sectionOutput, "Link Section Output to Resource") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> removeSectionOuputToResource(
			WoofSectionOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofResourceModel>(link,
				"Remove Section Output to Resource");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(
			WoofExceptionModel exception, WoofTemplateModel template) {
		// TODO implement WoofChanges.linkExceptionToTemplate
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.linkExceptionToTemplate");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(
			WoofExceptionToWoofTemplateModel link) {
		// TODO implement WoofChanges.removeExceptionToTemplate
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.removeExceptionToTemplate");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(
			WoofExceptionModel sectionOutput, WoofSectionInputModel sectionInput) {
		// TODO implement WoofChanges.linkExceptionToSectionInput
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.linkExceptionToSectionInput");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link) {
		// TODO implement WoofChanges.removeExceptionToSectionInput
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.removeExceptionToSectionInput");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(
			WoofExceptionModel sectionOutput, WoofResourceModel resource) {
		// TODO implement WoofChanges.linkExceptionToResource
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.linkExceptionToResource");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(
			WoofExceptionToWoofResourceModel link) {
		// TODO implement WoofChanges.removeExceptionToResource
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.removeExceptionToResource");
	}

	/**
	 * Abstract {@link Change} to add a {@link ConnectionModel}.
	 */
	private abstract class AddLinkChange<C extends ConnectionModel, S extends Model>
			extends AbstractChange<C> {

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
		 * @param connection
		 *            {@link ConnectionModel}.
		 * @param source
		 *            Source for {@link ConnectionModel}.
		 * @param changeDescription
		 *            Change descriptions.
		 */
		public AddLinkChange(C connection, S source, String changeDescription) {
			super(connection, changeDescription);
			this.source = source;
		}

		/**
		 * Adds the existing {@link ConnectionModel} instances.
		 * 
		 * @param source
		 *            Source of the {@link ConnectionModel}.
		 * @param list
		 *            List to add the {@link ConnectionModel} instances.
		 */
		protected abstract void addExistingConnections(S source,
				List<ConnectionModel> list);

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
	private class RemoveLinkChange<C extends ConnectionModel> extends
			AbstractChange<C> {

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ConnectionModel}.
		 * @param changeDescription
		 *            Change description.
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
	};

}