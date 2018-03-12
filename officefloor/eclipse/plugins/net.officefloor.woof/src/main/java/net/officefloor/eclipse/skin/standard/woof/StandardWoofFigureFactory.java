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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.woof.AccessFigure;
import net.officefloor.eclipse.skin.woof.AccessFigureContext;
import net.officefloor.eclipse.skin.woof.AccessInputFigure;
import net.officefloor.eclipse.skin.woof.AccessInputFigureContext;
import net.officefloor.eclipse.skin.woof.SecurityOutputFigure;
import net.officefloor.eclipse.skin.woof.SecurityOutputFigureContext;
import net.officefloor.eclipse.skin.woof.SecurityOutputToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.SecurityOutputToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.SecurityOutputToTemplateFigureContext;
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
import net.officefloor.eclipse.skin.woof.SectionOutputToAccessInputFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.SectionOutputToTemplateFigureContext;
import net.officefloor.eclipse.skin.woof.StartFigure;
import net.officefloor.eclipse.skin.woof.StartFigureContext;
import net.officefloor.eclipse.skin.woof.StartToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateFigure;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToAccessInputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToResourceFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToSectionInputFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputToTemplateFigureContext;
import net.officefloor.eclipse.skin.woof.WoofFigureFactory;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

/**
 * Standard {@link WoofFigureFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardWoofFigureFactory implements WoofFigureFactory {

	/**
	 * Decorates the connection.
	 * 
	 * @param figure
	 *            {@link PolylineConnection}.
	 */
	private void decorateConnection(PolylineConnection figure) {

		// Provide arrow
		PolylineDecoration arrow = new PolylineDecoration();
		arrow.setBackgroundColor(CommonWoofColours.CONNECTIONS());
		arrow.setAlpha(100);
		arrow.setOpaque(true);

		// Decorate connection
		figure.setTargetDecoration(arrow);
		figure.setForegroundColor(CommonWoofColours.CONNECTIONS());
		figure.setAlpha(100);
		figure.setOpaque(false);
	}

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
	public AccessFigure createAccessFigure(AccessFigureContext context) {
		return new StandardAccessFigure(context);
	}

	@Override
	public AccessInputFigure createAccessInputFigure(
			AccessInputFigureContext context) {
		return new StandardAccessInputFigure(context);
	}

	@Override
	public SecurityOutputFigure createSecurityOutputFigure(
			SecurityOutputFigureContext context) {
		return new StandardAccessOutputFigure(context);
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
	public StartFigure createStartFigure(StartFigureContext context) {
		return new StandardStartFigure(context);
	}

	@Override
	public void decorateTemplateOutputToTemplateFigure(
			PolylineConnection figure,
			TemplateOutputToTemplateFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateTemplateOutputToSectionInputFigure(
			PolylineConnection figure,
			TemplateOutputToSectionInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateTemplateOutputToSecurityFigure(
			PolylineConnection figure,
			TemplateOutputToAccessInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateTemplateOutputToResourceFigure(
			PolylineConnection figure,
			TemplateOutputToResourceFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSectionOutputToTemplateFigure(
			PolylineConnection figure,
			SectionOutputToTemplateFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSectionOutputToSectionInputFigure(
			PolylineConnection figure,
			SectionOutputToSectionInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSectionOutputToAccessInputFigure(
			PolylineConnection figure,
			SectionOutputToAccessInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSectionOutputToResourceFigure(
			PolylineConnection figure,
			SectionOutputToResourceFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSecurityOutputToTemplateFigure(PolylineConnection figure,
			SecurityOutputToTemplateFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSecurityOutputToSectionInputFigure(
			PolylineConnection figure,
			SecurityOutputToSectionInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateSecurityOutputToResourceFigure(PolylineConnection figure,
			SecurityOutputToResourceFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateGovernanceToGovernanceAreaFigure(
			PolylineConnection figure,
			GovernanceToGovernanceAreaFigureContext context) {
		figure.setForegroundColor(CommonWoofColours.CONNECTIONS());

		// Match governance outline
		figure.setAlpha(100);
		figure.setOpaque(false);
	}

	@Override
	public void decorateExceptionToTemplateFigure(PolylineConnection figure,
			ExceptionToTemplateFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateExceptionToSectionInputFigure(
			PolylineConnection figure,
			ExceptionToSectionInputFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateExceptionToResourceFigure(PolylineConnection figure,
			ExceptionToResourceFigureContext context) {
		this.decorateConnection(figure);
	}

	@Override
	public void decorateStartToSectionInputFigure(PolylineConnection figure,
			StartToSectionInputFigureContext context) {
		this.decorateConnection(figure);
	}

}