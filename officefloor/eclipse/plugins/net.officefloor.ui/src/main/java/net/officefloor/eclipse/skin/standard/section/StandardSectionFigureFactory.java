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

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.section.ExternalFlowFigure;
import net.officefloor.eclipse.skin.section.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigureContext;
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

/**
 * Standard {@link SectionFigureFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardSectionFigureFactory implements SectionFigureFactory {

	@Override
	public ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {
		return new StandardExternalFlowFigure(context);
	}

	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
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
	public SectionManagedObjectFigure createSectionManagedObjectFigure(
			SectionManagedObjectFigureContext context) {
		return new StandardSectionManagedObjectFigure(context);
	}

	@Override
	public SectionManagedObjectDependencyFigure createSectionManagedObjectDependencyFigure(
			SectionManagedObjectDependencyFigureContext context) {
		return new StandardSectionManagedObjectDependencyFigure(context);
	}

	@Override
	public SubSectionFigure createSubSectionFigure(
			SubSectionFigureContext context) {
		return new StandardSubSectionFigure(context);
	}

	@Override
	public SubSectionInputFigure createSubSectionInputFigure(
			final SubSectionInputFigureContext context) {
		return new StandardSubSectionInputFigure(context);
	}

	@Override
	public SubSectionObjectFigure createSubSectionObjectFigure(
			SubSectionObjectFigureContext context) {
		return new StandardSubSectionObjectFigure(context);
	}

	@Override
	public SubSectionOutputFigure createSubSectionOutputFigure(
			SubSectionOutputFigureContext context) {
		return new StandardSubSectionOutputFigure(context);
	}

	@Override
	public void decorateSubSectionObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			SubSectionObjectToExternalManagedObjectFigureContext context) {
		// Leave as default
	}

	@Override
	public void decorateSubSectionObjectToSectionManagedObjectFigure(
			PolylineConnection figure,
			SubSectionObjectToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateSubSectionOutputToSubSectionInput(
			PolylineConnection figure,
			SubSectionOutputToSubSectionInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSubSectionOutputToExternalFlowFigure(
			PolylineConnection figure,
			SubSectionOutputToExternalFlowFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectSourceFlowToExternalFlowFigure(
			PolylineConnection figure,
			SectionManagedObjectSourceFlowToExternalFlowFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectSourceFlowToSubSectionInputFigure(
			PolylineConnection figure,
			SectionManagedObjectSourceFlowToSubSectionInputFigureContext context) {
		// Provide arrow
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionManagedObjectToSectionManagedObjectSourceFigure(
			PolylineConnection figure,
			SectionManagedObjectToSectionManagedObjectSourceFigureContext context) {
		// Link line
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateSectionManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			SectionManagedObjectDependencyToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateSectionManagedObjectDependencyToSectionManagedObjectFigure(
			PolylineConnection figure,
			SectionManagedObjectDependencyToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

}