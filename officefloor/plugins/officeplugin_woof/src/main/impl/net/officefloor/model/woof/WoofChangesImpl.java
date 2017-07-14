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
package net.officefloor.model.woof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.AggregateChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityFlowType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoader;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoaderImpl;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

	/**
	 * {@link WoofTemplateModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofTemplateModel> TEMPLATE_NAME_EXTRACTOR = new NameExtractor<WoofTemplateModel>() {
		@Override
		public String extractName(WoofTemplateModel model) {
			return model.getWoofTemplateName();
		}
	};

	/**
	 * {@link WoofTemplateLinkModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofTemplateLinkModel> TEMPLATE_LINK_NAME_EXTRACTOR = new NameExtractor<WoofTemplateLinkModel>() {
		@Override
		public String extractName(WoofTemplateLinkModel model) {
			return model.getWoofTemplateLinkName();
		}
	};

	/**
	 * {@link WoofTemplateRedirectModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofTemplateRedirectModel> TEMPLATE_REDIRECT_NAME_EXTRACTOR = new NameExtractor<WoofTemplateRedirectModel>() {
		@Override
		public String extractName(WoofTemplateRedirectModel model) {
			return model.getWoofTemplateRedirectHttpMethod();
		}
	};

	/**
	 * {@link WoofSectionModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionModel> SECTION_NAME_EXTRACTOR = new NameExtractor<WoofSectionModel>() {
		@Override
		public String extractName(WoofSectionModel model) {
			return model.getWoofSectionName();
		}
	};

	/**
	 * {@link WoofSectionInputModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionInputModel> SECTION_INPUT_NAME_EXTRACTOR = new NameExtractor<WoofSectionInputModel>() {
		@Override
		public String extractName(WoofSectionInputModel model) {
			return model.getWoofSectionInputName();
		}
	};

	/**
	 * {@link WoofSectionOutputModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionOutputModel> SECTION_OUTPUT_NAME_EXTRACTOR = new NameExtractor<WoofSectionOutputModel>() {
		@Override
		public String extractName(WoofSectionOutputModel model) {
			return model.getWoofSectionOutputName();
		}
	};

	/**
	 * {@link WoofAccessInputModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofAccessInputModel> ACCESS_INPUT_NAME_EXTRACTOR = new NameExtractor<WoofAccessInputModel>() {
		@Override
		public String extractName(WoofAccessInputModel model) {
			return model.getWoofAccessInputName();
		}
	};

	/**
	 * {@link WoofGovernanceModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofGovernanceModel> GOVERNANCE_NAME_EXTRACTOR = new NameExtractor<WoofGovernanceModel>() {
		@Override
		public String extractName(WoofGovernanceModel model) {
			return model.getWoofGovernanceName();
		}
	};

	/**
	 * {@link WoofGovernanceAreaModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofGovernanceAreaModel> GOVERNANCE_AREA_IDENTIFIER_EXTRACTOR = new NameExtractor<WoofGovernanceAreaModel>() {
		@Override
		public String extractName(WoofGovernanceAreaModel model) {
			return model.getWidth() + "-" + model.getHeight();
		}
	};

	/**
	 * {@link WoofResourceModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofResourceModel> RESOURCE_NAME_EXTRACTOR = new NameExtractor<WoofResourceModel>() {
		@Override
		public String extractName(WoofResourceModel model) {
			return model.getWoofResourceName();
		}
	};

	/**
	 * {@link WoofExceptionModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofExceptionModel> EXCEPTION_NAME_EXTRACTOR = new NameExtractor<WoofExceptionModel>() {
		@Override
		public String extractName(WoofExceptionModel model) {
			return model.getClassName();
		}
	};

	/**
	 * Sorts the models by name.
	 * 
	 * @param models
	 *            Models.
	 * @param nameExtractor
	 *            {@link NameExtractor}.
	 */
	private static <M> void sortByName(List<M> models, final NameExtractor<M> nameExtractor) {
		Collections.sort(models, new Comparator<M>() {
			@Override
			public int compare(M a, M b) {
				String nameA = nameExtractor.extractName(a);
				String nameB = nameExtractor.extractName(b);
				return String.CASE_INSENSITIVE_ORDER.compare(nameA, nameB);
			}
		});
	}

	/**
	 * Sorts the {@link WoofTemplateOutputModel} and
	 * {@link WoofTemplateExtensionModel} instances of the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 */
	private static void sortTemplateConfiguration(WoofTemplateModel template) {

		// Sort outputs keeping template render complete output last
		Collections.sort(template.getOutputs(), new Comparator<WoofTemplateOutputModel>() {
			@Override
			public int compare(WoofTemplateOutputModel a, WoofTemplateOutputModel b) {
				String nameA = a.getWoofTemplateOutputName();
				String nameB = b.getWoofTemplateOutputName();
				if (nameA.equals(nameB)) {
					return 0; // same
				} else if (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME.equals(nameA)) {
					return 1; // render complete output always last
				} else if (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME.equals(nameB)) {
					return -1; // render complete output always last
				} else {
					// Sort by name
					return String.CASE_INSENSITIVE_ORDER.compare(nameA, nameB);
				}
			}
		});

		// Sort the links
		sortByName(template.getLinks(), TEMPLATE_LINK_NAME_EXTRACTOR);

		// Sort the redirects
		sortByName(template.getRedirects(), TEMPLATE_REDIRECT_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofSectionInputModel} and
	 * {@link WoofSectionOutputModel} instances of the {@link WoofSectionModel}.
	 * 
	 * @param section
	 *            {@link WoofSectionModel}.
	 */
	private static void sortSectionInputOutputs(WoofSectionModel section) {
		sortByName(section.getInputs(), SECTION_INPUT_NAME_EXTRACTOR);
		sortByName(section.getOutputs(), SECTION_OUTPUT_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofAccessInputModel} and {@link WoofAccessOutputModel}
	 * instances of the {@link WoofAccessModel}.
	 * 
	 * @param access
	 *            {@link WoofAccessModel}.
	 */
	private static void sortAccessInputOutputs(WoofAccessModel access) {

		// Sort the inputs
		sortByName(access.getInputs(), ACCESS_INPUT_NAME_EXTRACTOR);

		// Sort outputs keeping Failure output last
		Collections.sort(access.getOutputs(), new Comparator<WoofAccessOutputModel>() {
			@Override
			public int compare(WoofAccessOutputModel a, WoofAccessOutputModel b) {
				String nameA = a.getWoofAccessOutputName();
				String nameB = b.getWoofAccessOutputName();
				if (nameA.equals(nameB)) {
					return 0; // same
				} else if (HttpSecuritySectionSource.OUTPUT_FAILURE.equals(nameA)) {
					return 1; // render complete output always last
				} else if (HttpSecuritySectionSource.OUTPUT_FAILURE.equals(nameB)) {
					return -1; // render complete output always last
				} else {
					// Sort by name
					return String.CASE_INSENSITIVE_ORDER.compare(nameA, nameB);
				}
			}
		});
	}

	/**
	 * Obtains the unique name.
	 * 
	 * @param name
	 *            Base name.
	 * @param model
	 *            Model being named. May be <code>null</code>.
	 * @param models
	 *            Listing of the existing models.
	 * @param nameExtractor
	 *            {@link NameExtractor}.
	 * @return Unique name.
	 */
	private static <M> String getUniqueName(final String name, M model, List<M> models,
			NameExtractor<M> nameExtractor) {

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
				String extractedName = nameExtractor.extractName(check);
				if (uniqueName.equals(extractedName)) {
					isNameExist = true;
				}
			}
		} while (isNameExist);

		// Return the unique name
		return uniqueName;
	}

	/**
	 * Extracts the name from the model.
	 */
	private static interface NameExtractor<M> {

		/**
		 * Obtains the name from the model.
		 * 
		 * @param model
		 *            Model.
		 * @return Name of the model.
		 */
		String extractName(M model);
	}

	/**
	 * Obtains the {@link WoofTemplateModel} name.
	 * 
	 * @param templatePath
	 *            Template Path.
	 * @param uri
	 *            URI.
	 * @param template
	 *            {@link WoofTemplateModel}. May be <code>null</code>.
	 * @param templates
	 *            {@link WoofTemplateModel} instances.
	 * @return Unique {@link WoofTemplateModel} name.
	 */
	private static String getTemplateName(String templatePath, String uri, WoofTemplateModel template,
			List<WoofTemplateModel> templates) {

		// Name based on template URI
		String templateName = WoofOfficeFloorSource.getTemplateSectionName(uri);

		// Obtain the unique template name
		templateName = getUniqueName(templateName, template, templates, TEMPLATE_NAME_EXTRACTOR);

		// Return the template name
		return templateName;
	}

	/**
	 * Removes the {@link ConnectionModel}.
	 * 
	 * @param connection
	 *            {@link ConnectionModel} to remove. May be <code>null</code> if
	 *            nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
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
	 * @param connections
	 *            Listing of {@link ConnectionModel} instances to remove. May be
	 *            <code>null</code> if nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
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
	 * @param connections
	 *            {@link ConnectionModel} instances to reconnect. May be
	 *            <code>null</code> if nothing to reconnect.
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
	 * Refactors the {@link WoofTemplateExtensionModel} instances.
	 * 
	 * @param existingTemplate
	 *            Existing {@link WoofTemplateModel}. May be <code>null</code>
	 *            if adding.
	 * @param newUri
	 *            New URI. May be <code>null</code> if removing
	 *            {@link WoofTemplateModel}.
	 * @param extensions
	 *            {@link WoofTemplateExtension} instances to refactor the
	 *            {@link WoofTemplateModel} to have. May be <code>null</code> to
	 *            indicate no changes to {@link WoofTemplateExtensionModel}
	 *            instances for the {@link WoofTemplateModel}.
	 * @param changeTemplate
	 *            {@link WoofTemplateModel} to be changed.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param issues
	 *            {@link WoofChangeIssues}.
	 * @return {@link Change} to refactor the {@link WoofTemplateExtensionModel}
	 *         instances on the {@link WoofTemplateModel}.
	 */
	@SuppressWarnings("unchecked")
	private static Change<WoofTemplateModel> refactorExtensions(WoofTemplateModel existingTemplate, String newUri,
			WoofTemplateExtension[] extensions, final WoofTemplateModel changeTemplate, SourceContext sourceContext,
			ConfigurationContext configurationContext, WoofChangeIssues issues) {

		// Create the list of existing extensions
		final List<WoofTemplateExtensionModel> existingExtensions = new ArrayList<WoofTemplateExtensionModel>(
				(existingTemplate == null ? Collections.EMPTY_LIST : existingTemplate.getExtensions()));

		// Create the list of new extensions (loaded later)
		final List<WoofTemplateExtensionModel> newExtensions = new LinkedList<WoofTemplateExtensionModel>();

		// Create change to refactor extensions
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(changeTemplate,
				"Refactor extensions") {
			@Override
			public void apply() {
				changeTemplate.getExtensions().clear();
				changeTemplate.getExtensions().addAll(newExtensions);
			}

			@Override
			public void revert() {
				changeTemplate.getExtensions().clear();
				changeTemplate.getExtensions().addAll(existingExtensions);
			}
		};

		// Obtain the old URI
		String oldUri = (existingTemplate == null ? null : existingTemplate.getUri());

		// Load the list of new extensions
		List<WoofTemplateExtensionModel> availableExtensions = new LinkedList<WoofTemplateExtensionModel>();
		availableExtensions.addAll(existingExtensions);
		if (extensions != null) {

			// Refactor to the provided extensions
			for (WoofTemplateExtension newExtension : extensions) {

				// Construct the extension model
				WoofTemplateExtensionModel extensionModel = new WoofTemplateExtensionModel();
				extensionModel.setExtensionClassName(newExtension.getWoofTemplateExtensionSourceClassName());
				for (WoofTemplateExtensionProperty property : newExtension.getWoofTemplateExtensionProperties()) {
					extensionModel.addProperty(new PropertyModel(property.getName(), property.getValue()));
				}

				// Refactor the extension
				Change<?> extensionChange = refactorExtension(extensionModel, oldUri, newUri, availableExtensions,
						changeTemplate, sourceContext, configurationContext, issues);

				// Include potential change into aggregate change
				if (extensionChange != null) {
					change = new AggregateChange<WoofTemplateModel>(changeTemplate, "Refactor extensions", change,
							extensionChange);
				}

				// Add the next extension
				newExtensions.add(extensionModel);
			}

		} else {
			// Refactor existing extensions (likely due to URI change)
			for (WoofTemplateExtensionModel extensionModel : existingExtensions) {

				// Refactor the extension
				Change<?> extensionChange = refactorExtension(extensionModel, oldUri, newUri, availableExtensions,
						changeTemplate, sourceContext, configurationContext, issues);

				// Include potential change into aggregate change
				if (extensionChange != null) {
					change = new AggregateChange<WoofTemplateModel>(changeTemplate, "Refactor extensions", change,
							extensionChange);
				}

				// Add the next extension
				newExtensions.add(extensionModel);
			}
		}

		// Remove available that no longer match extension
		for (WoofTemplateExtensionModel extensionModel : new ArrayList<WoofTemplateExtensionModel>(
				availableExtensions)) {

			// Refactor the extension (to remove)
			Change<?> extensionChange = refactorExtension(extensionModel, oldUri, null, availableExtensions,
					changeTemplate, sourceContext, configurationContext, issues);

			// Include potential change into aggregate change
			if (extensionChange != null) {
				change = new AggregateChange<WoofTemplateModel>(changeTemplate, "Refactor extensions", change,
						extensionChange);
			}
		}

		// Return the change
		return change;
	}

	/**
	 * Refactors the {@link WoofTemplateExtension} onto the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param extension
	 *            {@link WoofTemplateExtensionModel} to refactor.
	 * @param oldUri
	 *            Old URI. May be <code>null</code> if adding
	 *            {@link WoofTemplateExtensionModel}.
	 * @param newUri
	 *            New URI. May be <code>null</code> if removing
	 *            {@link WoofTemplateExtensionModel}.
	 * @param existingExtensions
	 *            Existing {@link WoofTemplateExtensionModel} instances. As
	 *            {@link WoofTemplateExtension} instances are refactored they
	 *            are removed from this list.
	 * @param changeTemplate
	 *            {@link WoofTemplateModel} being changed.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param issues
	 *            {@link WoofChangeIssues}.
	 * @return {@link Change} to refactor the {@link WoofTemplateExtension}.
	 */
	private static Change<?> refactorExtension(WoofTemplateExtensionModel extension, String oldUri, String newUri,
			List<WoofTemplateExtensionModel> existingExtensions, WoofTemplateModel changeTemplate,
			SourceContext sourceContext, ConfigurationContext configurationContext, WoofChangeIssues issues) {

		// Obtain the extension source class
		String extensionSourceClassName = extension.getExtensionClassName();

		// Obtain the first matching available extension
		WoofTemplateExtensionModel matchingExtension = null;
		FOUND_EXTENSION: for (Iterator<WoofTemplateExtensionModel> iterator = existingExtensions.iterator(); iterator
				.hasNext();) {
			WoofTemplateExtensionModel existingExtension = iterator.next();
			if (extensionSourceClassName.equals(existingExtension.getExtensionClassName())) {
				// Found matching extension
				matchingExtension = existingExtension;
				iterator.remove(); // remove as matched
				break FOUND_EXTENSION;
			}
		}

		// Construct the old properties
		SourcePropertiesImpl oldProperties = new SourcePropertiesImpl();
		if (matchingExtension == null) {
			// No matching extension, so indicate adding
			oldUri = null;

		} else {
			// Load properties for existing extension
			for (PropertyModel property : matchingExtension.getProperties()) {
				oldProperties.addProperty(property.getName(), property.getValue());
			}
		}

		// Construct the new properties
		SourcePropertiesImpl newProperties = new SourcePropertiesImpl();
		for (PropertyModel property : extension.getProperties()) {
			newProperties.addProperty(property.getName(), property.getValue());
		}

		// Create the possible change
		WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();
		Change<?> change = loader.refactorTemplateExtension(extensionSourceClassName, oldUri, oldProperties, newUri,
				newProperties, configurationContext, sourceContext, issues);

		// Return the possible change
		return change;
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
		sortByName(this.model.getWoofTemplates(), TEMPLATE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofSectionModel} instances.
	 */
	private void sortSections() {
		sortByName(this.model.getWoofSections(), SECTION_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofGovernanceModel} instances.
	 */
	private void sortGovernances() {
		sortByName(this.model.getWoofGovernances(), GOVERNANCE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofGovernanceAreaModel} for the
	 * {@link WoofGovernanceModel}.
	 * 
	 * @param governance
	 *            {@link WoofGovernanceModel} to have its
	 *            {@link WoofGovernanceAreaModel} instances sorted.
	 */
	private void sortGovernanceAreas(WoofGovernanceModel governance) {
		sortByName(governance.getGovernanceAreas(), GOVERNANCE_AREA_IDENTIFIER_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofResourceModel} instances.
	 */
	private void sortResources() {
		sortByName(this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofExceptionModel} instances.
	 */
	private void sortExceptions() {
		sortByName(this.model.getWoofExceptions(), EXCEPTION_NAME_EXTRACTOR);
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

	/**
	 * Obtains the {@link WoofGovernanceModel} for the
	 * {@link WoofGovernanceAreaModel}.
	 * 
	 * @param area
	 *            {@link WoofGovernanceAreaModel}.
	 * @return {@link WoofGovernanceModel} for the
	 *         {@link WoofGovernanceAreaModel} or <code>null</code> if not
	 *         within the {@link WoofModel}.
	 */
	private WoofGovernanceModel getGovernance(WoofGovernanceAreaModel area) {

		// Find the containing governance
		WoofGovernanceModel containingGovernance = null;
		for (WoofGovernanceModel governance : this.model.getWoofGovernances()) {
			for (WoofGovernanceAreaModel check : governance.getGovernanceAreas()) {
				if (check == area) {
					// Found containing governance
					containingGovernance = governance;
				}
			}
		}

		// Return the containing governance
		return containingGovernance;
	}

	/*
	 * ======================= WoofChanges =======================
	 */

	@Override
	public Map<String, WoofTemplateInheritance> getWoofTemplateInheritances() {

		// Create mapping of template by its name
		Map<String, WoofTemplateModel> templates = new HashMap<String, WoofTemplateModel>();
		for (WoofTemplateModel template : this.model.getWoofTemplates()) {
			String templateName = template.getWoofTemplateName();
			templates.put(templateName, template);
		}

		// Obtain the inheritance for each template
		Map<String, WoofTemplateInheritance> templateInheritances = new HashMap<String, WoofTemplateInheritance>();
		for (WoofTemplateModel template : this.model.getWoofTemplates()) {

			// Create the hierarchy for the template
			List<WoofTemplateModel> hierarchyList = new LinkedList<WoofTemplateModel>();
			WoofTemplateModel currentTemplate = template;
			boolean isCyclicHierarchy = false;
			while ((currentTemplate != null) && (!isCyclicHierarchy)) {

				// Include current template in hierarchy
				hierarchyList.add(currentTemplate);

				// Obtain the super template for next iteration
				String superTemplateName = currentTemplate.getSuperTemplate();
				currentTemplate = templates.get(superTemplateName);
				if (hierarchyList.contains(currentTemplate)) {
					isCyclicHierarchy = true;
				}
			}
			WoofTemplateModel[] hierarchy = hierarchyList.toArray(new WoofTemplateModel[hierarchyList.size()]);

			// Create the template path value and set of inherited output names
			StringBuilder templatePathValue = new StringBuilder();
			Set<String> inheritedOutputNames = new HashSet<String>();
			boolean isFirst = true;
			for (int i = (hierarchy.length - 1); i >= 0; i--) {
				WoofTemplateModel superTemplate = hierarchy[i];

				// Include the template path
				if (!isFirst) {
					templatePathValue.append(", ");
				}
				isFirst = false;
				templatePathValue.append(superTemplate.getTemplatePath());

				// Include the inherited output names
				for (WoofTemplateOutputModel output : superTemplate.getOutputs()) {
					String outputName = output.getWoofTemplateOutputName();
					inheritedOutputNames.add(outputName);
				}
			}

			// Create and include template inheritance
			WoofTemplateInheritance inheritance = new WoofTemplateInheritanceImpl(template, hierarchy,
					templatePathValue.toString(), inheritedOutputNames);
			templateInheritances.put(template.getWoofTemplateName(), inheritance);
		}

		// Return the template inheritances
		return templateInheritances;
	}

	@Override
	public Change<WoofTemplateModel> addTemplate(String uri, String templatePath, String templateLogicClass,
			SectionType section, WoofTemplateModel superTemplate, String contentType, boolean isTemplateSecure,
			Map<String, Boolean> linksSecure, String[] renderRedirectHttpMethods, boolean isContinueRendering,
			WoofTemplateExtension[] extensions, WoofTemplateChangeContext context) {

		// Obtain the template name
		String templateName = getTemplateName(templatePath, uri, null, this.model.getWoofTemplates());

		// Obtain the super template name
		String superTemplateName = (superTemplate == null ? null : superTemplate.getWoofTemplateName());

		// Create the template
		final WoofTemplateModel template = new WoofTemplateModel(templateName, uri, templatePath, superTemplateName,
				templateLogicClass, contentType, isTemplateSecure, isContinueRendering);

		// Determine if have links
		if (linksSecure != null) {
			// Add the links
			for (String linkName : linksSecure.keySet()) {
				Boolean isLinkSecure = linksSecure.get(linkName);
				template.addLink(new WoofTemplateLinkModel(linkName, isLinkSecure.booleanValue()));
			}
		}

		// Determine if have redirects
		if (renderRedirectHttpMethods != null) {
			// Add the redirects
			for (String redirectMethod : renderRedirectHttpMethods) {
				template.addRedirect(new WoofTemplateRedirectModel(redirectMethod));
			}
		}

		// Obtain the possible template inheritance
		Set<String> inheritedOutputNames = null;
		if (superTemplate != null) {
			// Obtain the inherited template outputs
			WoofTemplateInheritance inheritance = this.getWoofTemplateInheritances().get(superTemplateName);
			if (inheritance != null) {
				inheritedOutputNames = inheritance.getInheritedWoofTemplateOutputNames();
			}
		}

		// Add the outputs for the template
		for (SectionOutputType output : section.getSectionOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Obtain the output details
			String outputName = output.getSectionOutputName();
			String argumentType = output.getArgumentType();

			// Ignore if inherited
			if ((inheritedOutputNames != null) && (inheritedOutputNames.contains(outputName))) {
				continue;
			}

			// Ignore continue rendering (if appropriate)
			if ((!isContinueRendering) && (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME.equals(outputName))) {
				continue; // ignore continue rendering
			}

			// Add the Woof Template Output
			template.addOutput(new WoofTemplateOutputModel(outputName, argumentType));
		}

		// Return change to add template
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(template, "Add Template") {
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

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Include adding extensions
		Change<WoofTemplateModel> extensionChange = refactorExtensions(null, uri, extensions, template, context,
				context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			change = new AggregateChange<WoofTemplateModel>(template, change.getChangeDescription(), change,
					extensionChange);
		}

		// Sort template configuration to ensure deterministic configuration
		sortTemplateConfiguration(template);

		// Return the change
		return change;
	}

	@Override
	public Change<WoofTemplateModel> refactorTemplate(final WoofTemplateModel template, final String uri,
			final String templatePath, final String templateLogicClass, SectionType sectionType,
			WoofTemplateModel superTemplate, Set<String> inheritedTemplateOutputNames, String contentType,
			final boolean isTemplateSecure, final Map<String, Boolean> linksSecure,
			final String[] renderRedirectHttpMethods, final boolean isContinueRendering,
			WoofTemplateExtension[] extensions, Map<String, String> templateOutputNameMapping,
			WoofTemplateChangeContext context) {

		// Obtain the template name after URI change
		final String newTemplateName = getTemplateName(templatePath, uri, template, this.model.getWoofTemplates());

		// Create change to sort outputs
		Change<WoofTemplateModel> sortChange = new AbstractChange<WoofTemplateModel>(template, "Sort outputs") {
			@Override
			public void apply() {
				sortTemplateConfiguration(template);
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
		final String existingTemplateName = template.getWoofTemplateName();
		final String existingUri = template.getUri();
		final String existingTemplatePath = template.getTemplatePath();
		final String existingTemplateClassName = template.getTemplateClassName();
		final String existingSuperTemplateName = template.getSuperTemplate();
		final String existingContentType = template.getTemplateContentType();
		final boolean existingIsTemplateSecure = template.getIsTemplateSecure();
		final List<WoofTemplateLinkModel> existingTemplateLinks = new ArrayList<WoofTemplateLinkModel>(
				template.getLinks());
		final List<WoofTemplateRedirectModel> existingTemplateRedirects = new ArrayList<WoofTemplateRedirectModel>(
				template.getRedirects());
		final boolean existingIsContinueRendering = template.getIsContinueRendering();

		// Obtain the calculated new details
		final String newSuperTemplateName = (superTemplate == null ? null : superTemplate.getWoofTemplateName());

		// Create change to attributes
		Change<WoofTemplateModel> attributeChange = new AbstractChange<WoofTemplateModel>(template,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				template.setWoofTemplateName(newTemplateName);
				template.setUri(uri);
				template.setTemplatePath(templatePath);
				template.setTemplateClassName(templateLogicClass);
				template.setSuperTemplate(newSuperTemplateName);
				template.setTemplateContentType(contentType);
				template.setIsTemplateSecure(isTemplateSecure);
				template.setIsContinueRendering(isContinueRendering);

				// Refactor the links
				for (WoofTemplateLinkModel link : new ArrayList<WoofTemplateLinkModel>(template.getLinks())) {
					template.removeLink(link);
				}
				if (linksSecure != null) {
					// Add the refactored links
					for (String linkName : linksSecure.keySet()) {
						Boolean isLinkSecure = linksSecure.get(linkName);
						template.addLink(new WoofTemplateLinkModel(linkName, isLinkSecure.booleanValue()));
					}
				}

				// Refactor the redirects
				for (WoofTemplateRedirectModel redirect : new ArrayList<WoofTemplateRedirectModel>(
						template.getRedirects())) {
					template.removeRedirect(redirect);
				}
				if (renderRedirectHttpMethods != null) {
					// Add the redirects
					for (String redirectMethod : renderRedirectHttpMethods) {
						template.addRedirect(new WoofTemplateRedirectModel(redirectMethod));
					}
				}
			}

			@Override
			public void revert() {
				// Revert attributes
				template.setWoofTemplateName(existingTemplateName);
				template.setUri(existingUri);
				template.setTemplatePath(existingTemplatePath);
				template.setTemplateClassName(existingTemplateClassName);
				template.setSuperTemplate(existingSuperTemplateName);
				template.setTemplateContentType(existingContentType);
				template.setIsTemplateSecure(existingIsTemplateSecure);
				template.setIsContinueRendering(existingIsContinueRendering);

				// Revert the links
				for (WoofTemplateLinkModel link : new ArrayList<WoofTemplateLinkModel>(template.getLinks())) {
					template.removeLink(link);
				}
				for (WoofTemplateLinkModel link : existingTemplateLinks) {
					template.addLink(link);
				}

				// Revert the redirects
				for (WoofTemplateRedirectModel redirect : new ArrayList<WoofTemplateRedirectModel>(
						template.getRedirects())) {
					template.removeRedirect(redirect);
				}
				for (WoofTemplateRedirectModel redirect : existingTemplateRedirects) {
					template.addRedirect(redirect);
				}
			}
		};
		changes.add(attributeChange);

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Refactor extensions (ensuring have extensions)
		extensions = (extensions == null ? new WoofTemplateExtension[0] : extensions);
		Change<WoofTemplateModel> extensionChange = refactorExtensions(template, uri, extensions, template, context,
				context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			changes.add(extensionChange);
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofTemplateOutputModel> existingOutputNameMapping = new HashMap<String, WoofTemplateOutputModel>();
		for (WoofTemplateOutputModel output : template.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofTemplateOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final SectionOutputType outputType : sectionType.getSectionOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the output details
			final String outputName = outputType.getSectionOutputName();
			final String argumentType = outputType.getArgumentType();

			// Ignore if inheriting the output configuration
			if ((inheritedTemplateOutputNames != null) && (inheritedTemplateOutputNames.contains(outputName))) {
				continue;
			}

			// Ignore continue rendering (if appropriate)
			if ((!isContinueRendering) && (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME.equals(outputName))) {
				continue; // ignore continue rendering
			}

			// Obtain the mapped section output model
			String mappedOutputName = templateOutputNameMapping.get(outputName);
			final WoofTemplateOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Determine action to take based on existing output
			Change<WoofTemplateOutputModel> templateOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getWoofTemplateOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				templateOutputChange = new AbstractChange<WoofTemplateOutputModel>(existingOutputModel,
						"Refactor Template Output") {
					@Override
					public void apply() {
						existingOutputModel.setWoofTemplateOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel.setWoofTemplateOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output (with no URI)
				final WoofTemplateOutputModel newOutputModel = new WoofTemplateOutputModel(outputName, argumentType);
				templateOutputChange = new AbstractChange<WoofTemplateOutputModel>(newOutputModel,
						"Add Template Output") {
					@Override
					public void apply() {
						template.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						template.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(templateOutputChange);
		}
		for (final WoofTemplateOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<WoofTemplateOutputModel> unmappedOutputChange = new AbstractChange<WoofTemplateOutputModel>(
					unmappedOutputModel, "Remove Template Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getWoofResource(), list);
					removeConnection(unmappedOutputModel.getWoofSectionInput(), list);
					removeConnection(unmappedOutputModel.getWoofTemplate(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the template output
					template.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to template
					template.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort outputs at end (so apply has right order)
		changes.add(sortChange);

		// Create the aggregate change for refactoring
		Change<WoofTemplateModel> change = new AggregateChange<WoofTemplateModel>(template, "Refactor Template",
				changes.toArray(new Change[changes.size()]));

		// Include super template name changes
		change = this.includeSuperTemplateNameChanges(change, existingTemplateName, newTemplateName);

		// Return the change for refactoring
		return change;
	}

	@Override
	public Change<WoofTemplateModel> changeTemplateUri(final WoofTemplateModel template, final String uri,
			WoofTemplateChangeContext context) {

		// Keep track of original values
		final String originalTemplateName = template.getWoofTemplateName();
		final String originalUri = template.getUri();

		// Obtain the template name after URI change
		final String newTemplateName = getTemplateName(template.getTemplatePath(), uri, template,
				this.model.getWoofTemplates());

		// Create change to template URI
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(template, "Change Template URI") {
			@Override
			public void apply() {
				// Update template URI
				template.setUri(uri);
				template.setWoofTemplateName(newTemplateName);
				WoofChangesImpl.this.sortTemplates();
			}

			@Override
			public void revert() {

				// Revert template URI
				template.setUri(originalUri);
				template.setWoofTemplateName(originalTemplateName);
				WoofChangesImpl.this.sortTemplates();
			}
		};

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Refactor extensions for changed URI
		Change<WoofTemplateModel> extensionChange = refactorExtensions(template, uri, null, template, context,
				context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			change = new AggregateChange<WoofTemplateModel>(template, change.getChangeDescription(), change,
					extensionChange);
		}

		// Include changes for super template name
		change = this.includeSuperTemplateNameChanges(change, originalTemplateName, newTemplateName);

		// Return the change
		return change;
	}

	@Override
	public Change<WoofTemplateModel> removeTemplate(final WoofTemplateModel template,
			WoofTemplateChangeContext context) {

		// Obtain the template name
		String templateName = template.getWoofTemplateName();

		// Ensure template available to remove
		boolean isInModel = false;
		for (WoofTemplateModel model : this.model.getWoofTemplates()) {
			if (model == template) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Template model not in model
			return new NoChange<WoofTemplateModel>(template, "Remove template " + templateName,
					"Template " + templateName + " is not in WoOF model");
		}

		// Create change to remove template
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(template,
				"Remove template " + templateName) {

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
				removeConnections(template.getWoofAccessOutputs(), list);
				removeConnections(template.getWoofExceptions(), list);
				for (WoofTemplateOutputModel output : template.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofAccessInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

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

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Refactor extensions for removing template
		Change<WoofTemplateModel> extensionChange = refactorExtensions(template, null, null, template, context,
				context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			change = new AggregateChange<WoofTemplateModel>(template, change.getChangeDescription(), change,
					extensionChange);
		}

		// Include changes for child templates no longer inheriting
		change = this.includeSuperTemplateNameChanges(change, templateName, null);

		// Return the change
		return change;
	}

	/**
	 * Includes the {@link Change} instances for the super template name
	 * potentially changing.
	 * 
	 * @param currentChange
	 *            Current {@link Change}.
	 * @param originalTemplateName
	 *            Original {@link WoofTemplateModel} name.
	 * @param newTemplateName
	 *            New {@link WoofTemplateModel} name.
	 * @return {@link Change} including any potential super template name
	 *         changes.
	 */
	private Change<WoofTemplateModel> includeSuperTemplateNameChanges(Change<WoofTemplateModel> currentChange,
			final String originalTemplateName, final String newTemplateName) {

		// Identify the child templates
		List<WoofTemplateModel> childTemplates = new LinkedList<WoofTemplateModel>();
		for (WoofTemplateModel checkTemplate : this.model.getWoofTemplates()) {
			if (originalTemplateName.equals(checkTemplate.getSuperTemplate())) {
				childTemplates.add(checkTemplate);
			}
		}
		if (childTemplates.size() == 0) {
			// No child templates, so no additional changes
			return currentChange;
		}

		// Create changes for child templates (+1 to include current change)
		Change<?>[] changes = new Change[childTemplates.size() + 1];
		changes[0] = currentChange;
		int changeIndex = 1;
		for (final WoofTemplateModel childTemplate : childTemplates) {
			changes[changeIndex++] = new AbstractChange<WoofTemplateModel>(childTemplate,
					"Change Super Template Name") {
				@Override
				public void apply() {
					childTemplate.setSuperTemplate(newTemplateName);
				}

				@Override
				public void revert() {
					childTemplate.setSuperTemplate(originalTemplateName);
				}
			};
		}

		// Create and return the aggregate change
		return new AggregateChange<WoofTemplateModel>(currentChange.getTarget(), currentChange.getChangeDescription(),
				changes);
	}

	@Override
	public Change<WoofSectionModel> addSection(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties, SectionType section, Map<String, String> inputToUri) {

		// Obtain the unique section name
		sectionName = getUniqueName(sectionName, null, this.model.getWoofSections(), SECTION_NAME_EXTRACTOR);

		// Create the section
		final WoofSectionModel woofSection = new WoofSectionModel(sectionName, sectionSourceClassName, sectionLocation);

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
			String uri = inputToUri.get(inputName);
			woofSection.addInput(new WoofSectionInputModel(inputName, parameterType, uri));
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
			woofSection.addOutput(new WoofSectionOutputModel(outputName, argumentType));
		}

		// Sort the inputs/outputs
		sortSectionInputOutputs(woofSection);

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
	public Change<WoofSectionModel> refactorSection(final WoofSectionModel section, final String sectionName,
			final String sectionSourceClassName, final String sectionLocation, final PropertyList properties,
			final SectionType sectionType, Map<String, String> sectionInputNameMapping,
			Map<String, String> sectionOutputNameMapping) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (WoofSectionModel model : this.model.getWoofSections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<WoofSectionModel>(section, "Refactor section",
					"Section " + section.getWoofSectionName() + " is not in WoOF model");
		}

		// Create change to sort inputs/outputs
		Change<WoofSectionModel> sortChange = new AbstractChange<WoofSectionModel>(section, "Sort inputs/outputs") {
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
		final String existingSectionName = section.getWoofSectionName();
		final String existingSectionSourceClassName = section.getSectionSourceClassName();
		final String existingSectionLocation = section.getSectionLocation();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(section.getProperties());

		// Create change to attributes and properties
		Change<WoofSectionModel> attributeChange = new AbstractChange<WoofSectionModel>(section,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				section.setWoofSectionName(sectionName);
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
				section.setWoofSectionName(existingSectionName);
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
		Map<String, WoofSectionInputModel> existingInputNameMapping = new HashMap<String, WoofSectionInputModel>();
		for (WoofSectionInputModel input : section.getInputs()) {
			existingInputNameMapping.put(input.getWoofSectionInputName(), input);
		}

		// Refactor the inputs (either refactoring, adding or removing)
		for (final SectionInputType inputType : sectionType.getSectionInputTypes()) {

			// Obtain the mapped section input model
			final String inputName = inputType.getSectionInputName();
			String mappedInputName = sectionInputNameMapping.get(inputName);
			final WoofSectionInputModel existingInputModel = existingInputNameMapping.remove(mappedInputName);

			// Obtain further type details
			final String parameterType = inputType.getParameterType();

			// Determine action to take based on existing input
			Change<WoofSectionInputModel> sectionInputChange;
			if (existingInputModel != null) {
				// Create change to refactor existing input
				final String existingInputName = existingInputModel.getWoofSectionInputName();
				final String existingParameterType = existingInputModel.getParameterType();
				sectionInputChange = new AbstractChange<WoofSectionInputModel>(existingInputModel,
						"Refactor Section Input") {
					@Override
					public void apply() {
						existingInputModel.setWoofSectionInputName(inputName);
						existingInputModel.setParameterType(parameterType);

						// Rename connections links
						this.renameConnections(existingInputModel, sectionName, inputName);
					}

					@Override
					public void revert() {
						existingInputModel.setWoofSectionInputName(existingInputName);
						existingInputModel.setParameterType(existingParameterType);

						// Revert connection links
						this.renameConnections(existingInputModel, existingSectionName, existingInputName);
					}

					/**
					 * Renames the {@link WoofSectionInputModel} connection
					 * names.
					 * 
					 * @param input
					 *            {@link WoofSectionInputModel}.
					 * @param sectionName
					 *            {@link WoofSectionModel} name.
					 * @param inputName
					 *            {@link WoofSectionInputModel} name.
					 */
					private void renameConnections(WoofSectionInputModel input, String sectionName, String inputName) {

						// Rename exception connections
						for (WoofExceptionToWoofSectionInputModel conn : input.getWoofExceptions()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename section output connections
						for (WoofSectionOutputToWoofSectionInputModel conn : input.getWoofSectionOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename start connections
						for (WoofStartToWoofSectionInputModel conn : input.getWoofStarts()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename template connections
						for (WoofTemplateOutputToWoofSectionInputModel conn : input.getWoofTemplateOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}
					}
				};

			} else {
				// Create change to add input (with no URI)
				final WoofSectionInputModel newInputModel = new WoofSectionInputModel(inputName, parameterType, null);
				sectionInputChange = new AbstractChange<WoofSectionInputModel>(newInputModel, "Add Section Input") {
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
		for (final WoofSectionInputModel unmappedInputModel : existingInputNameMapping.values()) {
			// Create change to remove the unmapped input model
			Change<WoofSectionInputModel> unmappedInputChange = new AbstractChange<WoofSectionInputModel>(
					unmappedInputModel, "Remove Section Input") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnections(unmappedInputModel.getWoofExceptions(), list);
					removeConnections(unmappedInputModel.getWoofSectionOutputs(), list);
					removeConnections(unmappedInputModel.getWoofStarts(), list);
					removeConnections(unmappedInputModel.getWoofTemplateOutputs(), list);
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
		Map<String, WoofSectionOutputModel> existingOutputNameMapping = new HashMap<String, WoofSectionOutputModel>();
		for (WoofSectionOutputModel output : section.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofSectionOutputName(), output);
		}

		// Refactor the ouputs (either refactoring, adding or removing)
		for (final SectionOutputType outputType : sectionType.getSectionOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the mapped section output model
			final String outputName = outputType.getSectionOutputName();
			String mappedOutputName = sectionOutputNameMapping.get(outputName);
			final WoofSectionOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = outputType.getArgumentType();

			// Determine action to take based on existing output
			Change<WoofSectionOutputModel> sectionOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getWoofSectionOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				sectionOutputChange = new AbstractChange<WoofSectionOutputModel>(existingOutputModel,
						"Refactor Section Output") {
					@Override
					public void apply() {
						existingOutputModel.setWoofSectionOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel.setWoofSectionOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output (with no URI)
				final WoofSectionOutputModel newOutputModel = new WoofSectionOutputModel(outputName, argumentType);
				sectionOutputChange = new AbstractChange<WoofSectionOutputModel>(newOutputModel, "Add Section Output") {
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
		for (final WoofSectionOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<WoofSectionOutputModel> unmappedOutputChange = new AbstractChange<WoofSectionOutputModel>(
					unmappedOutputModel, "Remove Section Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getWoofResource(), list);
					removeConnection(unmappedOutputModel.getWoofSectionInput(), list);
					removeConnection(unmappedOutputModel.getWoofTemplate(), list);
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
		return new AggregateChange<WoofSectionModel>(section, "Refactor Section",
				changes.toArray(new Change[changes.size()]));
	}

	@Override
	public Change<WoofSectionInputModel> changeSectionInputUri(final WoofSectionInputModel sectionInput,
			final String uri) {

		// Maintain original URI
		final String originalUri = sectionInput.getUri();

		// Return change to URI
		return new AbstractChange<WoofSectionInputModel>(sectionInput, "Change Section Input URI") {
			@Override
			public void apply() {
				sectionInput.setUri(uri);
			}

			@Override
			public void revert() {
				sectionInput.setUri(originalUri);
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
			return new NoChange<WoofSectionModel>(section, "Remove section " + section.getWoofSectionName(),
					"Section " + section.getWoofSectionName() + " is not in WoOF model");
		}

		// Return change to remove section
		return new AbstractChange<WoofSectionModel>(section, "Remove section " + section.getWoofSectionName()) {

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
					removeConnections(input.getWoofAccessOutputs(), list);
					removeConnections(input.getWoofExceptions(), list);
					removeConnections(input.getWoofStarts(), list);
				}
				for (WoofSectionOutputModel output : section.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofAccessInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

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
	public Change<WoofAccessModel> setAccess(String httpSecuritySourceClassName, long timeout, PropertyList properties,
			HttpSecurityType<?, ?, ?, ?> httpSecurityType) {

		// Create the action
		final WoofAccessModel woofAccess = new WoofAccessModel(httpSecuritySourceClassName, timeout);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofAccess.addProperty(new PropertyModel(property.getName(), property.getValue()));
			}
		}

		// Add the outputs
		boolean isOutputFlow = false;
		for (HttpSecurityFlowType<?> output : httpSecurityType.getFlowTypes()) {
			// Add the output
			String outputName = output.getFlowName();
			Class<?> argumentType = output.getArgumentType();
			woofAccess.addOutput(
					new WoofAccessOutputModel(outputName, (argumentType == null ? null : argumentType.getName())));

			// Has output flow
			isOutputFlow = true;
		}
		woofAccess.addOutput(
				new WoofAccessOutputModel(HttpSecuritySectionSource.OUTPUT_FAILURE, Throwable.class.getName()));

		// Add the inputs (only if have output requiring application behaviour)
		if (isOutputFlow) {
			Class<?> credentialsType = httpSecurityType.getCredentialsClass();
			woofAccess.addInput(new WoofAccessInputModel("Authenticate",
					(credentialsType == null ? null : credentialsType.getName())));
		}

		// Sort the inputs/outputs
		sortAccessInputOutputs(woofAccess);

		// Create change to set access
		Change<WoofAccessModel> change = new AbstractChange<WoofAccessModel>(woofAccess, "Set Access") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.setWoofAccess(woofAccess);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.setWoofAccess(null);
			}
		};

		// Remove access if already specified
		WoofAccessModel existingAccess = this.model.getWoofAccess();
		if (existingAccess != null) {
			// Create change to remove access
			Change<WoofAccessModel> removeChange = this.removeAccess(existingAccess);

			// Provide aggregate change to remove and set
			change = new AggregateChange<WoofAccessModel>(woofAccess, "Set Access", removeChange, change);
		}

		// Return the change
		return change;
	}

	@Override
	public Change<WoofAccessModel> refactorAccess(final WoofAccessModel access,
			final String httpSecuritySourceClassName, final long timeout, final PropertyList properties,
			HttpSecurityType<?, ?, ?, ?> httpSecurityType, Map<String, String> accessOutputNameMapping) {

		// Ensure access to remove
		if (access != this.model.getWoofAccess()) {
			// Access model not in model
			return new NoChange<WoofAccessModel>(access, "Refactor access",
					"Access " + access.getHttpSecuritySourceClassName() + " is not in WoOF model");
		}

		// Create change to sort outputs
		Change<WoofAccessModel> sortChange = new AbstractChange<WoofAccessModel>(access, "Sort outputs") {
			@Override
			public void apply() {
				sortAccessInputOutputs(access);
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
		final String existingHttpSecuritySourceClassName = access.getHttpSecuritySourceClassName();
		final long existingTimeout = access.getTimeout();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(access.getProperties());

		// Create change to attributes and properties
		Change<WoofAccessModel> attributeChange = new AbstractChange<WoofAccessModel>(access, "Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				access.setHttpSecuritySourceClassName(httpSecuritySourceClassName);
				access.setTimeout(timeout);

				// Refactor the properties
				access.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						access.addProperty(new PropertyModel(property.getName(), property.getValue()));
					}
				}
			}

			@Override
			public void revert() {
				// Revert attributes
				access.setHttpSecuritySourceClassName(existingHttpSecuritySourceClassName);
				access.setTimeout(existingTimeout);

				// Revert the properties
				access.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					access.addProperty(property);
				}
			}
		};
		changes.add(attributeChange);

		// Create the listing of inputs of resulting refactor
		List<ModelItemStruct> outputs = new LinkedList<ModelItemStruct>();
		for (HttpSecurityFlowType<?> flowType : httpSecurityType.getFlowTypes()) {

			// Obtain flow details
			final String outputName = flowType.getFlowName();
			Class<?> argumentTypeClass = flowType.getArgumentType();
			final String argumentType = (argumentTypeClass == null ? null : argumentTypeClass.getName());

			// Create the output for the flow
			outputs.add(new ModelItemStruct(outputName, argumentType));
		}
		outputs.add(new ModelItemStruct(HttpSecuritySectionSource.OUTPUT_FAILURE, Throwable.class.getName()));

		// Determine if application behaviour required (except for failure)
		List<ModelItemStruct> inputs = new LinkedList<ModelItemStruct>();
		if (outputs.size() > 1) {
			// Require application behaviour so allow application authentication
			Class<?> credentialsClass = httpSecurityType.getCredentialsClass();
			String credentialsType = (credentialsClass == null ? null : credentialsClass.getName());
			inputs.add(new ModelItemStruct(HttpSecuritySectionSource.INPUT_AUTHENTICATE, credentialsType));
		}

		// Obtain the mapping of existing inputs
		Map<String, WoofAccessInputModel> existingInputNameMapping = new HashMap<String, WoofAccessInputModel>();
		for (WoofAccessInputModel input : access.getInputs()) {
			existingInputNameMapping.put(input.getWoofAccessInputName(), input);
		}

		// Refactor the inputs (either refactoring, adding or removing)
		for (ModelItemStruct input : inputs) {

			// Obtain the access input model details
			final String inputName = input.name;
			final String parameterType = input.type;

			// Obtain the equivalent input on model
			final WoofAccessInputModel existingInputModel = existingInputNameMapping.remove(inputName);

			// Determine action to take based on existing input
			Change<WoofAccessInputModel> accessInputChange;
			if (existingInputModel != null) {
				// Create change to refactor existing input
				final String existingInputName = existingInputModel.getWoofAccessInputName();
				final String existingParameterType = existingInputModel.getParameterType();
				accessInputChange = new AbstractChange<WoofAccessInputModel>(existingInputModel,
						"Refactor Access Input") {
					@Override
					public void apply() {
						existingInputModel.setWoofAccessInputName(inputName);
						existingInputModel.setParameterType(parameterType);

						// Rename connections links
						this.renameConnections(existingInputModel, inputName);
					}

					@Override
					public void revert() {
						existingInputModel.setWoofAccessInputName(existingInputName);
						existingInputModel.setParameterType(existingParameterType);

						// Revert connection links
						this.renameConnections(existingInputModel, existingInputName);
					}

					/**
					 * Renames the {@link WoofAccessInputModel} connection
					 * names.
					 * 
					 * @param input
					 *            {@link WoofAccessInputModel}.
					 * @param inputName
					 *            {@link WoofAccessInputModel} name.
					 */
					private void renameConnections(WoofAccessInputModel input, String inputName) {

						// Rename section output connections
						for (WoofSectionOutputToWoofAccessInputModel conn : input.getWoofSectionOutputs()) {
							conn.setInputName(inputName);
						}

						// Rename template connections
						for (WoofTemplateOutputToWoofAccessInputModel conn : input.getWoofTemplateOutputs()) {
							conn.setInputName(inputName);
						}
					}
				};

			} else {
				// Create change to add input (with no URI)
				final WoofAccessInputModel newInputModel = new WoofAccessInputModel(inputName, parameterType);
				accessInputChange = new AbstractChange<WoofAccessInputModel>(newInputModel, "Add Access Input") {
					@Override
					public void apply() {
						access.addInput(newInputModel);
					}

					@Override
					public void revert() {
						access.removeInput(newInputModel);
					}
				};
			}
			changes.add(accessInputChange);
		}
		for (final WoofAccessInputModel unmappedInputModel : existingInputNameMapping.values()) {
			// Create change to remove the unmapped input model
			Change<WoofAccessInputModel> unmappedInputChange = new AbstractChange<WoofAccessInputModel>(
					unmappedInputModel, "Remove Access Input") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnections(unmappedInputModel.getWoofSectionOutputs(), list);
					removeConnections(unmappedInputModel.getWoofTemplateOutputs(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the access input
					access.removeInput(unmappedInputModel);
				}

				@Override
				public void revert() {

					// Add input back to access
					access.addInput(unmappedInputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedInputChange);
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofAccessOutputModel> existingOutputNameMapping = new HashMap<String, WoofAccessOutputModel>();
		for (WoofAccessOutputModel output : access.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofAccessOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (ModelItemStruct output : outputs) {

			// Obtain the mapped access output model
			final String outputName = output.name;
			String mappedOutputName = (accessOutputNameMapping == null ? null
					: accessOutputNameMapping.get(outputName));
			final WoofAccessOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = output.type;

			// Determine action to take based on existing output
			Change<WoofAccessOutputModel> accessOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getWoofAccessOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				accessOutputChange = new AbstractChange<WoofAccessOutputModel>(existingOutputModel,
						"Refactor Access Output") {
					@Override
					public void apply() {
						existingOutputModel.setWoofAccessOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel.setWoofAccessOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output
				final WoofAccessOutputModel newOutputModel = new WoofAccessOutputModel(outputName, argumentType);
				accessOutputChange = new AbstractChange<WoofAccessOutputModel>(newOutputModel, "Add Access Output") {
					@Override
					public void apply() {
						access.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						access.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(accessOutputChange);
		}
		for (final WoofAccessOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<WoofAccessOutputModel> unmappedOutputChange = new AbstractChange<WoofAccessOutputModel>(
					unmappedOutputModel, "Remove Access Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getWoofResource(), list);
					removeConnection(unmappedOutputModel.getWoofSectionInput(), list);
					removeConnection(unmappedOutputModel.getWoofTemplate(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the access output
					access.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to access
					access.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort inputs/outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<WoofAccessModel>(access, "Refactor Access",
				changes.toArray(new Change[changes.size()]));
	}

	/**
	 * Item of a {@link Model}.
	 */
	private static class ModelItemStruct {

		/**
		 * Name.
		 */
		public final String name;

		/**
		 * Type.
		 */
		public final String type;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 */
		public ModelItemStruct(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}

	@Override
	public Change<WoofAccessModel> removeAccess(final WoofAccessModel access) {

		// Ensure access to remove
		if (access != this.model.getWoofAccess()) {
			// Access model not in model
			return new NoChange<WoofAccessModel>(access, "Remove access " + access.getHttpSecuritySourceClassName(),
					"Access " + access.getHttpSecuritySourceClassName() + " is not in WoOF model");
		}

		// Return change to remove access
		return new AbstractChange<WoofAccessModel>(access, "Remove access " + access.getHttpSecuritySourceClassName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				for (WoofAccessInputModel input : access.getInputs()) {
					removeConnections(input.getWoofTemplateOutputs(), list);
					removeConnections(input.getWoofSectionOutputs(), list);
				}
				for (WoofAccessOutputModel output : access.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the access
				WoofChangesImpl.this.model.setWoofAccess(null);
			}

			@Override
			public void revert() {
				// Add back the access
				WoofChangesImpl.this.model.setWoofAccess(access);
				reconnectConnections(this.connections);
				sortAccessInputOutputs(access);
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> addGovernance(String governanceName, String governanceSourceClassName,
			PropertyList properties, GovernanceType<?, ?> governanceType) {

		// Obtain the unique governance name
		governanceName = getUniqueName(governanceName, null, this.model.getWoofGovernances(),
				GOVERNANCE_NAME_EXTRACTOR);

		// Create the governance
		final WoofGovernanceModel woofGovernance = new WoofGovernanceModel(governanceName, governanceSourceClassName);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofGovernance.addProperty(new PropertyModel(property.getName(), property.getValue()));
			}
		}

		// Return the change to add governance
		return new AbstractChange<WoofGovernanceModel>(woofGovernance, "Add Governance") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofGovernance(woofGovernance);
				WoofChangesImpl.this.sortGovernances();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofGovernance(woofGovernance);
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> refactorGovernance(final WoofGovernanceModel governance, String governanceName,
			final String governanceSourceClassName, final PropertyList properties,
			GovernanceType<?, ?> governanceType) {

		// Ensure governance available to remove
		boolean isInModel = false;
		for (WoofGovernanceModel model : this.model.getWoofGovernances()) {
			if (model == governance) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Governance model not in model
			return new NoChange<WoofGovernanceModel>(governance,
					"Refactor governance " + governance.getWoofGovernanceName(),
					"Governance " + governance.getWoofGovernanceName() + " is not in WoOF model");
		}

		// Obtain the existing details
		final String existingGovernanceName = governance.getWoofGovernanceName();
		final String existingGovernanceSourceClassName = governance.getGovernanceSourceClassName();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(governance.getProperties());

		// Obtain the unique governance name
		final String uniqueGovernanceName = getUniqueName(governanceName, governance, this.model.getWoofGovernances(),
				GOVERNANCE_NAME_EXTRACTOR);

		// Return change to refactor governance
		return new AbstractChange<WoofGovernanceModel>(governance, "Refactor Governance") {

			@Override
			public void apply() {
				// Apply attribute changes
				governance.setWoofGovernanceName(uniqueGovernanceName);
				governance.setGovernanceSourceClassName(governanceSourceClassName);

				// Apply property changes
				governance.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						governance.addProperty(new PropertyModel(property.getName(), property.getValue()));
					}
				}

				// Update order of governances (after name change)
				WoofChangesImpl.this.sortGovernances();
			}

			@Override
			public void revert() {
				// Revert attributes
				governance.setWoofGovernanceName(existingGovernanceName);
				governance.setGovernanceSourceClassName(existingGovernanceSourceClassName);

				// Revert properties
				governance.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					governance.addProperty(property);
				}

				// Update order of governances (after name revert)
				WoofChangesImpl.this.sortGovernances();
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> removeGovernance(final WoofGovernanceModel governance) {

		// Ensure governance available to remove
		boolean isInModel = false;
		for (WoofGovernanceModel model : this.model.getWoofGovernances()) {
			if (model == governance) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Governance model not in model
			return new NoChange<WoofGovernanceModel>(governance,
					"Remove governance " + governance.getWoofGovernanceName(),
					"Governance " + governance.getWoofGovernanceName() + " is not in WoOF model");
		}

		// Return change to remove governance
		return new AbstractChange<WoofGovernanceModel>(governance,
				"Remove governance " + governance.getWoofGovernanceName()) {

			@Override
			public void apply() {
				// Remove the governance
				WoofChangesImpl.this.model.removeWoofGovernance(governance);
			}

			@Override
			public void revert() {
				// Add back the governance
				WoofChangesImpl.this.model.addWoofGovernance(governance);
				WoofChangesImpl.this.sortGovernances();
			}
		};
	}

	@Override
	public Change<WoofGovernanceAreaModel> addGovernanceArea(final WoofGovernanceModel governance, int width,
			int height) {

		// Create the governance area
		final WoofGovernanceAreaModel area = new WoofGovernanceAreaModel(width, height);

		// Return the change to add governance area
		return new AbstractChange<WoofGovernanceAreaModel>(area, "Add governance area") {
			@Override
			public void apply() {
				governance.addGovernanceArea(area);
				WoofChangesImpl.this.sortGovernanceAreas(governance);
			}

			@Override
			public void revert() {
				governance.removeGovernanceArea(area);
			}
		};
	}

	@Override
	public Change<WoofGovernanceAreaModel> removeGovernanceArea(final WoofGovernanceAreaModel governanceArea) {

		// Ensure governance area in WoOF to remove
		final WoofGovernanceModel governance = this.getGovernance(governanceArea);
		if (governance == null) {
			// Governance area not in model
			return new NoChange<WoofGovernanceAreaModel>(governanceArea, "Remove governance area ",
					"Governance area is not in WoOF model");
		}

		// Return change to remove governance area
		return new AbstractChange<WoofGovernanceAreaModel>(governanceArea, "Remove governance area") {

			@Override
			public void apply() {
				// Remove the governance area
				governance.removeGovernanceArea(governanceArea);
			}

			@Override
			public void revert() {
				// Add back the governance area
				governance.addGovernanceArea(governanceArea);
				WoofChangesImpl.this.sortGovernanceAreas(governance);
			}
		};
	}

	@Override
	public Change<WoofResourceModel> addResource(String resourcePath) {

		// Obtain unique resource name
		String resourceName = getUniqueName(resourcePath, null, this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);

		// Create the resource
		final WoofResourceModel resource = new WoofResourceModel(resourceName, resourcePath);

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
	public Change<WoofResourceModel> refactorResource(WoofResourceModel resource, String resourcePath) {
		return this.changeResourcePath(resource, resourcePath, "Refactor Resource");
	}

	@Override
	public Change<WoofResourceModel> changeResourcePath(final WoofResourceModel resource, final String resourcePath) {
		return this.changeResourcePath(resource, resourcePath, "Change Resource Path");
	}

	/**
	 * Changes the path for the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel} to have is path changed.
	 * @param resourcePath
	 *            New path for the {@link WoofResourceModel}.
	 * @param changeDescription
	 *            Description of type of {@link Change}.
	 * @return {@link Change} to the path for the {@link WoofResourceModel}.
	 */
	private Change<WoofResourceModel> changeResourcePath(final WoofResourceModel resource, final String resourcePath,
			String changeDescription) {

		// No change if no resource path
		if (CompileUtil.isBlank(resourcePath)) {
			return new NoChange<WoofResourceModel>(resource, changeDescription, "Must provide resource path");
		}

		// Track original values
		final String originalName = resource.getWoofResourceName();
		final String originalPath = resource.getResourcePath();

		// Obtain the resource name after the resource path
		final String newName = getUniqueName(resourcePath, resource, this.model.getWoofResources(),
				RESOURCE_NAME_EXTRACTOR);

		// Return change to resource path
		return new AbstractChange<WoofResourceModel>(resource, changeDescription) {
			@Override
			public void apply() {
				resource.setResourcePath(resourcePath);
				resource.setWoofResourceName(newName);
				WoofChangesImpl.this.sortResources();
			}

			@Override
			public void revert() {
				resource.setResourcePath(originalPath);
				resource.setWoofResourceName(originalName);
				WoofChangesImpl.this.sortResources();
			}
		};
	};

	@Override
	public Change<WoofResourceModel> removeResource(final WoofResourceModel resource) {

		// Ensure resource available to remove
		boolean isInModel = false;
		for (WoofResourceModel model : this.model.getWoofResources()) {
			if (model == resource) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Resource model not in model
			return new NoChange<WoofResourceModel>(resource, "Remove resource " + resource.getWoofResourceName(),
					"Resource " + resource.getWoofResourceName() + " is not in WoOF model");
		}

		// Return change to remove resource
		return new AbstractChange<WoofResourceModel>(resource, "Remove resource " + resource.getWoofResourceName()) {

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
				removeConnections(resource.getWoofAccessOutputs(), list);
				removeConnections(resource.getWoofExceptions(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

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

		// Determine if the exception has already been added
		WoofExceptionModel existingModel = null;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {
			if (model.getClassName().equals(exceptionClassName)) {
				existingModel = model;
			}
		}
		if (existingModel != null) {
			// Provide change to only move model back on revert
			final WoofExceptionModel model = existingModel;
			final int x = existingModel.getX();
			final int y = existingModel.getY();
			return new AbstractChange<WoofExceptionModel>(model, "Add Exception") {
				@Override
				public void apply() {
					// No change as will be positioned
				}

				@Override
				public void revert() {
					// Move back to old position
					model.setX(x);
					model.setY(y);
				}
			};
		}

		// Create the exception
		final WoofExceptionModel exception = new WoofExceptionModel(exceptionClassName);

		// Return change to add exception
		return new AbstractChange<WoofExceptionModel>(exception, "Add Exception") {
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
	public Change<WoofExceptionModel> refactorException(final WoofExceptionModel exception,
			final String exceptionClassName) {

		// Determine if the exception has already been handled
		boolean isAlreadyHandled = false;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {

			// Ignore the exception being refactored
			if (model == exception) {
				continue; // ignore
			}

			// Determine if handling exception
			if (model.getClassName().equals(exceptionClassName)) {
				isAlreadyHandled = true;
			}
		}
		if (isAlreadyHandled) {
			// Exception already handled
			return new NoChange<WoofExceptionModel>(exception, "Refactor Exception",
					"Exception " + exceptionClassName + " is already handled");
		}

		// Obtain the existing exception class name (for revert)
		final String existingExceptionClassName = exception.getClassName();

		// Return change to refactor exception
		return new AbstractChange<WoofExceptionModel>(exception, "Refactor Exception") {
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
	public Change<WoofExceptionModel> removeException(final WoofExceptionModel exception) {

		// Ensure exception available to remove
		boolean isInModel = false;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {
			if (model == exception) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Exception model not in model
			return new NoChange<WoofExceptionModel>(exception, "Remove exception " + exception.getClassName(),
					"Exception " + exception.getClassName() + " is not in WoOF model");
		}

		// Return change to remove exception
		return new AbstractChange<WoofExceptionModel>(exception, "Remove exception " + exception.getClassName()) {

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
				this.connections = list.toArray(new ConnectionModel[list.size()]);

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
	public Change<WoofStartModel> addStart() {

		// Create the start
		final WoofStartModel start = new WoofStartModel();

		// Return change to add start
		return new AbstractChange<WoofStartModel>(start, "Add Start") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofStart(start);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofStart(start);
			}
		};
	}

	@Override
	public Change<WoofStartModel> removeStart(final WoofStartModel start) {

		// Return change to remove start
		return new AbstractChange<WoofStartModel>(start, "Remove start") {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(start.getWoofSectionInput(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the start
				WoofChangesImpl.this.model.removeWoofStart(start);
			}

			@Override
			public void revert() {
				// Add back the start
				WoofChangesImpl.this.model.addWoofStart(start);
				reconnectConnections(this.connections);
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(
			final WoofTemplateOutputModel templateOutput, WoofTemplateModel template) {

		// Create the connection
		final WoofTemplateOutputToWoofTemplateModel connection = new WoofTemplateOutputToWoofTemplateModel(
				template.getWoofTemplateName(), templateOutput, template);

		// Return change to link
		return new AddLinkChange<WoofTemplateOutputToWoofTemplateModel, WoofTemplateOutputModel>(connection,
				templateOutput, "Link Template Output to Template") {
			@Override
			protected void addExistingConnections(WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOuputToTemplate(
			final WoofTemplateOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofTemplateModel>(link, "Remove Template Output to Template");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			final WoofTemplateOutputModel templateOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofTemplateOutputToWoofSectionInputModel>(
					new WoofTemplateOutputToWoofSectionInputModel(),
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Create the connection
		final WoofTemplateOutputToWoofSectionInputModel connection = new WoofTemplateOutputToWoofSectionInputModel(
				section.getWoofSectionName(), sectionInput.getWoofSectionInputName(), templateOutput, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofSectionInputModel, WoofTemplateOutputModel>(connection,
				templateOutput, "Link Template Output to Section Input") {
			@Override
			protected void addExistingConnections(WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOuputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofSectionInputModel>(link,
				"Remove Template Output to Section Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofAccessInputModel> linkTemplateOutputToAccessInput(
			WoofTemplateOutputModel templateOutput, WoofAccessInputModel accessInput) {

		// Create the connection
		final WoofTemplateOutputToWoofAccessInputModel connection = new WoofTemplateOutputToWoofAccessInputModel(
				accessInput.getWoofAccessInputName(), templateOutput, accessInput);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofAccessInputModel, WoofTemplateOutputModel>(connection,
				templateOutput, "Link Template Output to Access Input") {
			@Override
			protected void addExistingConnections(WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofAccessInputModel> removeTemplateOuputToAccessInput(
			WoofTemplateOutputToWoofAccessInputModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofAccessInputModel>(link,
				"Remove Template Output to Access Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(
			WoofTemplateOutputModel templateOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofTemplateOutputToWoofResourceModel connection = new WoofTemplateOutputToWoofResourceModel(
				resource.getWoofResourceName(), templateOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofResourceModel, WoofTemplateOutputModel>(connection,
				templateOutput, "Link Template Output to Resource") {
			@Override
			protected void addExistingConnections(WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOuputToResource(
			WoofTemplateOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofResourceModel>(link, "Remove Template Output to Resource");
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(
			WoofSectionOutputModel sectionOutput, WoofTemplateModel template) {

		// Create the connection
		final WoofSectionOutputToWoofTemplateModel connection = new WoofSectionOutputToWoofTemplateModel(
				template.getWoofTemplateName(), sectionOutput, template);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofTemplateModel, WoofSectionOutputModel>(connection,
				sectionOutput, "Link Section Output to Template") {
			@Override
			protected void addExistingConnections(WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> removeSectionOuputToTemplate(
			WoofSectionOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofTemplateModel>(link, "Remove Section Output to Template");
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofSectionOutputToWoofSectionInputModel>(
					new WoofSectionOutputToWoofSectionInputModel(),
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Create the connection
		final WoofSectionOutputToWoofSectionInputModel connection = new WoofSectionOutputToWoofSectionInputModel(
				section.getWoofSectionName(), sectionInput.getWoofSectionInputName(), sectionOutput, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofSectionInputModel, WoofSectionOutputModel>(connection,
				sectionOutput, "Link Section Output to Section Input") {
			@Override
			protected void addExistingConnections(WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOuputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofSectionInputModel>(link,
				"Remove Section Output to Section Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofAccessInputModel> linkSectionOutputToAccessInput(
			WoofSectionOutputModel sectionOutput, WoofAccessInputModel accessInput) {

		// Create the connection
		final WoofSectionOutputToWoofAccessInputModel connection = new WoofSectionOutputToWoofAccessInputModel(
				accessInput.getWoofAccessInputName(), sectionOutput, accessInput);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofAccessInputModel, WoofSectionOutputModel>(connection,
				sectionOutput, "Link Section Output to Access Input") {
			@Override
			protected void addExistingConnections(WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofAccessInputModel> removeSectionOuputToAccessInput(
			WoofSectionOutputToWoofAccessInputModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofAccessInputModel>(link,
				"Remove Section Output to Access Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(
			WoofSectionOutputModel sectionOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofSectionOutputToWoofResourceModel connection = new WoofSectionOutputToWoofResourceModel(
				resource.getWoofResourceName(), sectionOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofResourceModel, WoofSectionOutputModel>(connection,
				sectionOutput, "Link Section Output to Resource") {
			@Override
			protected void addExistingConnections(WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofAccessInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> removeSectionOuputToResource(
			WoofSectionOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofResourceModel>(link, "Remove Section Output to Resource");
	}

	@Override
	public Change<WoofAccessOutputToWoofTemplateModel> linkAccessOutputToTemplate(WoofAccessOutputModel accessOutput,
			WoofTemplateModel template) {

		// Create the connection
		final WoofAccessOutputToWoofTemplateModel connection = new WoofAccessOutputToWoofTemplateModel(
				template.getWoofTemplateName(), accessOutput, template);

		// Return change to add connection
		return new AddLinkChange<WoofAccessOutputToWoofTemplateModel, WoofAccessOutputModel>(connection, accessOutput,
				"Link Access Output to Template") {
			@Override
			protected void addExistingConnections(WoofAccessOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofAccessOutputToWoofTemplateModel> removeAccessOuputToTemplate(
			WoofAccessOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofAccessOutputToWoofTemplateModel>(link, "Remove Access Output to Template");
	}

	@Override
	public Change<WoofAccessOutputToWoofSectionInputModel> linkAccessOutputToSectionInput(
			WoofAccessOutputModel accessOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofAccessOutputToWoofSectionInputModel>(new WoofAccessOutputToWoofSectionInputModel(),
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Create the connection
		final WoofAccessOutputToWoofSectionInputModel connection = new WoofAccessOutputToWoofSectionInputModel(
				section.getWoofSectionName(), sectionInput.getWoofSectionInputName(), accessOutput, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofAccessOutputToWoofSectionInputModel, WoofAccessOutputModel>(connection,
				accessOutput, "Link Access Output to Section Input") {
			@Override
			protected void addExistingConnections(WoofAccessOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofAccessOutputToWoofSectionInputModel> removeAccessOuputToSectionInput(
			WoofAccessOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofAccessOutputToWoofSectionInputModel>(link,
				"Remove Access Output to Section Input");
	}

	@Override
	public Change<WoofAccessOutputToWoofResourceModel> linkAccessOutputToResource(WoofAccessOutputModel accessOutput,
			WoofResourceModel resource) {

		// Create the connection
		final WoofAccessOutputToWoofResourceModel connection = new WoofAccessOutputToWoofResourceModel(
				resource.getWoofResourceName(), accessOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofAccessOutputToWoofResourceModel, WoofAccessOutputModel>(connection, accessOutput,
				"Link Access Output to Resource") {
			@Override
			protected void addExistingConnections(WoofAccessOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofAccessOutputToWoofResourceModel> removeAccessOuputToResource(
			WoofAccessOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofAccessOutputToWoofResourceModel>(link, "Remove Access Output to Resource");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(WoofExceptionModel exception,
			WoofTemplateModel template) {

		// Create the connection
		final WoofExceptionToWoofTemplateModel connection = new WoofExceptionToWoofTemplateModel(
				template.getWoofTemplateName(), exception, template);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofTemplateModel, WoofExceptionModel>(connection, exception,
				"Link Exception to Template") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(WoofExceptionToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofTemplateModel>(link, "Remove Exception to Template");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(WoofExceptionModel exception,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofExceptionToWoofSectionInputModel>(new WoofExceptionToWoofSectionInputModel(),
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Create the connection
		final WoofExceptionToWoofSectionInputModel connection = new WoofExceptionToWoofSectionInputModel(
				section.getWoofSectionName(), sectionInput.getWoofSectionInputName(), exception, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofSectionInputModel, WoofExceptionModel>(connection, exception,
				"Link Exception to Section Input") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofSectionInputModel>(link, "Remove Exception to Section Input");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(WoofExceptionModel exception,
			WoofResourceModel resource) {

		// Create the connection
		final WoofExceptionToWoofResourceModel connection = new WoofExceptionToWoofResourceModel(
				resource.getWoofResourceName(), exception, resource);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofResourceModel, WoofExceptionModel>(connection, exception,
				"Link Exception to Resource") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(WoofExceptionToWoofResourceModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofResourceModel>(link, "Remove Exception to Resource");
	}

	@Override
	public Change<WoofStartToWoofSectionInputModel> linkStartToSectionInput(WoofStartModel start,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofStartToWoofSectionInputModel>(new WoofStartToWoofSectionInputModel(),
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Create the connection
		final WoofStartToWoofSectionInputModel connection = new WoofStartToWoofSectionInputModel(
				section.getWoofSectionName(), sectionInput.getWoofSectionInputName(), start, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofStartToWoofSectionInputModel, WoofStartModel>(connection, start,
				"Link Start to Section Input") {
			@Override
			protected void addExistingConnections(WoofStartModel source, List<ConnectionModel> list) {
				list.add(source.getWoofSectionInput());
			}
		};
	}

	@Override
	public Change<WoofStartToWoofSectionInputModel> removeStartToSectionInput(WoofStartToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofStartToWoofSectionInputModel>(link, "Remove Start to Section Input");
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
	}

}