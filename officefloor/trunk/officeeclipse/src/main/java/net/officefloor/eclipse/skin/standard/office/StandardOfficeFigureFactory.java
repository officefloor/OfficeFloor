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
package net.officefloor.eclipse.skin.standard.office;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.office.AdministratorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.eclipse.skin.office.AdministratorToOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectToAdministratorFigureContext;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputToOfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityToOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTaskToPostDutyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTaskToPreDutyFigureContext;
import net.officefloor.eclipse.skin.office.OfficeTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.office.TaskAdministrationJoinPointFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigureContext;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.office.OfficeSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;

/**
 * Standard {@link OfficeFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardOfficeFigureFactory implements OfficeFigureFactory {

	@Override
	public AdministratorFigure createAdministratorFigure(
			AdministratorFigureContext context) {
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
		return new StandardOficeSectionResponsibilityFigure(context);
	}

	@Override
	public OfficeSubSectionFigure createOfficeSubSectionFigure(
			OfficeSubSectionFigureContext context) {
		return new StandardOfficeSubSectionFigure(context);
	}

	@Override
	public OfficeTaskFigure createOfficeTaskFigure(
			OfficeTaskFigureContext context) {
		return new StandardOfficeTaskFigure(context);
	}

	@Override
	public TaskAdministrationJoinPointFigure createTaskAdministrationJoinPointFigure() {
		return new StandardTaskAdministrationJoinPointFigure();
	}

	@Override
	public void decorateOfficeSectionObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			OfficeSectionObjectToExternalManagedObjectFigureContext context) {
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
	public void decorateOfficeTaskToPreDutyFigure(PolylineConnection figure,
			OfficeTaskToPreDutyFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateOfficeTaskToPostDutyFigure(PolylineConnection figure,
			OfficeTaskToPostDutyFigureContext context) {
		// Leave as default line
	}

}