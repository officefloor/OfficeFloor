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
package net.officefloor.eclipse.skin.standard.section;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.section.ExternalFlowFigure;
import net.officefloor.eclipse.skin.section.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SectionFigureFactory;
import net.officefloor.eclipse.skin.section.SubSectionFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionInputFigure;
import net.officefloor.eclipse.skin.section.SubSectionInputFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigure;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigure;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.SubSectionOutputToSubSectionInputFigureContext;

/**
 * Standard {@link SectionFigureFactory}.
 * 
 * @author Daniel
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

}