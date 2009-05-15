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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigureContext;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigureContext;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamToOfficeFloorTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigureContext;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

/**
 * Standard {@link OfficeFloorFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorFigureFactory implements
		OfficeFloorFigureFactory {

	@Override
	public OfficeFloorManagedObjectSourceFigure createOfficeFloorManagedObjectSourceFigure(
			OfficeFloorManagedObjectSourceFigureContext context) {
		return new StandardOfficeFloorManagedObjectSourceFigure(context);
	}

	@Override
	public OfficeFloorManagedObjectSourceFlowFigure createOfficeFloorManagedObjectSourceFlowFigure(
			OfficeFloorManagedObjectSourceFlowFigureContext context) {
		return new StandardOfficeFloorManagedObjectSourceFlowFigure(context);
	}

	@Override
	public OfficeFloorManagedObjectSourceTeamFigure createOfficeFloorManagedObjectSourceTeamFigure(
			OfficeFloorManagedObjectSourceTeamFigureContext context) {
		return new StandardOfficeFloorManagedObjectSourceTeamFigure(context);
	}

	@Override
	public OfficeFloorManagedObjectFigure createOfficeFloorManagedObjectFigure(
			OfficeFloorManagedObjectFigureContext context) {
		return new StandardOfficeFloorManagedObjectFigure(context);
	}

	@Override
	public OfficeFloorManagedObjectDependencyFigure createOfficeFloorManagedObjectDependencyFigure(
			OfficeFloorManagedObjectDependencyFigureContext context) {
		return new StandardOfficeFloorManagedObjectDependencyFigure(context);
	}

	@Override
	public DeployedOfficeFigure createDeployedOfficeFigure(
			DeployedOfficeFigureContext context) {
		return new StandardDeployedOfficeFigure(context);
	}

	@Override
	public DeployedOfficeObjectFigure createDeployedOfficeObjectFigure(
			DeployedOfficeObjectFigureContext context) {
		return new StandardDeployedOfficeObjectFigure(context);
	}

	@Override
	public net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigure createDeployedOfficeInputFigure(
			DeployedOfficeInputFigureContext context) {
		return new StandardDeployedOfficeInputFigure(context);
	}

	@Override
	public DeployedOfficeTeamFigure createDeployedOfficeTeamFigure(
			DeployedOfficeTeamFigureContext context) {
		return new StandardDeployedOfficeTeamFigure(context);
	}

	@Override
	public OfficeFloorTeamFigure createOfficeFloorTeamFigure(
			OfficeFloorTeamFigureContext context) {
		return new StandardOfficeFloorTeamFigure(context);
	}

	@Override
	public void decorateOfficeFloorManagedObjectToOfficeFloorManagedObjectSourceFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceFigureContext context) {
		figure.setForegroundColor(ColorConstants.lightBlue);
	}

	@Override
	public void decorateDeployedOfficeObjectToOfficeFloorManagedObjectFigure(
			PolylineConnection figure,
			DeployedOfficeObjectToOfficeFloorManagedObjectFigureContext context) {
		// Leave as default
	}

	@Override
	public void decorateDeployedOfficeTeamToOfficeFloorTeamFigure(
			PolylineConnection figure,
			DeployedOfficeTeamToOfficeFloorTeamFigureContext context) {
		// Leave as default
	}

	@Override
	public void decorateOfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigureContext context) {
		// Leave as default
	}

	@Override
	public void decorateOfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateOfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigureContext context) {
		// Leave as default
	}

	@Override
	public OfficeFloorManagedObjectSourceToDeployedOfficeFigure decorateOfficeFloorManagedObjectSourceToDeployedOfficeFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext context) {
		return new StandardOfficeFloorManagedObjectSourceToDeployedOfficeFigure(figure, context);
	}

}