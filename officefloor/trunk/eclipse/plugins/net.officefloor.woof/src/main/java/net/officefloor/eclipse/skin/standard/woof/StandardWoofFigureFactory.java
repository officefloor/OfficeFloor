/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.eclipse.skin.standard.woof;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import net.officefloor.eclipse.skin.standard.StandardWoofColours;
import net.officefloor.eclipse.skin.woof.ExceptionFigure;
import net.officefloor.eclipse.skin.woof.ExceptionFigureContext;
import net.officefloor.eclipse.skin.woof.ExceptionToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.ExceptionToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.ExceptionToTemplateFigureContext;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigure;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigureContext;
import net.officefloor.eclipse.skin.woof.GovernanceFigure;
import net.officefloor.eclipse.skin.woof.GovernanceFigureContext;
import net.officefloor.eclipse.skin.woof.GovernanceToGovernanceAreaFigureContext;
import net.officefloor.eclipse.skin.woof.ResourceFigure;
import net.officefloor.eclipse.skin.woof.ResourceFigureContext;
import net.officefloor.eclipse.skin.woof.SectionFigure;
import net.officefloor.eclipse.skin.woof.SectionFigureContext;
import net.officefloor.eclipse.skin.woof.SectionInputFigure;
import net.officefloor.eclipse.skin.woof.SectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputFigure;
import net.officefloor.eclipse.skin.woof.SectionOutputFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToTemplateFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateFigure;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToTemplateFigureContext;
import net.officefloor.eclipse.skin.woof.WoofFigureFactory;

/**
 * Standard {@link WoofFigureFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardWoofFigureFactory implements WoofFigureFactory {

	/*
	 * ======================== WoofFigureFactory =========================
	 */

	@Override
	public TemplateFigure createTemplateFigure(TemplateFigureContext context) {
		return new StandardTemplateFigure(context);
	}

	@Override
	public TemplateOutputFigure createTemplateOutputFigure(
			TemplateOutputFigureContext context) {
		return new StandardTemplateOutputFigure(context);
	}

	@Override
	public SectionFigure createSectionFigure(SectionFigureContext context) {
		return new StandardSectionFigure(context);
	}

	@Override
	public SectionInputFigure createSectionInputFigure(
			SectionInputFigureContext context) {
		return new StandardSectionInputFigure(context);
	}

	@Override
	public SectionOutputFigure createSectionOutputFigure(
			SectionOutputFigureContext context) {
		return new StandardSectionOutputFigure(context);
	}

	@Override
	public GovernanceFigure createGovernanceFigure(
			GovernanceFigureContext context) {
		return new StandardGovernanceFigure(context);
	}

	@Override
	public GovernanceAreaFigure createGovernanceAreaFigure(
			GovernanceAreaFigureContext context) {
		return new StandardGovernanceAreaFigure(context);
	}

	@Override
	public ResourceFigure createResourceFigure(ResourceFigureContext context) {
		return new StandardResourceFigure(context);
	}

	@Override
	public ExceptionFigure createExceptionFigure(ExceptionFigureContext context) {
		return new StandardExceptionFigure(context);
	}

	@Override
	public void decorateTemplateOutputToTemplateFigure(
			PolylineConnection figure,
			TemplateOutputToTemplateFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTemplateOutputToSectionInputFigure(
			PolylineConnection figure,
			TemplateOutputToSectionInputFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTemplateOutputToResourceFigure(
			PolylineConnection figure,
			TemplateOutputToResourceFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionOutputToTemplateFigure(
			PolylineConnection figure,
			SectionOutputToTemplateFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionOutputToSectionInputFigure(
			PolylineConnection figure,
			SectionOutputToSectionInputFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateSectionOutputToResourceFigure(
			PolylineConnection figure,
			SectionOutputToResourceFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateGovernanceToGovernanceAreaFigure(
			PolylineConnection figure,
			GovernanceToGovernanceAreaFigureContext context) {
		figure.setForegroundColor(StandardWoofColours.GOVERNANCE());
	}

	@Override
	public void decorateExceptionToTemplateFigure(PolylineConnection figure,
			ExceptionToTemplateFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateExceptionToSectionInputFigure(
			PolylineConnection figure,
			ExceptionToSectionInputFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateExceptionToResourceFigure(PolylineConnection figure,
			ExceptionToResourceFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

}