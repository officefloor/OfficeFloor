/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.office;

import net.officefloor.eclipse.office.models.AbstractTaskAdministrationJoinPointModel;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTeamModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link OfficeModel}.
 * 
 * @author Daniel
 */
public interface OfficeFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link AdministratorModel}.
	 * 
	 * @param context
	 *            {@link AdministratorFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	AdministratorFigure createAdministratorFigure(
			AdministratorFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DutyModel}.
	 * 
	 * @param context
	 *            {@link DutyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DutyFigure createDutyFigure(DutyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeTeamModel}.
	 * 
	 * @param context
	 *            {@link OfficeTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeTeamFigure createOfficeTeamFigure(OfficeTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeTaskModel}.
	 * 
	 * @param context
	 *            {@link OfficeTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeTaskFigure createOfficeTaskFigure(OfficeTaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link AbstractTaskAdministrationJoinPointModel}.
	 * 
	 * @return {@link OfficeFloorFigure}.
	 */
	TaskAdministrationJoinPointFigure createTaskAdministrationJoinPointFigure();

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeSectionModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeSectionFigure createOfficeSectionFigure(
			OfficeSectionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionInputFigureContext}.
	 * @return {@link OfficeSectionInputFigure}.
	 */
	OfficeSectionInputFigure createOfficeSectionInputFigure(
			OfficeSectionInputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionOutputModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionOutputFigureContext}.
	 * @return {@link OfficeSectionOutputFigure}.
	 */
	OfficeSectionOutputFigure createOfficeSectionOutputFigure(
			OfficeSectionOutputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionObjectFigure}.
	 * @return {@link OfficeSectionObjectFigure}.
	 */
	OfficeSectionObjectFigure createOfficeSectionObjectFigure(
			OfficeSectionObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeEscalationModel}.
	 * 
	 * @param context
	 *            {@link OfficeEscalationFigureContext}.
	 * @return {@link OfficeEscalationFigure}.
	 */
	OfficeEscalationFigure createOfficeEscalationFigure(
			OfficeEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeSectionResponsibilityModel}.
	 * 
	 * @param context
	 *            {@link OfficeSectionResponsibilityFigureContext}.
	 * @return {@link OfficeSectionResponsibilityFigure}.
	 */
	OfficeSectionResponsibilityFigure createOfficeSectionResponsibilityFigure(
			OfficeSectionResponsibilityFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionObjectToExternalManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionObjectToExternalManagedObjectFigureContext}
	 */
	void decorateOfficeSectionObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			OfficeSectionObjectToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionOutputToOfficeSectionInputModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionOutputToOfficeSectionInputFigureContext}.
	 */
	void decorateOfficeSectionOutputToOfficeSectionInput(
			PolylineConnection figure,
			OfficeSectionOutputToOfficeSectionInputFigureContext context);

	/**
	 * Decorates the {@link OfficeSectionResponsibilityToOfficeTeamModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeSectionResponsibilityToOfficeTeamFigureContext}.
	 */
	void decorateOfficeSectionResponsibilityToOfficeTeam(
			PolylineConnection figure,
			OfficeSectionResponsibilityToOfficeTeamFigureContext context);

}