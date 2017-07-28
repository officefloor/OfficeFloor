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
package net.officefloor.eclipse.skin.standard.section;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.section.ExternalFlowFigure;
import net.officefloor.eclipse.skin.section.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigure;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFigure;
import net.officefloor.eclipse.skin.section.FunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowFigure;
import net.officefloor.eclipse.skin.section.FunctionFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigure;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigureContext;
import net.officefloor.eclipse.skin.section.FunctionToNextExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionToNextFunctionFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectToSectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.SectionFigureFactory;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyToSectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowToSubSectionInputFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectToSectionManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionInputFigure;
import net.officefloor.eclipse.skin.section.SubSectionInputFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigure;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionObjectToSectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigure;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputToSubSectionInputFigureContext;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Standard {@link SectionFigureFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardSectionFigureFactory implements SectionFigureFactory {

	/**
	 * Decorates the {@link Figure} based on whether spawning a
	 * {@link ThreadState}.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param isSpawnThreadState
	 *            <code>true</code> to spawn a {@link ThreadState}.
	 */
	private void decorateSpawnThreadState(PolylineConnection figure, boolean isSpawnThreadState) {

		// Decorate
		figure.setTargetDecoration(new PolylineDecoration());

		// If spawn, then alter look
		if (isSpawnThreadState) {
			figure.setLineStyle(Graphics.LINE_DASH);
		}
	}

	/*
	 * ===================== DeskFigureFactory ============================
	 */

	@Override
	public ExternalFlowFigure createExternalFlowFigure(ExternalFlowFigureContext context) {
		return new StandardExternalFlowFigure(context);
	}

	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	@Override
	public SectionManagedObjectSourceFigure createSectionManagedObjectSourceFigure(
			SectionManagedObjectSourceFigureContext context) {
		return new StandardSectionManagedObjectSourceFigure(context);
	}

	@Override
	public SectionManagedObjectSourceFlowFigure createSectionManagedObjectSourceFlowFigure(
			SectionManagedObjectSourceFlowFigureContext context) {
		return new StandardSectionManagedObjectSourceFlowFigure(context);
	}

	@Override
	public SectionManagedObjectFigure createSectionManagedObjectFigure(SectionManagedObjectFigureContext context) {
		return new StandardSectionManagedObjectFigure(context);
	}

	@Override
	public SectionManagedObjectDependencyFigure createSectionManagedObjectDependencyFigure(
			SectionManagedObjectDependencyFigureContext context) {
		return new StandardSectionManagedObjectDependencyFigure(context);
	}

	@Override
	public SubSectionFigure createSubSectionFigure(SubSectionFigureContext context) {
		return new StandardSubSectionFigure(context);
	}

	@Override
	public SubSectionInputFigure createSubSectionInputFigure(final SubSectionInputFigureContext context) {
		return new StandardSubSectionInputFigure(context);
	}

	@Override
	public SubSectionObjectFigure createSubSectionObjectFigure(SubSectionObjectFigureContext context) {
		return new StandardSubSectionObjectFigure(context);
	}

	@Override
	public SubSectionOutputFigure createSubSectionOutputFigure(SubSectionOutputFigureContext context) {
		return new StandardSubSectionOutputFigure(context);
	}

	@Override
	public FunctionNamespaceFigure createFunctionNamespaceFigure(FunctionNamespaceFigureContext context) {
		return new StandardFunctionNamespaceFigure(context);
	}

	@Override
	public ManagedFunctionFigure createManagedFunctionFigure(ManagedFunctionFigureContext context) {
		return new StandardManagedFunctionFigure(context);
	}

	@Override
	public ManagedFunctionObjectFigure createManagedFunctionObjectFigure(ManagedFunctionObjectFigureContext context) {
		return new StandardManagedFunctionObjectFigure(context);
	}

	@Override
	public FunctionFigure createFunctionFigure(FunctionFigureContext context) {
		return new StandardFunctionFigure(context);
	}

	@Override
	public FunctionFlowFigure createFunctionFlowFigure(FunctionFlowFigureContext context) {
		return new StandardFunctionFlowFigure(context);
	}

	@Override
	public FunctionEscalationFigure createFunctionEscalationFigure(FunctionEscalationFigureContext context) {
		return new StandardFunctionEscalationFigure(context);
	}

	@Override
	public void decorateSubSectionObjectToExternalManagedObjectFigure(PolylineConnection figure,
			SubSectionObjectToExternalManagedObjectFigureContext context) {
		// Leave as default
	}

	@Override
	public void decorateSubSectionObjectToSectionManagedObjectFigure(PolylineConnection figure,
			SubSectionObjectToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateSubSectionOutputToSubSectionInput(PolylineConnection figure,
			SubSectionOutputToSubSectionInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSubSectionOutputToExternalFlowFigure(PolylineConnection figure,
			SubSectionOutputToExternalFlowFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectSourceFlowToExternalFlowFigure(PolylineConnection figure,
			SectionManagedObjectSourceFlowToExternalFlowFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectSourceFlowToSubSectionInputFigure(PolylineConnection figure,
			SectionManagedObjectSourceFlowToSubSectionInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectToSectionManagedObjectSourceFigure(PolylineConnection figure,
			SectionManagedObjectToSectionManagedObjectSourceFigureContext context) {
		// Link line
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateSectionManagedObjectDependencyToExternalManagedObjectFigure(PolylineConnection figure,
			SectionManagedObjectDependencyToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateSectionManagedObjectDependencyToSectionManagedObjectFigure(PolylineConnection figure,
			SectionManagedObjectDependencyToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateManagedFunctionToFunctionFigure(PolylineConnection figure,
			ManagedFunctionToFunctionFigureContext context) {
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateManagedFunctionObjectToExternalManagedObjectFigure(PolylineConnection figure,
			ManagedFunctionObjectToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateManagedFunctionObjectToSectionManagedObjectFigure(PolylineConnection figure,
			ManagedFunctionObjectToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateFunctionkFlowToFunctionFigure(PolylineConnection figure,
			FunctionFlowToFunctionFigureContext context) {
		this.decorateSpawnThreadState(figure, context.isSpawnThreadState());
	}

	@Override
	public void decorateFunctionFlowToExternalFlowFigure(PolylineConnection figure,
			FunctionFlowToExternalFlowFigureContext context) {
		this.decorateSpawnThreadState(figure, context.isSpawnThreadState());
	}

	@Override
	public void decorateFunctionToNextFunctionFigure(PolylineConnection figure,
			FunctionToNextFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateFunctionToNextExternalFlowFigure(PolylineConnection figure,
			FunctionToNextExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateFunctionEscalationToFunctionFigure(PolylineConnection figure,
			FunctionEscalationToFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateFunctionEscalationToExternalFlowFigure(PolylineConnection figure,
			FunctionEscalationToExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectSourceFlowToFunctionFigure(PolylineConnection figure,
			SectionManagedObjectSourceFlowToFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

}