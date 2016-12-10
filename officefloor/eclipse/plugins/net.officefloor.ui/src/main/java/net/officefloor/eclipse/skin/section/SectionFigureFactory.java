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
package net.officefloor.eclipse.skin.section;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link SectionModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface SectionFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 *
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context);

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
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SectionManagedObjectSourceModel}.
	 *
	 * @param context
	 *            {@link SectionManagedObjectSourceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SectionManagedObjectSourceFigure createSectionManagedObjectSourceFigure(
			SectionManagedObjectSourceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SectionManagedObjectSourceFlowModel}.
	 *
	 * @param context
	 *            {@link SectionManagedObjectSourceFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SectionManagedObjectSourceFlowFigure createSectionManagedObjectSourceFlowFigure(
			SectionManagedObjectSourceFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SectionManagedObjectModel}.
	 *
	 * @param context
	 *            {@link SectionManagedObjectFigureContext}.
	 * @return {@link SectionManagedObjectFigure}.
	 */
	SectionManagedObjectFigure createSectionManagedObjectFigure(
			SectionManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SectionManagedObjectDependencyModel}.
	 *
	 * @param context
	 *            {@link SectionManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SectionManagedObjectDependencyFigure createSectionManagedObjectDependencyFigure(
			SectionManagedObjectDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link SubSectionModel}.
	 *
	 * @param context
	 *            {@link SubSectionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubSectionFigure createSubSectionFigure(SubSectionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionInputModel}.
	 *
	 * @param context
	 *            {@link SubSectionInputFigureContext}.
	 * @return {@link SubSectionInputFigure}.
	 */
	SubSectionInputFigure createSubSectionInputFigure(
			SubSectionInputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionObjectModel}.
	 *
	 * @param context
	 *            {@link SubSectionObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubSectionObjectFigure createSubSectionObjectFigure(
			SubSectionObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionOutputModel}.
	 *
	 * @param context
	 *            {@link SubSectionOutputFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubSectionOutputFigure createSubSectionOutputFigure(
			SubSectionOutputFigureContext context);

	/**
	 * Decorates the
	 * {@link SectionManagedObjectToSectionManagedObjectSourceModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectToSectionManagedObjectSourceModel}
	 */
	void decorateSectionManagedObjectToSectionManagedObjectSourceFigure(
			PolylineConnection figure,
			SectionManagedObjectToSectionManagedObjectSourceFigureContext context);

	/**
	 * Decorates the {@link SubSectionObjectToExternalManagedObjectModel}
	 * connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SubSectionObjectToExternalManagedObjectFigureContext}.
	 */
	void decorateSubSectionObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			SubSectionObjectToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link SubSectionObjectToSectionManagedObjectModel}
	 * connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SubSectionObjectToSectionManagedObjectFigureContext}.
	 */
	void decorateSubSectionObjectToSectionManagedObjectFigure(
			PolylineConnection figure,
			SubSectionObjectToSectionManagedObjectFigureContext context);

	/**
	 * Decorates the {@link SubSectionOutputToSubSectionInputModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SubSectionObjectToExternalManagedObjectFigureContext}.
	 */
	void decorateSubSectionOutputToSubSectionInput(PolylineConnection figure,
			SubSectionOutputToSubSectionInputFigureContext context);

	/**
	 * Decorates the {@link SubSectionOutputToExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SubSectionOutputToExternalFlowFigureContext}.
	 */
	void decorateSubSectionOutputToExternalFlowFigure(
			PolylineConnection figure,
			SubSectionOutputToExternalFlowFigureContext context);

	/**
	 * Decorates the
	 * {@link SectionManagedObjectSourceFlowToSubSectionInputModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectSourceFlowToSubSectionInputFigureContext}
	 */
	void decorateSectionManagedObjectSourceFlowToSubSectionInputFigure(
			PolylineConnection figure,
			SectionManagedObjectSourceFlowToSubSectionInputFigureContext context);

	/**
	 * Decorates the {@link SectionManagedObjectSourceFlowToExternalFlowModel}
	 * figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectSourceFlowToExternalFlowFigureContext}
	 */
	void decorateSectionManagedObjectSourceFlowToExternalFlowFigure(
			PolylineConnection figure,
			SectionManagedObjectSourceFlowToExternalFlowFigureContext context);

	/**
	 * Decorates the
	 * {@link SectionManagedObjectDependencyToSectionManagedObjectModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectDependencyToSectionManagedObjectFigureContext}
	 */
	void decorateSectionManagedObjectDependencyToSectionManagedObjectFigure(
			PolylineConnection figure,
			SectionManagedObjectDependencyToSectionManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link SectionManagedObjectDependencyToExternalManagedObjectModel}
	 * figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectDependencyToExternalManagedObjectFigureContext}
	 */
	void decorateSectionManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			SectionManagedObjectDependencyToExternalManagedObjectFigureContext context);

}