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
package net.officefloor.eclipse.skin.standard.office;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.office.AdministrationFigure;
import net.officefloor.eclipse.skin.office.AdministrationFigureContext;
import net.officefloor.eclipse.skin.office.AdministratorToOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectToAdministratorFigureContext;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.eclipse.skin.office.OfficeEscalationToOfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyToOfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFlowToOfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamToOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectToAdministratorFigureContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectToOfficeManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectToOfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputToOfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityToOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.OfficeStartFigure;
import net.officefloor.eclipse.skin.office.OfficeStartFigureContext;
import net.officefloor.eclipse.skin.office.OfficeStartToOfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTaskToPostDutyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTaskToPreDutyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.TaskAdministrationJoinPointFigure;
import net.officefloor.eclipse.skin.office.OfficeFunctionFigure;
import net.officefloor.eclipse.skin.office.OfficeFunctionFigureContext;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.office.OfficeSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;

/**
 * Standard {@link OfficeFigureFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFigureFactory implements OfficeFigureFactory {

	@Override
	public OfficeManagedObjectSourceFigure createOfficeManagedObjectSourceFigure(
			OfficeManagedObjectSourceFigureContext context) {
		return new StandardOfficeManagedObjectSourceFigure(context);
	}

	@Override
	public OfficeManagedObjectSourceFlowFigure createOfficeManagedObjectSourceFlowFigure(
			OfficeManagedObjectSourceFlowFigureContext context) {
		return new StandardOfficeManagedObjectSourceFlowFigure(context);
	}

	@Override
	public OfficeManagedObjectSourceTeamFigure createOfficeManagedObjectSourceTeamFigure(
			OfficeManagedObjectSourceTeamFigureContext context) {
		return new StandardOfficeManagedObjectSourceTeamFigure(context);
	}

	@Override
	public OfficeManagedObjectFigure createOfficeManagedObjectFigure(
			OfficeManagedObjectFigureContext context) {
		return new StandardOfficeManagedObjectFigure(context);
	}

	@Override
	public OfficeManagedObjectDependencyFigure createOfficeManagedObjectDependencyFigure(
			OfficeManagedObjectDependencyFigureContext context) {
		return new StandardOfficeManagedObjectDependencyFigure(context);
	}

	@Override
	public OfficeInputManagedObjectDependencyFigure createOfficeInputManagedObjectDependencyFigure(
			OfficeInputManagedObjectDependencyFigureContext context) {
		return new StandardOfficeInputManagedObjectDependencyFigure(context);
	}

	@Override
	public AdministrationFigure createAdministratorFigure(
			AdministrationFigureContext context) {
		return new StandardAdministratorFigure(context);
	}

	@Override
	public DutyFigure createDutyFigure(DutyFigureContext context) {
		return new StandardDutyFigure(context);
	}

	@Override
	public OfficeEscalationFigure createOfficeEscalationFigure(
			OfficeEscalationFigureContext context) {
		return new StandardOfficeEscalationFigure(context);
	}

	@Override
	public OfficeTeamFigure createOfficeTeamFigure(
			OfficeTeamFigureContext context) {
		return new StandardOfficeTeamFigure(context);
	}

	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			final ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	@Override
	public OfficeSectionFigure createOfficeSectionFigure(
			OfficeSectionFigureContext context) {
		return new StandardOfficeSectionFigure(context);
	}

	@Override
	public OfficeSectionInputFigure createOfficeSectionInputFigure(
			OfficeSectionInputFigureContext context) {
		return new StandardOfficeSectionInputFigure(context);
	}

	@Override
	public OfficeSectionOutputFigure createOfficeSectionOutputFigure(
			OfficeSectionOutputFigureContext context) {
		return new StandardOfficeSectionOutputFigure(context);
	}

	@Override
	public OfficeSectionObjectFigure createOfficeSectionObjectFigure(
			OfficeSectionObjectFigureContext context) {
		return new StandardOfficeSectionObjectFigure(context);
	}

	@Override
	public OfficeSectionResponsibilityFigure createOfficeSectionResponsibilityFigure(
			OfficeSectionResponsibilityFigureContext context) {
		return new StandardOfficeSectionResponsibilityFigure(context);
	}

	@Override
	public OfficeSubSectionFigure createOfficeSubSectionFigure(
			OfficeSubSectionFigureContext context) {
		return new StandardOfficeSubSectionFigure(context);
	}

	@Override
	public OfficeFunctionFigure createOfficeTaskFigure(
			OfficeFunctionFigureContext context) {
		return new StandardOfficeTaskFigure(context);
	}

	@Override
	public TaskAdministrationJoinPointFigure createTaskAdministrationJoinPointFigure() {
		return new StandardTaskAdministrationJoinPointFigure();
	}

	@Override
	public OfficeStartFigure createOfficeStartFigure(
			OfficeStartFigureContext context) {
		return new StandardOfficeStartFigure(context);
	}

	@Override
	public void decorateOfficeManagedObjectToOfficeManagedObjectSourceFigure(
			PolylineConnection figure,
			OfficeManagedObjectToOfficeManagedObjectSourceFigureContext context) {
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateOfficeManagedObjectDependencyToOfficeManagedObjectFigure(
			PolylineConnection figure,
			OfficeManagedObjectDependencyToOfficeManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			OfficeManagedObjectDependencyToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeInputManagedObjectDependencyToOfficeManagedObjectFigure(
			PolylineConnection figure,
			OfficeInputManagedObjectDependencyToOfficeManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeInputManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			OfficeInputManagedObjectDependencyToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeManagedObjectSourceFlowToOfficeSectionInputFigure(
			PolylineConnection figure,
			OfficeManagedObjectSourceFlowToOfficeSectionInputFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeSectionObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			OfficeSectionObjectToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeSectionObjectToOfficeManagedObjectFigure(
			PolylineConnection figure,
			OfficeSectionObjectToOfficeManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeSectionOutputToOfficeSectionInput(
			PolylineConnection figure,
			OfficeSectionOutputToOfficeSectionInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateOfficeSectionResponsibilityToOfficeTeam(
			PolylineConnection figure,
			OfficeSectionResponsibilityToOfficeTeamFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeManagedObjectSourceTeamToOfficeTeamFigure(
			PolylineConnection figure,
			OfficeManagedObjectSourceTeamToOfficeTeamFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateAdministratorToOfficeTeamFigure(
			PolylineConnection figure,
			AdministratorToOfficeTeamFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateExternalManagedObjectToAdministratorFigure(
			PolylineConnection figure,
			ExternalManagedObjectToAdministratorFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeManagedObjectToAdministratorFigure(
			PolylineConnection figure,
			OfficeManagedObjectToAdministratorFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeTaskToPreDutyFigure(PolylineConnection figure,
			OfficeTaskToPreDutyFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeTaskToPostDutyFigure(PolylineConnection figure,
			OfficeTaskToPostDutyFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeEscalationToOfficeSectionInputFigure(
			PolylineConnection figure,
			OfficeEscalationToOfficeSectionInputFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeStartToOfficeSectionInputFigure(
			PolylineConnection figure,
			OfficeStartToOfficeSectionInputFigureContext context) {
		// Leave as default line
	}

}