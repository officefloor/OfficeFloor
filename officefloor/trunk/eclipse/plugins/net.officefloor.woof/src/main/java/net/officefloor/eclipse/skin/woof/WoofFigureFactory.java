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
package net.officefloor.eclipse.skin.woof;

import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofGovernanceToWoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofFigureFactory {

	/**
	 * Creates the {@link TemplateFigure}.
	 * 
	 * @param context
	 *            {@link TemplateFigureContext}.
	 * @return {@link TemplateFigure}.
	 */
	TemplateFigure createTemplateFigure(TemplateFigureContext context);

	/**
	 * Creates the {@link TemplateOutputFigure}.
	 * 
	 * @param context
	 *            {@link TemplateOutputFigureContext}.
	 * @return {@link TemplateOutputFigure}.
	 */
	TemplateOutputFigure createTemplateOutputFigure(
			TemplateOutputFigureContext context);

	/**
	 * Creates the {@link SectionFigure}.
	 * 
	 * @param context
	 *            {@link SectionFigureContext}.
	 * @return {@link SectionFigure}.
	 */
	SectionFigure createSectionFigure(SectionFigureContext context);

	/**
	 * Creates the {@link SectionInputFigure}.
	 * 
	 * @param context
	 *            {@link SectionInputFigureContext}.
	 * @return {@link SectionInputFigure}.
	 */
	SectionInputFigure createSectionInputFigure(
			SectionInputFigureContext context);

	/**
	 * Creates the {@link SectionOutputFigure}.
	 * 
	 * @param context
	 *            {@link SectionOutputFigureContext}.
	 * @return {@link SectionOutputFigure}.
	 */
	SectionOutputFigure createSectionOutputFigure(
			SectionOutputFigureContext context);

	/**
	 * Creates the {@link GovernanceFigure}.
	 * 
	 * @param context
	 *            {@link GovernanceFigureContext}.
	 * @return {@link GovernanceFigure}.
	 */
	GovernanceFigure createGovernanceFigure(GovernanceFigureContext context);

	/**
	 * Creates the {@link GovernanceAreaFigure}.
	 * 
	 * @param context
	 *            {@link GovernanceAreaFigureContext}.
	 * @return {@link GovernanceAreaFigure}.
	 */
	GovernanceAreaFigure createGovernanceAreaFigure(
			GovernanceAreaFigureContext context);

	/**
	 * Creates the {@link ResourceFigure}.
	 * 
	 * @param context
	 *            {@link ResourceFigureContext}.
	 * @return {@link ResourceFigure}.
	 */
	ResourceFigure createResourceFigure(ResourceFigureContext context);

	/**
	 * Creates the {@link ExceptionFigure}.
	 * 
	 * @param context
	 *            {@link ExceptionFigureContext}.
	 * @return {@link ExceptionFigure}.
	 */
	ExceptionFigure createExceptionFigure(ExceptionFigureContext context);

	/**
	 * Creates the {@link StartFigure}.
	 * 
	 * @param context
	 *            {@link StartFigureContext}.
	 * @return {@link StartFigure}.
	 */
	StartFigure createStartFigure(StartFigureContext context);

	/**
	 * Decorates the {@link WoofTemplateOutputToWoofTemplateModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TemplateOutputToTemplateFigureContext}
	 */
	void decorateTemplateOutputToTemplateFigure(PolylineConnection figure,
			TemplateOutputToTemplateFigureContext context);

	/**
	 * Decorates the {@link WoofTemplateOutputToWoofSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TemplateOutputToSectionInputFigureContext}
	 */
	void decorateTemplateOutputToSectionInputFigure(PolylineConnection figure,
			TemplateOutputToSectionInputFigureContext context);

	/**
	 * Decorates the {@link WoofTemplateOutputToWoofResourceModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TemplateOutputToResourceFigureContext}
	 */
	void decorateTemplateOutputToResourceFigure(PolylineConnection figure,
			TemplateOutputToResourceFigureContext context);

	/**
	 * Decorates the {@link WoofSectionOutputToWoofTemplateModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionOutputToTemplateFigureContext}
	 */
	void decorateSectionOutputToTemplateFigure(PolylineConnection figure,
			SectionOutputToTemplateFigureContext context);

	/**
	 * Decorates the {@link WoofSectionOutputToWoofSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionOutputToSectionInputFigureContext}.
	 */
	void decorateSectionOutputToSectionInputFigure(PolylineConnection figure,
			SectionOutputToSectionInputFigureContext context);

	/**
	 * Decorates the {@link WoofSectionOutputToWoofResourceModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link SectionOutputToResourceFigureContext}.
	 */
	void decorateSectionOutputToResourceFigure(PolylineConnection figure,
			SectionOutputToResourceFigureContext context);

	/**
	 * Decorates the {@link WoofGovernanceToWoofGovernanceAreaModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link GovernanceToGovernanceAreaFigureContext}.
	 */
	void decorateGovernanceToGovernanceAreaFigure(PolylineConnection figure,
			GovernanceToGovernanceAreaFigureContext context);

	/**
	 * Decorates the {@link WoofExceptionToWoofTemplateModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ExceptionToTemplateFigureContext}.
	 */
	void decorateExceptionToTemplateFigure(PolylineConnection figure,
			ExceptionToTemplateFigureContext context);

	/**
	 * Decorates the {@link WoofExceptionToWoofSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ExceptionToSectionInputFigureContext}
	 */
	void decorateExceptionToSectionInputFigure(PolylineConnection figure,
			ExceptionToSectionInputFigureContext context);

	/**
	 * Decorates the {@link WoofExceptionToWoofResourceModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link ExceptionToResourceFigureContext}
	 */
	void decorateExceptionToResourceFigure(PolylineConnection figure,
			ExceptionToResourceFigureContext context);

	/**
	 * Decorates the {@link WoofStartToWoofSectionInputModel} figure.
	 * 
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link StartToSectionInputFigureContext}.
	 */
	void decorateStartToSectionInputFigure(PolylineConnection figure,
			StartToSectionInputFigureContext context);

}