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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
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

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link SectionModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface SectionFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link FunctionNamespaceModel}.
	 *
	 * @param context
	 *            {@link FunctionNamespaceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	FunctionNamespaceFigure createFunctionNamespaceFigure(FunctionNamespaceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedFunctionModel}.
	 *
	 * @param context
	 *            {@link ManagedFunctionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ManagedFunctionFigure createManagedFunctionFigure(ManagedFunctionFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the
	 * {@link ManagedFunctionObjectModel}.
	 *
	 * @param context
	 *            {@link ManagedFunctionObjectFigureContext}.
	 * @return {@link ManagedFunctionObjectFigure}.
	 */
	ManagedFunctionObjectFigure createManagedFunctionObjectFigure(ManagedFunctionObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link FunctionModel}.
	 *
	 * @param context
	 *            {@link FunctionFigureContext}.
	 * @return {@link FunctionFigure}.
	 */
	FunctionFigure createFunctionFigure(FunctionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link FunctionFlowModel}.
	 *
	 * @param context
	 *            {@link FunctionFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	FunctionFlowFigure createFunctionFlowFigure(FunctionFlowFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the
	 * {@link FunctionEscalationModel}.
	 *
	 * @param context
	 *            {@link FunctionEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	FunctionEscalationFigure createFunctionEscalationFigure(FunctionEscalationFigureContext context);

	/**
	 * Decorates the {@link ManagedFunctionToFunctionModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ManagedFunctionToFunctionFigureContext}.
	 */
	void decorateManagedFunctionToFunctionFigure(PolylineConnection figure,
			ManagedFunctionToFunctionFigureContext context);

	/**
	 * Decorates the {@link ManagedFunctionObjectToExternalManagedObjectModel}
	 * connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ManagedFunctionObjectToExternalManagedObjectModel}.
	 */
	void decorateManagedFunctionObjectToExternalManagedObjectFigure(PolylineConnection figure,
			ManagedFunctionObjectToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link ManagedFunctionObjectToDeskManagedObjectModel}
	 * connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ManagedFunctionObjectToDeskManagedObjectModel}.
	 */
	void decorateManagedFunctionObjectToSectionManagedObjectFigure(PolylineConnection figure,
			ManagedFunctionObjectToSectionManagedObjectFigureContext context);

	/**
	 * Decorates the {@link FunctionFlowToFunctionModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionFlowToFunctionFigureContext}.
	 */
	void decorateFunctionkFlowToFunctionFigure(PolylineConnection figure, FunctionFlowToFunctionFigureContext context);

	/**
	 * Decorates the {@link FunctionFlowToExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionFlowToExternalFlowFigureContext}.
	 */
	void decorateFunctionFlowToExternalFlowFigure(PolylineConnection figure,
			FunctionFlowToExternalFlowFigureContext context);

	/**
	 * Decorates the {@link FunctionToNextFunctionModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionToNextFunctionFigureContext}.
	 */
	void decorateFunctionToNextFunctionFigure(PolylineConnection figure, FunctionToNextFunctionFigureContext context);

	/**
	 * Decorates the {@link FunctionToNextExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionToNextExternalFlowModel}.
	 */
	void decorateFunctionToNextExternalFlowFigure(PolylineConnection figure,
			FunctionToNextExternalFlowFigureContext context);

	/**
	 * Decorates the {@link FunctionEscalationToFunctionModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionEscalationToFunctionFigureContext}.
	 */
	void decorateFunctionEscalationToFunctionFigure(PolylineConnection figure,
			FunctionEscalationToFunctionFigureContext context);

	/**
	 * Decorates the {@link FunctionEscalationToExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link FunctionEscalationToExternalFlowModel}.
	 */
	void decorateFunctionEscalationToExternalFlowFigure(PolylineConnection figure,
			FunctionEscalationToExternalFlowFigureContext context);

	/**
	 * Decorates the {@link SectionManagedObjectSourceFlowFunctionModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectSourceFlowToFunctionFigureContext}
	 */
	void decorateSectionManagedObjectSourceFlowToFunctionFigure(PolylineConnection figure,
			SectionManagedObjectSourceFlowToFunctionFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 *
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalFlowFigure createExternalFlowFigure(ExternalFlowFigureContext context);

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
	SectionManagedObjectFigure createSectionManagedObjectFigure(SectionManagedObjectFigureContext context);

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
	SubSectionInputFigure createSubSectionInputFigure(SubSectionInputFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionObjectModel}.
	 *
	 * @param context
	 *            {@link SubSectionObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubSectionObjectFigure createSubSectionObjectFigure(SubSectionObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionOutputModel}.
	 *
	 * @param context
	 *            {@link SubSectionOutputFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubSectionOutputFigure createSubSectionOutputFigure(SubSectionOutputFigureContext context);

	/**
	 * Decorates the
	 * {@link SectionManagedObjectToSectionManagedObjectSourceModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionManagedObjectToSectionManagedObjectSourceModel}
	 */
	void decorateSectionManagedObjectToSectionManagedObjectSourceFigure(PolylineConnection figure,
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
	void decorateSubSectionObjectToExternalManagedObjectFigure(PolylineConnection figure,
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
	void decorateSubSectionObjectToSectionManagedObjectFigure(PolylineConnection figure,
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
	void decorateSubSectionOutputToExternalFlowFigure(PolylineConnection figure,
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
	void decorateSectionManagedObjectSourceFlowToSubSectionInputFigure(PolylineConnection figure,
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
	void decorateSectionManagedObjectSourceFlowToExternalFlowFigure(PolylineConnection figure,
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
	void decorateSectionManagedObjectDependencyToSectionManagedObjectFigure(PolylineConnection figure,
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
	void decorateSectionManagedObjectDependencyToExternalManagedObjectFigure(PolylineConnection figure,
			SectionManagedObjectDependencyToExternalManagedObjectFigureContext context);

}