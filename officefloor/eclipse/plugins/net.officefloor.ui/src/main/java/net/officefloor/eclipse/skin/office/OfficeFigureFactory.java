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
package net.officefloor.eclipse.skin.office;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

import net.officefloor.eclipse.office.models.AbstractFunctionAdministrationJoinPointModel;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToExternalManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTeamModel;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectSourceModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectSourceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeManagedObjectSourceFigure createOfficeManagedObjectSourceFigure(
			OfficeManagedObjectSourceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectSourceFlowModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectSourceFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeManagedObjectSourceFlowFigure createOfficeManagedObjectSourceFlowFigure(
			OfficeManagedObjectSourceFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectSourceTeamModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectSourceTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeManagedObjectSourceTeamFigure createOfficeManagedObjectSourceTeamFigure(
			OfficeManagedObjectSourceTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeInputManagedObjectDependencyModel}.
	 * 
	 * @param context
	 *            {@link OfficeInputManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeInputManagedObjectDependencyFigure createOfficeInputManagedObjectDependencyFigure(
			OfficeInputManagedObjectDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectFigureContext}.
	 * @return {@link OfficeManagedObjectFigure}.
	 */
	OfficeManagedObjectFigure createOfficeManagedObjectFigure(OfficeManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectDependencyModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeManagedObjectDependencyFigure createOfficeManagedObjectDependencyFigure(
			OfficeManagedObjectDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link AdministrationModel}.
	 * 
	 * @param context
	 *            {@link AdministrationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	AdministrationFigure createAdministrationFigure(AdministrationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalManagedObjectFigure createExternalManagedObjectFigure(ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeEscalationModel}.
	 * 
	 * @param context
	 *            {@link OfficeEscalationFigureContext}.
	 * @return {@link OfficeEscalationFigure}.
	 */
	OfficeEscalationFigure createOfficeEscalationFigure(OfficeEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeTeamModel}.
	 * 
	 * @param context
	 *            {@link OfficeTeamFigureContext}.
	 * @return {@link OfficeTeamFigure}.
	 */
	OfficeTeamFigure createOfficeTeamFigure(OfficeTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeStartModel}.
	 * 
	 * @param context
	 *            {@link OfficeStartFigureContext}.
	 * @return {@link OfficeStartFigure}.
	 */
	OfficeStartFigure createOfficeStartFigure(OfficeStartFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeSectionModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeSectionFigure createOfficeSectionFigure(OfficeSectionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionInputFigureContext}.
	 * @return {@link OfficeSectionInputFigure}.
	 */
	OfficeSectionInputFigure createOfficeSectionInputFigure(OfficeSectionInputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionOutputModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionOutputFigureContext}.
	 * @return {@link OfficeSectionOutputFigure}.
	 */
	OfficeSectionOutputFigure createOfficeSectionOutputFigure(OfficeSectionOutputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionObjectFigure}.
	 * @return {@link OfficeSectionObjectFigure}.
	 */
	OfficeSectionObjectFigure createOfficeSectionObjectFigure(OfficeSectionObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeSubSectionModel}.
	 * 
	 * @param context
	 *            {@link OfficeSubSectionFigureContext}.
	 * @return {@link OfficeSubSectionFigure}.
	 */
	OfficeSubSectionFigure createOfficeSubSectionFigure(OfficeSubSectionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeFunctionModel}.
	 * 
	 * @param context
	 *            {@link OfficeFunctionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFunctionFigure createOfficeFunctionFigure(OfficeFunctionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link AbstractFunctionAdministrationJoinPointModel}.
	 * 
	 * @return {@link OfficeFloorFigure}.
	 */
	FunctionAdministrationJoinPointFigure createTaskAdministrationJoinPointFigure();

	/**
	 * Decorates the {@link OfficeManagedObjectToOfficeManagedObjectSourceModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeManagedObjectToOfficeManagedObjectSourceModel}
	 */
	void decorateOfficeManagedObjectToOfficeManagedObjectSourceFigure(PolylineConnection figure,
			OfficeManagedObjectToOfficeManagedObjectSourceFigureContext context);

	/**
	 * Decorates the {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeManagedObjectDependencyToOfficeManagedObjectFigureContext}
	 */
	void decorateOfficeManagedObjectDependencyToOfficeManagedObjectFigure(PolylineConnection figure,
			OfficeManagedObjectDependencyToOfficeManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeInputManagedObjectDependencyToOfficeManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeInputManagedObjectDependencyToOfficeManagedObjectFigureContext}
	 */
	void decorateOfficeInputManagedObjectDependencyToOfficeManagedObjectFigure(PolylineConnection figure,
			OfficeInputManagedObjectDependencyToOfficeManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeManagedObjectDependencyToExternalManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeManagedObjectDependencyToExternalManagedObjectFigureContext}
	 */
	void decorateOfficeManagedObjectDependencyToExternalManagedObjectFigure(PolylineConnection figure,
			OfficeManagedObjectDependencyToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeInputManagedObjectDependencyToExternalManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeInputManagedObjectDependencyToExternalManagedObjectFigureContext}
	 */
	void decorateOfficeInputManagedObjectDependencyToExternalManagedObjectFigure(PolylineConnection figure,
			OfficeInputManagedObjectDependencyToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeManagedObjectSourceFlowToOfficeSectionInputFigureContext}
	 */
	void decorateOfficeManagedObjectSourceFlowToOfficeSectionInputFigure(PolylineConnection figure,
			OfficeManagedObjectSourceFlowToOfficeSectionInputFigureContext context);

	/**
	 * Decorates the {@link OfficeManagedObjectSourceTeamToOfficeTeamModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeManagedObjectSourceTeamToOfficeTeamFigureContext}
	 */
	void decorateOfficeManagedObjectSourceTeamToOfficeTeamFigure(PolylineConnection figure,
			OfficeManagedObjectSourceTeamToOfficeTeamFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionObjectToExternalManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionObjectToExternalManagedObjectFigureContext}
	 */
	void decorateOfficeSectionObjectToExternalManagedObjectFigure(PolylineConnection figure,
			OfficeSectionObjectToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionObjectToOfficeManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionObjectToOfficeManagedObjectFigureContext}
	 */
	void decorateOfficeSectionObjectToOfficeManagedObjectFigure(PolylineConnection figure,
			OfficeSectionObjectToOfficeManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionOutputToOfficeSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionOutputToOfficeSectionInputFigureContext}.
	 */
	void decorateOfficeSectionOutputToOfficeSectionInput(PolylineConnection figure,
			OfficeSectionOutputToOfficeSectionInputFigureContext context);

	/**
	 * Decorates the {@link OfficeFunctionToOfficeTeamModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFunctionToOfficeTeamFigureContext}.
	 */
	void decorateOfficeFunctionToOfficeTeam(PolylineConnection figure, OfficeFunctionToOfficeTeamFigureContext context);

	/**
	 * Decorates the {@link AdministrationToOfficeTeamModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link AdministrationToOfficeTeamFigureContext}.
	 */
	void decorateAdministrationToOfficeTeamFigure(PolylineConnection figure,
			AdministrationToOfficeTeamFigureContext context);

	/**
	 * Decorates the {@link AdministrationToExternalManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link AdministrationToExternalManagedObjectFigureContext}.
	 */
	void decorateAdministrationToExternalManagedObjectFigure(PolylineConnection figure,
			AdministrationToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link AdministrationToOfficeManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link AdministrationToOfficeManagedObjectFigureContext}.
	 */
	void decorateAdministrationToOfficeManagedObjectFigure(PolylineConnection figure,
			AdministrationToOfficeManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeFunctionToPreAdministrationModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFunctionToPreAdministrationFigureContext}.
	 */
	void decorateOfficeFunctionToPreAdministrationFigure(PolylineConnection figure,
			OfficeFunctionToPreAdministrationFigureContext context);

	/**
	 * Decorates the {@link OfficeFunctionToPostAdministrationModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFunctionToPostAdministrationFigureContext}.
	 */
	void decorateOfficeFunctionToPostAdministrationFigure(PolylineConnection figure,
			OfficeFunctionToPostAdministrationFigureContext context);

	/**
	 * Decorates the {@link OfficeEscalationToOfficeSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeEscalationToOfficeSectionInputFigureContext}
	 */
	void decorateOfficeEscalationToOfficeSectionInputFigure(PolylineConnection figure,
			OfficeEscalationToOfficeSectionInputFigureContext context);

	/**
	 * Decorates the {@link OfficeStartToOfficeSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeStartToOfficeSectionInputFigureContext}.
	 */
	void decorateOfficeStartToOfficeSectionInputFigure(PolylineConnection figure,
			OfficeStartToOfficeSectionInputFigureContext context);

}