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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.model.change.Change;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.type.WebTemplateLoader;
import net.officefloor.web.template.type.WebTemplateType;

/**
 * Changes that can be made to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofChanges {

	/**
	 * Adds a {@link WoofHttpContinuationModel}.
	 * 
	 * @param applicationPath URI to the {@link WoofHttpContinuationModel}.
	 * @param isSecure        <code>true</code> to require a secure
	 *                        {@link ServerHttpConnection}.
	 * @return {@link Change} to add a {@link WoofHttpContinuationModel}.
	 */
	Change<WoofHttpContinuationModel> addHttpContinuation(String applicationPath, boolean isSecure);

	/**
	 * Documents the {@link WoofHttpContinuationModel}.
	 * 
	 * @param continuation  {@link WoofHttpContinuationModel} to have documentation.
	 * @param documentation Documentation for the {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to provide documentation to the
	 *         {@link WoofHttpContinuationModel}.
	 */
	Change<WoofHttpContinuationModel> addDocumentation(WoofHttpContinuationModel continuation, String documentation);

	/**
	 * Refactors the {@link WoofHttpContinuationModel}.
	 * 
	 * @param continuation    {@link WoofHttpContinuationModel} to be refactored.
	 * @param applicationPath Application path for the
	 *                        {@link WoofHttpContinuationModel}.
	 * @param isSecure        Indicates if secure.
	 * @return {@link Change} to refactor the {@link WoofHttpContinuationModel}.
	 */
	Change<WoofHttpContinuationModel> refactorHttpContinuation(WoofHttpContinuationModel continuation,
			String applicationPath, boolean isSecure);

	/**
	 * Changes the application path for the {@link WoofHttpContinuationModel}.
	 * 
	 * @param continuation    {@link WoofHttpContinuationModel}.
	 * @param applicationPath New application path.
	 * @return {@link Change} for the application path.
	 */
	Change<WoofHttpContinuationModel> changeApplicationPath(WoofHttpContinuationModel continuation,
			String applicationPath);

	/**
	 * Removes a {@link WoofHttpContinuationModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel} to remove.
	 * @return {@link Change} to remove the {@link WoofHttpContinuationModel}.
	 */
	Change<WoofHttpContinuationModel> removeHttpContinuation(WoofHttpContinuationModel httpContinuation);

	/**
	 * Adds a {@link WoofHttpInputModel}.
	 * 
	 * @param applicationPath Application path to the {@link WoofHttpInputModel}.
	 * @param httpMethodName  Name of the {@link HttpMethod}.
	 * @param isSecure        <code>true</code> to require a secure
	 *                        {@link ServerHttpConnection}.
	 * @return {@link Change} to add a {@link WoofHttpInputModel}.
	 */
	Change<WoofHttpInputModel> addHttpInput(String applicationPath, String httpMethodName, boolean isSecure);

	/**
	 * Documents the {@link WoofHttpInputModel}.
	 * 
	 * @param input         {@link WoofHttpInputModel} to have documentation.
	 * @param documentation Documentation for the {@link WoofHttpInputModel}.
	 * @return {@link Change} to provide documentation to the
	 *         {@link WoofHttpInputModel}.
	 */
	Change<WoofHttpInputModel> addDocumentation(WoofHttpInputModel input, String documentation);

	/**
	 * Refactors the {@link WoofHttpInputModel}.
	 * 
	 * @param input           {@link WoofHttpInputModel} to refactor.
	 * @param applicationPath New application path.
	 * @param httpMethod      New {@link HttpMethod} name.
	 * @param isSecure        Indicates if secure.
	 * @return {@link Change} to refactor the {@link WoofHttpInputModel}.
	 */
	Change<WoofHttpInputModel> refactorHttpInput(WoofHttpInputModel input, String applicationPath, String httpMethod,
			boolean isSecure);

	/**
	 * Changes the application path for the {@link WoofHttpInputModel}.
	 *
	 * @param input           {@link WoofHttpInputModel}.
	 * @param applicationPath New application path.
	 * @return {@link Change} to the application path.
	 */
	Change<WoofHttpInputModel> changeApplicationPath(WoofHttpInputModel input, String applicationPath);

	/**
	 * Removes a {@link WoofHttpInputModel}.
	 * 
	 * @param httpInput {@link WoofHttpInputModel} to remove.
	 * @return {@link Change} to remove the {@link WoofHttpContinuationModel}.
	 */
	Change<WoofHttpInputModel> removeHttpInput(WoofHttpInputModel httpInput);

	/**
	 * Adds a {@link WoofTemplateModel}.
	 * 
	 * @param applicationPath        URI to the {@link WoofTemplateModel}.
	 * @param templateLocation       Path to the template file.
	 * @param templateLogicClass     Name of the logic {@link Class} for the
	 *                               template.
	 * @param webTemplateType        {@link WebTemplateType} for the
	 *                               {@link WoofTemplateModel}.
	 * @param redirectValuesFunction Render redirect {@link ManagedFunction} name.
	 * @param contentType            Content-Type for the {@link WoofTemplateModel}.
	 *                               May be <code>null</code>.
	 * @param charsetName            Name of {@link Charset} for the
	 *                               {@link WoofTemplateModel}.
	 * @param isTemplateSecure       <code>true</code> for the
	 *                               {@link WoofTemplateModel} to require a secure
	 *                               {@link ServerHttpConnection}.
	 * @param linkSeparatorCharacter Link separator {@link Character}.
	 * @param linksSecure            Link secure configuration overriding
	 *                               {@link WoofTemplateModel} secure.
	 * @param renderHttpMethods      Listing of HTTP methods that are to render the
	 *                               {@link WoofTemplateModel}.
	 * @param extensions             {@link WoofTemplateExtension} instances for the
	 *                               {@link WoofTemplateModel}.
	 * @param context                {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to add a {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> addTemplate(String applicationPath, String templateLocation, String templateLogicClass,
			WebTemplateType webTemplateType, String redirectValuesFunction, String contentType, String charsetName,
			boolean isTemplateSecure, String linkSeparatorCharacter, Map<String, Boolean> linksSecure,
			String[] renderHttpMethods, WoofTemplateExtension[] extensions, WoofTemplateChangeContext context);

	/**
	 * <p>
	 * Obtains the inheritable {@link WoofTemplateOutputModel} names for the
	 * {@link WoofTemplateModel}.
	 * <p>
	 * Note that this searches the super {@link WoofTemplateModel} of the input
	 * {@link WoofTemplateModel}. The {@link WoofTemplateOutputModel} instances from
	 * the input {@link WoofTemplateModel} are not included.
	 *
	 * @param childTemplate Child {@link WoofTemplateModel}.
	 * @return Inheritable {@link WoofTemplateOutputModel} names.
	 */
	Set<String> getInheritableOutputNames(WoofTemplateModel childTemplate);

	/**
	 * Loads the super {@link WoofTemplateModel} models to the {@link WebTemplate}.
	 * 
	 * @param template       {@link WebTemplate} to be loaded within its super
	 *                       {@link WebTemplate} instances.
	 * @param woofTemplate   {@link WoofTemplateModel} within the configuration for
	 *                       the {@link WebTemplate}.
	 * @param templateLoader {@link WebTemplateLoader}.
	 */
	void loadSuperTemplates(WebTemplate template, WoofTemplateModel woofTemplate, WebTemplateLoader templateLoader);

	/**
	 * Refactors the {@link WoofTemplateModel}.
	 * 
	 * @param template                     {@link WoofTemplateModel} to refactor.
	 * @param applicationPath              New application path for the
	 *                                     {@link WoofTemplateModel}.
	 * @param templateLocation             New template location for the
	 *                                     {@link WoofTemplateModel}.
	 * @param templateLogicClass           New logic class for the
	 *                                     {@link WoofTemplateModel}.
	 * @param webTemplateType              {@link WebTemplateType} for the
	 *                                     refactored {@link WoofTemplateModel}.
	 * @param redirectValuesFunction       New render redirect
	 *                                     {@link ManagedFunction} name.
	 * @param inheritedTemplateOutputNames Inherited {@link WoofTemplateOutputModel}
	 *                                     configuration from the super
	 *                                     {@link WoofTemplateModel} and its
	 *                                     subsequent ancestors.
	 * @param contentType                  Content-Type for the
	 *                                     {@link WoofTemplateModel}. May be
	 *                                     <code>null</code>.
	 * @param charsetName                  Name of {@link Charset} for the
	 *                                     {@link WoofTemplateModel}. May be
	 *                                     <code>null</code>.
	 * @param isTemplateSecure             <code>true</code> for the
	 *                                     {@link WoofTemplateModel} to require a
	 *                                     secure {@link ServerHttpConnection}.
	 * @param linkSeparatorCharacter       New link separator {@link Character}.
	 * @param linksSecure                  Link secure configuration overriding
	 *                                     {@link WoofTemplateModel} secure.
	 * @param renderHttpMethods            Listing of HTTP methods that render the
	 *                                     {@link WoofTemplateModel}.
	 * @param extensions                   {@link WoofTemplateExtension} instances
	 *                                     for the {@link WoofTemplateModel}.
	 * @param templateOutputNameMapping    Mapping of {@link SectionOutputType} name
	 *                                     to existing
	 *                                     {@link WoofTemplateOutputModel} name to
	 *                                     allow maintaining links to other items
	 *                                     within the {@link WoofModel}.
	 * @param context                      {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to refactor the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> refactorTemplate(WoofTemplateModel template, String applicationPath,
			String templateLocation, String templateLogicClass, WebTemplateType webTemplateType,
			String redirectValuesFunction, Set<String> inheritedTemplateOutputNames, String contentType,
			String charsetName, boolean isTemplateSecure, String linkSeparatorCharacter,
			Map<String, Boolean> linksSecure, String[] renderHttpMethods, WoofTemplateExtension[] extensions,
			Map<String, String> templateOutputNameMapping, WoofTemplateChangeContext context);

	/**
	 * Changes the application path for the {@link WoofTemplateModel}.
	 * 
	 * @param template        {@link WoofTemplateModel}.
	 * @param applicationPath Application path.
	 * @param context         {@link WoofTemplateChangeContext}.
	 * @return {@link Change} for the URI.
	 */
	Change<WoofTemplateModel> changeApplicationPath(WoofTemplateModel template, String applicationPath,
			WoofTemplateChangeContext context);

	/**
	 * Removes the {@link WoofTemplateModel}.
	 * 
	 * @param template {@link WoofTemplateModel} to remove.
	 * @param context  {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to remove the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> removeTemplate(WoofTemplateModel template, WoofTemplateChangeContext context);

	/**
	 * Adds a {@link WoofProcedureModel}.
	 * 
	 * @param procedureName Name of the {@link WoofProcedureModel}.
	 * @param resource      Resource.
	 * @param sourceName    Source name.
	 * @param procedure     {@link Procedure} name.
	 * @param properties    {@link PropertyList}
	 * @param procedureType {@link ProcedureType} for the
	 *                      {@link WoofProcedureModel}.
	 * @return {@link Change} to add the {@link WoofProcedureModel}.
	 */
	Change<WoofProcedureModel> addProcedure(String procedureName, String resource, String sourceName, String procedure,
			PropertyList properties, ProcedureType procedureType);

	/**
	 * Refactors the {@link WoofProcedureModel}.
	 * 
	 * @param procedureModel    {@link WoofProcedureModel} to refactor.
	 * @param procedureName     Name of the {@link WoofProcedureModel}.
	 * @param resource          Resource.
	 * @param sourceName        Source name.
	 * @param procedure         {@link Procedure} name.
	 * @param properties        {@link PropertyList}.
	 * @param procedureType     {@link ProcedureType} for the
	 *                          {@link WoofProcedureModel}.
	 * @param outputNameMapping Mapping of {@link ProcedureFlowType} name to
	 *                          existing {@link WoofProcedureOutputModel} name to
	 *                          allow maintaining links to other items within the
	 *                          {@link WoofModel}.
	 * @return {@link Change} to refactor the {@link WoofProcedureModel}.
	 */
	Change<WoofProcedureModel> refactorProcedure(WoofProcedureModel procedureModel, String procedureName,
			String resource, String sourceName, String procedure, PropertyList properties, ProcedureType procedureType,
			Map<String, String> outputNameMapping);

	/**
	 * Removes the {@link WoofProcedureModel}.
	 * 
	 * @param procedureModel {@link WoofProcedureModel} to remove.
	 * @return {@link Change} to remove the {@link WoofProcedureModel}.
	 */
	Change<WoofProcedureModel> removeProcedure(WoofProcedureModel procedureModel);

	/**
	 * Adds a {@link WoofSectionModel}.
	 * 
	 * @param sectionName            Name of the {@link WoofSectionModel}.
	 * @param sectionSourceClassName {@link SectionSource} class name.
	 * @param sectionLocation        Location of the section.
	 * @param properties             {@link PropertyList}.
	 * @param sectionType            {@link SectionType} for the
	 *                               {@link WoofSectionModel}.
	 * @return {@link Change} to add the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> addSection(String sectionName, String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType);

	/**
	 * Refactors the {@link WoofSectionModel}.
	 * 
	 * @param section                  {@link WoofSectionModel} to refactor.
	 * @param sectionName              New name of the {@link WoofSectionModel}.
	 * @param sectionSourceClassName   New {@link SectionSource} class name for the
	 *                                 {@link WoofSectionModel}.
	 * @param sectionLocation          New location for the
	 *                                 {@link WoofSectionModel}.
	 * @param properties               New {@link PropertyList} for the
	 *                                 {@link WoofSectionModel}.
	 * @param sectionType              {@link SectionType} of the refactor
	 *                                 {@link WoofSectionModel}.
	 * @param sectionInputNameMapping  Mapping of {@link SectionInputType} name to
	 *                                 existing {@link WoofSectionInputModel} name
	 *                                 to allow maintaining links to other items
	 *                                 within the {@link WoofModel}.
	 * @param sectionOutputNameMapping Mapping of {@link SectionOutputType} name to
	 *                                 existing {@link WoofSectionOutputModel} name
	 *                                 to allow maintaining links to other items
	 *                                 within the {@link WoofModel}.
	 * @return {@link Change} to refactor the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> refactorSection(WoofSectionModel section, String sectionName,
			String sectionSourceClassName, String sectionLocation, PropertyList properties, SectionType sectionType,
			Map<String, String> sectionInputNameMapping, Map<String, String> sectionOutputNameMapping);

	/**
	 * Removes the {@link WoofSectionModel}.
	 * 
	 * @param section {@link WoofSectionModel} to remove.
	 * @return {@link Change} to remove the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> removeSection(WoofSectionModel section);

	/**
	 * Adds a {@link WoofSecurityModel}.
	 * 
	 * @param httpSecurityName            Name of the {@link HttpSecurity}.
	 * @param httpSecuritySourceClassName {@link HttpSecuritySource} class name.
	 * @param timeout                     Time out in authenticating.
	 * @param properties                  {@link PropertyList}.
	 * @param contentTypes                Content types.
	 * @param httpSecurityType            {@link HttpSecurityType} for the
	 *                                    {@link WoofSecurityModel}.
	 * @return {@link Change} to specify the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> addSecurity(String httpSecurityName, String httpSecuritySourceClassName, long timeout,
			PropertyList properties, String[] contentTypes, HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType);

	/**
	 * Refactors the {@link WoofSecurityModel}.
	 * 
	 * @param security                    {@link WoofSecurityModel} to refactor.
	 * @param httpSecurityName            Name of the {@link HttpSecurity}.
	 * @param httpSecuritySourceClassName {@link HttpSecuritySource} class name.
	 * @param timeout                     Time out in authenticating.
	 * @param properties                  {@link PropertyList}.
	 * @param contentTypes                Content types.
	 * @param httpSecurityType            {@link HttpSecurityType} for the
	 *                                    {@link WoofSecurityModel}.
	 * @param securityOutputNameMapping   Mapping of {@link HttpSecurityFlowType}
	 *                                    name to existing
	 *                                    {@link WoofSecurityOutputModel} name to
	 *                                    allow maintaining links to other items
	 *                                    within the {@link WoofModel}.
	 * @return {@link Change} to refactor the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> refactorSecurity(WoofSecurityModel security, String httpSecurityName,
			String httpSecuritySourceClassName, long timeout, PropertyList properties, String[] contentTypes,
			HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType, Map<String, String> securityOutputNameMapping);

	/**
	 * Removes the {@link WoofSecurityModel}.
	 * 
	 * @param security {@link WoofSecurityModel} to remove.
	 * @return {@link Change} to remove the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> removeSecurity(WoofSecurityModel security);

	/**
	 * Adds a {@link WoofGovernanceModel}.
	 * 
	 * @param governanceName            Name of the {@link WoofGovernanceModel}.
	 * @param governanceSourceClassName {@link GovernanceSource} class name.
	 * @param properties                {@link PropertyList}.
	 * @param governanceType            {@link GovernanceType}.
	 * @return {@link Change} to add the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> addGovernance(String governanceName, String governanceSourceClassName,
			PropertyList properties, GovernanceType<?, ?> governanceType);

	/**
	 * Refactors the {@link WoofGovernanceModel}.
	 * 
	 * @param governance                {@link WoofGovernanceModel} to refactor.
	 * @param governanceName            New name of the {@link WoofGovernanceModel}.
	 * @param governanceSourceClassName New {@link GovernanceSource} class name for
	 *                                  the {@link WoofGovernanceModel}.
	 * @param properties                New {@link PropertyList} for the
	 *                                  {@link WoofGovernanceModel}.
	 * @param governanceType            {@link GovernanceType} of the refactored
	 *                                  {@link WoofGovernanceModel}.
	 * @return {@link Change} to refactor the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> refactorGovernance(WoofGovernanceModel governance, String governanceName,
			String governanceSourceClassName, PropertyList properties, GovernanceType<?, ?> governanceType);

	/**
	 * Removes the {@link WoofGovernanceModel}.
	 * 
	 * @param governance {@link WoofGovernanceModel} to remove.
	 * @return {@link Change} to remove the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> removeGovernance(WoofGovernanceModel governance);

	/**
	 * Adds a {@link WoofGovernanceAreaModel} for a {@link WoofGovernanceModel}.
	 * 
	 * @param governance {@link WoofGovernanceModel}.
	 * @param width      Width of {@link WoofGovernanceAreaModel}.
	 * @param height     Height of {@link WoofGovernanceAreaModel}.
	 * @return {@link Change} to add {@link WoofGovernanceAreaModel}.
	 */
	Change<WoofGovernanceAreaModel> addGovernanceArea(WoofGovernanceModel governance, int width, int height);

	/**
	 * Removes the {@link WoofGovernanceAreaModel}.
	 * 
	 * @param governanceArea {@link WoofGovernanceAreaModel}.
	 * @return {@link Change} to remove the {@link WoofGovernanceAreaModel}.
	 */
	Change<WoofGovernanceAreaModel> removeGovernanceArea(WoofGovernanceAreaModel governanceArea);

	/**
	 * Adds a {@link WoofResourceModel}.
	 * 
	 * @param resourcePath Path to the resource.
	 * @return {@link Change} to add the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> addResource(String resourcePath);

	/**
	 * Refactors the {@link WoofResourceModel}.
	 * 
	 * @param resource     {@link WoofResourceModel} to refactor.
	 * @param resourcePath New resource path.
	 * @return {@link Change} to refactor the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> refactorResource(WoofResourceModel resource, String resourcePath);

	/**
	 * Changes the resource path for the {@link WoofResourceModel}.
	 * 
	 * @param resource     {@link WoofResourceModel}.
	 * @param resourcePath Resource path.
	 * @return {@link Change} for the resource path.
	 */
	Change<WoofResourceModel> changeResourcePath(WoofResourceModel resource, String resourcePath);

	/**
	 * Removes the {@link WoofResourceModel}.
	 * 
	 * @param resource {@link WoofResourceModel} to remove.
	 * @return {@link Change} to remove the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> removeResource(WoofResourceModel resource);

	/**
	 * Adds a {@link WoofExceptionModel}.
	 * 
	 * @param exceptionClassName {@link Throwable} class name.
	 * @return {@link Change} to add the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> addException(String exceptionClassName);

	/**
	 * Refactors a {@link WoofExceptionModel}.
	 * 
	 * @param exception          {@link WoofExceptionModel} to refactor.
	 * @param exceptionClassName New {@link Exception} class name.
	 * @return {@link Change} to refactor the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> refactorException(WoofExceptionModel exception, String exceptionClassName);

	/**
	 * Removes the {@link WoofExceptionModel}.
	 * 
	 * @param exception {@link WoofExceptionModel} to remove.
	 * @return {@link Change} to remove the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> removeException(WoofExceptionModel exception);

	/**
	 * Adds a {@link WoofStartModel}.
	 * 
	 * @return {@link Change} to add the {@link WoofStartModel}.
	 */
	Change<WoofStartModel> addStart();

	/**
	 * Removes the {@link WoofStartModel}.
	 * 
	 * @param start {@link WoofStartModel} to remove.
	 * @return {@link Change} to remove the {@link WoofStartModel}.
	 */
	Change<WoofStartModel> removeStart(WoofStartModel start);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the
	 * {@link WoofHttpContinuationModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param httpRedirect     {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofHttpContinuationModel> linkHttpContinuationToHttpContinuation(
			WoofHttpContinuationModel httpContinuation, WoofHttpContinuationModel httpRedirect);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofHttpContinuationModel> removeHttpContinuationToHttpContinuation(
			WoofHttpContinuationToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the {@link WoofTemplateModel}
	 * .
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param template         {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofTemplateModel> linkHttpContinuationToTemplate(
			WoofHttpContinuationModel httpContinuation, WoofTemplateModel template);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofTemplateModel> removeHttpContinuationToTemplate(
			WoofHttpContinuationToWoofTemplateModel link);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param sectionInput     {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofSectionInputModel> linkHttpContinuationToSectionInput(
			WoofHttpContinuationModel httpContinuation, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofSectionInputModel> removeHttpContinuationToSectionInput(
			WoofHttpContinuationToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param security         {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofSecurityModel> linkHttpContinuationToSecurity(
			WoofHttpContinuationModel httpContinuation, WoofSecurityModel security);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofSecurityModel> removeHttpContinuationToSecurity(
			WoofHttpContinuationToWoofSecurityModel link);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the
	 * {@link WoofProcedureModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param procedure        {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofProcedureModel> linkHttpContinuationToProcedure(
			WoofHttpContinuationModel httpContinuation, WoofProcedureModel procedure);

	/**
	 * Links the {@link WoofHttpContinuationModel} to the {@link WoofResourceModel}.
	 * 
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @param resource         {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpContinuationToWoofResourceModel> linkHttpContinuationToResource(
			WoofHttpContinuationModel httpContinuation, WoofResourceModel resource);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofResourceModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofResourceModel> removeHttpContinuationToResource(
			WoofHttpContinuationToWoofResourceModel link);

	/**
	 * Removes the {@link WoofHttpContinuationToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofHttpContinuationToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpContinuationToWoofProcedureModel> removeHttpContinuationToProcedure(
			WoofHttpContinuationToWoofProcedureModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the
	 * {@link WoofHttpContinuationModel}.
	 * 
	 * @param httpInput       {@link WoofHttpInputModel}.
	 * @param applicationPath {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofHttpContinuationModel> linkHttpInputToHttpContinuation(WoofHttpInputModel httpInput,
			WoofHttpContinuationModel applicationPath);

	/**
	 * Removes the {@link WoofHttpInputToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofHttpContinuationModel> removeHttpInputToHttpContinuation(
			WoofHttpInputToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param httpInput {@link WoofHttpInputModel}.
	 * @param template  {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofTemplateModel> linkHttpInputToTemplate(WoofHttpInputModel httpInput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofHttpInputToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofTemplateModel> removeHttpInputToTemplate(WoofHttpInputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the {@link WoofSectionInputModel}.
	 * 
	 * @param httpInput    {@link WoofHttpInputModel}.
	 * @param sectionInput {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofSectionInputModel> linkHttpInputToSectionInput(WoofHttpInputModel httpInput,
			WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofHttpInputToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofSectionInputModel> removeHttpInputToSectionInput(
			WoofHttpInputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the {@link WoofSecurityModel} .
	 * 
	 * @param httpInput {@link WoofHttpInputModel}.
	 * @param security  {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofSecurityModel> linkHttpInputToSecurity(WoofHttpInputModel httpInput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofHttpInputToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofSecurityModel> removeHttpInputToSecurity(WoofHttpInputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the {@link WoofResourceModel} .
	 * 
	 * @param httpInput {@link WoofHttpInputModel}.
	 * @param resource  {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofResourceModel> linkHttpInputToResource(WoofHttpInputModel httpInput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofHttpInputToWoofResourceModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofResourceModel> removeHttpInputToResource(WoofHttpInputToWoofResourceModel link);

	/**
	 * Links the {@link WoofHttpInputModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param httpInput {@link WoofHttpInputModel}.
	 * @param procedure {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofHttpInputToWoofProcedureModel> linkHttpInputToProcedure(WoofHttpInputModel httpInput,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofHttpInputToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofHttpInputToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofHttpInputToWoofProcedureModel> removeHttpInputToProcedure(WoofHttpInputToWoofProcedureModel link);

	/**
	 * Link the {@link WoofTemplateModel} to its super {@link WoofTemplateModel}.
	 * 
	 * @param childTemplate Child {@link WoofTemplateModel}.
	 * @param superTemplate Super {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateToSuperWoofTemplateModel> linkTemplateToSuperTemplate(WoofTemplateModel childTemplate,
			WoofTemplateModel superTemplate);

	/**
	 * Removes the {@link WoofTemplateToSuperWoofTemplateModel}.
	 * 
	 * @param link {@link WoofTemplateToSuperWoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateToSuperWoofTemplateModel> removeTemplateToSuperTemplate(
			WoofTemplateToSuperWoofTemplateModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the {@link WoofTemplateModel}.
	 * 
	 * @param templateOutput {@link WoofTemplateOutputModel}.
	 * @param template       {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(WoofTemplateOutputModel templateOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofTemplateOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOutputToTemplate(
			WoofTemplateOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param templateOutput {@link WoofTemplateOutputModel}.
	 * @param sectionInput   {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			WoofTemplateOutputModel templateOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofTemplateOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOutputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param templateOutput {@link WoofTemplateOutputModel}.
	 * @param security       {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofSecurityModel> linkTemplateOutputToSecurity(WoofTemplateOutputModel templateOutput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofTemplateOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofSecurityModel> removeTemplateOutputToSecurity(
			WoofTemplateOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the {@link WoofResourceModel}.
	 * 
	 * @param templateOutput {@link WoofTemplateOutputModel}.
	 * @param resource       {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(WoofTemplateOutputModel templateOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofResourceModel}.
	 * 
	 * @param link {@link WoofTemplateOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOutputToResource(
			WoofTemplateOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param templateOutput {@link WoofTemplateOutputModel}.
	 * @param procedure      {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofProcedureModel> linkTemplateOutputToProcedure(WoofTemplateOutputModel templateOutput,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofTemplateOutputToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofProcedureModel> removeTemplateOutputToProcedure(
			WoofTemplateOutputToWoofProcedureModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param procedureNext {@link WoofProcedureNextModel}.
	 * @param template      {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofTemplateModel> linkProcedureNextToTemplate(WoofProcedureNextModel procedureNext,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofProcedureNextToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofTemplateModel> removeProcedureNextToTemplate(
			WoofProcedureNextToWoofTemplateModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param procedureNext {@link WoofProcedureNextModel}.
	 * @param sectionInput  {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofSectionInputModel> linkProcedureNextToSectionInput(
			WoofProcedureNextModel procedureNext, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofProcedureNextToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofSectionInputModel> removeProcedureNextToSectionInput(
			WoofProcedureNextToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param procedureNext {@link WoofProcedureNextModel}.
	 * @param security      {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofSecurityModel> linkProcedureNextToSecurity(WoofProcedureNextModel procedureNext,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofProcedureNextToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofSecurityModel> removeProcedureNextToSecurity(
			WoofProcedureNextToWoofSecurityModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the {@link WoofResourceModel} .
	 * 
	 * @param procedureNext {@link WoofProcedureNextModel}.
	 * @param resource      {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofResourceModel> linkProcedureNextToResource(WoofProcedureNextModel procedureNext,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofProcedureNextToWoofResourceModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofResourceModel> removeProcedureNextToResource(
			WoofProcedureNextToWoofResourceModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the
	 * {@link WoofHttpContinuationModel} .
	 * 
	 * @param procedureNext    {@link WoofProcedureNextModel}.
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofHttpContinuationModel> linkProcedureNextToHttpContinuation(
			WoofProcedureNextModel procedureNext, WoofHttpContinuationModel httpContinuation);

	/**
	 * Removes the {@link WoofProcedureNextToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofHttpContinuationModel> removeProcedureNextToHttpContinuation(
			WoofProcedureNextToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofProcedureNextModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param procedureNext {@link WoofProcedureNextModel}.
	 * @param procedure     {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureNextToWoofProcedureModel> linkProcedureNextToProcedure(WoofProcedureNextModel procedureNext,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofProcedureNextToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofProcedureNextToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureNextToWoofProcedureModel> removeProcedureNextToProcedure(
			WoofProcedureNextToWoofProcedureModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param procedureOutput {@link WoofProcedureOutputModel}.
	 * @param template        {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofTemplateModel> linkProcedureOutputToTemplate(
			WoofProcedureOutputModel procedureOutput, WoofTemplateModel template);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofTemplateModel> removeProcedureOutputToTemplate(
			WoofProcedureOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param procedureOutput {@link WoofProcedureOutputModel}.
	 * @param sectionInput    {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofSectionInputModel> linkProcedureOutputToSectionInput(
			WoofProcedureOutputModel procedureOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofSectionInputModel> removeProcedureOutputToSectionInput(
			WoofProcedureOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param procedureOutput {@link WoofProcedureOutputModel}.
	 * @param security        {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofSecurityModel> linkProcedureOutputToSecurity(
			WoofProcedureOutputModel procedureOutput, WoofSecurityModel security);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofSecurityModel> removeProcedureOutputToSecurity(
			WoofProcedureOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the {@link WoofResourceModel} .
	 * 
	 * @param procedureOutput {@link WoofProcedureOutputModel}.
	 * @param resource        {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofResourceModel> linkProcedureOutputToResource(
			WoofProcedureOutputModel procedureOutput, WoofResourceModel resource);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofResourceModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofResourceModel> removeProcedureOutputToResource(
			WoofProcedureOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the
	 * {@link WoofHttpContinuationModel} .
	 * 
	 * @param procedureOutput  {@link WoofProcedureOutputModel}.
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofHttpContinuationModel> linkProcedureOutputToHttpContinuation(
			WoofProcedureOutputModel procedureOutput, WoofHttpContinuationModel httpContinuation);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofHttpContinuationModel> removeProcedureOutputToHttpContinuation(
			WoofProcedureOutputToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofProcedureOutputModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param procedureOutput {@link WoofProcedureOutputModel}.
	 * @param procedure       {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofProcedureOutputToWoofProcedureModel> linkProcedureOutputToProcedure(
			WoofProcedureOutputModel procedureOutput, WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofProcedureOutputToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofProcedureOutputToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofProcedureOutputToWoofProcedureModel> removeProcedureOutputToProcedure(
			WoofProcedureOutputToWoofProcedureModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param sectionOutput {@link WoofSectionOutputModel}.
	 * @param template      {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(WoofSectionOutputModel sectionOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofSectionOutputToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofTemplateModel> removeSectionOutputToTemplate(
			WoofSectionOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param sectionOutput {@link WoofSectionOutputModel}.
	 * @param sectionInput  {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofSectionOutputToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOutputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param sectionOutput {@link WoofSectionOutputModel}.
	 * @param security      {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofSecurityModel> linkSectionOutputToSecurity(WoofSectionOutputModel sectionOutput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofSectionOutputToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofSecurityModel> removeSectionOutputToSecurity(
			WoofSectionOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofResourceModel} .
	 * 
	 * @param sectionOutput {@link WoofSectionOutputModel}.
	 * @param resource      {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(WoofSectionOutputModel sectionOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofSectionOutputToWoofResourceModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofResourceModel> removeSectionOutputToResource(
			WoofSectionOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the
	 * {@link WoofHttpContinuationModel} .
	 * 
	 * @param sectionOutput    {@link WoofSectionOutputModel}.
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofHttpContinuationModel> linkSectionOutputToHttpContinuation(
			WoofSectionOutputModel sectionOutput, WoofHttpContinuationModel httpContinuation);

	/**
	 * Removes the {@link WoofSectionOutputToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofHttpContinuationModel> removeSectionOutputToHttpContinuation(
			WoofSectionOutputToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param sectionOutput {@link WoofSectionOutputModel}.
	 * @param procedure     {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofProcedureModel> linkSectionOutputToProcedure(WoofSectionOutputModel sectionOutput,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofSectionOutputToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofSectionOutputToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofProcedureModel> removeSectionOutputToProcedure(
			WoofSectionOutputToWoofProcedureModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the {@link WoofTemplateModel}.
	 * 
	 * @param securityOutput {@link WoofSecurityOutputModel}.
	 * @param template       {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofTemplateModel> linkSecurityOutputToTemplate(WoofSecurityOutputModel securityOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofTemplateModel> removeSecurityOutputToTemplate(
			WoofSecurityOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param securityOutput {@link WoofSecurityOutputModel}.
	 * @param sectionInput   {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofSectionInputModel> linkSecurityOutputToSectionInput(
			WoofSecurityOutputModel securityOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofSectionInputModel> removeSecurityOutputToSectionInput(
			WoofSecurityOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the {@link WoofSecurityModel}.
	 * 
	 * @param securityOutput {@link WoofSecurityOutputModel}.
	 * @param security       {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofSecurityModel> linkSecurityOutputToSecurity(WoofSecurityOutputModel securityOutput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofSecurityModel> removeSecurityOutputToSecurity(
			WoofSecurityOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the {@link WoofResourceModel} .
	 * 
	 * @param securityOutput {@link WoofSecurityOutputModel}.
	 * @param resource       {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofResourceModel> linkSecurityOutputToResource(WoofSecurityOutputModel securityOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofResourceModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofResourceModel> removeSecurityOutputToResource(
			WoofSecurityOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the
	 * {@link WoofHttpContinuationModel}.
	 * 
	 * @param securityOutput   {@link WoofSecurityOutputModel}.
	 * @param httpContinuation {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofHttpContinuationModel> linkSecurityOutputToHttpContinuation(
			WoofSecurityOutputModel securityOutput, WoofHttpContinuationModel httpContinuation);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofHttpContinuationModel> removeSecurityOutputToHttpContinuation(
			WoofSecurityOutputToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param securityOutput {@link WoofSectionOutputModel}.
	 * @param procedure      {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofProcedureModel> linkSecurityOutputToProcedure(WoofSecurityOutputModel securityOutput,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofSecurityOutputToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofProcedureModel> removeSecurityOutputToProcedure(
			WoofSecurityOutputToWoofProcedureModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the
	 * {@link WoofHttpContinuationModel}.
	 * 
	 * @param exception       {@link WoofExceptionModel}.
	 * @param applicationPath {@link WoofHttpContinuationModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofHttpContinuationModel> linkExceptionToHttpContinuation(WoofExceptionModel exception,
			WoofHttpContinuationModel applicationPath);

	/**
	 * Removes the {@link WoofExceptionToWoofHttpContinuationModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofHttpContinuationModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofHttpContinuationModel> removeExceptionToHttpContinuation(
			WoofExceptionToWoofHttpContinuationModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param exception {@link WoofExceptionModel}.
	 * @param template  {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(WoofExceptionModel exception,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofExceptionToWoofTemplateModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(WoofExceptionToWoofTemplateModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofSectionInputModel} .
	 * 
	 * @param exception    {@link WoofExceptionModel}.
	 * @param sectionInput {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(WoofExceptionModel exception,
			WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofExceptionToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofSecurityModel} .
	 * 
	 * @param exception {@link WoofExceptionModel}.
	 * @param security  {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofSecurityModel> linkExceptionToSecurity(WoofExceptionModel exception,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofExceptionToWoofSecurityModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofSecurityModel> removeExceptionToSecurity(WoofExceptionToWoofSecurityModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofResourceModel} .
	 * 
	 * @param exception {@link WoofExceptionModel}.
	 * @param resource  {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(WoofExceptionModel exception,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofExceptionToWoofResourceModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(WoofExceptionToWoofResourceModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param exception {@link WoofExceptionModel}.
	 * @param procedure {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofProcedureModel> linkExceptionToProcedure(WoofExceptionModel exception,
			WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofExceptionToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofExceptionToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofProcedureModel> removeExceptionToProcedure(WoofExceptionToWoofProcedureModel link);

	/**
	 * Links the {@link WoofStartModel} to the {@link WoofSectionInputModel}.
	 * 
	 * @param start        {@link WoofStartModel}.
	 * @param sectionInput {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofStartToWoofSectionInputModel> linkStartToSectionInput(WoofStartModel start,
			WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofStartToWoofSectionInputModel}.
	 * 
	 * @param link {@link WoofStartToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofStartToWoofSectionInputModel> removeStartToSectionInput(WoofStartToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofStartModel} to the {@link WoofProcedureModel}.
	 * 
	 * @param start     {@link WoofStartModel}.
	 * @param procedure {@link WoofProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofStartToWoofProcedureModel> linkStartToProcedure(WoofStartModel start, WoofProcedureModel procedure);

	/**
	 * Removes the {@link WoofStartToWoofProcedureModel}.
	 * 
	 * @param link {@link WoofStartToWoofProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofStartToWoofProcedureModel> removeStartToProcedure(WoofStartToWoofProcedureModel link);

}
