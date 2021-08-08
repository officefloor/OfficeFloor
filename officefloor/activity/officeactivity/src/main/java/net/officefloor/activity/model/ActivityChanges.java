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

import java.util.Map;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link ActivityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityChanges {

	/**
	 * Adds an {@link ActivityProcedureModel}.
	 * 
	 * @param procedureName Name of the {@link ActivityProcedureModel}.
	 * @param resource      Resource.
	 * @param sourceName    Source name.
	 * @param procedure     {@link Procedure} name.
	 * @param properties    {@link PropertyList}
	 * @param procedureType {@link ProcedureType} for the
	 *                      {@link ActivityProcedureModel}.
	 * @return {@link Change} to add the {@link ActivityProcedureModel}.
	 */
	Change<ActivityProcedureModel> addProcedure(String procedureName, String resource, String sourceName,
			String procedure, PropertyList properties, ProcedureType procedureType);

	/**
	 * Refactors an {@link ActivityProcedureModel}.
	 * 
	 * @param procedureModel    {@link ActivityProcedureModel} to refactor.
	 * @param procedureName     Name of the {@link ActivityProcedureModel}.
	 * @param resource          Resource.
	 * @param sourceName        Source name.
	 * @param procedure         {@link Procedure} name.
	 * @param properties        {@link PropertyList}.
	 * @param procedureType     {@link ProcedureType} for the
	 *                          {@link ActivityProcedureModel}.
	 * @param outputNameMapping Mapping of {@link ProcedureFlowType} name to
	 *                          existing {@link ActivityProcedureOutputModel} name
	 *                          to allow maintaining links to other items within the
	 *                          {@link ActivityModel}.
	 * @return {@link Change} to refactor the {@link ActivityProcedureModel}.
	 */
	Change<ActivityProcedureModel> refactorProcedure(ActivityProcedureModel procedureModel, String procedureName,
			String resource, String sourceName, String procedure, PropertyList properties, ProcedureType procedureType,
			Map<String, String> outputNameMapping);

	/**
	 * Removes an {@link ActivityProcedureModel}.
	 * 
	 * @param procedureModel {@link ActivityProcedureModel} to remove.
	 * @return {@link Change} to remove the {@link ActivityProcedureModel}.
	 */
	Change<ActivityProcedureModel> removeProcedure(ActivityProcedureModel procedureModel);

	/**
	 * Adds an {@link ActivitySectionModel}.
	 * 
	 * @param sectionName            Name of the {@link ActivitySectionModel}.
	 * @param sectionSourceClassName {@link SectionSource} class name.
	 * @param sectionLocation        Location of the section.
	 * @param properties             {@link PropertyList}.
	 * @param sectionType            {@link SectionType} for the
	 *                               {@link ActivitySectionModel}.
	 * @return {@link Change} to add the {@link ActivitySectionModel}.
	 */
	Change<ActivitySectionModel> addSection(String sectionName, String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType);

	/**
	 * Refactors an {@link ActivitySectionModel}.
	 * 
	 * @param section                  {@link ActivitySectionModel} to refactor.
	 * @param sectionName              New name of the {@link ActivitySectionModel}.
	 * @param sectionSourceClassName   New {@link SectionSource} class name for the
	 *                                 {@link ActivitySectionModel}.
	 * @param sectionLocation          New location for the
	 *                                 {@link ActivitySectionModel}.
	 * @param properties               New {@link PropertyList} for the
	 *                                 {@link ActivitySectionModel}.
	 * @param sectionType              {@link SectionType} of the refactor
	 *                                 {@link ActivitySectionModel}.
	 * @param sectionInputNameMapping  Mapping of {@link SectionInputType} name to
	 *                                 existing {@link ActivitySectionInputModel}
	 *                                 name to allow maintaining links to other
	 *                                 items within the {@link ActivityModel}.
	 * @param sectionOutputNameMapping Mapping of {@link SectionOutputType} name to
	 *                                 existing {@link ActivitySectionOutputModel}
	 *                                 name to allow maintaining links to other
	 *                                 items within the {@link ActivityModel}.
	 * @return {@link Change} to refactor the {@link ActivitySectionModel}.
	 */
	Change<ActivitySectionModel> refactorSection(ActivitySectionModel section, String sectionName,
			String sectionSourceClassName, String sectionLocation, PropertyList properties, SectionType sectionType,
			Map<String, String> sectionInputNameMapping, Map<String, String> sectionOutputNameMapping);

	/**
	 * Removes an {@link ActivitySectionModel}.
	 * 
	 * @param section {@link ActivitySectionModel} to remove.
	 * @return {@link Change} to remove the {@link ActivitySectionModel}.
	 */
	Change<ActivitySectionModel> removeSection(ActivitySectionModel section);

	/**
	 * Adds an {@link ActivityExceptionModel}.
	 * 
	 * @param exceptionClassName {@link Throwable} class name.
	 * @return {@link Change} to add the {@link ActivityExceptionModel}.
	 */
	Change<ActivityExceptionModel> addException(String exceptionClassName);

	/**
	 * Refactors an {@link ActivityExceptionModel}.
	 * 
	 * @param exception          {@link ActivityExceptionModel} to refactor.
	 * @param exceptionClassName New {@link Exception} class name.
	 * @return {@link Change} to refactor the {@link ActivityExceptionModel}.
	 */
	Change<ActivityExceptionModel> refactorException(ActivityExceptionModel exception, String exceptionClassName);

	/**
	 * Removes an {@link ActivityExceptionModel}.
	 * 
	 * @param exception {@link ActivityExceptionModel} to remove.
	 * @return {@link Change} to remove the {@link ActivityExceptionModel}.
	 */
	Change<ActivityExceptionModel> removeException(ActivityExceptionModel exception);

	/**
	 * Add an {@link ActivityInputModel}.
	 * 
	 * @param inputName    Name of {@link ActivityInputModel}.
	 * @param argumentType Type of argument expected from
	 *                     {@link ActivityInputModel}.
	 * @return {@link Change} to add {@link ActivityInputModel}.
	 */
	Change<ActivityInputModel> addInput(String inputName, String argumentType);

	/**
	 * Refactors an {@link ActivityInputModel}.
	 * 
	 * @param input        {@link ActivityInputModel} to refactor.
	 * @param inputName    Name of {@link ActivityInputModel}.
	 * @param argumentType Type of argument expected from
	 *                     {@link ActivityInputModel}.
	 * @return {@link Change} to refactor {@link ActivityInputModel}.
	 */
	Change<ActivityInputModel> refactorInput(ActivityInputModel input, String inputName, String argumentType);

	/**
	 * Removes an {@link ActivityInputModel}.
	 * 
	 * @param input {@link ActivityInputModel} to remove.
	 * @return {@link Change} to remove the {@link ActivityInputModel}.
	 */
	Change<ActivityInputModel> removeInput(ActivityInputModel input);

	/**
	 * Add an {@link ActivityOutputModel}.
	 * 
	 * @param outputName    Name of {@link ActivityOutputModel}.
	 * @param parameterType Parameter type to {@link ActivityOutputModel}.
	 * @return {@link Change} to add {@link ActivityOutputModel}.
	 */
	Change<ActivityOutputModel> addOutput(String outputName, String parameterType);

	/**
	 * Refactors an {@link ActivityOutputModel}.
	 * 
	 * @param output        {@link ActivityOutputModel} to refactor.
	 * @param outputName    Name of {@link ActivityOutputModel}.
	 * @param parameterType Parameter type to {@link ActivityOutputModel}.
	 * @return {@link Change} to refactor {@link ActivityOutputModel}.
	 */
	Change<ActivityOutputModel> refactorOutput(ActivityOutputModel output, String outputName, String parameterType);

	/**
	 * Removes an {@link ActivityOutputModel}.
	 * 
	 * @param output {@link ActivityOutputModel} to remove.
	 * @return {@link Change} to remove the {@link ActivityOutputModel}.
	 */
	Change<ActivityOutputModel> removeOutput(ActivityOutputModel output);

	/**
	 * Links the {@link ActivityProcedureNextModel} to the
	 * {@link ActivitySectionInputModel}.
	 * 
	 * @param procedureNext {@link ActivityProcedureNextModel}.
	 * @param sectionInput  {@link ActivitySectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureNextToActivitySectionInputModel> linkProcedureNextToSectionInput(
			ActivityProcedureNextModel procedureNext, ActivitySectionInputModel sectionInput);

	/**
	 * Removes the {@link ActivityProcedureNextToActivitySectionInputModel}.
	 * 
	 * @param link {@link ActivityProcedureNextToActivitySectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureNextToActivitySectionInputModel> removeProcedureNextToSectionInput(
			ActivityProcedureNextToActivitySectionInputModel link);

	/**
	 * Links the {@link ActivityProcedureNextModel} to the
	 * {@link ActivityProcedureModel}.
	 * 
	 * @param procedureNext {@link ActivityProcedureNextModel}.
	 * @param procedure     {@link ActivityProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureNextToActivityProcedureModel> linkProcedureNextToProcedure(
			ActivityProcedureNextModel procedureNext, ActivityProcedureModel procedure);

	/**
	 * Removes the {@link ActivityProcedureNextToActivityProcedureModel}.
	 * 
	 * @param link {@link ActivityProcedureNextToActivityProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureNextToActivityProcedureModel> removeProcedureNextToProcedure(
			ActivityProcedureNextToActivityProcedureModel link);

	/**
	 * Links the {@link ActivityProcedureNextModel} to the
	 * {@link ActivityOutputModel}.
	 * 
	 * @param procedureNext {@link ActivityProcedureNextModel}.
	 * @param output        {@link ActivityOutputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureNextToActivityOutputModel> linkProcedureNextToOutput(
			ActivityProcedureNextModel procedureNext, ActivityOutputModel output);

	/**
	 * Removes the {@link ActivityProcedureNextToActivityOutputModel}.
	 * 
	 * @param link {@link ActivityProcedureNextToActivityOutputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureNextToActivityOutputModel> removeProcedureNextToOutput(
			ActivityProcedureNextToActivityOutputModel link);

	/**
	 * Links the {@link ActivityProcedureOutputModel} to the
	 * {@link ActivitySectionInputModel}.
	 * 
	 * @param procedureOutput {@link ActivityProcedureOutputModel}.
	 * @param sectionInput    {@link ActivitySectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureOutputToActivitySectionInputModel> linkProcedureOutputToSectionInput(
			ActivityProcedureOutputModel procedureOutput, ActivitySectionInputModel sectionInput);

	/**
	 * Removes the {@link ActivityProcedureOutputToActivitySectionInputModel}.
	 * 
	 * @param link {@link ActivityProcedureOutputToActivitySectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureOutputToActivitySectionInputModel> removeProcedureOutputToSectionInput(
			ActivityProcedureOutputToActivitySectionInputModel link);

	/**
	 * Links the {@link ActivityProcedureOutputModel} to the
	 * {@link ActivityProcedureModel}.
	 * 
	 * @param procedureOutput {@link ActivityProcedureOutputModel}.
	 * @param procedure       {@link ActivityProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureOutputToActivityProcedureModel> linkProcedureOutputToProcedure(
			ActivityProcedureOutputModel procedureOutput, ActivityProcedureModel procedure);

	/**
	 * Removes the {@link ActivityProcedureOutputToActivityProcedureModel}.
	 * 
	 * @param link {@link ActivityProcedureOutputToActivityProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureOutputToActivityProcedureModel> removeProcedureOutputToProcedure(
			ActivityProcedureOutputToActivityProcedureModel link);

	/**
	 * Links the {@link ActivityProcedureOutputModel} to the
	 * {@link ActivityOutputModel}.
	 * 
	 * @param procedureOutput {@link ActivityProcedureOutputModel}.
	 * @param output          {@link ActivityOutputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityProcedureOutputToActivityOutputModel> linkProcedureOutputToOutput(
			ActivityProcedureOutputModel procedureOutput, ActivityOutputModel output);

	/**
	 * Removes the {@link ActivityProcedureOutputToActivityOutputModel}.
	 * 
	 * @param link {@link ActivityProcedureOutputToActivityOutputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityProcedureOutputToActivityOutputModel> removeProcedureOutputToOutput(
			ActivityProcedureOutputToActivityOutputModel link);

	/**
	 * Links the {@link ActivitySectionOutputModel} to the
	 * {@link ActivitySectionInputModel}.
	 * 
	 * @param sectionOutput {@link ActivitySectionOutputModel}.
	 * @param sectionInput  {@link ActivitySectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivitySectionOutputToActivitySectionInputModel> linkSectionOutputToSectionInput(
			ActivitySectionOutputModel sectionOutput, ActivitySectionInputModel sectionInput);

	/**
	 * Removes the {@link ActivitySectionOutputToActivitySectionInputModel}.
	 * 
	 * @param link {@link ActivitySectionOutputToActivitySectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivitySectionOutputToActivitySectionInputModel> removeSectionOutputToSectionInput(
			ActivitySectionOutputToActivitySectionInputModel link);

	/**
	 * Links the {@link ActivitySectionOutputModel} to the
	 * {@link ActivityProcedureModel}.
	 * 
	 * @param sectionOutput {@link ActivitySectionOutputModel}.
	 * @param procedure     {@link ActivityProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivitySectionOutputToActivityProcedureModel> linkSectionOutputToProcedure(
			ActivitySectionOutputModel sectionOutput, ActivityProcedureModel procedure);

	/**
	 * Removes the {@link ActivitySectionOutputToActivityProcedureModel}.
	 * 
	 * @param link {@link ActivitySectionOutputToActivityProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivitySectionOutputToActivityProcedureModel> removeSectionOutputToProcedure(
			ActivitySectionOutputToActivityProcedureModel link);

	/**
	 * Links the {@link ActivitySectionOutputModel} to the
	 * {@link ActivityOutputModel}.
	 * 
	 * @param sectionOutput {@link ActivitySectionOutputModel}.
	 * @param output        {@link ActivityOutputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivitySectionOutputToActivityOutputModel> linkSectionOutputToOutput(
			ActivitySectionOutputModel sectionOutput, ActivityOutputModel output);

	/**
	 * Removes the {@link ActivitySectionOutputToActivityOutputModel}.
	 * 
	 * @param link {@link ActivitySectionOutputToActivityOutputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivitySectionOutputToActivityOutputModel> removeSectionOutputToOutput(
			ActivitySectionOutputToActivityOutputModel link);

	/**
	 * Links the {@link ActivityExceptionModel} to the
	 * {@link ActivitySectionInputModel} .
	 * 
	 * @param exception    {@link ActivityExceptionModel}.
	 * @param sectionInput {@link ActivitySectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityExceptionToActivitySectionInputModel> linkExceptionToSectionInput(ActivityExceptionModel exception,
			ActivitySectionInputModel sectionInput);

	/**
	 * Removes the {@link ActivityExceptionToActivitySectionInputModel}.
	 * 
	 * @param link {@link ActivityExceptionToActivitySectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityExceptionToActivitySectionInputModel> removeExceptionToSectionInput(
			ActivityExceptionToActivitySectionInputModel link);

	/**
	 * Links the {@link ActivityExceptionModel} to the
	 * {@link ActivityProcedureModel}.
	 * 
	 * @param exception {@link ActivityExceptionModel}.
	 * @param procedure {@link ActivityProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityExceptionToActivityProcedureModel> linkExceptionToProcedure(ActivityExceptionModel exception,
			ActivityProcedureModel procedure);

	/**
	 * Removes the {@link ActivityExceptionToActivityProcedureModel}.
	 * 
	 * @param link {@link ActivityExceptionToActivityProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityExceptionToActivityProcedureModel> removeExceptionToProcedure(
			ActivityExceptionToActivityProcedureModel link);

	/**
	 * Links the {@link ActivityExceptionModel} to the {@link ActivityOutputModel}.
	 * 
	 * @param exception {@link ActivityExceptionModel}.
	 * @param output    {@link ActivityOutputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityExceptionToActivityOutputModel> linkExceptionToOutput(ActivityExceptionModel exception,
			ActivityOutputModel output);

	/**
	 * Removes the {@link ActivityExceptionToActivityOutputModel}.
	 * 
	 * @param link {@link ActivityExceptionToActivityOutputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityExceptionToActivityOutputModel> removeExceptionToOutput(ActivityExceptionToActivityOutputModel link);

	/**
	 * Links the {@link ActivityInputModel} to the
	 * {@link ActivitySectionInputModel}.
	 * 
	 * @param input        {@link ActivityInputModel}.
	 * @param sectionInput {@link ActivitySectionInputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityInputToActivitySectionInputModel> linkInputToSectionInput(ActivityInputModel input,
			ActivitySectionInputModel sectionInput);

	/**
	 * Removes the {@link ActivityInputToActivitySectionInputModel}.
	 * 
	 * @param link {@link ActivityInputToActivitySectionInputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityInputToActivitySectionInputModel> removeInputToSectionInput(
			ActivityInputToActivitySectionInputModel link);

	/**
	 * Links the {@link ActivityInputModel} to the {@link ActivityProcedureModel}.
	 * 
	 * @param input     {@link ActivityInputModel}.
	 * @param procedure {@link ActivityProcedureModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityInputToActivityProcedureModel> linkInputToProcedure(ActivityInputModel input,
			ActivityProcedureModel procedure);

	/**
	 * Removes the {@link ActivityInputToActivityProcedureModel}.
	 * 
	 * @param link {@link ActivityInputToActivityProcedureModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityInputToActivityProcedureModel> removeInputToProcedure(ActivityInputToActivityProcedureModel link);

	/**
	 * Links the {@link ActivityInputModel} to the {@link ActivityOutputModel}.
	 * 
	 * @param input  {@link ActivityInputModel}.
	 * @param output {@link ActivityOutputModel}.
	 * @return {@link Change} to make the link.
	 */
	Change<ActivityInputToActivityOutputModel> linkInputToOutput(ActivityInputModel input, ActivityOutputModel output);

	/**
	 * Removes the {@link ActivityInputToActivityOutputModel}.
	 * 
	 * @param link {@link ActivityInputToActivityOutputModel}.
	 * @return {@link Change} to remove the link.
	 */
	Change<ActivityInputToActivityOutputModel> removeInputToOutput(ActivityInputToActivityOutputModel link);

}
