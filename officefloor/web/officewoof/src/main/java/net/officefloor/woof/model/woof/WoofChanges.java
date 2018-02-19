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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

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
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateToSuperWoofTemplateModel;

/**
 * Changes that can be made to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofChanges {

	/**
	 * Obtains the {@link WoofTemplateInheritance} instances by their super
	 * {@link WoofTemplateModel} application path.
	 * 
	 * @return {@link WoofTemplateInheritance} instances by their super
	 *         {@link WoofTemplateModel} application path.
	 */
	Map<String, WoofTemplateInheritance> getWoofTemplateInheritances();

	/**
	 * Adds a {@link WoofApplicationPathModel}.
	 * 
	 * @param applicationPath
	 *            URI to the {@link WoofApplicationPathModel}.
	 * @param isSecure
	 *            <code>true</code> to require a secure
	 *            {@link ServerHttpConnection}.
	 * @param serviceHttpMethods
	 *            Names of the {@link HttpMethod} instances for this
	 *            {@link WoofApplicationPathModel}.
	 * @return {@link Change} to add a {@link WoofApplicationPathModel}.
	 */
	Change<WoofApplicationPathModel> addApplicationPath(String applicationPath, boolean isSecure,
			String[] serviceHttpMethods);

	/**
	 * Removes a {@link WoofApplicationPathModel}.
	 * 
	 * @param applicationPath
	 *            {@link WoofApplicationPathModel} to remove.
	 * @return {@link Change} to remove the {@link WoofApplicationPathModel}.
	 */
	Change<WoofApplicationPathModel> removeApplicationPath(WoofApplicationPathModel applicationPath);

	/**
	 * Adds a {@link WoofTemplateModel}.
	 * 
	 * @param applicationPath
	 *            URI to the {@link WoofTemplateModel}.
	 * @param templateLocation
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Name of the logic {@link Class} for the template.
	 * @param sectionType
	 *            {@link SectionType} for the {@link WoofTemplateModel}.
	 * @param redirectValuesFunction
	 *            Render redirect {@link ManagedFunction} name.
	 * @param contentType
	 *            Content-Type for the {@link WoofTemplateModel}. May be
	 *            <code>null</code>.
	 * @param charsetName
	 *            Name of {@link Charset} for the {@link WoofTemplateModel}.
	 * @param isTemplateSecure
	 *            <code>true</code> for the {@link WoofTemplateModel} to require
	 *            a secure {@link ServerHttpConnection}.
	 * @param linkSeparatorCharacter
	 *            Link separator {@link Character}.
	 * @param linksSecure
	 *            Link secure configuration overriding {@link WoofTemplateModel}
	 *            secure.
	 * @param renderHttpMethods
	 *            Listing of HTTP methods that are to render the
	 *            {@link WoofTemplateModel}.
	 * @param extensions
	 *            {@link WoofTemplateExtension} instances for the
	 *            {@link WoofTemplateModel}.
	 * @param context
	 *            {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to add a {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> addTemplate(String applicationPath, String templateLocation, String templateLogicClass,
			SectionType sectionType, String redirectValuesFunction, String contentType, String charsetName,
			boolean isTemplateSecure, String linkSeparatorCharacter, Map<String, Boolean> linksSecure,
			String[] renderHttpMethods, WoofTemplateExtension[] extensions, WoofTemplateChangeContext context);

	/**
	 * Refactors the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel} to refactor.
	 * @param applicationPath
	 *            New application path for the {@link WoofTemplateModel}.
	 * @param templateLocation
	 *            New template location for the {@link WoofTemplateModel}.
	 * @param templateLogicClass
	 *            New logic class for the {@link WoofTemplateModel}.
	 * @param sectionType
	 *            {@link SectionType} for the refactored
	 *            {@link WoofTemplateModel}.
	 * @param redirectValuesFunction
	 *            New render redirect {@link ManagedFunction} name.
	 * @param inheritedTemplateOutputNames
	 *            Inherited {@link WoofTemplateOutputModel} configuration from
	 *            the super {@link WoofTemplateModel} and its subsequent
	 *            ancestors.
	 * @param contentType
	 *            Content-Type for the {@link WoofTemplateModel}. May be
	 *            <code>null</code>.
	 * @param charsetName
	 *            Name of {@link Charset} for the {@link WoofTemplateModel}. May
	 *            be <code>null</code>.
	 * @param isTemplateSecure
	 *            <code>true</code> for the {@link WoofTemplateModel} to require
	 *            a secure {@link ServerHttpConnection}.
	 * @param linkSeparatorCharacter
	 *            New link separator {@link Character}.
	 * @param linksSecure
	 *            Link secure configuration overriding {@link WoofTemplateModel}
	 *            secure.
	 * @param renderHttpMethods
	 *            Listing of HTTP methods that render the
	 *            {@link WoofTemplateModel}.
	 * @param extensions
	 *            {@link WoofTemplateExtension} instances for the
	 *            {@link WoofTemplateModel}.
	 * @param templateOutputNameMapping
	 *            Mapping of {@link SectionOutputType} name to existing
	 *            {@link WoofTemplateOutputModel} name to allow maintaining
	 *            links to other items within the {@link WoofModel}.
	 * @param context
	 *            {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to refactor the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> refactorTemplate(WoofTemplateModel template, String applicationPath,
			String templateLocation, String templateLogicClass, SectionType sectionType, String redirectValuesFunction,
			Set<String> inheritedTemplateOutputNames, String contentType, String charsetName, boolean isTemplateSecure,
			String linkSeparatorCharacter, Map<String, Boolean> linksSecure, String[] renderHttpMethods,
			WoofTemplateExtension[] extensions, Map<String, String> templateOutputNameMapping,
			WoofTemplateChangeContext context);

	/**
	 * Changes the application path for the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @param applicationPath
	 *            Application path.
	 * @param context
	 *            {@link WoofTemplateChangeContext}.
	 * @return {@link Change} for the URI.
	 */
	Change<WoofTemplateModel> changeTemplateApplicationPath(WoofTemplateModel template, String applicationPath,
			WoofTemplateChangeContext context);

	/**
	 * Removes the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel} to remove.
	 * @param context
	 *            {@link WoofTemplateChangeContext}.
	 * @return {@link Change} to remove the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> removeTemplate(WoofTemplateModel template, WoofTemplateChangeContext context);

	/**
	 * Adds a {@link WoofSectionModel}.
	 * 
	 * @param sectionName
	 *            Name of the {@link WoofSectionModel}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the section.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param sectionType
	 *            {@link SectionType} for the {@link WoofSectionModel}.
	 * @return {@link Change} to add the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> addSection(String sectionName, String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType);

	/**
	 * Refactors the {@link WoofSectionModel}.
	 * 
	 * @param section
	 *            {@link WoofSectionModel} to refactor.
	 * @param sectionName
	 *            New name of the {@link WoofSectionModel}.
	 * @param sectionSourceClassName
	 *            New {@link SectionSource} class name for the
	 *            {@link WoofSectionModel}.
	 * @param sectionLocation
	 *            New location for the {@link WoofSectionModel}.
	 * @param properties
	 *            New {@link PropertyList} for the {@link WoofSectionModel}.
	 * @param sectionType
	 *            {@link SectionType} of the refactor {@link WoofSectionModel}.
	 * @param sectionInputNameMapping
	 *            Mapping of {@link SectionInputType} name to existing
	 *            {@link WoofSectionInputModel} name to allow maintaining links
	 *            to other items within the {@link WoofModel}.
	 * @param sectionOutputNameMapping
	 *            Mapping of {@link SectionOutputType} name to existing
	 *            {@link WoofSectionOutputModel} name to allow maintaining links
	 *            to other items within the {@link WoofModel}.
	 * @return {@link Change} to refactor the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> refactorSection(WoofSectionModel section, String sectionName,
			String sectionSourceClassName, String sectionLocation, PropertyList properties, SectionType sectionType,
			Map<String, String> sectionInputNameMapping, Map<String, String> sectionOutputNameMapping);

	/**
	 * Removes the {@link WoofSectionModel}.
	 * 
	 * @param section
	 *            {@link WoofSectionModel} to remove.
	 * @return {@link Change} to remove the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> removeSection(WoofSectionModel section);

	/**
	 * Adds a {@link WoofSecurityModel}.
	 * 
	 * @param httpSecurityName
	 *            Name of the {@link HttpSecurity}.
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param timeout
	 *            Time out in authenticating.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param contentTypes
	 *            Content types.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType} for the {@link WoofSecurityModel}.
	 * @return {@link Change} to specify the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> addSecurity(String httpSecurityName, String httpSecuritySourceClassName, long timeout,
			PropertyList properties, String[] contentTypes, HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType);

	/**
	 * Refactors the {@link WoofSecurityModel}.
	 * 
	 * @param security
	 *            {@link WoofSecurityModel} to refactor.
	 * @param httpSecurityName
	 *            Name of the {@link HttpSecurity}.
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param timeout
	 *            Time out in authenticating.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType} for the {@link WoofSecurityModel}.
	 * @param securityOutputNameMapping
	 *            Mapping of {@link HttpSecurityFlowType} name to existing
	 *            {@link WoofSecurityOutputModel} name to allow maintaining
	 *            links to other items within the {@link WoofModel}.
	 * @return {@link Change} to refactor the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> refactorSecurity(WoofSecurityModel security, String httpSecurityName,
			String httpSecuritySourceClassName, long timeout, PropertyList properties,
			HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType, Map<String, String> securityOutputNameMapping);

	/**
	 * Removes the {@link WoofSecurityModel}.
	 * 
	 * @param security
	 *            {@link WoofSecurityModel} to remove.
	 * @return {@link Change} to remove the {@link WoofSecurityModel}.
	 */
	Change<WoofSecurityModel> removeSecurity(WoofSecurityModel security);

	/**
	 * Adds a {@link WoofGovernanceModel}.
	 * 
	 * @param governanceName
	 *            Name of the {@link WoofGovernanceModel}.
	 * @param governanceSourceClassName
	 *            {@link GovernanceSource} class name.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param governanceType
	 *            {@link GovernanceType}.
	 * @return {@link Change} to add the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> addGovernance(String governanceName, String governanceSourceClassName,
			PropertyList properties, GovernanceType<?, ?> governanceType);

	/**
	 * Refactors the {@link WoofGovernanceModel}.
	 * 
	 * @param governance
	 *            {@link WoofGovernanceModel} to refactor.
	 * @param governanceName
	 *            New name of the {@link WoofGovernanceModel}.
	 * @param governanceSourceClassName
	 *            New {@link GovernanceSource} class name for the
	 *            {@link WoofGovernanceModel}.
	 * @param properties
	 *            New {@link PropertyList} for the {@link WoofGovernanceModel}.
	 * @param governanceType
	 *            {@link GovernanceType} of the refactored
	 *            {@link WoofGovernanceModel}.
	 * @return {@link Change} to refactor the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> refactorGovernance(WoofGovernanceModel governance, String governanceName,
			String governanceSourceClassName, PropertyList properties, GovernanceType<?, ?> governanceType);

	/**
	 * Removes the {@link WoofGovernanceModel}.
	 * 
	 * @param governance
	 *            {@link WoofGovernanceModel} to remove.
	 * @return {@link Change} to remove the {@link WoofGovernanceModel}.
	 */
	Change<WoofGovernanceModel> removeGovernance(WoofGovernanceModel governance);

	/**
	 * Adds a {@link WoofGovernanceAreaModel} for a {@link WoofGovernanceModel}.
	 * 
	 * @param governance
	 *            {@link WoofGovernanceModel}.
	 * @param width
	 *            Width of {@link WoofGovernanceAreaModel}.
	 * @param height
	 *            Height of {@link WoofGovernanceAreaModel}.
	 * @return {@link Change} to add {@link WoofGovernanceAreaModel}.
	 */
	Change<WoofGovernanceAreaModel> addGovernanceArea(WoofGovernanceModel governance, int width, int height);

	/**
	 * Removes the {@link WoofGovernanceAreaModel}.
	 * 
	 * @param governanceArea
	 *            {@link WoofGovernanceAreaModel}.
	 * @return {@link Change} to remove the {@link WoofGovernanceAreaModel}.
	 */
	Change<WoofGovernanceAreaModel> removeGovernanceArea(WoofGovernanceAreaModel governanceArea);

	/**
	 * Adds a {@link WoofResourceModel}.
	 * 
	 * @param resourcePath
	 *            Path to the resource.
	 * @return {@link Change} to add the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> addResource(String resourcePath);

	/**
	 * Refactors the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel} to refactor.
	 * @param resourcePath
	 *            New resource path.
	 * @return {@link Change} to refactor the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> refactorResource(WoofResourceModel resource, String resourcePath);

	/**
	 * Changes the resource path for the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel}.
	 * @param resourcePath
	 *            Resource path.
	 * @return {@link Change} for the resource path.
	 */
	Change<WoofResourceModel> changeResourcePath(WoofResourceModel resource, String resourcePath);

	/**
	 * Removes the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel} to remove.
	 * @return {@link Change} to remove the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> removeResource(WoofResourceModel resource);

	/**
	 * Adds a {@link WoofExceptionModel}.
	 * 
	 * @param exceptionClassName
	 *            {@link Throwable} class name.
	 * @return {@link Change} to add the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> addException(String exceptionClassName);

	/**
	 * Refactors a {@link WoofExceptionModel}.
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel} to refactor.
	 * @param exceptionClassName
	 *            New {@link Exception} class name.
	 * @return {@link Change} to refactor the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> refactorException(WoofExceptionModel exception, String exceptionClassName);

	/**
	 * Removes the {@link WoofExceptionModel}.
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel} to remove.
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
	 * @param start
	 *            {@link WoofStartModel} to remove.
	 * @return {@link Change} to remove the {@link WoofStartModel}.
	 */
	Change<WoofStartModel> removeStart(WoofStartModel start);

	/**
	 * Link the {@link WoofTemplateModel} to its super
	 * {@link WoofTemplateModel}.
	 * 
	 * @param childTemplate
	 *            Child {@link WoofTemplateModel}.
	 * @param superTemplate
	 *            Super {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateToSuperWoofTemplateModel> linkTemplateToSuperTemplate(WoofTemplateModel childTemplate,
			WoofTemplateModel superTemplate);

	/**
	 * Removes the {@link WoofTemplateToSuperWoofTemplateModel}.
	 * 
	 * @param link
	 *            {@link WoofTemplateToSuperWoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateToSuperWoofTemplateModel> removeTemplateToSuperTemplate(
			WoofTemplateToSuperWoofTemplateModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param templateOutput
	 *            {@link WoofTemplateOutputModel}.
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(WoofTemplateOutputModel templateOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofTemplateModel}.
	 * 
	 * @param link
	 *            {@link WoofTemplateOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOuputToTemplate(
			WoofTemplateOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param templateOutput
	 *            {@link WoofTemplateOutputModel}.
	 * @param sectionInput
	 *            {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			WoofTemplateOutputModel templateOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofSectionInputModel}.
	 * 
	 * @param link
	 *            {@link WoofTemplateOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOuputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the
	 * {@link WoofSecurityModel}.
	 * 
	 * @param templateOutput
	 *            {@link WoofTemplateOutputModel}.
	 * @param security
	 *            {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofSecurityModel> linkTemplateOutputToSecurity(WoofTemplateOutputModel templateOutput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofSecurityModel}.
	 * 
	 * @param link
	 *            {@link WoofTemplateOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofSecurityModel> removeTemplateOuputToSecurity(
			WoofTemplateOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofTemplateOutputModel} to the
	 * {@link WoofResourceModel}.
	 * 
	 * @param templateOutput
	 *            {@link WoofTemplateOutputModel}.
	 * @param resource
	 *            {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(WoofTemplateOutputModel templateOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofTemplateOutputToWoofResourceModel}.
	 * 
	 * @param link
	 *            {@link WoofTemplateOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOuputToResource(
			WoofTemplateOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofTemplateModel}
	 * .
	 * 
	 * @param sectionOutput
	 *            {@link WoofSectionOutputModel}.
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(WoofSectionOutputModel sectionOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofSectionOutputToWoofTemplateModel}.
	 * 
	 * @param link
	 *            {@link WoofSectionOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofTemplateModel> removeSectionOuputToTemplate(
			WoofSectionOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param sectionOutput
	 *            {@link WoofSectionOutputModel}.
	 * @param sectionInput
	 *            {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofSectionOutputToWoofSectionInputModel}.
	 * 
	 * @param link
	 *            {@link WoofSectionOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOuputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the
	 * {@link WoofSecurityModel}.
	 * 
	 * @param sectionOutput
	 *            {@link WoofAccessOutputModel}.
	 * @param security
	 *            {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofSecurityModel> linkSectionOutputToSecurity(WoofSectionOutputModel sectionOutput,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofSectionOutputToWoofSecurityModel}.
	 * 
	 * @param link
	 *            {@link WoofSectionOutputToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofSecurityModel> removeSectionOuputToSecurity(
			WoofSectionOutputToWoofSecurityModel link);

	/**
	 * Links the {@link WoofSectionOutputModel} to the {@link WoofResourceModel}
	 * .
	 * 
	 * @param sectionOutput
	 *            {@link WoofSectionOutputModel}.
	 * @param resource
	 *            {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(WoofSectionOutputModel sectionOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofSectionOutputToWoofResourceModel}.
	 * 
	 * @param link
	 *            {@link WoofSectionOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSectionOutputToWoofResourceModel> removeSectionOuputToResource(
			WoofSectionOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param securityOutput
	 *            {@link WoofSecurityOutputModel}.
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofTemplateModel> linkSecurityOutputToTemplate(WoofSecurityOutputModel securityOutput,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofTemplateModel}.
	 * 
	 * @param link
	 *            {@link WoofSecurityOutputToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofTemplateModel> removeSecurityOuputToTemplate(
			WoofSecurityOutputToWoofTemplateModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param securityOutput
	 *            {@link WoofSecurityOutputModel}.
	 * @param sectionInput
	 *            {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofSectionInputModel> linkSecurityOutputToSectionInput(
			WoofSecurityOutputModel securityOutput, WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofSectionInputModel}.
	 * 
	 * @param link
	 *            {@link WoofSecurityOutputToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofSectionInputModel> removeSecurityOuputToSectionInput(
			WoofSecurityOutputToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofSecurityOutputModel} to the
	 * {@link WoofResourceModel} .
	 * 
	 * @param securityOutput
	 *            {@link WoofSecurityOutputModel}.
	 * @param resource
	 *            {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofSecurityOutputToWoofResourceModel> linkSecurityOutputToResource(WoofSecurityOutputModel securityOutput,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofSecurityOutputToWoofResourceModel}.
	 * 
	 * @param link
	 *            {@link WoofSecurityOutputToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofSecurityOutputToWoofResourceModel> removeSecurityOuputToResource(
			WoofSecurityOutputToWoofResourceModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the
	 * {@link WoofApplicationPathModel}.
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel}.
	 * @param applicationPath
	 *            {@link WoofApplicationPathModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofApplicationPathModel> linkExceptionToApplicationPath(WoofExceptionModel exception,
			WoofApplicationPathModel applicationPath);

	/**
	 * Removes the {@link WoofExceptionToWoofApplicationPathModel}.
	 * 
	 * @param link
	 *            {@link WoofExceptionToWoofApplicationPathModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofApplicationPathModel> removeExceptionToApplicationPath(
			WoofExceptionToWoofApplicationPathModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofTemplateModel} .
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel}.
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(WoofExceptionModel exception,
			WoofTemplateModel template);

	/**
	 * Removes the {@link WoofExceptionToWoofTemplateModel}.
	 * 
	 * @param link
	 *            {@link WoofExceptionToWoofTemplateModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(WoofExceptionToWoofTemplateModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofSectionInputModel}
	 * .
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel}.
	 * @param sectionInput
	 *            {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(WoofExceptionModel exception,
			WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofExceptionToWoofSectionInputModel}.
	 * 
	 * @param link
	 *            {@link WoofExceptionToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofSecurityModel} .
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel}.
	 * @param security
	 *            {@link WoofSecurityModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofSecurityModel> linkExceptionToSecurity(WoofExceptionModel exception,
			WoofSecurityModel security);

	/**
	 * Removes the {@link WoofExceptionToWoofSecurityModel}.
	 * 
	 * @param link
	 *            {@link WoofExceptionToWoofSecurityModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofSecurityModel> removeExceptionToSecurity(WoofExceptionToWoofSecurityModel link);

	/**
	 * Links the {@link WoofExceptionModel} to the {@link WoofResourceModel} .
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel}.
	 * @param resource
	 *            {@link WoofResourceModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(WoofExceptionModel exception,
			WoofResourceModel resource);

	/**
	 * Removes the {@link WoofExceptionToWoofResourceModel}.
	 * 
	 * @param link
	 *            {@link WoofExceptionToWoofResourceModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(WoofExceptionToWoofResourceModel link);

	/**
	 * Links the {@link WoofStartModel} to the {@link WoofSectionInputModel}.
	 * 
	 * @param start
	 *            {@link WoofStartModel}.
	 * @param sectionInput
	 *            {@link WoofSectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<WoofStartToWoofSectionInputModel> linkStartToSectionInput(WoofStartModel start,
			WoofSectionInputModel sectionInput);

	/**
	 * Removes the {@link WoofStartToWoofSectionInputModel}.
	 * 
	 * @param link
	 *            {@link WoofStartToWoofSectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<WoofStartToWoofSectionInputModel> removeStartToSectionInput(WoofStartToWoofSectionInputModel link);

}