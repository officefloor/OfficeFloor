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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.AggregateChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.type.WebTemplateLoader;
import net.officefloor.web.template.type.WebTemplateOutputType;
import net.officefloor.web.template.type.WebTemplateType;
import net.officefloor.woof.template.WoofTemplateExtensionLoader;
import net.officefloor.woof.template.WoofTemplateExtensionLoaderImpl;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

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
	 * Sorts the {@link WoofTemplateOutputModel} and
	 * {@link WoofTemplateExtensionModel} instances of the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param template {@link WoofTemplateModel}.
	 */
	private static void sortTemplateConfiguration(WoofTemplateModel template) {

		// Sort outputs keeping template render complete output last
		sortModelList(template.getOutputs(), (model) -> model.getWoofTemplateOutputName());

		// Sort the links
		sortModelList(template.getLinks(), (model) -> model.getWoofTemplateLinkName());

		// Sort the render HTTP methods
		sortModelList(template.getRenderHttpMethods(), (model) -> model.getWoofTemplateRenderHttpMethodName());
	}

	/**
	 * Sorts the {@link WoofProcedureOutputModel} instances of the
	 * {@link WoofProcedureModel}.
	 * 
	 * @param procedure {@link WoofProcedureModel}.
	 */
	private static void sortProcedureOutputs(WoofProcedureModel procedure) {
		sortModelList(procedure.getOutputs(), (model) -> model.getWoofProcedureOutputName());
	}

	/**
	 * Sorts the {@link WoofSectionInputModel} and {@link WoofSectionOutputModel}
	 * instances of the {@link WoofSectionModel}.
	 * 
	 * @param section {@link WoofSectionModel}.
	 */
	private static void sortSectionInputOutputs(WoofSectionModel section) {
		sortModelList(section.getInputs(), (model) -> model.getWoofSectionInputName());
		sortModelList(section.getOutputs(), (model) -> model.getWoofSectionOutputName());
	}

	/**
	 * Sorts the {@link WoofSecurityModel} and {@link WoofSecurityOutputModel}
	 * instances of the {@link WoofSecurityModel}.
	 * 
	 * @param security {@link WoofSecurityModel}.
	 */
	private static void sortSecurityOutputs(WoofSecurityModel security) {
		sortModelList(security.getOutputs(), (output) -> output.getWoofSecurityOutputName());
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
	 * Refactors the {@link WoofTemplateExtensionModel} instances.
	 * 
	 * @param existingTemplate     Existing {@link WoofTemplateModel}. May be
	 *                             <code>null</code> if adding.
	 * @param newUri               New URI. May be <code>null</code> if removing
	 *                             {@link WoofTemplateModel}.
	 * @param extensions           {@link WoofTemplateExtension} instances to
	 *                             refactor the {@link WoofTemplateModel} to have.
	 *                             May be <code>null</code> to indicate no changes
	 *                             to {@link WoofTemplateExtensionModel} instances
	 *                             for the {@link WoofTemplateModel}.
	 * @param changeTemplate       {@link WoofTemplateModel} to be changed.
	 * @param sourceContext        {@link SourceContext}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
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

		// Obtain the old application path
		String oldApplicationPath = (existingTemplate == null ? null : existingTemplate.getApplicationPath());

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
				Change<?> extensionChange = refactorExtension(extensionModel, oldApplicationPath, newUri,
						availableExtensions, changeTemplate, sourceContext, configurationContext, issues);

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
				Change<?> extensionChange = refactorExtension(extensionModel, oldApplicationPath, newUri,
						availableExtensions, changeTemplate, sourceContext, configurationContext, issues);

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
			Change<?> extensionChange = refactorExtension(extensionModel, oldApplicationPath, null, availableExtensions,
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
	 * @param extension            {@link WoofTemplateExtensionModel} to refactor.
	 * @param oldUri               Old URI. May be <code>null</code> if adding
	 *                             {@link WoofTemplateExtensionModel}.
	 * @param newUri               New URI. May be <code>null</code> if removing
	 *                             {@link WoofTemplateExtensionModel}.
	 * @param existingExtensions   Existing {@link WoofTemplateExtensionModel}
	 *                             instances. As {@link WoofTemplateExtension}
	 *                             instances are refactored they are removed from
	 *                             this list.
	 * @param changeTemplate       {@link WoofTemplateModel} being changed.
	 * @param sourceContext        {@link SourceContext}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
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
	 * @param model {@link WoofModel} to change.
	 */
	public WoofChangesImpl(WoofModel model) {
		this.model = model;
	}

	/**
	 * Sorts the {@link WoofTemplateModel} instances.
	 */
	private void sortTemplates() {
		sortModelList(this.model.getWoofTemplates(), (model) -> model.getApplicationPath());
	}

	/**
	 * Sorts the {@link WoofSecurityModel} instances.
	 */
	private void sortSecurities() {
		sortModelList(this.model.getWoofSecurities(), (model) -> model.getHttpSecurityName());
	}

	/**
	 * Sorts the {@link WoofProcedureModel} instances.
	 */
	private void sortProcedures() {
		sortModelList(this.model.getWoofProcedures(), (model) -> model.getWoofProcedureName());
	}

	/**
	 * Sorts the {@link WoofSectionModel} instances.
	 */
	private void sortSections() {
		sortModelList(this.model.getWoofSections(), (model) -> model.getWoofSectionName());
	}

	/**
	 * Sorts the {@link WoofGovernanceModel} instances.
	 */
	private void sortGovernances() {
		sortModelList(this.model.getWoofGovernances(), (model) -> model.getWoofGovernanceName());
	}

	/**
	 * Sorts the {@link WoofGovernanceAreaModel} for the
	 * {@link WoofGovernanceModel}.
	 * 
	 * @param governance {@link WoofGovernanceModel} to have its
	 *                   {@link WoofGovernanceAreaModel} instances sorted.
	 */
	private void sortGovernanceAreas(WoofGovernanceModel governance) {
		sortModelList(governance.getGovernanceAreas(), (model) -> model.getWidth() + "-" + model.getHeight());
	}

	/**
	 * Sorts the {@link WoofResourceModel} instances.
	 */
	private void sortResources() {
		sortModelList(this.model.getWoofResources(), (model) -> model.getResourcePath());
	}

	/**
	 * Sorts the {@link WoofExceptionModel} instances.
	 */
	private void sortExceptions() {
		sortModelList(this.model.getWoofExceptions(), (model) -> model.getClassName());
	}

	/**
	 * Sorts the {@link WoofHttpContinuationModel} instances.
	 */
	private void sortHttpContinuations() {
		sortModelList(this.model.getWoofHttpContinuations(), (model) -> model.getApplicationPath());
	}

	/**
	 * Sorts the {@link WoofHttpInputModel} instances.
	 */
	private void sortHttpInputs() {
		sortModelList(this.model.getWoofHttpInputs(),
				(model) -> model.getHttpMethod() + ":" + model.getApplicationPath());
	}

	/**
	 * Checks for unique GET application path.
	 * 
	 * @param applicationPath   Application path to check if unique.
	 * @param modelWithPath     {@link Model} with the existing application path
	 *                          (not included in check).
	 * @param changeDescription Change description.
	 * @return {@link Change} for the {@link Conflict}, or <code>null</code> no
	 *         {@link Conflict}.
	 */
	private <T extends Model> Change<T> checkUniqueGetApplicationPath(String applicationPath, T modelWithPath,
			String changeDescription) {

		// Check HTTP Continutations
		Change<T> change = this.checkUniqueApplicationPath(applicationPath, modelWithPath, changeDescription,
				this.model.getWoofHttpContinuations().stream(), "HTTP Continuation",
				(httpContinuation) -> httpContinuation.getApplicationPath());
		if (change != null) {
			return change;
		}

		// Check HTTP Inputs (for GET)
		change = this.checkUniqueApplicationPath(applicationPath, modelWithPath, changeDescription,
				this.model.getWoofHttpInputs().stream().filter((model) -> "GET".equals(model.getHttpMethod())),
				"HTTP Input", (httpInput) -> httpInput.getApplicationPath());
		if (change != null) {
			return change;
		}

		// Check Templates
		change = this.checkUniqueApplicationPath(applicationPath, modelWithPath, changeDescription,
				this.model.getWoofTemplates().stream(), "Template", (template) -> template.getApplicationPath());
		if (change != null) {
			return change;
		}

		// No conflict in application path
		return null;
	}

	/**
	 * Checks for unique application path for specified {@link HttpMethod}.
	 * 
	 * @param httpMethodName    Name of the {@link HttpMethod}.
	 * @param applicationPath   Application path to check if unique. {@link Model}
	 *                          with the existing application path (not included in
	 *                          check).
	 * @param changeDescription Change description.
	 * @return {@link Change} for the {@link Conflict}, or <code>null</code> no
	 *         {@link Conflict}.
	 */
	private <T extends Model> Change<T> checkUniqueMethodApplicationPath(String httpMethodName, String applicationPath,
			T modelWithPath, String changeDescription) {

		// Handle GET HTTP method
		if ("GET".equals(httpMethodName)) {
			return this.checkUniqueGetApplicationPath(applicationPath, modelWithPath, changeDescription);
		}

		// Determine if unique for particular HTTP method
		return this.checkUniqueApplicationPath(applicationPath, modelWithPath, changeDescription,
				this.model.getWoofHttpInputs().stream().filter((model) -> httpMethodName.equals(model.getHttpMethod())),
				"HTTP Input", (httpInput) -> httpInput.getApplicationPath());
	}

	/**
	 * Determines if application path is unique within the {@link List}.
	 * 
	 * @param applicationPath    Application path.
	 * @param modelWithPath      {@link Model} with the existing application path
	 *                           (not included in check).
	 * @param changeDescription  Change description.
	 * @param modelList          {@link Model} {@link List} to check.
	 * @param modelItemTypeName  Type name of the {@link Model}.
	 * @param getApplicationPath {@link Function} to obtain the application path
	 *                           from the {@link Model}.
	 * @return {@link Change} for the {@link Conflict}, or <code>null</code> no
	 *         {@link Conflict}.
	 */
	private <T extends Model, M extends Model> Change<T> checkUniqueApplicationPath(String applicationPath,
			T modelWithPath, String changeDescription, Stream<M> modelList, String modelItemTypeName,
			Function<M, String> getApplicationPath) {

		// Must have application path
		if (CompileUtil.isBlank(applicationPath)) {
			return new NoChange<T>(modelWithPath, changeDescription, "Must provide an application path");
		}

		// Ensure no conflict in application path
		boolean isNoConflict = modelList.allMatch((model) -> {

			// Ignore if model with the application path
			if (modelWithPath == model) {
				return true;
			}

			// Obtain the application path
			String modelApplicationPath = getApplicationPath.apply(model);
			return (!applicationPath.equals(modelApplicationPath));
		});

		// Return possible change conflict
		return isNoConflict ? null
				: new NoChange<T>(modelWithPath, changeDescription,
						"Application path '" + applicationPath + "' already configured for " + modelItemTypeName);
	}

	/**
	 * Obtains the {@link WoofSectionModel} for the {@link WoofSectionInputModel}.
	 * 
	 * @param input {@link WoofSectionInputModel}.
	 * @return {@link WoofSectionModel} containing the {@link WoofSectionInputModel}
	 *         or <code>null</code> if not within {@link WoofModel}.
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
	 * @param area {@link WoofGovernanceAreaModel}.
	 * @return {@link WoofGovernanceModel} for the {@link WoofGovernanceAreaModel}
	 *         or <code>null</code> if not within the {@link WoofModel}.
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
	public Change<WoofHttpContinuationModel> addHttpContinuation(String applicationPath, boolean isSecure) {

		// Create the HTTP continuation
		final WoofHttpContinuationModel path = new WoofHttpContinuationModel(isSecure, applicationPath);

		// Determine if not unique application path
		Change<WoofHttpContinuationModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, path,
				"Add HTTP Continuation");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Return change to add HTTP continuation
		return new AbstractChange<WoofHttpContinuationModel>(path, "Add HTTP Continuation") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofHttpContinuation(path);
				WoofChangesImpl.this.sortHttpContinuations();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofHttpContinuation(path);
			}
		};
	}

	@Override
	public Change<WoofHttpContinuationModel> addDocumentation(WoofHttpContinuationModel continuation,
			String description) {

		// Obtain the existing documentation
		DocumentationModel existingDocumentation = continuation.getDocumentation();

		// Return change to add documentation
		return new AbstractChange<WoofHttpContinuationModel>(continuation,
				((existingDocumentation == null ? "Add" : "Change") + " HTTP Continuation Documentation")) {

			@Override
			public void apply() {
				DocumentationModel documentation = null;
				if (!CompileUtil.isBlank(description)) {
					documentation = new DocumentationModel(description);
				}
				continuation.setDocumentation(documentation);
			}

			@Override
			public void revert() {
				continuation.setDocumentation(existingDocumentation);
			}
		};
	}

	@Override
	public Change<WoofHttpContinuationModel> refactorHttpContinuation(WoofHttpContinuationModel continuation,
			String applicationPath, boolean isSecure) {

		// Determine if not unique application path
		Change<WoofHttpContinuationModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, continuation,
				"Refactor HTTP Continuation");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Capture the existing values
		final String existingApplicationPath = continuation.getApplicationPath();
		final boolean existingIsSecure = continuation.getIsSecure();

		// Refactor
		return new AbstractChange<WoofHttpContinuationModel>(continuation, "Refactor HTTP Continuation") {
			@Override
			public void apply() {
				continuation.setApplicationPath(applicationPath);
				continuation.setIsSecure(isSecure);
				WoofChangesImpl.this.sortHttpContinuations();
				WoofChangesImpl.renameConnections(continuation);
			}

			@Override
			public void revert() {
				continuation.setApplicationPath(existingApplicationPath);
				continuation.setIsSecure(existingIsSecure);
				WoofChangesImpl.this.sortHttpContinuations();
				WoofChangesImpl.renameConnections(continuation);
			}
		};
	}

	@Override
	public Change<WoofHttpContinuationModel> changeApplicationPath(WoofHttpContinuationModel continuation,
			String applicationPath) {

		// Determine if not unique application path
		Change<WoofHttpContinuationModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, continuation,
				"Change Application Path");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Capture existing application path
		final String existingApplicationPath = continuation.getApplicationPath();

		// Change the application path
		return new AbstractChange<WoofHttpContinuationModel>(continuation, "Change Application Path") {
			@Override
			public void apply() {
				continuation.setApplicationPath(applicationPath);
				WoofChangesImpl.this.sortHttpContinuations();
				WoofChangesImpl.renameConnections(continuation);
			}

			@Override
			public void revert() {
				continuation.setApplicationPath(existingApplicationPath);
				WoofChangesImpl.this.sortHttpContinuations();
				WoofChangesImpl.renameConnections(continuation);
			}
		};
	}

	/**
	 * Renames the {@link ConnectionModel} instances referencing the
	 * {@link WoofHttpContinuationModel}.
	 */
	private static void renameConnections(WoofHttpContinuationModel continuation) {
		String applicationPath = continuation.getApplicationPath();

		for (WoofSectionOutputToWoofHttpContinuationModel conn : continuation.getWoofSectionOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofTemplateOutputToWoofHttpContinuationModel conn : continuation.getWoofTemplateOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofSecurityOutputToWoofHttpContinuationModel conn : continuation.getWoofSecurityOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofExceptionToWoofHttpContinuationModel conn : continuation.getWoofExceptions()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofHttpInputToWoofHttpContinuationModel conn : continuation.getWoofHttpInputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofHttpContinuationToWoofHttpContinuationModel conn : continuation.getWoofHttpContinuations()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofProcedureNextToWoofHttpContinuationModel conn : continuation.getWoofProcedureNexts()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofProcedureOutputToWoofHttpContinuationModel conn : continuation.getWoofProcedureOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
	}

	@Override
	public Change<WoofHttpContinuationModel> removeHttpContinuation(WoofHttpContinuationModel httpContinuation) {

		// Ensure HTTP continuation available to remove
		boolean isInModel = false;
		for (WoofHttpContinuationModel model : this.model.getWoofHttpContinuations()) {
			if (model == httpContinuation) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Application path model not in model
			return new NoChange<WoofHttpContinuationModel>(httpContinuation,
					"Remove HTTP continuation " + httpContinuation.getApplicationPath(), " is not in WoOF model");
		}

		// Return change to remove application path
		return new AbstractChange<WoofHttpContinuationModel>(httpContinuation,
				"Remove HTTP continuation " + httpContinuation.getApplicationPath()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(httpContinuation.getWoofSectionOutputs(), list);
				removeConnections(httpContinuation.getWoofTemplateOutputs(), list);
				removeConnections(httpContinuation.getWoofSecurityOutputs(), list);
				removeConnections(httpContinuation.getWoofExceptions(), list);
				removeConnections(httpContinuation.getWoofHttpContinuations(), list);
				removeConnections(httpContinuation.getWoofHttpInputs(), list);
				removeConnections(httpContinuation.getWoofProcedureNexts(), list);
				removeConnections(httpContinuation.getWoofProcedureOutputs(), list);
				removeConnection(httpContinuation.getWoofSectionInput(), list);
				removeConnection(httpContinuation.getWoofTemplate(), list);
				removeConnection(httpContinuation.getWoofSecurity(), list);
				removeConnection(httpContinuation.getWoofResource(), list);
				removeConnection(httpContinuation.getWoofRedirect(), list);
				removeConnection(httpContinuation.getWoofProcedure(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the HTTP continuation
				WoofChangesImpl.this.model.removeWoofHttpContinuation(httpContinuation);
			}

			@Override
			public void revert() {
				// Add back the HTTP continuation
				WoofChangesImpl.this.model.addWoofHttpContinuation(httpContinuation);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortHttpContinuations();
			}
		};
	}

	@Override
	public Change<WoofHttpInputModel> addHttpInput(String applicationPath, String httpMethodName, boolean isSecure) {

		// Create the HTTP input
		final WoofHttpInputModel path = new WoofHttpInputModel(isSecure, httpMethodName, applicationPath);

		// Determine if not unique application path
		Change<WoofHttpInputModel> nonUnique = this.checkUniqueMethodApplicationPath(httpMethodName, applicationPath,
				path, "Add HTTP Input");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Return change to add HTTP input
		return new AbstractChange<WoofHttpInputModel>(path, "Add HTTP Input") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofHttpInput(path);
				WoofChangesImpl.this.sortHttpInputs();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofHttpInput(path);
			}
		};
	}

	@Override
	public Change<WoofHttpInputModel> addDocumentation(WoofHttpInputModel input, String description) {

		// Obtain the existing documentation
		DocumentationModel existingDocumentation = input.getDocumentation();

		// Return change to add documentation
		return new AbstractChange<WoofHttpInputModel>(input,
				((existingDocumentation == null ? "Add" : "Change") + " HTTP Input Documentation")) {

			@Override
			public void apply() {
				DocumentationModel documentation = null;
				if (!CompileUtil.isBlank(description)) {
					documentation = new DocumentationModel(description);
				}
				input.setDocumentation(documentation);
			}

			@Override
			public void revert() {
				input.setDocumentation(existingDocumentation);
			}
		};
	}

	@Override
	public Change<WoofHttpInputModel> refactorHttpInput(final WoofHttpInputModel input, String applicationPath,
			String httpMethod, boolean isSecure) {

		// Determine if not unique application path
		Change<WoofHttpInputModel> nonUnique = this.checkUniqueMethodApplicationPath(httpMethod, applicationPath, input,
				"Refactor HTTP Input");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Capture existing details
		final String existingApplicationPath = input.getApplicationPath();
		final String existingHttpMethod = input.getHttpMethod();
		final boolean existingIsSecure = input.getIsSecure();

		// Return change to refactor
		return new AbstractChange<WoofHttpInputModel>(input, "Refactor HTTP Input") {
			@Override
			public void apply() {
				input.setApplicationPath(applicationPath);
				input.setHttpMethod(httpMethod);
				input.setIsSecure(isSecure);
				WoofChangesImpl.this.sortHttpInputs();
			}

			@Override
			public void revert() {
				input.setApplicationPath(existingApplicationPath);
				input.setHttpMethod(existingHttpMethod);
				input.setIsSecure(existingIsSecure);
				WoofChangesImpl.this.sortHttpInputs();
			}
		};
	}

	@Override
	public Change<WoofHttpInputModel> changeApplicationPath(WoofHttpInputModel input, String applicationPath) {

		// Determine if not unique application path
		Change<WoofHttpInputModel> nonUnique = this.checkUniqueMethodApplicationPath(input.getHttpMethod(),
				applicationPath, input, "Change Application Path");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Capture existing application path
		final String existingApplicationPath = input.getApplicationPath();

		// Return change to refactor
		return new AbstractChange<WoofHttpInputModel>(input, "Change Application Path") {
			@Override
			public void apply() {
				input.setApplicationPath(applicationPath);
				WoofChangesImpl.this.sortHttpInputs();
			}

			@Override
			public void revert() {
				input.setApplicationPath(existingApplicationPath);
				WoofChangesImpl.this.sortHttpInputs();
			}
		};
	}

	@Override
	public Change<WoofHttpInputModel> removeHttpInput(WoofHttpInputModel httpInput) {

		// Ensure HTTP input available to remove
		boolean isInModel = false;
		for (WoofHttpInputModel model : this.model.getWoofHttpInputs()) {
			if (model == httpInput) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Application path model not in model
			return new NoChange<WoofHttpInputModel>(httpInput, "Remove HTTP input " + httpInput.getApplicationPath(),
					" is not in WoOF model");
		}

		// Return change to remove application path
		return new AbstractChange<WoofHttpInputModel>(httpInput,
				"Remove HTTP input " + httpInput.getHttpMethod() + " " + httpInput.getApplicationPath()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(httpInput.getWoofSectionInput(), list);
				removeConnection(httpInput.getWoofTemplate(), list);
				removeConnection(httpInput.getWoofSecurity(), list);
				removeConnection(httpInput.getWoofResource(), list);
				removeConnection(httpInput.getWoofHttpContinuation(), list);
				removeConnection(httpInput.getWoofProcedure(), list);
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the HTTP input
				WoofChangesImpl.this.model.removeWoofHttpInput(httpInput);
			}

			@Override
			public void revert() {
				// Add back the HTTP input
				WoofChangesImpl.this.model.addWoofHttpInput(httpInput);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortHttpInputs();
			}
		};
	}

	@Override
	public Change<WoofTemplateModel> addTemplate(String applicationPath, String templateLocation,
			String templateLogicClass, WebTemplateType templateType, String redirectValuesFunction, String contentType,
			String charsetName, boolean isTemplateSecure, String linkSeparatorCharacter,
			Map<String, Boolean> linksSecure, String[] renderHttpMethods, WoofTemplateExtension[] extensions,
			WoofTemplateChangeContext context) {

		// Create the template
		final WoofTemplateModel template = new WoofTemplateModel(applicationPath, templateLocation, templateLogicClass,
				redirectValuesFunction, contentType, charsetName, linkSeparatorCharacter, isTemplateSecure);

		// Determine if not unique application path
		Change<WoofTemplateModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, template,
				"Add Template");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Determine if have links
		if (linksSecure != null) {
			// Add the links
			for (String linkName : linksSecure.keySet()) {
				Boolean isLinkSecure = linksSecure.get(linkName);
				template.addLink(new WoofTemplateLinkModel(linkName, isLinkSecure.booleanValue()));
			}
		}

		// Determine if have render HTTP methods
		if (renderHttpMethods != null) {
			// Add the render HTTP methods
			for (String renderHttpMethodName : renderHttpMethods) {
				template.addRenderHttpMethod(new WoofTemplateRenderHttpMethodModel(renderHttpMethodName));
			}
		}

		// Add the outputs for the template
		for (WebTemplateOutputType output : templateType.getWebTemplateOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Obtain the output details
			String outputName = output.getWebTemplateOutputName();
			String argumentType = output.getArgumentType();

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
		Change<WoofTemplateModel> extensionChange = refactorExtensions(null, applicationPath, extensions, template,
				context, context.getConfigurationContext(), issues);
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
	public Set<String> getInheritableOutputNames(WoofTemplateModel childTemplate) {
		Set<String> outputNames = new HashSet<>();

		// Load the super templates
		WoofTemplateToSuperWoofTemplateModel superLink = childTemplate.getSuperWoofTemplate();
		while (superLink != null) {
			WoofTemplateModel superTemplate = superLink.getSuperWoofTemplate();
			if (superTemplate != null) {

				// Load the template outputs
				for (WoofTemplateOutputModel output : superTemplate.getOutputs()) {
					outputNames.add(output.getWoofTemplateOutputName());
				}
			}

			// Setup for next iteration
			superLink = (superTemplate == null) ? null : superTemplate.getSuperWoofTemplate();
		}

		// Return the output names
		return outputNames;
	}

	@Override
	public void loadSuperTemplates(WebTemplate template, WoofTemplateModel woofTemplate,
			WebTemplateLoader templateLoader) {

		// Function to obtain the super template model
		Function<WoofTemplateModel, WoofTemplateModel> getSuperTemplate = (child) -> {
			WoofTemplateModel superTemplate = null;
			WoofTemplateToSuperWoofTemplateModel conn = child.getSuperWoofTemplate();
			if (conn != null) {
				superTemplate = conn.getSuperWoofTemplate();
			}
			return superTemplate;
		};

		// Load the woof super templates
		WoofTemplateModel superTemplateModel = getSuperTemplate.apply(woofTemplate);
		while (superTemplateModel != null) {

			// Create template for super template
			WebTemplate superTemplate = templateLoader.addTemplate(superTemplateModel.getIsTemplateSecure(),
					superTemplateModel.getApplicationPath(), superTemplateModel.getTemplateLocation());

			// Load as the super template
			template.setSuperTemplate(superTemplate);

			// Make super template current for next super template
			template = superTemplate;

			// Obtain the further super template
			superTemplateModel = getSuperTemplate.apply(superTemplateModel);
		}
	}

	@Override
	public Change<WoofTemplateToSuperWoofTemplateModel> linkTemplateToSuperTemplate(WoofTemplateModel childTemplate,
			WoofTemplateModel superTemplate) {

		// Create the connection
		WoofTemplateToSuperWoofTemplateModel conn = new WoofTemplateToSuperWoofTemplateModel(
				superTemplate.getApplicationPath(), childTemplate, superTemplate);

		// Capture the existing connection
		final WoofTemplateToSuperWoofTemplateModel existingConn = childTemplate.getSuperWoofTemplate();

		// Return change to link super template
		return new AbstractChange<WoofTemplateToSuperWoofTemplateModel>(conn, "Link Template to Super Template") {

			/**
			 * Inherited {@link WoofTemplateOutputModel} instances.
			 */
			private List<WoofTemplateOutputModel> inheritedOutputs;

			@Override
			public void apply() {

				// Connect template to super template
				if (existingConn != null) {
					existingConn.remove();
				}
				conn.connect();

				// Obtain the inheritable output names for new super template
				final Set<String> inheritableOutputNames = WoofChangesImpl.this
						.getInheritableOutputNames(childTemplate);

				// Load the list of inheritable outputs
				this.inheritedOutputs = new LinkedList<>();
				for (WoofTemplateOutputModel output : childTemplate.getOutputs()) {
					if (inheritableOutputNames.contains(output.getWoofTemplateOutputName())) {

						// Potential inherited link (if not already connected)
						boolean isNotLinked = (output.getWoofHttpContinuation() == null)
								&& (output.getWoofResource() == null) && (output.getWoofSectionInput() == null)
								&& (output.getWoofSecurity() == null) && (output.getWoofTemplate() == null);
						if (isNotLinked) {
							this.inheritedOutputs.add(output);
						}
					}
				}

				// Clear the inherited outputs
				for (WoofTemplateOutputModel output : this.inheritedOutputs) {
					childTemplate.removeOutput(output);
				}
			}

			@Override
			public void revert() {

				// Add the template outputs back in
				for (WoofTemplateOutputModel output : this.inheritedOutputs) {
					childTemplate.addOutput(output);
				}
				WoofChangesImpl.sortTemplateConfiguration(childTemplate);

				// Connect previous possible super template
				conn.remove();
				if (existingConn != null) {
					existingConn.connect();
				}
			}
		};
	}

	@Override
	public Change<WoofTemplateToSuperWoofTemplateModel> removeTemplateToSuperTemplate(
			WoofTemplateToSuperWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Template to Super Template");
	}

	@Override
	public Change<WoofTemplateModel> refactorTemplate(WoofTemplateModel template, String applicationPath,
			String templateLocation, String templateLogicClass, WebTemplateType templateType,
			String redirectValuesFunction, Set<String> inheritedTemplateOutputNames, String contentType,
			String charsetName, boolean isTemplateSecure, String linkSeparatorCharacter,
			Map<String, Boolean> linksSecure, String[] renderHttpMethods, WoofTemplateExtension[] extensions,
			Map<String, String> templateOutputNameMapping, WoofTemplateChangeContext context) {

		// Determine if not unique application path
		Change<WoofTemplateModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, template,
				"Refactor Template");
		if (nonUnique != null) {
			return nonUnique;
		}

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
		final String existingApplicationPath = template.getApplicationPath();
		final boolean existingIsTemplateSecure = template.getIsTemplateSecure();
		final String existingTemplateLocation = template.getTemplateLocation();
		final String existingTemplateClassName = template.getTemplateClassName();
		final String existingContentType = template.getTemplateContentType();
		final String existingCharsetName = template.getTemplateCharset();
		final String existingRedirectValuesFunction = template.getRedirectValuesFunction();
		final String existingLinkSeparatorCharacter = template.getLinkSeparatorCharacter();
		final List<WoofTemplateLinkModel> existingTemplateLinks = new ArrayList<WoofTemplateLinkModel>(
				template.getLinks());
		final List<WoofTemplateRenderHttpMethodModel> existingTemplateRenderHttpMethods = new ArrayList<>(
				template.getRenderHttpMethods());

		// Create change to attributes
		Change<WoofTemplateModel> attributeChange = new AbstractChange<WoofTemplateModel>(template,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				template.setApplicationPath(applicationPath);
				template.setIsTemplateSecure(isTemplateSecure);
				template.setTemplateLocation(templateLocation);
				template.setTemplateClassName(templateLogicClass);
				template.setTemplateContentType(contentType);
				template.setTemplateCharset(charsetName);
				template.setRedirectValuesFunction(redirectValuesFunction);
				template.setLinkSeparatorCharacter(linkSeparatorCharacter);

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

				// Refactor the render HTTP methods
				for (WoofTemplateRenderHttpMethodModel renderHttpMethod : new ArrayList<>(
						template.getRenderHttpMethods())) {
					template.removeRenderHttpMethod(renderHttpMethod);
				}
				if (renderHttpMethods != null) {
					// Add the render HTTP methods
					for (String renderHttpMethod : renderHttpMethods) {
						template.addRenderHttpMethod(new WoofTemplateRenderHttpMethodModel(renderHttpMethod));
					}
				}

				// Ensure reflect application path change
				WoofChangesImpl.this.sortTemplates();
				WoofChangesImpl.renameConnections(template);
			}

			@Override
			public void revert() {
				// Revert attributes
				template.setApplicationPath(existingApplicationPath);
				template.setIsTemplateSecure(existingIsTemplateSecure);
				template.setTemplateLocation(existingTemplateLocation);
				template.setTemplateClassName(existingTemplateClassName);
				template.setTemplateContentType(existingContentType);
				template.setTemplateCharset(existingCharsetName);
				template.setRedirectValuesFunction(existingRedirectValuesFunction);
				template.setLinkSeparatorCharacter(existingLinkSeparatorCharacter);

				// Revert the links
				for (WoofTemplateLinkModel link : new ArrayList<>(template.getLinks())) {
					template.removeLink(link);
				}
				for (WoofTemplateLinkModel link : existingTemplateLinks) {
					template.addLink(link);
				}

				// Revert the render HTTP methods
				for (WoofTemplateRenderHttpMethodModel renderHttpMethod : new ArrayList<>(
						template.getRenderHttpMethods())) {
					template.removeRenderHttpMethod(renderHttpMethod);
				}
				for (WoofTemplateRenderHttpMethodModel renderHttpMethod : existingTemplateRenderHttpMethods) {
					template.addRenderHttpMethod(renderHttpMethod);
				}

				// Ensure reflect application path change
				WoofChangesImpl.this.sortTemplates();
				WoofChangesImpl.renameConnections(template);
			}
		};
		changes.add(attributeChange);

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Refactor extensions (ensuring have extensions)
		extensions = (extensions == null ? new WoofTemplateExtension[0] : extensions);
		Change<WoofTemplateModel> extensionChange = refactorExtensions(template, applicationPath, extensions, template,
				context, context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			changes.add(extensionChange);
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofTemplateOutputModel> existingOutputNameMapping = new HashMap<String, WoofTemplateOutputModel>();
		for (WoofTemplateOutputModel output : template.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofTemplateOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final WebTemplateOutputType outputType : templateType.getWebTemplateOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the output details
			final String outputName = outputType.getWebTemplateOutputName();
			final String argumentType = outputType.getArgumentType();

			// Ignore if inheriting the output configuration
			if ((inheritedTemplateOutputNames != null) && (inheritedTemplateOutputNames.contains(outputName))) {
				continue;
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
					removeConnection(unmappedOutputModel.getWoofSecurity(), list);
					removeConnection(unmappedOutputModel.getWoofHttpContinuation(), list);
					removeConnection(unmappedOutputModel.getWoofProcedure(), list);
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

		// Return the change for refactoring
		return change;
	}

	@Override
	public Change<WoofTemplateModel> changeApplicationPath(final WoofTemplateModel template,
			final String applicationPath, WoofTemplateChangeContext context) {

		// Determine if not unique application path
		Change<WoofTemplateModel> nonUnique = this.checkUniqueGetApplicationPath(applicationPath, template,
				"Change Template Application Path");
		if (nonUnique != null) {
			return nonUnique;
		}

		// Keep track of original values
		final String originalApplicationPath = template.getApplicationPath();

		// Create change to template URI
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(template,
				"Change Template Application Path") {
			@Override
			public void apply() {
				template.setApplicationPath(applicationPath);
				WoofChangesImpl.this.sortTemplates();
				WoofChangesImpl.renameConnections(template);
			}

			@Override
			public void revert() {
				template.setApplicationPath(originalApplicationPath);
				WoofChangesImpl.this.sortTemplates();
				WoofChangesImpl.renameConnections(template);
			}
		};

		// Obtain the WoOF change issues
		WoofChangeIssues issues = context.getWoofChangeIssues();

		// Refactor extensions for changed URI
		Change<WoofTemplateModel> extensionChange = refactorExtensions(template, applicationPath, null, template,
				context, context.getConfigurationContext(), issues);
		if (extensionChange != null) {
			change = new AggregateChange<WoofTemplateModel>(template, change.getChangeDescription(), change,
					extensionChange);
		}

		// Return the change
		return change;
	}

	/**
	 * Renames the {@link ConnectionModel} instances referencing the
	 * {@link WoofTemplateModel}.
	 */
	private static void renameConnections(WoofTemplateModel template) {
		String applicationPath = template.getApplicationPath();

		for (WoofTemplateToSuperWoofTemplateModel conn : template.getChildWoofTemplates()) {
			conn.setSuperWoofTemplateApplicationPath(template.getApplicationPath());
		}
		for (WoofSectionOutputToWoofTemplateModel conn : template.getWoofSectionOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofTemplateOutputToWoofTemplateModel conn : template.getWoofTemplateOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofSecurityOutputToWoofTemplateModel conn : template.getWoofSecurityOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofExceptionToWoofTemplateModel conn : template.getWoofExceptions()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofHttpInputToWoofTemplateModel conn : template.getWoofHttpInputs()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofHttpContinuationToWoofTemplateModel conn : template.getWoofHttpContinuations()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofProcedureNextToWoofTemplateModel conn : template.getWoofProcedureNexts()) {
			conn.setApplicationPath(applicationPath);
		}
		for (WoofProcedureOutputToWoofTemplateModel conn : template.getWoofProcedureOutputs()) {
			conn.setApplicationPath(applicationPath);
		}
	}

	@Override
	public Change<WoofTemplateModel> removeTemplate(final WoofTemplateModel template,
			WoofTemplateChangeContext context) {

		// Obtain the application path
		String applicationPath = template.getApplicationPath();

		// Ensure template available to remove
		boolean isInModel = false;
		for (WoofTemplateModel model : this.model.getWoofTemplates()) {
			if (model == template) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Template model not in model
			return new NoChange<WoofTemplateModel>(template, "Remove template " + applicationPath,
					"Template " + applicationPath + " is not in WoOF model");
		}

		// Create change to remove template
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(template,
				"Remove template " + applicationPath) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(template.getSuperWoofTemplate(), list);
				removeConnections(template.getChildWoofTemplates(), list);
				removeConnections(template.getWoofTemplateOutputs(), list);
				removeConnections(template.getWoofSectionOutputs(), list);
				removeConnections(template.getWoofSecurityOutputs(), list);
				removeConnections(template.getWoofExceptions(), list);
				removeConnections(template.getWoofHttpContinuations(), list);
				removeConnections(template.getWoofHttpInputs(), list);
				removeConnections(template.getWoofProcedureNexts(), list);
				removeConnections(template.getWoofProcedureOutputs(), list);
				for (WoofTemplateOutputModel output : template.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofSecurity(), list);
					removeConnection(output.getWoofResource(), list);
					removeConnection(output.getWoofHttpContinuation(), list);
					removeConnection(output.getWoofProcedure(), list);
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

		// Return the change
		return change;
	}

	@Override
	public Change<WoofSectionModel> addSection(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties, SectionType section) {

		// Obtain the unique section name
		sectionName = getUniqueName(sectionName, null, this.model.getWoofSections(),
				(model) -> model.getWoofSectionName());

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
			woofSection.addInput(new WoofSectionInputModel(inputName, parameterType));
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
		Map<String, WoofSectionInputModel> existingInputNameMapping = new HashMap<>();
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
					 * Renames the {@link WoofSectionInputModel} connection names.
					 * 
					 * @param input       {@link WoofSectionInputModel}.
					 * @param sectionName {@link WoofSectionModel} name.
					 * @param inputName   {@link WoofSectionInputModel} name.
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

						// Rename security connections
						for (WoofSecurityOutputToWoofSectionInputModel conn : input.getWoofSecurityOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the HTTP continuation connections
						for (WoofHttpContinuationToWoofSectionInputModel conn : input.getWoofHttpContinuations()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the HTTP input connections
						for (WoofHttpInputToWoofSectionInputModel conn : input.getWoofHttpInputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the procedure next connections
						for (WoofProcedureNextToWoofSectionInputModel conn : input.getWoofProcedureNexts()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}

						// Rename the procedure output connections
						for (WoofProcedureOutputToWoofSectionInputModel conn : input.getWoofProcedureOutputs()) {
							conn.setSectionName(sectionName);
							conn.setInputName(inputName);
						}
					}
				};

			} else {
				// Create change to add input (with no URI)
				final WoofSectionInputModel newInputModel = new WoofSectionInputModel(inputName, parameterType);
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
					removeConnections(unmappedInputModel.getWoofSecurityOutputs(), list);
					removeConnections(unmappedInputModel.getWoofHttpContinuations(), list);
					removeConnections(unmappedInputModel.getWoofHttpInputs(), list);
					removeConnections(unmappedInputModel.getWoofProcedureNexts(), list);
					removeConnections(unmappedInputModel.getWoofProcedureOutputs(), list);
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

		// Refactor the outputs (either refactoring, adding or removing)
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
					removeConnection(unmappedOutputModel.getWoofSecurity(), list);
					removeConnection(unmappedOutputModel.getWoofHttpContinuation(), list);
					removeConnection(unmappedOutputModel.getWoofProcedure(), list);
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
					removeConnections(input.getWoofSecurityOutputs(), list);
					removeConnections(input.getWoofExceptions(), list);
					removeConnections(input.getWoofHttpContinuations(), list);
					removeConnections(input.getWoofHttpInputs(), list);
					removeConnections(input.getWoofStarts(), list);
					removeConnections(input.getWoofProcedureNexts(), list);
					removeConnections(input.getWoofProcedureOutputs(), list);
				}
				for (WoofSectionOutputModel output : section.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofSecurity(), list);
					removeConnection(output.getWoofResource(), list);
					removeConnection(output.getWoofHttpContinuation(), list);
					removeConnection(output.getWoofProcedure(), list);
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
	public Change<WoofProcedureModel> addProcedure(String procedureName, String resource, String sourceName,
			String procedure, PropertyList properties, ProcedureType procedureType) {

		// Obtain the unique procedure name
		procedureName = getUniqueName(procedureName, null, this.model.getWoofProcedures(),
				(model) -> model.getWoofProcedureName());

		// Create the procedure
		final WoofProcedureModel woofProcedure = new WoofProcedureModel(procedureName, resource, sourceName, procedure);

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
			woofProcedure.addOutput(new WoofProcedureOutputModel(flowName, argumentTypeName));
		}

		// Add next details
		Class<?> nextArgumentType = procedureType.getNextArgumentType();
		String nextArgumentTypeName = nextArgumentType == null ? null : nextArgumentType.getName();
		woofProcedure.setNext(new WoofProcedureNextModel(nextArgumentTypeName));

		// Sort the outputs
		sortProcedureOutputs(woofProcedure);

		// Return the change to add procedure
		return new AbstractChange<WoofProcedureModel>(woofProcedure, "Add Procedure") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofProcedure(woofProcedure);
				WoofChangesImpl.this.sortProcedures();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofProcedure(woofProcedure);
			}
		};
	}

	@Override
	public Change<WoofProcedureModel> refactorProcedure(WoofProcedureModel procedureModel, String procedureName,
			String resource, String sourceName, String procedure, PropertyList properties, ProcedureType procedureType,
			Map<String, String> outputNameMapping) {

		// Ensure procedure available to remove
		boolean isInModel = false;
		for (WoofProcedureModel model : this.model.getWoofProcedures()) {
			if (model == procedureModel) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Procedure model not in model
			return new NoChange<WoofProcedureModel>(procedureModel, "Refactor procedure",
					"Procedure " + procedureModel.getWoofProcedureName() + " is not in WoOF model");
		}

		// Create change to sort outputs
		Change<WoofProcedureModel> sortChange = new AbstractChange<WoofProcedureModel>(procedureModel, "Sort outputs") {
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
		final String existingProcedureName = procedureModel.getWoofProcedureName();
		final String existingResource = procedureModel.getResource();
		final String existingSourceName = procedureModel.getSourceName();
		final String existingProcedure = procedureModel.getProcedureName();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(procedureModel.getProperties());

		// Obtain the next argument type (ensuring always next)
		WoofProcedureNextModel nextModel = procedureModel.getNext();
		if (nextModel == null) {
			nextModel = new WoofProcedureNextModel();
			procedureModel.setNext(nextModel);
		}
		final String existingNextArgumentType = nextModel.getArgumentType();

		// Create change to attributes and properties
		Change<WoofProcedureModel> attributeChange = new AbstractChange<WoofProcedureModel>(procedureModel,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				procedureModel.setWoofProcedureName(procedureName);
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
				procedureModel.setWoofProcedureName(existingProcedureName);
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
		Change<WoofProcedureModel> procedureUseChange = new AbstractChange<WoofProcedureModel>(procedureModel,
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
			 * Renames the {@link WoofProcedureModel} connection names.
			 * 
			 * @param procedureModel {@link WoofProcedureModel}.
			 * @param procedureName  {@link WoofProcedureModel} name.
			 */
			private void renameConnections(WoofProcedureModel procedureModel, String procedureName) {

				// Rename exception connections
				for (WoofExceptionToWoofProcedureModel conn : procedureModel.getWoofExceptions()) {
					conn.setProcedureName(procedureName);
				}

				// Rename section output connections
				for (WoofSectionOutputToWoofProcedureModel conn : procedureModel.getWoofSectionOutputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename start connections
				for (WoofStartToWoofProcedureModel conn : procedureModel.getWoofStarts()) {
					conn.setProcedureName(procedureName);
				}

				// Rename template connections
				for (WoofTemplateOutputToWoofProcedureModel conn : procedureModel.getWoofTemplateOutputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename security connections
				for (WoofSecurityOutputToWoofProcedureModel conn : procedureModel.getWoofSecurityOutputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the HTTP continuation connections
				for (WoofHttpContinuationToWoofProcedureModel conn : procedureModel.getWoofHttpContinuations()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the HTTP input connections
				for (WoofHttpInputToWoofProcedureModel conn : procedureModel.getWoofHttpInputs()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the procedure next connections
				for (WoofProcedureNextToWoofProcedureModel conn : procedureModel.getWoofProcedureNexts()) {
					conn.setProcedureName(procedureName);
				}

				// Rename the procedure output connections
				for (WoofProcedureOutputToWoofProcedureModel conn : procedureModel.getWoofProcedureOutputs()) {
					conn.setProcedureName(procedureName);
				}
			}
		};
		changes.add(procedureUseChange);

		// Obtain the mapping of existing outputs
		Map<String, WoofProcedureOutputModel> existingOutputNameMapping = new HashMap<>();
		for (WoofProcedureOutputModel output : procedureModel.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofProcedureOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final ProcedureFlowType flowType : procedureType.getFlowTypes()) {

			// Obtain the mapped procedure output model
			final String outputName = flowType.getFlowName();
			String mappedOutputName = outputNameMapping.get(outputName);
			final WoofProcedureOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			Class<?> argumentType = flowType.getArgumentType();
			final String argumentTypeName = argumentType == null ? null : argumentType.getName();

			// Determine action to take based on existing output
			Change<WoofProcedureOutputModel> procedureOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getWoofProcedureOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				procedureOutputChange = new AbstractChange<WoofProcedureOutputModel>(existingOutputModel,
						"Refactor Procedure Output") {
					@Override
					public void apply() {
						existingOutputModel.setWoofProcedureOutputName(outputName);
						existingOutputModel.setArgumentType(argumentTypeName);
					}

					@Override
					public void revert() {
						existingOutputModel.setWoofProcedureOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output
				final WoofProcedureOutputModel newOutputModel = new WoofProcedureOutputModel(outputName,
						argumentTypeName);
				procedureOutputChange = new AbstractChange<WoofProcedureOutputModel>(newOutputModel,
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
		for (final WoofProcedureOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<WoofProcedureOutputModel> unmappedOutputChange = new AbstractChange<WoofProcedureOutputModel>(
					unmappedOutputModel, "Remove Procedure Output") {

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
					removeConnection(unmappedOutputModel.getWoofSecurity(), list);
					removeConnection(unmappedOutputModel.getWoofHttpContinuation(), list);
					removeConnection(unmappedOutputModel.getWoofProcedure(), list);
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
		return new AggregateChange<WoofProcedureModel>(procedureModel, "Refactor Procedure",
				changes.toArray(new Change[changes.size()]));
	}

	@Override
	public Change<WoofProcedureModel> removeProcedure(WoofProcedureModel procedure) {

		// Ensure procedure available to remove
		boolean isInModel = false;
		for (WoofProcedureModel model : this.model.getWoofProcedures()) {
			if (model == procedure) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Procedure model not in model
			return new NoChange<WoofProcedureModel>(procedure, "Remove procedure " + procedure.getWoofProcedureName(),
					"Procedure " + procedure.getWoofProcedureName() + " is not in WoOF model");
		}

		// Return change to remove section
		return new AbstractChange<WoofProcedureModel>(procedure,
				"Remove procedure " + procedure.getWoofProcedureName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(procedure.getWoofTemplateOutputs(), list);
				removeConnections(procedure.getWoofSectionOutputs(), list);
				removeConnections(procedure.getWoofSecurityOutputs(), list);
				removeConnections(procedure.getWoofExceptions(), list);
				removeConnections(procedure.getWoofHttpContinuations(), list);
				removeConnections(procedure.getWoofHttpInputs(), list);
				removeConnections(procedure.getWoofStarts(), list);
				removeConnections(procedure.getWoofProcedureNexts(), list);
				removeConnections(procedure.getWoofProcedureOutputs(), list);
				WoofProcedureNextModel next = procedure.getNext();
				if (next != null) {
					removeConnection(next.getWoofTemplate(), list);
					removeConnection(next.getWoofSectionInput(), list);
					removeConnection(next.getWoofSecurity(), list);
					removeConnection(next.getWoofResource(), list);
					removeConnection(next.getWoofHttpContinuation(), list);
					removeConnection(next.getWoofProcedure(), list);
				}
				for (WoofProcedureOutputModel output : procedure.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofSecurity(), list);
					removeConnection(output.getWoofResource(), list);
					removeConnection(output.getWoofHttpContinuation(), list);
					removeConnection(output.getWoofProcedure(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the procedure
				WoofChangesImpl.this.model.removeWoofProcedure(procedure);
			}

			@Override
			public void revert() {
				// Add back the procedure
				WoofChangesImpl.this.model.addWoofProcedure(procedure);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortProcedures();
			}
		};
	}

	@Override
	public Change<WoofSecurityModel> addSecurity(String httpSecurityName, String httpSecuritySourceClassName,
			long timeout, PropertyList properties, String[] contentTypes,
			HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType) {

		// Obtain the unique name
		httpSecurityName = getUniqueName(httpSecurityName, null, this.model.getWoofSecurities(),
				(model) -> model.getHttpSecurityName());

		// Create the security
		final WoofSecurityModel woofSecurity = new WoofSecurityModel(httpSecurityName, httpSecuritySourceClassName,
				timeout);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofSecurity.addProperty(new PropertyModel(property.getName(), property.getValue()));
			}
		}

		// Add the content types (if available)
		if (contentTypes != null) {
			for (String contentType : contentTypes) {
				woofSecurity.addContentType(new WoofSecurityContentTypeModel(contentType));
			}
		}

		// Add the outputs
		for (HttpSecurityFlowType<?> output : httpSecurityType.getFlowTypes()) {
			// Add the output
			String outputName = output.getFlowName();
			Class<?> argumentType = output.getArgumentType();
			woofSecurity.addOutput(
					new WoofSecurityOutputModel(outputName, (argumentType == null ? null : argumentType.getName())));
		}

		// Sort the inputs/outputs
		sortSecurityOutputs(woofSecurity);

		// Create and return change to set access
		return new AbstractChange<WoofSecurityModel>(woofSecurity, "Add Security") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofSecurity(woofSecurity);
				WoofChangesImpl.this.sortSecurities();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofSecurity(woofSecurity);
			}
		};
	}

	@Override
	public Change<WoofSecurityModel> refactorSecurity(final WoofSecurityModel security, String httpSecurityName,
			final String httpSecuritySourceClassName, final long timeout, final PropertyList properties,
			String[] contentTypes, HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType,
			Map<String, String> accessOutputNameMapping) {

		// Create change to sort outputs
		Change<WoofSecurityModel> sortChange = new AbstractChange<WoofSecurityModel>(security, "Sort outputs") {
			@Override
			public void apply() {
				sortSecurityOutputs(security);
				WoofChangesImpl.this.sortSecurities();
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
		final String existingHttpSecurityName = security.getHttpSecurityName();
		final String existingHttpSecuritySourceClassName = security.getHttpSecuritySourceClassName();
		final long existingTimeout = security.getTimeout();
		final List<PropertyModel> existingProperties = new ArrayList<>(security.getProperties());
		final List<WoofSecurityContentTypeModel> existingContentTypes = new ArrayList<>(security.getContentTypes());

		// Obtain the unique name
		final String uniqueHttpSecurityName = getUniqueName(httpSecurityName, security, this.model.getWoofSecurities(),
				(model) -> model.getHttpSecurityName());

		// Create the listing of new content types
		List<WoofSecurityContentTypeModel> newContentTypes = new ArrayList<>(
				contentTypes == null ? 0 : contentTypes.length);
		if (contentTypes != null) {
			for (String contentType : contentTypes) {
				newContentTypes.add(new WoofSecurityContentTypeModel(contentType));
			}
		}

		// Create change to attributes and properties
		Change<WoofSecurityModel> attributeChange = new AbstractChange<WoofSecurityModel>(security,
				"Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				security.setHttpSecurityName(uniqueHttpSecurityName);
				security.setHttpSecuritySourceClassName(httpSecuritySourceClassName);
				security.setTimeout(timeout);

				// Refactor the content types
				for (WoofSecurityContentTypeModel contentType : existingContentTypes) {
					security.removeContentType(contentType);
				}
				for (WoofSecurityContentTypeModel contentType : newContentTypes) {
					security.addContentType(contentType);
				}

				// Refactor the properties
				security.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						security.addProperty(new PropertyModel(property.getName(), property.getValue()));
					}
				}

				// Rename connection links
				this.renameConnections();
			}

			@Override
			public void revert() {
				// Revert attributes
				security.setHttpSecurityName(existingHttpSecurityName);
				security.setHttpSecuritySourceClassName(existingHttpSecuritySourceClassName);
				security.setTimeout(existingTimeout);

				// Revert the content types
				for (WoofSecurityContentTypeModel contentType : newContentTypes) {
					security.removeContentType(contentType);
				}
				for (WoofSecurityContentTypeModel contentType : existingContentTypes) {
					security.addContentType(contentType);
				}

				// Revert the properties
				security.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					security.addProperty(property);
				}

				// Revert connection links
				this.renameConnections();
			}

			/**
			 * Renames the {@link WoofSecurityModel} connection names.
			 */
			private void renameConnections() {
				String securityName = security.getHttpSecurityName();

				// Rename connections
				for (WoofExceptionToWoofSecurityModel conn : security.getWoofExceptions()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofSectionOutputToWoofSecurityModel conn : security.getWoofSectionOutputs()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofTemplateOutputToWoofSecurityModel conn : security.getWoofTemplateOutputs()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofSecurityOutputToWoofSecurityModel conn : security.getWoofSecurityOutputs()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofHttpContinuationToWoofSecurityModel conn : security.getWoofHttpContinuations()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofHttpInputToWoofSecurityModel conn : security.getWoofHttpInputs()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofProcedureNextToWoofSecurityModel conn : security.getWoofProcedureNexts()) {
					conn.setHttpSecurityName(securityName);
				}
				for (WoofProcedureOutputToWoofSecurityModel conn : security.getWoofProcedureOutputs()) {
					conn.setHttpSecurityName(securityName);
				}
			}
		};
		changes.add(attributeChange);

		// Create the listing of outputs of resulting refactor
		List<ModelItemStruct> outputs = new LinkedList<ModelItemStruct>();
		for (HttpSecurityFlowType<?> flowType : httpSecurityType.getFlowTypes()) {

			// Obtain flow details
			final String outputName = flowType.getFlowName();
			Class<?> argumentTypeClass = flowType.getArgumentType();
			final String argumentType = (argumentTypeClass == null ? null : argumentTypeClass.getName());

			// Create the output for the flow
			outputs.add(new ModelItemStruct(outputName, argumentType));
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofSecurityOutputModel> existingOutputNameMapping = new HashMap<>();
		for (WoofSecurityOutputModel output : security.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofSecurityOutputName(), output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (ModelItemStruct output : outputs) {

			// Obtain the mapped access output model
			final String outputName = output.name;
			String mappedOutputName = (accessOutputNameMapping == null ? null
					: accessOutputNameMapping.get(outputName));
			final WoofSecurityOutputModel existingOutputModel = existingOutputNameMapping.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = output.type;

			// Determine action to take based on existing output
			Change<WoofSecurityOutputModel> securityOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel.getWoofSecurityOutputName();
				final String existingArgumentType = existingOutputModel.getArgumentType();
				securityOutputChange = new AbstractChange<WoofSecurityOutputModel>(existingOutputModel,
						"Refactor Security Output") {
					@Override
					public void apply() {
						existingOutputModel.setWoofSecurityOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel.setWoofSecurityOutputName(existingOutputName);
						existingOutputModel.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output
				final WoofSecurityOutputModel newOutputModel = new WoofSecurityOutputModel(outputName, argumentType);
				securityOutputChange = new AbstractChange<WoofSecurityOutputModel>(newOutputModel,
						"Add Security Output") {
					@Override
					public void apply() {
						security.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						security.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(securityOutputChange);
		}
		for (final WoofSecurityOutputModel unmappedOutputModel : existingOutputNameMapping.values()) {
			// Create change to remove the unmapped output model
			Change<WoofSecurityOutputModel> unmappedOutputChange = new AbstractChange<WoofSecurityOutputModel>(
					unmappedOutputModel, "Remove Security Output") {

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
					removeConnection(unmappedOutputModel.getWoofSecurity(), list);
					removeConnection(unmappedOutputModel.getWoofHttpContinuation(), list);
					removeConnection(unmappedOutputModel.getWoofProcedure(), list);
					this.connections = list.toArray(new ConnectionModel[list.size()]);

					// Remove the access output
					security.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to access
					security.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort inputs/outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<WoofSecurityModel>(security, "Refactor Security",
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
		 * @param name Name.
		 * @param type Type.
		 */
		public ModelItemStruct(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}

	@Override
	public Change<WoofSecurityModel> removeSecurity(final WoofSecurityModel security) {

		// Return change to remove access
		return new AbstractChange<WoofSecurityModel>(security, "Remove security " + security.getHttpSecurityName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(security.getWoofSectionOutputs(), list);
				removeConnections(security.getWoofTemplateOutputs(), list);
				removeConnections(security.getWoofSecurityOutputs(), list);
				removeConnections(security.getWoofExceptions(), list);
				removeConnections(security.getWoofHttpContinuations(), list);
				removeConnections(security.getWoofHttpInputs(), list);
				removeConnections(security.getWoofProcedureNexts(), list);
				removeConnections(security.getWoofProcedureOutputs(), list);
				for (WoofSecurityOutputModel output : security.getOutputs()) {
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSecurity(), list);
					removeConnection(output.getWoofResource(), list);
					removeConnection(output.getWoofHttpContinuation(), list);
					removeConnection(output.getWoofProcedure(), list);
				}
				this.connections = list.toArray(new ConnectionModel[list.size()]);

				// Remove the access
				WoofChangesImpl.this.model.removeWoofSecurity(security);
			}

			@Override
			public void revert() {
				// Add back the access
				WoofChangesImpl.this.model.addWoofSecurity(security);
				reconnectConnections(this.connections);
				sortSecurityOutputs(security);
				WoofChangesImpl.this.sortSecurities();
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> addGovernance(String governanceName, String governanceSourceClassName,
			PropertyList properties, GovernanceType<?, ?> governanceType) {

		// Obtain the unique governance name
		governanceName = getUniqueName(governanceName, null, this.model.getWoofGovernances(),
				(model) -> model.getWoofGovernanceName());

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
				(model) -> model.getWoofGovernanceName());

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

		// Create the resource
		final WoofResourceModel resource = new WoofResourceModel(resourcePath);

		// Ensure resource not already added
		if (!isUniqueModelIdentifier(resourcePath, resource, this.model.getWoofResources(),
				(model) -> model.getResourcePath())) {
			return new NoChange<WoofResourceModel>(resource, "Add Resource",
					"Resource already exists for '" + resourcePath + "'");
		}

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
	 * @param resource          {@link WoofResourceModel} to have is path changed.
	 * @param resourcePath      New path for the {@link WoofResourceModel}.
	 * @param changeDescription Description of type of {@link Change}.
	 * @return {@link Change} to the path for the {@link WoofResourceModel}.
	 */
	private Change<WoofResourceModel> changeResourcePath(final WoofResourceModel resource, final String resourcePath,
			String changeDescription) {

		// No change if no resource path
		if (CompileUtil.isBlank(resourcePath)) {
			return new NoChange<WoofResourceModel>(resource, changeDescription, "Must provide resource path");
		}

		// Ensure resource not already added
		if (!isUniqueModelIdentifier(resourcePath, resource, this.model.getWoofResources(),
				(model) -> model.getResourcePath())) {
			return new NoChange<WoofResourceModel>(resource, changeDescription,
					"Resource already exists for '" + resourcePath + "'");
		}

		// Track original values
		final String originalResourcePath = resource.getResourcePath();

		// Return change to resource path
		return new AbstractChange<WoofResourceModel>(resource, changeDescription) {
			@Override
			public void apply() {
				resource.setResourcePath(resourcePath);
				WoofChangesImpl.this.sortResources();
				this.renameConnections();
			}

			@Override
			public void revert() {
				resource.setResourcePath(originalResourcePath);
				WoofChangesImpl.this.sortResources();
				this.renameConnections();
			}

			/**
			 * Renames the {@link WoofResourceModel} connection names.
			 */
			private void renameConnections() {
				String resourcePath = resource.getResourcePath();

				// Rename connections
				for (WoofExceptionToWoofResourceModel conn : resource.getWoofExceptions()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofSectionOutputToWoofResourceModel conn : resource.getWoofSectionOutputs()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofTemplateOutputToWoofResourceModel conn : resource.getWoofTemplateOutputs()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofSecurityOutputToWoofResourceModel conn : resource.getWoofSecurityOutputs()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofHttpContinuationToWoofResourceModel conn : resource.getWoofHttpContinuations()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofHttpInputToWoofResourceModel conn : resource.getWoofHttpInputs()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofProcedureNextToWoofResourceModel conn : resource.getWoofProcedureNexts()) {
					conn.setResourcePath(resourcePath);
				}
				for (WoofProcedureOutputToWoofResourceModel conn : resource.getWoofProcedureOutputs()) {
					conn.setResourcePath(resourcePath);
				}
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
			return new NoChange<WoofResourceModel>(resource, "Remove resource " + resource.getResourcePath(),
					"Resource " + resource.getResourcePath() + " is not in WoOF model");
		}

		// Return change to remove resource
		return new AbstractChange<WoofResourceModel>(resource, "Remove resource " + resource.getResourcePath()) {

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
				removeConnections(resource.getWoofSecurityOutputs(), list);
				removeConnections(resource.getWoofExceptions(), list);
				removeConnections(resource.getWoofHttpContinuations(), list);
				removeConnections(resource.getWoofHttpInputs(), list);
				removeConnections(resource.getWoofProcedureNexts(), list);
				removeConnections(resource.getWoofProcedureOutputs(), list);
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

		// Create the exception
		final WoofExceptionModel exception = new WoofExceptionModel(exceptionClassName);

		// Ensure exception not already added
		if (!isUniqueModelIdentifier(exceptionClassName, null, this.model.getWoofExceptions(),
				(model) -> model.getClassName())) {
			return new NoChange<WoofExceptionModel>(exception, "Add Exception",
					"Exception already exists for '" + exceptionClassName + "'");
		}

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

		// Ensure exception not already added
		if (!isUniqueModelIdentifier(exceptionClassName, exception, this.model.getWoofExceptions(),
				(model) -> model.getClassName())) {
			return new NoChange<WoofExceptionModel>(exception, "Refactor Exception",
					"Exception already exists for '" + exceptionClassName + "'");
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
				removeConnection(exception.getWoofSectionInput(), list);
				removeConnection(exception.getWoofTemplate(), list);
				removeConnection(exception.getWoofSecurity(), list);
				removeConnection(exception.getWoofResource(), list);
				removeConnection(exception.getWoofHttpContinuation(), list);
				removeConnection(exception.getWoofProcedure(), list);
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
				removeConnection(start.getWoofProcedure(), list);
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

	/*
	 * ---------------------- HttpContinuation links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkHttpContinuation(C connection,
			WoofHttpContinuationModel httpContinuation, String changeDescription) {
		return new AddLinkChange<C, WoofHttpContinuationModel>(connection, httpContinuation, changeDescription) {
			@Override
			protected void addExistingConnections(WoofHttpContinuationModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofRedirect());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofHttpContinuationToWoofHttpContinuationModel> linkHttpContinuationToHttpContinuation(
			WoofHttpContinuationModel httpContinuation, WoofHttpContinuationModel httpRedirect) {
		return this
				.linkHttpContinuation(
						new WoofHttpContinuationToWoofHttpContinuationModel(httpRedirect.getApplicationPath(),
								httpContinuation, httpRedirect),
						httpContinuation, "Link HTTP Continuation to HTTP Continuation");
	}

	@Override
	public Change<WoofHttpContinuationToWoofHttpContinuationModel> removeHttpContinuationToHttpContinuation(
			WoofHttpContinuationToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to HTTP Continuation");
	}

	@Override
	public Change<WoofHttpContinuationToWoofTemplateModel> linkHttpContinuationToTemplate(
			WoofHttpContinuationModel httpContinuation, WoofTemplateModel template) {
		return this.linkHttpContinuation(
				new WoofHttpContinuationToWoofTemplateModel(template.getApplicationPath(), httpContinuation, template),
				httpContinuation, "Link HTTP Continuation to Template");
	}

	@Override
	public Change<WoofHttpContinuationToWoofTemplateModel> removeHttpContinuationToTemplate(
			WoofHttpContinuationToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to Template");
	}

	@Override
	public Change<WoofHttpContinuationToWoofSectionInputModel> linkHttpContinuationToSectionInput(
			WoofHttpContinuationModel httpContinuation, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofHttpContinuationToWoofSectionInputModel>(
					new WoofHttpContinuationToWoofSectionInputModel(), "Link HTTP Continuation to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkHttpContinuation(
				new WoofHttpContinuationToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), httpContinuation, sectionInput),
				httpContinuation, "Link HTTP Continuation to Section Input");
	}

	@Override
	public Change<WoofHttpContinuationToWoofSectionInputModel> removeHttpContinuationToSectionInput(
			WoofHttpContinuationToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to Section Input");
	}

	@Override
	public Change<WoofHttpContinuationToWoofSecurityModel> linkHttpContinuationToSecurity(
			WoofHttpContinuationModel httpContinuation, WoofSecurityModel security) {
		return this.linkHttpContinuation(
				new WoofHttpContinuationToWoofSecurityModel(security.getHttpSecurityName(), httpContinuation, security),
				httpContinuation, "Link HTTP Continuation to Security");
	}

	@Override
	public Change<WoofHttpContinuationToWoofSecurityModel> removeHttpContinuationToSecurity(
			WoofHttpContinuationToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to Security");
	}

	@Override
	public Change<WoofHttpContinuationToWoofResourceModel> linkHttpContinuationToResource(
			WoofHttpContinuationModel httpContinuation, WoofResourceModel resource) {
		return this.linkHttpContinuation(
				new WoofHttpContinuationToWoofResourceModel(resource.getResourcePath(), httpContinuation, resource),
				httpContinuation, "Link HTTP Continuation to Resource");
	}

	@Override
	public Change<WoofHttpContinuationToWoofResourceModel> removeHttpContinuationToResource(
			WoofHttpContinuationToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to Resource");
	}

	@Override
	public Change<WoofHttpContinuationToWoofProcedureModel> linkHttpContinuationToProcedure(
			WoofHttpContinuationModel httpContinuation, WoofProcedureModel procedure) {
		return this.linkHttpContinuation(new WoofHttpContinuationToWoofProcedureModel(procedure.getWoofProcedureName(),
				httpContinuation, procedure), httpContinuation, "Link HTTP Continuation to Procedure");
	}

	@Override
	public Change<WoofHttpContinuationToWoofProcedureModel> removeHttpContinuationToProcedure(
			WoofHttpContinuationToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Continuation to Procedure");
	}

	/*
	 * ---------------------- HttpInput links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkHttpInput(C connection, WoofHttpInputModel httpInput,
			String changeDescription) {
		return new AddLinkChange<C, WoofHttpInputModel>(connection, httpInput, changeDescription) {
			@Override
			protected void addExistingConnections(WoofHttpInputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofHttpInputToWoofHttpContinuationModel> linkHttpInputToHttpContinuation(
			WoofHttpInputModel httpInput, WoofHttpContinuationModel httpContinuation) {
		return this.linkHttpInput(new WoofHttpInputToWoofHttpContinuationModel(httpContinuation.getApplicationPath(),
				httpInput, httpContinuation), httpInput, "Link HTTP Input to HTTP Continuation");
	}

	@Override
	public Change<WoofHttpInputToWoofHttpContinuationModel> removeHttpInputToHttpContinuation(
			WoofHttpInputToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to HTTP Continuation");
	}

	@Override
	public Change<WoofHttpInputToWoofTemplateModel> linkHttpInputToTemplate(WoofHttpInputModel httpInput,
			WoofTemplateModel template) {
		return this.linkHttpInput(
				new WoofHttpInputToWoofTemplateModel(template.getApplicationPath(), httpInput, template), httpInput,
				"Link HTTP Input to Template");
	}

	@Override
	public Change<WoofHttpInputToWoofTemplateModel> removeHttpInputToTemplate(WoofHttpInputToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to Template");
	}

	@Override
	public Change<WoofHttpInputToWoofSectionInputModel> linkHttpInputToSectionInput(WoofHttpInputModel httpInput,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofHttpInputToWoofSectionInputModel>(new WoofHttpInputToWoofSectionInputModel(),
					"Link Http Input to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkHttpInput(
				new WoofHttpInputToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), httpInput, sectionInput),
				httpInput, "Link HTTP Input to Section Input");
	}

	@Override
	public Change<WoofHttpInputToWoofSectionInputModel> removeHttpInputToSectionInput(
			WoofHttpInputToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to Section Input");
	}

	@Override
	public Change<WoofHttpInputToWoofSecurityModel> linkHttpInputToSecurity(WoofHttpInputModel httpInput,
			WoofSecurityModel security) {
		return this.linkHttpInput(
				new WoofHttpInputToWoofSecurityModel(security.getHttpSecurityName(), httpInput, security), httpInput,
				"Link HTTP Input to Security");
	}

	@Override
	public Change<WoofHttpInputToWoofSecurityModel> removeHttpInputToSecurity(WoofHttpInputToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to Security");
	}

	@Override
	public Change<WoofHttpInputToWoofResourceModel> linkHttpInputToResource(WoofHttpInputModel httpInput,
			WoofResourceModel resource) {
		return this.linkHttpInput(new WoofHttpInputToWoofResourceModel(resource.getResourcePath(), httpInput, resource),
				httpInput, "Link HTTP Input to Resource");
	}

	@Override
	public Change<WoofHttpInputToWoofResourceModel> removeHttpInputToResource(WoofHttpInputToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to Resource");
	}

	@Override
	public Change<WoofHttpInputToWoofProcedureModel> linkHttpInputToProcedure(WoofHttpInputModel httpInput,
			WoofProcedureModel procedure) {
		return this.linkHttpInput(
				new WoofHttpInputToWoofProcedureModel(procedure.getWoofProcedureName(), httpInput, procedure),
				httpInput, "Link HTTP Input to Procedure");
	}

	@Override
	public Change<WoofHttpInputToWoofProcedureModel> removeHttpInputToProcedure(
			WoofHttpInputToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove HTTP Input to Procedure");
	}

	/*
	 * ---------------------- TemplateOutput links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkTemplateOutput(C connection,
			WoofTemplateOutputModel templateOutput, String changeDescription) {
		return new AddLinkChange<C, WoofTemplateOutputModel>(connection, templateOutput, changeDescription) {
			@Override
			protected void addExistingConnections(WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(
			final WoofTemplateOutputModel templateOutput, WoofTemplateModel template) {
		return this.linkTemplateOutput(
				new WoofTemplateOutputToWoofTemplateModel(template.getApplicationPath(), templateOutput, template),
				templateOutput, "Link Template Output to Template");
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOutputToTemplate(
			final WoofTemplateOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Template Output to Template");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			final WoofTemplateOutputModel templateOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofTemplateOutputToWoofSectionInputModel>(
					new WoofTemplateOutputToWoofSectionInputModel(), "Remove Template Output to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkTemplateOutput(
				new WoofTemplateOutputToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), templateOutput, sectionInput),
				templateOutput, "Link Template Output to Section Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOutputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Template Output to Section Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSecurityModel> linkTemplateOutputToSecurity(
			WoofTemplateOutputModel templateOutput, WoofSecurityModel security) {
		return this.linkTemplateOutput(
				new WoofTemplateOutputToWoofSecurityModel(security.getHttpSecurityName(), templateOutput, security),
				templateOutput, "Link Template Output to Security");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSecurityModel> removeTemplateOutputToSecurity(
			WoofTemplateOutputToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Template Output to Security");
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(
			WoofTemplateOutputModel templateOutput, WoofResourceModel resource) {
		return this.linkTemplateOutput(
				new WoofTemplateOutputToWoofResourceModel(resource.getResourcePath(), templateOutput, resource),
				templateOutput, "Link Template Output to Resource");
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOutputToResource(
			WoofTemplateOutputToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Template Output to Resource");
	}

	@Override
	public Change<WoofTemplateOutputToWoofProcedureModel> linkTemplateOutputToProcedure(
			WoofTemplateOutputModel templateOutput, WoofProcedureModel procedure) {
		return this.linkTemplateOutput(
				new WoofTemplateOutputToWoofProcedureModel(procedure.getWoofProcedureName(), templateOutput, procedure),
				templateOutput, "Link Template Output to Procedure");
	}

	@Override
	public Change<WoofTemplateOutputToWoofProcedureModel> removeTemplateOutputToProcedure(
			WoofTemplateOutputToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Template Output to Procedure");
	}

	/*
	 * ---------------------- SectionOutput links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkSectionOutput(C connection, WoofSectionOutputModel sectionOutput,
			String changeDescription) {
		return new AddLinkChange<C, WoofSectionOutputModel>(connection, sectionOutput, changeDescription) {
			@Override
			protected void addExistingConnections(WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(
			WoofSectionOutputModel sectionOutput, WoofTemplateModel template) {
		return this.linkSectionOutput(
				new WoofSectionOutputToWoofTemplateModel(template.getApplicationPath(), sectionOutput, template),
				sectionOutput, "Link Section Output to Template");
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> removeSectionOutputToTemplate(
			WoofSectionOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Template");
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofSectionOutputToWoofSectionInputModel>(
					new WoofSectionOutputToWoofSectionInputModel(), "Remove Section Output to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkSectionOutput(
				new WoofSectionOutputToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), sectionOutput, sectionInput),
				sectionOutput, "Link Section Output to Section Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOutputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Section Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofSecurityModel> linkSectionOutputToSecurity(
			WoofSectionOutputModel sectionOutput, WoofSecurityModel security) {
		return this.linkSectionOutput(
				new WoofSectionOutputToWoofSecurityModel(security.getHttpSecurityName(), sectionOutput, security),
				sectionOutput, "Link Section Output to Security");
	}

	@Override
	public Change<WoofSectionOutputToWoofSecurityModel> removeSectionOutputToSecurity(
			WoofSectionOutputToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Security");
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(
			WoofSectionOutputModel sectionOutput, WoofResourceModel resource) {
		return this.linkSectionOutput(
				new WoofSectionOutputToWoofResourceModel(resource.getResourcePath(), sectionOutput, resource),
				sectionOutput, "Link Section Output to Resource");
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> removeSectionOutputToResource(
			WoofSectionOutputToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Resource");
	}

	@Override
	public Change<WoofSectionOutputToWoofHttpContinuationModel> linkSectionOutputToHttpContinuation(
			WoofSectionOutputModel sectionOutput, WoofHttpContinuationModel httpContinuation) {
		return this
				.linkSectionOutput(
						new WoofSectionOutputToWoofHttpContinuationModel(httpContinuation.getApplicationPath(),
								sectionOutput, httpContinuation),
						sectionOutput, "Link Section Output to HTTP Continuation");
	}

	@Override
	public Change<WoofSectionOutputToWoofHttpContinuationModel> removeSectionOutputToHttpContinuation(
			WoofSectionOutputToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to HTTP Continuation");
	}

	@Override
	public Change<WoofSectionOutputToWoofProcedureModel> linkSectionOutputToProcedure(
			WoofSectionOutputModel sectionOutput, WoofProcedureModel procedure) {
		return this.linkSectionOutput(
				new WoofSectionOutputToWoofProcedureModel(procedure.getWoofProcedureName(), sectionOutput, procedure),
				sectionOutput, "Link Section Output to Procedure");
	}

	@Override
	public Change<WoofSectionOutputToWoofProcedureModel> removeSectionOutputToProcedure(
			WoofSectionOutputToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Section Output to Procedure");
	}

	/*
	 * ---------------------- Procedure Next links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkProcedureNext(C connection, WoofProcedureNextModel procedureNext,
			String changeDescription) {
		return new AddLinkChange<C, WoofProcedureNextModel>(connection, procedureNext, changeDescription) {
			@Override
			protected void addExistingConnections(WoofProcedureNextModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofProcedureNextToWoofTemplateModel> linkProcedureNextToTemplate(
			WoofProcedureNextModel procedureNext, WoofTemplateModel template) {
		return this.linkProcedureNext(
				new WoofProcedureNextToWoofTemplateModel(template.getApplicationPath(), procedureNext, template),
				procedureNext, "Link Procedure Next to Template");
	}

	@Override
	public Change<WoofProcedureNextToWoofTemplateModel> removeProcedureNextToTemplate(
			WoofProcedureNextToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Template");
	}

	@Override
	public Change<WoofProcedureNextToWoofSectionInputModel> linkProcedureNextToSectionInput(
			WoofProcedureNextModel procedureNext, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofProcedureNextToWoofSectionInputModel>(
					new WoofProcedureNextToWoofSectionInputModel(), "Link Procedure Next to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return the change to add connection
		return this.linkProcedureNext(
				new WoofProcedureNextToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), procedureNext, sectionInput),
				procedureNext, "Link Procedure Next to Section Input");
	}

	@Override
	public Change<WoofProcedureNextToWoofSectionInputModel> removeProcedureNextToSectionInput(
			WoofProcedureNextToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Section Input");
	}

	@Override
	public Change<WoofProcedureNextToWoofSecurityModel> linkProcedureNextToSecurity(
			WoofProcedureNextModel procedureNext, WoofSecurityModel security) {
		return this.linkProcedureNext(
				new WoofProcedureNextToWoofSecurityModel(security.getHttpSecurityName(), procedureNext, security),
				procedureNext, "Link Procedure Next to Security");
	}

	@Override
	public Change<WoofProcedureNextToWoofSecurityModel> removeProcedureNextToSecurity(
			WoofProcedureNextToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Security");
	}

	@Override
	public Change<WoofProcedureNextToWoofResourceModel> linkProcedureNextToResource(
			WoofProcedureNextModel procedureNext, WoofResourceModel resource) {
		return this.linkProcedureNext(
				new WoofProcedureNextToWoofResourceModel(resource.getResourcePath(), procedureNext, resource),
				procedureNext, "Link Procedure Next to Resource");
	}

	@Override
	public Change<WoofProcedureNextToWoofResourceModel> removeProcedureNextToResource(
			WoofProcedureNextToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Resource");
	}

	@Override
	public Change<WoofProcedureNextToWoofHttpContinuationModel> linkProcedureNextToHttpContinuation(
			WoofProcedureNextModel procedureNext, WoofHttpContinuationModel httpContinuation) {
		return this
				.linkProcedureNext(
						new WoofProcedureNextToWoofHttpContinuationModel(httpContinuation.getApplicationPath(),
								procedureNext, httpContinuation),
						procedureNext, "Link Procedure Next to HTTP Continuation");
	}

	@Override
	public Change<WoofProcedureNextToWoofHttpContinuationModel> removeProcedureNextToHttpContinuation(
			WoofProcedureNextToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to HTTP Continuation");
	}

	@Override
	public Change<WoofProcedureNextToWoofProcedureModel> linkProcedureNextToProcedure(
			WoofProcedureNextModel procedureNext, WoofProcedureModel procedure) {
		return this.linkProcedureNext(
				new WoofProcedureNextToWoofProcedureModel(procedure.getWoofProcedureName(), procedureNext, procedure),
				procedureNext, "Link Procedure Next to Procedure");
	}

	@Override
	public Change<WoofProcedureNextToWoofProcedureModel> removeProcedureNextToProcedure(
			WoofProcedureNextToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Next to Procedure");
	}

	/*
	 * ---------------------- Procedure Output links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkProcedureOutput(C connection,
			WoofProcedureOutputModel procedureOutput, String changeDescription) {
		return new AddLinkChange<C, WoofProcedureOutputModel>(connection, procedureOutput, changeDescription) {
			@Override
			protected void addExistingConnections(WoofProcedureOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofProcedureOutputToWoofTemplateModel> linkProcedureOutputToTemplate(
			WoofProcedureOutputModel procedureOutput, WoofTemplateModel template) {
		return this.linkProcedureOutput(
				new WoofProcedureOutputToWoofTemplateModel(template.getApplicationPath(), procedureOutput, template),
				procedureOutput, "Link Procedure Output to Template");
	}

	@Override
	public Change<WoofProcedureOutputToWoofTemplateModel> removeProcedureOutputToTemplate(
			WoofProcedureOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Template");
	}

	@Override
	public Change<WoofProcedureOutputToWoofSectionInputModel> linkProcedureOutputToSectionInput(
			WoofProcedureOutputModel procedureOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofProcedureOutputToWoofSectionInputModel>(
					new WoofProcedureOutputToWoofSectionInputModel(), "Link Procedure Output to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return the change to add connection
		return this.linkProcedureOutput(
				new WoofProcedureOutputToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), procedureOutput, sectionInput),
				procedureOutput, "Link Procedure Output to Section Input");
	}

	@Override
	public Change<WoofProcedureOutputToWoofSectionInputModel> removeProcedureOutputToSectionInput(
			WoofProcedureOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Section Input");
	}

	@Override
	public Change<WoofProcedureOutputToWoofSecurityModel> linkProcedureOutputToSecurity(
			WoofProcedureOutputModel procedureOutput, WoofSecurityModel security) {
		return this.linkProcedureOutput(
				new WoofProcedureOutputToWoofSecurityModel(security.getHttpSecurityName(), procedureOutput, security),
				procedureOutput, "Link Procedure Output to Security");
	}

	@Override
	public Change<WoofProcedureOutputToWoofSecurityModel> removeProcedureOutputToSecurity(
			WoofProcedureOutputToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Security");
	}

	@Override
	public Change<WoofProcedureOutputToWoofResourceModel> linkProcedureOutputToResource(
			WoofProcedureOutputModel procedureOutput, WoofResourceModel resource) {
		return this.linkProcedureOutput(
				new WoofProcedureOutputToWoofResourceModel(resource.getResourcePath(), procedureOutput, resource),
				procedureOutput, "Link Procedure Output to Resource");
	}

	@Override
	public Change<WoofProcedureOutputToWoofResourceModel> removeProcedureOutputToResource(
			WoofProcedureOutputToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Resource");
	}

	@Override
	public Change<WoofProcedureOutputToWoofHttpContinuationModel> linkProcedureOutputToHttpContinuation(
			WoofProcedureOutputModel procedureOutput, WoofHttpContinuationModel httpContinuation) {
		return this.linkProcedureOutput(
				new WoofProcedureOutputToWoofHttpContinuationModel(httpContinuation.getApplicationPath(),
						procedureOutput, httpContinuation),
				procedureOutput, "Link Procedure Output to HTTP Continuation");
	}

	@Override
	public Change<WoofProcedureOutputToWoofHttpContinuationModel> removeProcedureOutputToHttpContinuation(
			WoofProcedureOutputToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to HTTP Continuation");
	}

	@Override
	public Change<WoofProcedureOutputToWoofProcedureModel> linkProcedureOutputToProcedure(
			WoofProcedureOutputModel procedureOutput, WoofProcedureModel procedure) {
		return this.linkProcedureOutput(new WoofProcedureOutputToWoofProcedureModel(procedure.getWoofProcedureName(),
				procedureOutput, procedure), procedureOutput, "Link Procedure Output to Procedure");
	}

	@Override
	public Change<WoofProcedureOutputToWoofProcedureModel> removeProcedureOutputToProcedure(
			WoofProcedureOutputToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Procedure Output to Procedure");
	}

	/*
	 * ---------------------- SecurityOutput links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkSecurityOutput(C connection,
			WoofSecurityOutputModel securityOutput, String changeDescription) {
		return new AddLinkChange<C, WoofSecurityOutputModel>(connection, securityOutput, changeDescription) {
			@Override
			protected void addExistingConnections(WoofSecurityOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofResource());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofSecurityOutputToWoofTemplateModel> linkSecurityOutputToTemplate(
			WoofSecurityOutputModel securityOutput, WoofTemplateModel template) {
		return this.linkSecurityOutput(
				new WoofSecurityOutputToWoofTemplateModel(template.getApplicationPath(), securityOutput, template),
				securityOutput, "Link Security Output to Template");
	}

	@Override
	public Change<WoofSecurityOutputToWoofTemplateModel> removeSecurityOutputToTemplate(
			WoofSecurityOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to Template");
	}

	@Override
	public Change<WoofSecurityOutputToWoofSectionInputModel> linkSecurityOutputToSectionInput(
			WoofSecurityOutputModel securityOutput, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofSecurityOutputToWoofSectionInputModel>(
					new WoofSecurityOutputToWoofSectionInputModel(), "Remove Security Output to Security Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkSecurityOutput(
				new WoofSecurityOutputToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), securityOutput, sectionInput),
				securityOutput, "Link Security Output to Section Input");
	}

	@Override
	public Change<WoofSecurityOutputToWoofSectionInputModel> removeSecurityOutputToSectionInput(
			WoofSecurityOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to Section Input");
	}

	@Override
	public Change<WoofSecurityOutputToWoofSecurityModel> linkSecurityOutputToSecurity(
			WoofSecurityOutputModel securityOutput, WoofSecurityModel security) {
		return this.linkSecurityOutput(
				new WoofSecurityOutputToWoofSecurityModel(security.getHttpSecurityName(), securityOutput, security),
				securityOutput, "Link Security Output to Security");
	}

	@Override
	public Change<WoofSecurityOutputToWoofSecurityModel> removeSecurityOutputToSecurity(
			WoofSecurityOutputToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to Security");
	}

	@Override
	public Change<WoofSecurityOutputToWoofResourceModel> linkSecurityOutputToResource(
			WoofSecurityOutputModel securityOutput, WoofResourceModel resource) {
		return this.linkSecurityOutput(
				new WoofSecurityOutputToWoofResourceModel(resource.getResourcePath(), securityOutput, resource),
				securityOutput, "Link Security Output to Resource");
	}

	@Override
	public Change<WoofSecurityOutputToWoofResourceModel> removeSecurityOutputToResource(
			WoofSecurityOutputToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to Resource");
	}

	@Override
	public Change<WoofSecurityOutputToWoofHttpContinuationModel> linkSecurityOutputToHttpContinuation(
			WoofSecurityOutputModel securityOutput, WoofHttpContinuationModel httpContinuation) {
		return this
				.linkSecurityOutput(
						new WoofSecurityOutputToWoofHttpContinuationModel(httpContinuation.getApplicationPath(),
								securityOutput, httpContinuation),
						securityOutput, "Link Security Output to HTTP Continuation");
	}

	@Override
	public Change<WoofSecurityOutputToWoofHttpContinuationModel> removeSecurityOutputToHttpContinuation(
			WoofSecurityOutputToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to HTTP Continuation");
	}

	@Override
	public Change<WoofSecurityOutputToWoofProcedureModel> linkSecurityOutputToProcedure(
			WoofSecurityOutputModel securityOutput, WoofProcedureModel procedure) {
		return this.linkSecurityOutput(
				new WoofSecurityOutputToWoofProcedureModel(procedure.getWoofProcedureName(), securityOutput, procedure),
				securityOutput, "Link Security Output to Procedure");
	}

	@Override
	public Change<WoofSecurityOutputToWoofProcedureModel> removeSecurityOutputToProcedure(
			WoofSecurityOutputToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Security Output to Procedure");
	}

	/*
	 * ---------------------- Exception links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkException(C connection, WoofExceptionModel exception,
			String changeDescription) {
		return new AddLinkChange<C, WoofExceptionModel>(connection, exception, changeDescription) {
			@Override
			protected void addExistingConnections(WoofExceptionModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
				list.add(source.getWoofSecurity());
				list.add(source.getWoofHttpContinuation());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofHttpContinuationModel> linkExceptionToHttpContinuation(
			WoofExceptionModel exception, WoofHttpContinuationModel applicationPath) {
		return this.linkException(new WoofExceptionToWoofHttpContinuationModel(applicationPath.getApplicationPath(),
				exception, applicationPath), exception, "Link Exception to HTTP Continuation");
	}

	@Override
	public Change<WoofExceptionToWoofHttpContinuationModel> removeExceptionToHttpContinuation(
			WoofExceptionToWoofHttpContinuationModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to HTTP Continuation");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(WoofExceptionModel exception,
			WoofTemplateModel template) {
		return this.linkException(
				new WoofExceptionToWoofTemplateModel(template.getApplicationPath(), exception, template), exception,
				"Link Exception to Template");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(WoofExceptionToWoofTemplateModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Template");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(WoofExceptionModel exception,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofExceptionToWoofSectionInputModel>(new WoofExceptionToWoofSectionInputModel(),
					"Remove Exception to Section Input",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkException(
				new WoofExceptionToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), exception, sectionInput),
				exception, "Link Exception to Section Input");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Section Input");
	}

	@Override
	public Change<WoofExceptionToWoofSecurityModel> linkExceptionToSecurity(WoofExceptionModel exception,
			WoofSecurityModel security) {
		return this.linkException(
				new WoofExceptionToWoofSecurityModel(security.getHttpSecurityName(), exception, security), exception,
				"Link Exception to Security");
	}

	@Override
	public Change<WoofExceptionToWoofSecurityModel> removeExceptionToSecurity(WoofExceptionToWoofSecurityModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Security");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(WoofExceptionModel exception,
			WoofResourceModel resource) {
		return this.linkException(new WoofExceptionToWoofResourceModel(resource.getResourcePath(), exception, resource),
				exception, "Link Exception to Resource");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(WoofExceptionToWoofResourceModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Resource");
	}

	@Override
	public Change<WoofExceptionToWoofProcedureModel> linkExceptionToProcedure(WoofExceptionModel exception,
			WoofProcedureModel procedure) {
		return this.linkException(
				new WoofExceptionToWoofProcedureModel(procedure.getWoofProcedureName(), exception, procedure),
				exception, "Link Exception to Procedure");
	}

	@Override
	public Change<WoofExceptionToWoofProcedureModel> removeExceptionToProcedure(
			WoofExceptionToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Exception to Procedure");
	}

	/*
	 * ---------------------- Start links -------------------------
	 */

	private <C extends ConnectionModel> Change<C> linkStart(C connection, WoofStartModel start,
			String changeDescription) {
		return new AddLinkChange<C, WoofStartModel>(connection, start, changeDescription) {
			@Override
			protected void addExistingConnections(WoofStartModel source, List<ConnectionModel> list) {
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofProcedure());
			}
		};
	}

	@Override
	public Change<WoofStartToWoofSectionInputModel> linkStartToSectionInput(WoofStartModel start,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofStartToWoofSectionInputModel>(new WoofStartToWoofSectionInputModel(),
					"Remove Exception to Resource",
					"The section input '" + sectionInput.getWoofSectionInputName() + "' was not found");
		}

		// Return change to add connection
		return this.linkStart(
				new WoofStartToWoofSectionInputModel(section.getWoofSectionName(),
						sectionInput.getWoofSectionInputName(), start, sectionInput),
				start, "Link Start to Section Input");
	}

	@Override
	public Change<WoofStartToWoofSectionInputModel> removeStartToSectionInput(WoofStartToWoofSectionInputModel link) {
		return new RemoveLinkChange<>(link, "Remove Start to Section Input");
	}

	@Override
	public Change<WoofStartToWoofProcedureModel> linkStartToProcedure(WoofStartModel start,
			WoofProcedureModel procedure) {
		return this.linkStart(new WoofStartToWoofProcedureModel(procedure.getWoofProcedureName(), start, procedure),
				start, "Link Start to Procedure");
	}

	@Override
	public Change<WoofStartToWoofProcedureModel> removeStartToProcedure(WoofStartToWoofProcedureModel link) {
		return new RemoveLinkChange<>(link, "Remove Start to Procedure");
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
