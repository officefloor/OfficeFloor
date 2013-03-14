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
package net.officefloor.eclipse.skin.officefloor;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link OfficeFloorModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectSourceModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorManagedObjectSourceFigure createOfficeFloorManagedObjectSourceFigure(
			OfficeFloorManagedObjectSourceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectTaskFlowModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorManagedObjectSourceFlowFigure createOfficeFloorManagedObjectSourceFlowFigure(
			OfficeFloorManagedObjectSourceFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectTeamModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorManagedObjectSourceTeamFigure createOfficeFloorManagedObjectSourceTeamFigure(
			OfficeFloorManagedObjectSourceTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeFloorManagedObjectSourceInputDependencyModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceInputDependencyFigureContext}
	 *            .
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorManagedObjectSourceInputDependencyFigure createOfficeFloorManagedObjectSourceInputDependencyFigure(
			OfficeFloorManagedObjectSourceInputDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeFloorInputManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorInputManagedObjectFigureContext}.
	 * @return {@link OfficeFloorInputManagedObjectFigure}.
	 */
	OfficeFloorInputManagedObjectFigure createOfficeFloorInputManagedObjectFigure(
			OfficeFloorInputManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeFloorManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectFigureContext}.
	 * @return {@link OfficeFloorManagedObjectFigure}.
	 */
	OfficeFloorManagedObjectFigure createOfficeFloorManagedObjectFigure(
			OfficeFloorManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectDependencyModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorManagedObjectDependencyFigure createOfficeFloorManagedObjectDependencyFigure(
			OfficeFloorManagedObjectDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeModel}.
	 * 
	 * @param context
	 *            {@link DeployedOfficeFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeployedOfficeFigure createDeployedOfficeFigure(
			DeployedOfficeFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link DeployedOfficeObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeployedOfficeObjectFigure createDeployedOfficeObjectFigure(
			DeployedOfficeObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeTaskModel}.
	 * 
	 * @param context
	 *            {@link DeployedOfficeInputFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeployedOfficeInputFigure createDeployedOfficeInputFigure(
			DeployedOfficeInputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link }.
	 * 
	 * @param context
	 *            {@link DeployedOfficeTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeployedOfficeTeamFigure createDeployedOfficeTeamFigure(
			DeployedOfficeTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TeamModel}.
	 * 
	 * @param context
	 *            {@link OfficeFloorTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorTeamFigure createOfficeFloorTeamFigure(
			OfficeFloorTeamFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel}
	 */
	void decorateOfficeFloorManagedObjectToOfficeFloorManagedObjectSourceFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceFigureContext context);

	/**
	 * Decorates the {@link DeployedOfficeObjectToOfficeFloorManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeployedOfficeObjectToOfficeFloorManagedObjectFigureContext}
	 */
	void decorateDeployedOfficeObjectToOfficeFloorManagedObjectFigure(
			PolylineConnection figure,
			DeployedOfficeObjectToOfficeFloorManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectFigureContext}
	 *            .
	 */
	void decorateDeployedOfficeObjectToOfficeFloorInputManagedObjectFigure(
			PolylineConnection figure,
			DeployedOfficeObjectToOfficeFloorInputManagedObjectFigureContext context);

	/**
	 * Decorates the {@link DeployedOfficeTeamToOfficeFloorTeamModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeployedOfficeTeamToOfficeFloorTeamFigureContext}.
	 */
	void decorateDeployedOfficeTeamToOfficeFloorTeamFigure(
			PolylineConnection figure,
			DeployedOfficeTeamToOfficeFloorTeamFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigureContext}
	 */
	void decorateOfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectFigureContext}
	 */
	void decorateOfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectFigureContext context);

	/**
	 * Decorates the {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceToDeployedOffice}.
	 */
	void decorateOfficeFloorManagedObjectSourceToDeployedOfficeFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectFigureContext}
	 *            .
	 */
	void decorateOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceFigureContext}
	 *            .
	 */
	void decorateOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceFigure(
			PolylineConnection figure,
			OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigureContext}
	 */
	void decorateOfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigureContext}
	 */
	void decorateOfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamFigureContext context);

	/**
	 * Decorates the
	 * {@link OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel}
	 * figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectFigureContext}
	 */
	void decorateOfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectFigureContext context);

}